# Aday Memur

Kamu personel alım ilanlarını resmî kaynaklardan derleyen, KPSS şartlarıyla özetleyen ve kişiye göre eşleştiren Android uygulaması.

**Yapım:** selcukyus · **Paket adı:** `com.selcukyus.adaymemur`

```
adaymemur/
├─ app/          Expo (React Native) mobil uygulama
├─ pipeline/     İlan toplayıcı + ayrıştırıcı (Python) — GitHub Actions'ta çalışır
├─ content/      Elle düzenlenen içerik: KPSS takvimi, duyurular (JSON)
├─ web/          Gizlilik, koşullar, hesap silme, forum kuralları, tanıtım sayfası
├─ firebase/     Firestore güvenlik kuralları ve indeksler (forum)
├─ store/        Google Play mağaza metinleri ve form cevapları
└─ .github/      Veri güncelleme, test, Android derleme iş akışları
```

## Nasıl çalışır

```
Kariyer Kapısı (resmî RSS) ─┐
ilan.gov.tr (açık API)  ────┼─► GitHub Actions (30 dk'da bir) ─► GitHub Pages /data/*.json ─► Uygulama
İŞKUR e-Şube (kamu)  ───────┘        │
                                     └─► Firebase Cloud Messaging ─► Bildirim (Pro: anında, ücretsiz: günlük özet)
```

- **Hiçbir sunucu kiralanmaz.** Veri hattı public depoda GitHub Actions ile ücretsiz çalışır, sonuç GitHub Pages'te yayınlanır.
- **Kariyer Kapısı detay servisi yalnızca Türkiye IP'lerine açık.** GitHub sunucuları ABD'de olduğundan bu ilanların başlığı, kurumu ve tarihi sunucuda; tam metni ise kullanıcı ilanı açtığında **telefondan** çekilir ve cihazda ayrıştırılır (`app/src/lib/parse.ts`). Türkiye'de sürekli açık bir bilgisayarın varsa onu self-hosted runner yapıp `RUNNER=self-hosted` değişkenini ayarlayarak tüm metinleri sunucuda da işleyebilirsin.
- **Ayrıştırma kural tabanlıdır** (KPSS puan türü, taban puan, öğrenim, bölüm şartı, kadro, tarihler). İsteğe bağlı olarak `GEMINI_API_KEY` gizlisi eklenirse Gemini ücretsiz katmanıyla eksik alanlar tamamlanır.

## Özellikler

| Ücretsiz | Pro (abonelik) |
|---|---|
| Tüm ilanlar, arama, il/tür/öğrenim/KPSS filtresi | **Bana Uygun**: profile göre kişisel liste + her ilan için gerekçeli uygunluk |
| İlan detayı, resmî ilana yönlendirme, takvime ekleme | **Anında bildirim** (il seçimli) |
| 10 ilana kadar kaydetme | **Erken erişim**: yeni ilanlar 60 dk önce |
| Günlük ilan özeti bildirimi | Sınırsız kayıt |
| KPSS gündemi, forum | Reklamsız |

## Kurulum — sırasıyla

### 1) GitHub deposu
1. GitHub'da **public** `adaymemur` deposu aç (public depoda Actions dakikaları sınırsız ücretsiz) ve bu klasörü gönder.
2. **Settings → Pages → Source: GitHub Actions** seç.
3. **Settings → Secrets and variables → Actions → Variables**:
   - `SUPPORT_EMAIL` — destek e-postan (gizlilik sayfalarına ve uygulamaya yazılır)
4. **Actions → Veri güncelleme → Run workflow**. Birkaç dakika sonra `https://<kullanıcı>.github.io/adaymemur/data/ilanlar.json` açılmalı. Özet tablosunda her kaynağın durumu görünür.

> Depo adı ya da kullanıcı adı farklıysa `app/src/lib/config.ts` içindeki `SITE_URL` varsayılanını değiştir veya `SITE_URL` değişkenini tanımla.

### 2) Firebase (bildirim + forum) — ücretsiz Spark planı
1. console.firebase.google.com → yeni proje → Android uygulaması ekle, paket adı `com.selcukyus.adaymemur`.
2. `google-services.json` dosyasını indir. Depo gizlisi olarak ekle:
   `GOOGLE_SERVICES_JSON_BASE64` = `base64 -w0 google-services.json` çıktısı.
