import AsyncStorage from '@react-native-async-storage/async-storage';
import { useSyncExternalStore } from 'react';

import type { Filtre, Ilan, Profil } from './types';

/** Kalıcı (AsyncStorage) küçük durum deposu. */
export function kaliciDepo<T extends object>(anahtar: string, baslangic: T) {
  let durum = baslangic;
  let yuklendi = false;
  const dinleyiciler = new Set<() => void>();
  let yazZamanlayici: ReturnType<typeof setTimeout> | null = null;

  const yayinla = () => dinleyiciler.forEach((f) => f());
  const yaz = () => {
    if (yazZamanlayici) clearTimeout(yazZamanlayici);
    yazZamanlayici = setTimeout(() => {
      AsyncStorage.setItem(anahtar, JSON.stringify(durum)).catch(() => {});
    }, 250);
  };

  return {
    get: () => durum,
    set(p: Partial<T> | ((s: T) => Partial<T>)) {
      const ek = typeof p === 'function' ? p(durum) : p;
      durum = { ...durum, ...ek };
      yayinla();
      yaz();
    },
    sifirla() {
      durum = baslangic;
      yayinla();
      AsyncStorage.removeItem(anahtar).catch(() => {});
    },
    async yukle() {
      if (yuklendi) return;
      try {
        const ham = await AsyncStorage.getItem(anahtar);
        if (ham) durum = { ...baslangic, ...JSON.parse(ham) };
      } catch {
        // bozuk kayıt: varsayılanlarla devam
      }
      yuklendi = true;
      yayinla();
    },
    abone(f: () => void) {
      dinleyiciler.add(f);
      return () => {
        dinleyiciler.delete(f);
      };
    },
  };
}

type Depo<T extends object> = ReturnType<typeof kaliciDepo<T>>;

export function useDepo<T extends object, R>(depo: Depo<T>, sec: (s: T) => R): R {
  return useSyncExternalStore(depo.abone, () => sec(depo.get()), () => sec(depo.get()));
}

// ---------------------------------------------------------------------------

export const profilDeposu = kaliciDepo<Profil>('am.profil.v1', {
  egitim: null,
  bolum: '',
  puanlar: {},
  iller: [],
  turler: [],
  tamamlandi: false,
});

export const VARSAYILAN_FILTRE: Filtre = {
  arama: '',
  iller: [],
  turler: [],
  egitim: [],
  puanTuru: null,
  minPuan: null,
  suresiGecenleriGoster: false,
  siralama: 'yeni',
};

export const filtreDeposu = kaliciDepo<Filtre>('am.filtre.v1', VARSAYILAN_FILTRE);

export interface Kayit {
  ilan: Ilan;
  eklenme: string;
}

export const kayitDeposu = kaliciDepo<{ kayitlar: Record<string, Kayit> }>('am.kayit.v1', { kayitlar: {} });

export interface Ayarlar {
  karsilamaGoruldu: boolean;
  bildirim: boolean;
  bildirimIlleri: string[]; // Pro bildirimleri için; boş = tüm Türkiye
  sonGorulenGuncelleme: string | null;
  goruldu: Record<string, 1>; // açılmış ilanlar
  forumKosullariOnay: boolean;
  engellenenler: string[]; // forum: engellenen kullanıcı kimlikleri
  acilisSayisi: number;
  degerlendirmeSoruldu: boolean;
  fcmKonular: string[];
}

export const ayarDeposu = kaliciDepo<Ayarlar>('am.ayar.v1', {
  karsilamaGoruldu: false,
  bildirim: true,
  bildirimIlleri: [],
  sonGorulenGuncelleme: null,
  goruldu: {},
  forumKosullariOnay: false,
  engellenenler: [],
  acilisSayisi: 0,
  degerlendirmeSoruldu: false,
  fcmKonular: [],
});

export async function tumDepolariYukle() {
  await Promise.all([profilDeposu.yukle(), filtreDeposu.yukle(), kayitDeposu.yukle(), ayarDeposu.yukle()]);
}
