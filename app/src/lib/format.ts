const AYLAR = ['Ocak', 'Şubat', 'Mart', 'Nisan', 'Mayıs', 'Haziran', 'Temmuz', 'Ağustos', 'Eylül', 'Ekim', 'Kasım', 'Aralık'];
const AYLAR_KISA = ['Oca', 'Şub', 'Mar', 'Nis', 'May', 'Haz', 'Tem', 'Ağu', 'Eyl', 'Eki', 'Kas', 'Ara'];
const GUNLER = ['Pazar', 'Pazartesi', 'Salı', 'Çarşamba', 'Perşembe', 'Cuma', 'Cumartesi'];

/** ISO tarihi (YYYY-MM-DD) yerel gün başlangıcına çevirir. */
export function gun(iso: string): Date {
  const [y, m, d] = iso.slice(0, 10).split('-').map(Number);
  return new Date(y, m - 1, d);
}

export function bugun(): Date {
  const n = new Date();
  return new Date(n.getFullYear(), n.getMonth(), n.getDate());
}

/** Son başvuruya kalan gün (bugün = 0, geçmiş = negatif). */
export function kalanGun(iso: string | null): number | null {
  if (!iso) return null;
  return Math.round((gun(iso).getTime() - bugun().getTime()) / 86400000);
}

export function tarihUzun(iso: string): string {
  const d = gun(iso);
  return `${d.getDate()} ${AYLAR[d.getMonth()]} ${d.getFullYear()}`;
}

export function tarihKisa(iso: string): string {
  const d = gun(iso);
  return `${d.getDate()} ${AYLAR_KISA[d.getMonth()]}`;
}

export function gunAdi(iso: string): string {
  return GUNLER[gun(iso).getDay()];
}

export function kalanMetin(iso: string | null): { metin: string; ton: 'bad' | 'warn' | 'ok' | 'mute' } {
  const k = kalanGun(iso);
  if (k === null) return { metin: 'Tarih ilanda', ton: 'mute' };
  if (k < 0) return { metin: 'Süresi doldu', ton: 'mute' };
  if (k === 0) return { metin: 'Son gün bugün', ton: 'bad' };
  if (k === 1) return { metin: 'Son gün yarın', ton: 'bad' };
  if (k <= 3) return { metin: `Son ${k} gün`, ton: 'bad' };
  if (k <= 7) return { metin: `${k} gün kaldı`, ton: 'warn' };
  return { metin: `${k} gün kaldı`, ton: 'ok' };
}

export function goreli(isoDateTime: string): string {
  const t = new Date(isoDateTime).getTime();
  const fark = Math.max(0, Date.now() - t);
  const dk = Math.floor(fark / 60000);
  if (dk < 1) return 'şimdi';
  if (dk < 60) return `${dk} dk önce`;
  const sa = Math.floor(dk / 60);
  if (sa < 24) return `${sa} sa önce`;
  const g = Math.floor(sa / 24);
  if (g < 7) return `${g} gün önce`;
  return tarihKisa(new Date(t).toISOString().slice(0, 10));
}

export function sayi(n: number): string {
  return n.toLocaleString('tr-TR');
}

/** Karşılaştırma için Türkçe katlama. */
export function katla(s: string | null | undefined): string {
  if (!s) return '';
  return s
    .replace(/İ/g, 'i')
    .replace(/I/g, 'ı')
    .toLowerCase()
    .replace(/ı/g, 'i')
    .replace(/ş/g, 's')
    .replace(/ğ/g, 'g')
    .replace(/ü/g, 'u')
    .replace(/ö/g, 'o')
    .replace(/ç/g, 'c')
    .replace(/[âà]/g, 'a')
    .replace(/[îì]/g, 'i')
    .replace(/[ûù]/g, 'u')
    .replace(/\s+/g, ' ')
    .trim();
}
