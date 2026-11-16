/**
 * Yayın ayarları. Derleme sırasında EXPO_PUBLIC_* ortam değişkenleriyle değiştirilebilir
 * (bkz. app/.env.example). Boş bırakılan özellikler uygulamada sessizce devre dışı kalır.
 */
// Not: Expo yalnızca `process.env.EXPO_PUBLIC_X` biçimindeki statik erişimleri derlemeye gömer.
const env = (v: string | undefined, d = '') => (v ?? d).trim();

export const SITE_URL = env(process.env.EXPO_PUBLIC_SITE_URL, 'https://sinekvalesii.github.io/adaymemur').replace(/\/$/, '');
export const DATA_URL = `${SITE_URL}/data`;

export const LINKS = {
  gizlilik: `${SITE_URL}/gizlilik.html`,
  kosullar: `${SITE_URL}/kosullar.html`,
  hesapSil: `${SITE_URL}/hesap-silme.html`,
  forumKurallari: `${SITE_URL}/forum-kurallari.html`,
  destekEposta: env(process.env.EXPO_PUBLIC_SUPPORT_EMAIL),
  playStore: 'https://play.google.com/store/apps/details?id=com.selcukyus.adaymemur',
  abonelikYonet: 'https://play.google.com/store/account/subscriptions?package=com.selcukyus.adaymemur',
};

/** RevenueCat Google Play public SDK anahtarı (goog_...). */
export const REVENUECAT_ANDROID_KEY = env(process.env.EXPO_PUBLIC_REVENUECAT_ANDROID_KEY);
/** RevenueCat'te tanımlanacak yetki (entitlement) kimliği. */
export const PRO_ENTITLEMENT = env(process.env.EXPO_PUBLIC_PRO_ENTITLEMENT, 'pro');

/** AdMob reklam birimleri. Şimdilik boş: reklam alanları hiç görünmez. */
export const AD_UNITS = {
  banner: env(process.env.EXPO_PUBLIC_ADMOB_BANNER_ID),
  listeArasi: env(process.env.EXPO_PUBLIC_ADMOB_INLINE_ID),
};

/** Ücretsiz kullanıcılar yeni ilanları bu kadar dakika sonra görür (Pro: anında). */
export const ERKEN_ERISIM_DK = Number(env(process.env.EXPO_PUBLIC_EARLY_ACCESS_MIN, '60')) || 0;
/** Ücretsiz kullanıcının kaydedebileceği ilan sayısı. */
export const UCRETSIZ_KAYIT_LIMITI = 10;
