# Aday Memur — Android (Kotlin / Jetpack Compose)

Saf Android projesi. Ek kurulum gerekmez.

## Çalıştırma
1. Android Studio → **File → Open** → bu klasör (`AdayMemur`).
2. Gradle senkronizasyonu bitince üstte cihaz/emülatör seç → **Run ▶**.

## Google Play paketi (.aab)
1. **Build → Generate Signed App Bundle or APK → Android App Bundle**.
2. Yükleme anahtarını seç ya da **Create new…** ile oluştur. Anahtarı ve şifreyi yedekle.
3. Çıktı: `app/release/app-release.aab` → Play Console'a yüklenir.

İstersen `keystore.properties.example` dosyasını `keystore.properties` adıyla kopyalayıp doldur; `Build → Build Bundle(s)` release paketini otomatik imzalar.

Her yeni yüklemede `app/build.gradle.kts` içindeki `versionCode` değerini 1 artır.

## Ayarlar (`app/build.gradle.kts` → `defaultConfig`)
| Alan | Ne işe yarar |
|---|---|
| `SITE_URL` | İlan verisinin ve gizlilik sayfalarının adresi (GitHub Pages) |
| `DESTEK_EPOSTA` | Hesap ekranındaki destek adresi; boşsa satır gizlenir |
| `ADMOB_BANNER_ID` | AdMob banner birimi; **boşken reklam altyapısı hiç çalışmaz** |
| `admobAppId` | AdMob uygulama kimliği (şimdilik Google'ın test kimliği) |
| `ERKEN_ERISIM_DK` | Ücretsiz kullanıcının yeni ilanı göreceği gecikme (Pro: anında) |
| `UCRETSIZ_KAYIT_LIMITI` | Ücretsiz planda kaydedilebilecek ilan sayısı |

## Abonelik (Pro)
Play Console → **Para kazanma → Abonelikler**: `pro_aylik` ve `pro_yillik` ürünlerini oluştur (yıllıkta ücretsiz deneme teklifi önerilir). Ürünler, uygulama bir test kanalına yüklendikten sonra uygulamada görünür.

## Bildirimler
Sunucu gerekmez. Uygulama saatte bir resmî ilan listesini kontrol eder:
- **Pro:** yeni ilan çıkınca, seçilen illere göre bildirim
- **Ücretsiz:** her akşam 19:00'dan sonra günün yeni ilanlarının özeti

Yapım: selcukyus
