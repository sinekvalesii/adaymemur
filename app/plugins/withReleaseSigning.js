/**
 * Release derlemesini yükleme anahtarıyla imzalar.
 * Anahtar bilgileri ortam değişkenlerinden ya da ~/.gradle/gradle.properties'ten okunur:
 *   AM_UPLOAD_STORE_FILE, AM_UPLOAD_STORE_PASSWORD, AM_UPLOAD_KEY_ALIAS, AM_UPLOAD_KEY_PASSWORD
 * Tanımlı değilse release, debug anahtarıyla imzalanır (yalnızca yerel test için).
 */
const { withAppBuildGradle } = require('expo/config-plugins');

const BLOK = `
        release {
            def amStore = System.getenv("AM_UPLOAD_STORE_FILE") ?: project.findProperty("AM_UPLOAD_STORE_FILE")
            if (amStore) {
                storeFile file(amStore)
                storePassword System.getenv("AM_UPLOAD_STORE_PASSWORD") ?: project.findProperty("AM_UPLOAD_STORE_PASSWORD")
                keyAlias System.getenv("AM_UPLOAD_KEY_ALIAS") ?: project.findProperty("AM_UPLOAD_KEY_ALIAS")
                keyPassword System.getenv("AM_UPLOAD_KEY_PASSWORD") ?: project.findProperty("AM_UPLOAD_KEY_PASSWORD")
            }
        }`;

module.exports = function withReleaseSigning(config) {
  return withAppBuildGradle(config, (cfg) => {
    let g = cfg.modResults.contents;
    if (g.includes('AM_UPLOAD_STORE_FILE')) return cfg;
    // signingConfigs { debug { ... } } bloğunun sonuna release ekle
    g = g.replace(/signingConfigs\s*\{\s*debug\s*\{[^}]*\}/, (m) => m + BLOK);
    // release buildType: anahtar varsa release imzası
    g = g.replace(
      /(buildTypes\s*\{[\s\S]*?release\s*\{[\s\S]*?)signingConfig\s+signingConfigs\.debug/,
      (_m, on) =>
        `${on}signingConfig((System.getenv("AM_UPLOAD_STORE_FILE") ?: project.findProperty("AM_UPLOAD_STORE_FILE")) ? signingConfigs.release : signingConfigs.debug)`,
    );
    cfg.modResults.contents = g;
    return cfg;
  });
};
