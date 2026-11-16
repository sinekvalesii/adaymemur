from datetime import date

from adaymemur import parse
from adaymemur.text import fold, il_kodu, iller_bul, tr_title

ILAN_1 = """T.C. AİLE VE SOSYAL HİZMETLER BAKANLIĞI Personel Genel Müdürlüğü
SÖZLEŞMELİ PERSONEL ALIM İLANI
Bakanlığımız taşra teşkilatında istihdam edilmek üzere, 657 sayılı Devlet Memurları Kanununun 4 üncü
maddesinin (B) fıkrası uyarınca 2024 yılı KPSS (B) grubu puan sırası esas alınarak aşağıda unvanı,
puan türü, yeri ve adedi belirtilen münhal pozisyonlara, toplam 3000 sözleşmeli personel alımı yapılacaktır.
Büro Personeli KPSSP3 (Lisans) 1200 adet; Koruma ve Güvenlik Görevlisi KPSSP94 (Ortaöğretim) 800 adet;
Destek Personeli KPSSP93 (Önlisans) 1000 adet.
Herhangi bir lisans programından mezun olmak.
a) Adaylar başvurularını 15/12/2025 - 26/12/2025 tarihleri arasında son gün saat 23:59'a kadar
Cumhurbaşkanlığı Kariyer Kapısı üzerinden e-Devlet şifresi ile giriş yaparak yapacaklardır.
Sözlü sınav 12.01.2026 tarihinde yapılacaktır."""

ILAN_2 = """SERMAYE PİYASASI KURULU Sözleşmeli Bilişim Personeli Alım İlanı
Yazılım Geliştirici 3 kişi. Bilgisayar Mühendisliği, Yazılım Mühendisliği bölümlerinden mezun olmak.
KPSS şartı aranmaz. Başvurular 1 Nisan 2026 - 15 Nisan 2026 tarihleri arasında alınacaktır."""

ILAN_3 = """Gençlik ve Spor Bakanlığı 2026 KPSS sonuçlarına göre KPSS P93 puan türünden 70 ve üzeri
puan almış olmak. Başvurular 20 - 30 Nisan 2026 tarihleri arasında yapılacaktır. Toplam 45 sürekli işçi alınacaktır."""


def test_fold_and_titles():
    assert fold("İSTANBUL Üniversitesi Çağ") == "istanbul universitesi cag"
    assert tr_title("İSTANBUL BÜYÜKŞEHİR BELEDİYESİ") == "İstanbul Büyükşehir Belediyesi"
    assert il_kodu("İZMİR") == "35"
    assert iller_bul("Kocaeli Üniversitesi ve Ankara") == ["41", "06"]


def test_ilan1_full():
    p = parse.ayristir("Sözleşmeli Personel Alımı", ILAN_1, "Sözleşmeli Personel", date(2025, 12, 10))
    assert p["tur"] == "sozlesmeli"
    assert [k["tur"] for k in p["kpss"]] == ["P3", "P94", "P93"]
    assert p["kpssGerekli"] is True
    assert p["egitim"] == ["ortaogretim", "onlisans", "lisans"]
    assert p["bolumSarti"] == "yok"
    assert p["kadro"] == 3000
    assert p["basvuruBaslangic"] == "2025-12-15"
    assert p["sonBasvuru"] == "2025-12-26"
    assert p["unvanlar"][:3] == ["Büro Personeli", "Koruma ve Güvenlik Görevlisi", "Destek Personeli"]


def test_ilan2_no_kpss_textual_dates():
    p = parse.ayristir("Bilişim Personeli", ILAN_2, "", date(2026, 3, 30))
    assert p["kpssGerekli"] is False
    assert p["kpss"] == []
    assert p["bolumSarti"] == "var"
    assert p["sonBasvuru"] == "2026-04-15"
    assert p["basvuruBaslangic"] == "2026-04-01"
    assert "Yazılım Geliştirici" in p["unvanlar"]


def test_ilan3_min_score_short_range():
    p = parse.ayristir("İşçi alımı", ILAN_3, "İşçi", date(2026, 4, 18))
    assert p["kpss"] == [{"tur": "P93", "min": 70}]
    assert p["tur"] == "isci"
    assert p["sonBasvuru"] == "2026-04-30"
    assert p["kadro"] == 45
    assert "onlisans" in p["egitim"]


def test_onlisans_not_lisans():
    assert parse.egitim("ön lisans mezunu olmak") == ["onlisans"]
    assert parse.egitim("yüksek lisans mezunu olmak") == ["yukseklisans"]


def test_exam_date_not_deadline():
    t = "Yazılı sınav 10.02.2026 tarihinde yapılacaktır. Başvurular 01.02.2026 tarihine kadar alınacaktır."
    assert parse.tarihler(t, date(2026, 1, 20)) == (None, "2026-02-01")


def test_ozet():
    assert parse.ozet("a " * 400, 20).endswith("…")
