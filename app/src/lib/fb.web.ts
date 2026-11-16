/** Web önizleme için Firebase yerine geçen boş uygulama (yalnızca geliştirme). */
export type Kullanici = { uid: string; ad: string; eposta: string | null };
export type GelenBildirim = { baslik: string; govde: string; ilanId?: string };
export interface Konu {
  id: string; baslik: string; metin: string; kategori: string; yazarId: string; yazarAd: string;
  olusturma: number; sonHareket: number; yorumSayisi: number; raporSayisi: number;
}
export interface Yorum { id: string; metin: string; yazarId: string; yazarAd: string; olusturma: number; raporSayisi: number }

const saat = (h: number) => Date.now() - h * 3600000;
const ORNEK: Konu[] = [
  { id: 'k1', baslik: 'Önlisans P93 ile büro personeli atamasında taban puan kaç oldu?', metin: 'Geçen dönem 2024/9 tercihlerinde büro personeli tabanları hakkında bilgisi olan var mı?', kategori: 'KPSS', yazarId: 'u1', yazarAd: 'Merve K.', olusturma: saat(3), sonHareket: saat(1), yorumSayisi: 7, raporSayisi: 0 },
  { id: 'k2', baslik: 'Koruma ve güvenlik görevlisi mülakatında neler soruluyor?', metin: 'Sözlü sınava hazırlanıyorum, deneyimi olanlar paylaşabilir mi?', kategori: 'Mülakat', yazarId: 'u2', yazarAd: 'Emre T.', olusturma: saat(9), sonHareket: saat(4), yorumSayisi: 12, raporSayisi: 0 },
  { id: 'k3', baslik: 'Sözleşmeli personelin kadroya geçiş süresi', metin: '4/B sözleşmeli olarak başladım, kadroya geçiş ne zaman oluyor?', kategori: 'Atama', yazarId: 'u3', yazarAd: 'Ayşe D.', olusturma: saat(30), sonHareket: saat(20), yorumSayisi: 4, raporSayisi: 0 },
];

export const firebaseHazir = () => true;
export const bildirimIzniIste = async () => false;
export const konulariEsitle = async (o: { onceki: string[] }) => o.onceki;
export const bildirimDinle = () => () => {};
export function oturumDinle(cb: (k: Kullanici | null) => void) {
  cb(null);
  return () => {};
}
export const hataMetni = () => 'Web önizlemesinde hesap işlemleri kapalı.';
const kapali = async (..._a: unknown[]): Promise<never> => {
  throw new Error('web');
};
export const girisYap = kapali;
export const kayitOl = kapali;
export const sifreSifirla = kapali;
export const cikisYap = kapali;
export const hesabiSil = kapali;
export const konuAc = kapali as (b: string, m: string, k: string) => Promise<string>;
export const yorumYaz = kapali;
export const icerikSil = kapali;
export const raporla = kapali;
export function konulariDinle(kategori: string | null, cb: (k: Konu[]) => void, _h: (e: unknown) => void) {
  cb(kategori ? ORNEK.filter((k) => k.kategori === kategori) : ORNEK);
  return () => {};
}
export function konuDinle(id: string, cb: (k: Konu | null) => void) {
  cb(ORNEK.find((k) => k.id === id) ?? null);
  return () => {};
}
export function yorumlariDinle(_id: string, cb: (y: Yorum[]) => void) {
  cb([
    { id: 'y1', metin: 'Bende P93 ile 72 puanla yerleşen bir arkadaşım var, il tercihi çok etkiliyor.', yazarId: 'u4', yazarAd: 'Burak S.', olusturma: saat(2), raporSayisi: 0 },
    { id: 'y2', metin: 'ÖSYM tercih kılavuzundaki kadro sayısına bak, az kadrolu illerde taban yükseliyor.', yazarId: 'u5', yazarAd: 'Zeynep A.', olusturma: saat(1), raporSayisi: 0 },
  ]);
  return () => {};
}
