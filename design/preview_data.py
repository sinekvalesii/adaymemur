"""Yalnızca yerel web önizlemesi için örnek veri üretir (yayına girmez)."""
import sys, json
from datetime import timedelta
from pathlib import Path
sys.path.insert(0, "/home/claude/adaymemur/pipeline")
from adaymemur import build
from adaymemur.models import Ham
from adaymemur.sources import ilangov, iskur, kariyerkapisi

t = build.now_tr().date()
f = lambda d: (t + timedelta(days=d)).strftime("%d.%m.%Y")
ORN = [
    ("ilangov", "201", "Sözleşmeli Personel Alım İlanı", "Aile ve Sosyal Hizmetler Bakanlığı", None,
     f"657 sayılı Kanunun 4/B maddesi uyarınca 2024 KPSS (B) grubu puan sırası esas alınarak toplam 3000 sözleşmeli personel alımı yapılacaktır. Büro Personeli KPSSP3 (Lisans), Koruma ve Güvenlik Görevlisi KPSSP94 (Ortaöğretim), Destek Personeli KPSSP93 (Önlisans). Herhangi bir lisans programından mezun olmak. Adaylar başvurularını {f(-2)} - {f(9)} tarihleri arasında Kariyer Kapısı üzerinden yapacaklardır."),
    ("ilangov", "202", "Sürekli İşçi Alım İlanı", "İstanbul Büyükşehir Belediyesi", "34",
     f"Belediyemiz bünyesinde istihdam edilmek üzere toplam 120 sürekli işçi alınacaktır. Temizlik Personeli ve Şoför kadroları. KPSS şartı aranmaz. Başvurular {f(0)} - {f(4)} tarihleri arasında İŞKUR üzerinden alınacaktır."),
    ("ilangov", "203", "Sözleşmeli Bilişim Personeli Alım İlanı", "Sermaye Piyasası Kurulu", "06",
     f"Yazılım Geliştirici 3 kişi, Programcı 2 kişi. Bilgisayar Mühendisliği, Yazılım Mühendisliği bölümlerinden mezun olmak. KPSS şartı aranmaz. Başvurular {f(-5)} - {f(1)} tarihleri arasında alınacaktır."),
    ("ilangov", "204", "Memur Alımı (Zabıt Katibi)", "Adalet Bakanlığı", None,
     f"Adalet komisyonlarına 2024 KPSS P3 puan türünden 70 ve üzeri puan almış lisans mezunları arasından toplam 1500 zabıt katibi alınacaktır. Herhangi bir lisans programından mezun olmak. Başvurular {f(1)} - {f(15)} tarihleri arasında yapılacaktır."),
    ("ilangov", "205", "Sözleşmeli Sağlık Personeli Alımı", "Kocaeli Üniversitesi", "41",
     f"Hemşire (Lisans, KPSSP3) 40 kişi, Sağlık Teknikeri (Önlisans, KPSSP93) 25 kişi. Toplam 65 sözleşmeli personel alınacaktır. Hemşirelik bölümünden mezun olmak. Başvurular {f(-1)} - {f(12)} tarihleri arasında şahsen yapılacaktır."),
    ("ilangov", "206", "Lojistik Uzman Yardımcısı Alımı", "Toprak Mahsulleri Ofisi", "06",
     f"Toplam 20 uzman yardımcısı alınacaktır. İşletme, İktisat, Lojistik Yönetimi bölümlerinden mezun olmak. KPSS P48 puan türünden en az 70 puan almış olmak. Başvurular {f(3)} - {f(20)} tarihleri arasında alınacaktır."),
]
def ig_fetch(s):
    return [Ham(kaynak=k, kid=i, baslik=b, url=f"https://www.ilan.gov.tr/ilan/{i}", kurum=ku, il=il, yayin=t.isoformat(), extra={"m": m}) for k, i, b, ku, il, m in ORN]
def ig_enrich(s, h): h.metin = h.extra["m"]
kariyerkapisi.fetch = lambda s: [Ham(kaynak="kariyerkapisi", kid="1b2c3d4e-0000-4000-8000-0123456789ab", baslik="Zabıt Katibi ve Mübaşir Alımı", kurum="Ankara Adalet Komisyonu", url="https://kariyerkapisi.gov.tr/isealim", kategori="Memur", yayin=t.isoformat())]
kariyerkapisi.enrich = lambda s, h: None
ilangov.fetch, ilangov.enrich = ig_fetch, ig_enrich
iskur.fetch = lambda s: iskur.parse_page(f'<table><tr><td><span id="x_ctlIsverenDL">MİLLİ PARKLAR MÜDÜRLÜĞÜ</span></td><td><a data-ilanno="555" data-meslekler="Orman Muhafaza Memuru" data-il="MUĞLA" data-sontarih="{f(6)}" data-acikissayi="12">x</a></td></tr></table>')
iskur.enrich = lambda s, h: None
out = Path("/home/claude/adaymemur/design/preview")
build.run(out, local_state=out / "st.json.gz", notify=False, content_dir=Path("/home/claude/adaymemur/content"))
d = json.loads((out / "data/ilanlar.json").read_text())
# biri "yeni" (erken erişim kilidini göstermek için)
from datetime import datetime
d["ilanlar"][1]["eklenme"] = (build.now_tr() - timedelta(minutes=18)).isoformat(timespec="seconds")
for i in d["ilanlar"][2:]:
    i["eklenme"] = (build.now_tr() - timedelta(hours=30)).isoformat(timespec="seconds")
(out / "data/ilanlar.json").write_text(json.dumps(d, ensure_ascii=False))
for i in d["ilanlar"]: print(i["id"], i["tur"], i["kpss"], i["egitim"], i["kadro"], i["sonBasvuru"], i["bolumSarti"], i["unvanlar"][:3])
