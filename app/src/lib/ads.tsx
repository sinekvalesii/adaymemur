/**
 * Reklam altyapısı (Google AdMob).
 * Reklam birimi kimlikleri config.ts → AD_UNITS üzerinden gelir. Boşsa:
 *  - SDK başlatılmaz, onay (UMP) formu gösterilmez,
 *  - <ReklamAlani> hiçbir şey çizmez.
 * Pro aboneler reklam görmez.
 */
import { useState } from 'react';
import { View } from 'react-native';
import mobileAds, {
  AdsConsent,
  AdsConsentPrivacyOptionsRequirementStatus,
  BannerAd,
  BannerAdSize,
} from 'react-native-google-mobile-ads';

import { AD_UNITS } from './config';
import { proDeposu } from './pro';
import { kaliciDepo, useDepo } from './store';

const reklamDeposu = kaliciDepo<{ hazir: boolean; gizlilikSecenegi: boolean }>('am.reklam.v1', {
  hazir: false,
  gizlilikSecenegi: false,
});

export const reklamVar = () => Object.values(AD_UNITS).some(Boolean);

let baslatildi = false;
export async function reklamlariBaslat() {
  if (baslatildi || !reklamVar()) return;
  baslatildi = true;
  try {
    const info = await AdsConsent.gatherConsent();
    reklamDeposu.set({ gizlilikSecenegi: info.privacyOptionsRequirementStatus === AdsConsentPrivacyOptionsRequirementStatus.REQUIRED });
    if (!info.canRequestAds) return;
    await mobileAds().initialize();
    reklamDeposu.set({ hazir: true });
  } catch {
    // Onay alınamadı veya ağ hatası: reklam gösterilmez
  }
}

export async function reklamGizlilikAyarlari() {
  try {
    await AdsConsent.showPrivacyOptionsForm();
  } catch {
    // yok say
  }
}

export function useReklamGizlilikSecenegi() {
  return useDepo(reklamDeposu, (s) => s.gizlilikSecenegi);
}

export function ReklamAlani({ yer = 'banner' }: { yer?: keyof typeof AD_UNITS }) {
  const pro = useDepo(proDeposu, (s) => s.pro);
  const hazir = useDepo(reklamDeposu, (s) => s.hazir);
  const [hata, setHata] = useState(false);
  const birim = AD_UNITS[yer];
  if (pro || !hazir || !birim || hata) return null;
  return (
    <View style={{ alignItems: 'center', marginVertical: 8 }}>
      <BannerAd unitId={birim} size={BannerAdSize.ANCHORED_ADAPTIVE_BANNER} onAdFailedToLoad={() => setHata(true)} />
    </View>
  );
}
