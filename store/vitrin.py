"""Google Play vitrin görselleri (telefon ekran görüntüleri, 1080x1920).

Kullanım: python vitrin.py <ham_ekranlar_klasoru> <cikti_klasoru>
Ham ekranlar CI'daki emülatörden gelir (android-native/ci/ekran.sh).
"""
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

FONT = Path(__file__).resolve().parent.parent / "android-native/app/src/main/res/font"
W, H = 1080, 1920

ACIK = dict(bg=(244, 246, 249), ink=(27, 30, 35), ink2=(74, 80, 91), marker=(255, 228, 90), cerceve=(27, 30, 35))
KOYU = dict(bg=(15, 17, 21), ink=(236, 238, 241), ink2=(181, 187, 197), marker=(241, 210, 58), cerceve=(48, 54, 64))

SAHNELER = [
    ("02-ilanlar", ["Kamu ilanları,", "*tek yerde.*"], "Kariyer Kapısı, ilan.gov.tr ve İŞKUR’dan güncel personel alımları", ACIK),
    ("03-bana-uygun", ["Sana *uygun*", "ilanları gör"], "Öğrenim, bölüm ve KPSS puanına göre kişisel eşleştirme", ACIK),
    ("05-detay-uygunluk", ["Şartları *tek tek*", "karşılaştır"], "Her ilanda neden başvurabileceğini gör", ACIK),
    ("04-detay", ["Son başvuruyu", "*kaçırma.*"], "Geri sayım ve tek dokunuşla takvime ekleme", ACIK),
    ("06-filtre", ["KPSS puanına", "göre *süz*"], "Puan türü, taban puan, il ve kadro türü filtresi", ACIK),
    ("07-gundem", ["KPSS *gündemi*", "cebinde"], "ÖSYM sınav, başvuru ve sonuç tarihleri", ACIK),
    ("09-koyu-ilanlar", ["Gece de", "*göz yormaz*"], "Koyu tema desteği", KOYU),
    ("10-koyu-detay", ["Resmî kaynağa", "*tek dokunuş*"], "Başvurular yalnızca kurumların resmî kanallarından yapılır", KOYU),
]


def font(ad, boyut):
    return ImageFont.truetype(str(FONT / f"archivo_{ad}.ttf"), boyut)


def baslik_ciz(d: ImageDraw.ImageDraw, satirlar, y, renk):
    f = font("extrabold", 92)
    for satir in satirlar:
        x = 80
        parcalar = satir.split("*")
        for i, parca in enumerate(parcalar):
            if not parca:
                continue
            w = d.textlength(parca, font=f)
            if i % 2 == 1:  # fosforlu vurgu
                asc, desc = f.getmetrics()
                ust = y + asc * (0.48 if renk is ACIK else 0.08)  # koyu zeminde vurgu metnin tamamını kaplar
                d.rounded_rectangle([x - 10, ust, x + w + 10, y + asc + 12], radius=12, fill=renk["marker"])
                d.text((x, y), parca, font=f, fill=(27, 30, 35))
            else:
                d.text((x, y), parca, font=f, fill=renk["ink"])
            x += w
        y += 104
    return y


def alt_metin(d, metin, y, renk):
    f = font("medium", 38)
    kelimeler, satir, satirlar = metin.split(), "", []
    for k in kelimeler:
        dene = (satir + " " + k).strip()
        if d.textlength(dene, font=f) > W - 160:
            satirlar.append(satir)
            satir = k
        else:
            satir = dene
    satirlar.append(satir)
    for s in satirlar:
        d.text((80, y), s, font=f, fill=renk["ink2"])
        y += 52
    return y


def telefon(ekran: Image.Image, genislik: int, renk) -> Image.Image:
    # Durum çubuğunu (saat, simgeler) sayfa rengiyle örterek sadeleştir
    ekran = ekran.convert("RGB").copy()
    durum = int(ekran.height * 0.045)
    ornek = ekran.getpixel((ekran.width // 2, durum + 6))
    ImageDraw.Draw(ekran).rectangle([0, 0, ekran.width, durum], fill=ornek)
    oran = genislik / ekran.width
    ic = ekran.resize((genislik, int(ekran.height * oran)), Image.LANCZOS)
    kenar, r = 16, 64
    tel = Image.new("RGBA", (ic.width + kenar * 2, ic.height + kenar * 2), (0, 0, 0, 0))
    ImageDraw.Draw(tel).rounded_rectangle([0, 0, tel.width - 1, tel.height - 1], radius=r + kenar, fill=renk["cerceve"])
    maske = Image.new("L", ic.size, 0)
    ImageDraw.Draw(maske).rounded_rectangle([0, 0, ic.width - 1, ic.height - 1], radius=r, fill=255)
    tel.paste(ic, (kenar, kenar), maske)
    return tel


def uret(ham: Path, cikti: Path):
    cikti.mkdir(parents=True, exist_ok=True)
    for i, (ad, satirlar, alt, renk) in enumerate(SAHNELER, 1):
        kaynak = ham / f"{ad}.png"
        if not kaynak.exists():
            print("atlandı:", ad)
            continue
        tuval = Image.new("RGB", (W, H), renk["bg"])
        d = ImageDraw.Draw(tuval)
        y = baslik_ciz(d, satirlar, 120, renk)
        y = alt_metin(d, alt, y + 18, renk)
        tel = telefon(Image.open(kaynak), 820, renk)
        x = (W - tel.width) // 2
        ust = y + 56
        golge = Image.new("RGBA", (tel.width + 120, tel.height + 120), (0, 0, 0, 0))
        ImageDraw.Draw(golge).rounded_rectangle([60, 60, tel.width + 60, tel.height + 60], radius=80, fill=(0, 0, 0, 70 if renk is ACIK else 160))
        golge = golge.filter(ImageFilter.GaussianBlur(28))
        tuval.paste(golge, (x - 60, ust - 40), golge)
        tuval.paste(tel, (x, ust), tel)
        hedef = cikti / f"aday-memur-{i:02d}.png"
        tuval.save(hedef, optimize=True)
        print(hedef)


if __name__ == "__main__":
    uret(Path(sys.argv[1]), Path(sys.argv[2]))
