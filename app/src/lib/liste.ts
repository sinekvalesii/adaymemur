import { ERKEN_ERISIM_DK } from './config';
import { katla, kalanGun } from './format';
import { ILLER, TUR_ADI } from './sabitler';
import type { Filtre, Ilan } from './types';

/** Ücretsiz kullanıcı için ilanın kilidinin açılmasına kalan dakika (yoksa null). */
export function kilitDakika(ilan: Ilan, pro: boolean, simdi = Date.now()): number | null {
  if (pro || !ERKEN_ERISIM_DK) return null;
  const gecen = (simdi - new Date(ilan.eklenme).getTime()) / 60000;
  if (gecen < 0 || gecen >= ERKEN_ERISIM_DK) return null;
  return Math.max(1, Math.ceil(ERKEN_ERISIM_DK - gecen));
}

export function aktifFiltreSayisi(f: Filtre): number {
  return (
    (f.iller.length ? 1 : 0) +
    (f.turler.length ? 1 : 0) +
    (f.egitim.length ? 1 : 0) +
    (f.puanTuru ? 1 : 0) +
    (f.suresiGecenleriGoster ? 1 : 0)
  );
}

export function filtrele(ilanlar: Ilan[], f: Filtre): Ilan[] {
  const q = katla(f.arama);
  const terimler = q ? q.split(' ').filter(Boolean) : [];
  const out = ilanlar.filter((i) => {
    if (!f.suresiGecenleriGoster) {
      const k = kalanGun(i.sonBasvuru);
      if (k !== null && k < 0) return false;
    }
    if (f.iller.length && i.iller.length && !i.iller.some((x) => f.iller.includes(x))) return false;
    if (f.turler.length && !f.turler.includes(i.tur)) return false;
    if (f.egitim.length && i.egitim.length && !i.egitim.some((e) => f.egitim.includes(e))) return false;
    if (f.puanTuru) {
      if (i.kpssGerekli === false) {
        // KPSS şartsız ilanlar puan filtresinde de görünür
      } else {
        const k = i.kpss.find((x) => x.tur === f.puanTuru);
        if (!k) return false;
        if (f.minPuan !== null && k.min !== undefined && k.min > f.minPuan) return false;
      }
    }
    if (terimler.length) {
      const hay = katla(
        [i.baslik, i.kurum, i.unvanlar.join(' '), i.iller.map((x) => ILLER[x]).join(' '), TUR_ADI[i.tur], i.ozet, i.kpss.map((k) => k.tur).join(' ')].join(' '),
      );
      if (!terimler.every((t) => hay.includes(t))) return false;
    }
    return true;
  });
  if (f.siralama === 'sonGun') {
    out.sort((a, b) => (a.sonBasvuru ?? '9999').localeCompare(b.sonBasvuru ?? '9999'));
  } else if (f.siralama === 'kadro') {
    out.sort((a, b) => (b.kadro ?? 0) - (a.kadro ?? 0));
  }
  return out;
}
