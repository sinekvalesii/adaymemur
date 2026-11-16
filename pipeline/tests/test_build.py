import gzip
import json
from datetime import timedelta

from adaymemur import build
from adaymemur.models import Ham, SourceError
from adaymemur.sources import ilangov, iskur, kariyerkapisi

RSS = b"""<?xml version="1.0" encoding="utf-8"?><rss version="2.0"><channel><title>Kariyer Kapisi</title>
<item><title>Adalet Bakanl\xc4\xb1\xc4\x9f\xc4\xb1 - Zab\xc4\xb1t Katibi Al\xc4\xb1m\xc4\xb1</title>
<link>https://kariyerkapisi.gov.tr/isealim/ilan?i=1b2c3d4e-0000-4000-8000-0123456789ab</link>
<category>Memur</category><pubDate>Mon, 16 Nov 2026 09:00:00 +0300</pubDate>
<enclosure url="https://kariyerkapisi.gov.tr/logo.png" type="image/png"/></item>
<item><title>Kocaeli \xc3\x9cniversitesi - S\xc3\xb6zle\xc5\x9fmeli Personel</title>
<link>https://kariyerkapisi.gov.tr/isealim/ilan?i=99999999-0000-4000-8000-0123456789ab</link>
<category>S\xc3\xb6zle\xc5\x9fmeli Personel</category><pubDate>Sun, 15 Nov 2026 09:00:00 +0300</pubDate></item>
</channel></rss>"""


def test_rss_parse():
    items = kariyerkapisi.parse_feed(RSS)
    assert len(items) == 2
    a = items[0]
    assert a.id == "kk-1b2c3d4e-0000-4000-8000-0123456789ab"
    assert a.kurum == "Adalet Bakanlığı" and a.baslik == "Zabıt Katibi Alımı"
    assert a.yayin == "2026-11-16" and a.logo.endswith("logo.png")


ISKUR_HTML = """<table><tr><td><span id="ctl04_rpt_ctl01_ctlIsverenDL">MİLLİ PARKLAR MÜDÜRLÜĞÜ</span></td>
<td><a class="share-toggle" data-ilanno="555" data-meslekler="Orman İşçisi" data-il="MUĞLA"
data-sontarih="28.11.2026" data-acikissayi="12">x</a></td></tr></table>"""


def test_iskur_parse():
    (h,) = iskur.parse_page(ISKUR_HTML)
    assert h.id == "is-555" and h.il == "48" and h.son_basvuru == "2026-11-28" and h.adet == 12
    assert h.kurum == "Milli Parklar Müdürlüğü"


def _fake_sources(monkeypatch, extra_ig=None, fail_iskur=False):
    today = build.now_tr().date()
    son = (today + timedelta(days=9)).strftime("%d.%m.%Y")

    def kk_fetch(s):
        return kariyerkapisi.parse_feed(RSS)

    def ig_fetch(s):
        out = [Ham(kaynak="ilangov", kid="100", baslik="Zabıt Katibi Alımı", url="https://www.ilan.gov.tr/ilan/100",
                   kurum="Adalet Bakanlığı", yayin=today.isoformat())]
        return out + (extra_ig or [])

    def ig_enrich(s, h):
        h.metin = (f"KPSSP3 puan türünden 70 ve üzeri puan. Herhangi bir lisans programından mezun olmak. "
                   f"Başvurular {today.strftime('%d.%m.%Y')} - {son} tarihleri arasında alınacaktır. "
                   f"Toplam 150 zabıt katibi alınacaktır.")

    def is_fetch(s):
        if fail_iskur:
            raise SourceError("kapalı")
        return iskur.parse_page(ISKUR_HTML.replace("28.11.2026", son))

    monkeypatch.setattr(kariyerkapisi, "fetch", kk_fetch)
    monkeypatch.setattr(kariyerkapisi, "enrich", lambda s, h: None)
    monkeypatch.setattr(ilangov, "fetch", ig_fetch)
    monkeypatch.setattr(ilangov, "enrich", ig_enrich)
    monkeypatch.setattr(iskur, "fetch", is_fetch)
    monkeypatch.setattr(iskur, "enrich", lambda s, h: None)
    return son


def test_run_end_to_end(tmp_path, monkeypatch, caplog):
    _fake_sources(monkeypatch)
    st = tmp_path / "state.json.gz"
    out = tmp_path / "site"
    s1 = build.run(out, local_state=st, dry_run=True)
    assert s1["ilk"] is True and s1["bildirim"] == 0
    data = json.loads((out / "data" / "ilanlar.json").read_text())
    ids = [i["id"] for i in data["ilanlar"]]
    # KK Adalet ile ilan.gov.tr Adalet aynı son başvuru tarihine sahip değil (KK detaysız) → ayrı kalır
    assert "ig-100" in ids and "is-555" in ids
    ig = next(i for i in data["ilanlar"] if i["id"] == "ig-100")
    assert ig["kpss"] == [{"tur": "P3", "min": 70}] and ig["kadro"] == 150 and ig["bolumSarti"] == "yok"
    assert (out / "data" / "ilan" / "ig-100.json").exists()
    assert not any(k.startswith("_") for k in ig)

    # 2. çalıştırma: yeni ilan → dry-run bildirim
    _fake_sources(monkeypatch, extra_ig=[Ham(kaynak="ilangov", kid="101", baslik="Hemşire Alımı",
                                             url="https://www.ilan.gov.tr/ilan/101", kurum="Sağlık Bakanlığı", il="06")],
                  fail_iskur=True)
    s2 = build.run(out, local_state=st, dry_run=True)
    assert s2["yeni"] == 1 and s2["bildirim"] == 1
    data2 = json.loads((out / "data" / "ilanlar.json").read_text())
    ids2 = [i["id"] for i in data2["ilanlar"]]
    assert "is-555" in ids2  # kaynak çöktü ama önceki ilan korunur
    assert ids2[0] == "ig-101" or "ig-101" in ids2
    state = json.loads(gzip.decompress(st.read_bytes()))
    assert state["ilanlar"]["ig-100"]["eklenme"] == ig["eklenme"]  # ilk görülme sabit kalır


def test_dedupe_merges_sources():
    a = {"id": "ig-1", "kaynak": "ilangov", "kurum": "TÜİK", "sonBasvuru": "2026-12-01", "kadro": 10, "detay": True,
         "logo": None, "kaynaklar": [{"kaynak": "ilangov", "url": "u1"}]}
    b = {"id": "kk-1", "kaynak": "kariyerkapisi", "kurum": "Tüik", "sonBasvuru": "2026-12-01", "kadro": None,
         "detay": False, "logo": "l", "kaynaklar": [{"kaynak": "kariyerkapisi", "url": "u2"}]}
    out = build.dedupe([b, a])
    assert len(out) == 1 and out[0]["id"] == "ig-1" and out[0]["ayni"] == ["kk-1"] and out[0]["logo"] == "l"
    assert len(out[0]["kaynaklar"]) == 2
    # idempotent
    assert len(build.dedupe([b, a])[0]["kaynaklar"]) == 2
