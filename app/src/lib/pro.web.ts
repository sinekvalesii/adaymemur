import { kaliciDepo } from './store';

export interface Paket {
  id: string;
  tip: 'aylik' | 'yillik' | 'diger';
  fiyat: string;
  aylikKarsilik: string | null;
  deneme: string | null;
  ham: unknown;
}

export const proDeposu = kaliciDepo<{ pro: boolean; bitis: string | null; yenileniyor: boolean }>('am.pro.v1', {
  pro: false,
  bitis: null,
  yenileniyor: false,
});
export const aboneligeHazir = () => true;
export async function aboneligiBaslat() {}
export async function paketleriGetir(): Promise<Paket[]> {
  return [
    { id: 'yillik', tip: 'yillik', fiyat: '₺399,99', aylikKarsilik: '33,33 ₺', deneme: '7 gün ücretsiz', ham: null },
    { id: 'aylik', tip: 'aylik', fiyat: '₺59,99', aylikKarsilik: null, deneme: null, ham: null },
  ];
}
export async function satinAl(_p: Paket): Promise<'tamam' | 'iptal'> {
  proDeposu.set({ pro: true });
  return 'tamam';
}
export async function geriYukle() {
  return false;
}
