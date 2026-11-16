import { router } from 'expo-router';

import { bildir } from '../components/Bildirim';
import { UCRETSIZ_KAYIT_LIMITI } from './config';
import { proDeposu } from './pro';
import { kayitDeposu, useDepo } from './store';
import type { Ilan } from './types';

export function useKayitli(id: string) {
  return useDepo(kayitDeposu, (s) => !!s.kayitlar[id]);
}

export function useKayitIdleri(): Record<string, unknown> {
  return useDepo(kayitDeposu, (s) => s.kayitlar);
}

export function kaydetDegistir(ilan: Ilan) {
  const { kayitlar } = kayitDeposu.get();
  if (kayitlar[ilan.id]) {
    const yeni = { ...kayitlar };
    delete yeni[ilan.id];
    kayitDeposu.set({ kayitlar: yeni });
    bildir('Kaydedilenlerden çıkarıldı', undefined, { ikon: 'yerimi' });
    return;
  }
  if (!proDeposu.get().pro && Object.keys(kayitlar).length >= UCRETSIZ_KAYIT_LIMITI) {
    bildir(`Ücretsiz planda ${UCRETSIZ_KAYIT_LIMITI} ilan kaydedebilirsin`, 'Sınırsız kayıt için Pro’ya geç.', {
      ikon: 'kilit',
      onPress: () => router.push('/pro'),
    });
    return;
  }
  kayitDeposu.set({ kayitlar: { ...kayitlar, [ilan.id]: { ilan, eklenme: new Date().toISOString() } } });
  bildir('İlan kaydedildi', 'Son başvuru tarihini Kaydedilenler’den takip edebilirsin.', { ikon: 'yerimiDolu' });
}