3. **Authentication → Sign-in method → E-posta/Şifre**'yi aç.
4. **Firestore Database** oluştur (bölge: `europe-west`), ardından `firebase/` klasöründen kuralları yükle:
   `npx firebase-tools deploy --only firestore --project <proje-id>` (ya da konsolda Kurallar sekmesine `firestore.rules` içeriğini yapıştır, İndeksler'e iki kaydı ekle).
5. **Proje ayarları → Hizmet hesapları → Yeni özel anahtar oluştur** → indirilen JSON'un tamamını `FIREBASE_SERVICE_ACCOUNT` gizlisi olarak ekle (veri hattı bununla bildirim gönderir).
6. Moderasyon: Firestore'da `raporlar` koleksiyonunu izle; bir içeriği gizlemek için ilgili belgede `gizli` alanını `true` yap.

### 3) Abonelik (Google Play + RevenueCat) — gelir 2.500 $/ay'a kadar ücretsiz
1. Play Console → uygulama → **Para kazanma → Abonelikler**: `pro_aylik` ve `pro_yillik` ürünlerini oluştur (yıllıkta 7 gün ücretsiz deneme önerilir).
2. app.revenuecat.com → proje → Google Play uygulaması ekle (servis hesabı bağlantısını RevenueCat rehberine göre yap).
3. RevenueCat'te **Entitlement** kimliği `pro`; **Offering** `default` içine aylık ve yıllık paketleri ekle.
4. Google Play public SDK anahtarını (`goog_...`) `REVENUECAT_ANDROID_KEY` değişkeni olarak ekle.

Anahtar yoksa uygulama çalışır, Pro ekranı "abonelik seçenekleri yüklenemedi" der.

### 4) Reklam (şimdilik boş)
Reklam altyapısı hazır, birimler boş; boşken SDK başlatılmaz ve hiçbir reklam alanı görünmez. Açmak için:
1. AdMob'da uygulama oluştur; **Uygulama kimliğini** `ADMOB_ANDROID_APP_ID` değişkenine yaz.
2. Bir **Banner** (alt şerit) ve bir **Banner/Native** (liste arası) birimi oluştur: `ADMOB_BANNER_ID`, `ADMOB_INLINE_ID`.
3. AdMob → Gizlilik ve mesajlaşma → **GDPR/AB mesajını** oluştur (onay formu uygulamada otomatik çıkar).
4. Yeni sürüm derle. Pro aboneler reklam görmez.

### 5) Yükleme anahtarı ve derleme
```bash
keytool -genkeypair -v -storetype JKS -keystore upload.keystore -alias adaymemur -keyalg RSA -keysize 2048 -validity 10000
base64 -w0 upload.keystore   # çıktıyı UPLOAD_KEYSTORE_BASE64 gizlisine koy
```
Gizliler: `UPLOAD_KEYSTORE_BASE64`, `UPLOAD_KEYSTORE_PASSWORD`, `UPLOAD_KEY_ALIAS` (`adaymemur`), `UPLOAD_KEY_PASSWORD`.
**Anahtarı ve şifreleri güvenli yerde yedekle**; kaybedersen güncelleme yükleyemezsin (Play App Signing açıksa Google'dan sıfırlama istenebilir).

**Actions → Android sürümü → Run workflow** (`versionCode` her yüklemede bir artmalı). Bitince **Artifacts** içinden `.aab` (Play'e yüklenecek) ve `.apk` (telefonda test) iner.

Alternatif: `cd app && npx eas-cli build -p android` (Expo hesabıyla bulutta derleme).

### 6) Google Play'e yükleme
`store/PLAY_STORE.md` içinde: mağaza başlığı, kısa/uzun açıklama, kategori, içerik derecelendirme cevapları, Veri güvenliği formu, devlet bilgisi beyanı ve yayın kontrol listesi.

## Geliştirme

```bash
# Uygulama
cd app && npm install
npm run typecheck && npm test
npx expo run:android          # bağlı cihaz/emülatörde geliştirme derlemesi

# Veri hattı
cd pipeline && pip install -r requirements.txt pytest
python -m pytest -q
python -m adaymemur --out ../_site --content ../content --state-file /tmp/state.json.gz --dry-run
```

**İçerik güncelleme:** KPSS takvimi `content/takvim.json`, duyurular `content/duyurular.json`. GitHub'da dosyayı düzenleyip kaydetmen yeterli; yarım saat içinde uygulamaya düşer. `"vurgulu": true` olan duyuru İlanlar ekranının üstünde gösterilir.

## Sorumluluk

Uygulama bağımsızdır; hiçbir kamu kurumuyla bağlantısı yoktur. Otomatik özetler hata içerebilir; uygulama her ilanda resmî kaynağa yönlendirir ve bunu açıkça belirtir.
