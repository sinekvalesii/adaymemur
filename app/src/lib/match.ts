import { katla, kalanGun } from './format';
import { EGITIM_ADI, EGITIM_SIRA, ILLER } from './sabitler';
import type { Ilan, Profil } from './types';

export type Kontrol = 'evet' | 'hayir' | 'belirsiz';
export type Durum = 'uygun' | 'kontrol' | 'degil';

export interface Gerekce {
  alan: 'egitim' | 'kpss' | 'bolum' | 'il' | 'tarih';
  sonuc: Kontrol;
  metin: string;
}

export interface Uygunluk {
  durum: Durum;
  puan: number;
  gerekceler: Gerekce[];
}

const p2 = (n: number) => n.toLocaleString('tr-TR', { maximumFractionDigits: 3 });

const BOS_KELIMELER = new Set(['ve', 'bolumu', 'bolum', 'programi', 'lisans', 'onlisans', 'fakultesi', 'mezun', 'bilimi', 'bilimleri']);

/** "Lojistik Yönetimi" → ["lojistik"]; "Bilgisayar Mühendisliği" → ["bilgisayar muhendisligi"] */
export function bolumAnahtarlari(bolum: string): string[] {
  const k = katla(bolum).replace(/[^a-z\s]/g, ' ').replace(/\s+/g, ' ').trim();
  if (!k) return [];
  if (/muhendisligi$/.test(k)) return [k];
  const kel = k.split(' ').filter((w) => w.length > 3 && !BOS_KELIMELER.has(w));
  return kel.length ? [kel.join(' '), kel[0]] : [k];
}

export function uygunluk(ilan: Ilan, p: Profil, detayMetni?: string): Uygunluk {
  const g: Gerekce[] = [];

  // Başvuru tarihi
  const kalan = kalanGun(ilan.sonBasvuru);
  if (kalan !== null && kalan < 0) g.push({ alan: 'tarih', sonuc: 'hayir', metin: 'Başvuru süresi doldu' });

  // Öğrenim
  if (p.egitim) {
    if (!ilan.egitim.length) {
      g.push({ alan: 'egitim', sonuc: 'belirsiz', metin: 'Öğrenim şartı ilan metninde' });
    } else if (ilan.egitim.includes(p.egitim)) {
      g.push({ alan: 'egitim', sonuc: 'evet', metin: `${EGITIM_ADI[p.egitim]} mezunları başvurabilir` });
    } else {
      const benim = EGITIM_SIRA.indexOf(p.egitim);
      const ust = Math.max(...ilan.egitim.map((e) => EGITIM_SIRA.indexOf(e)));
      if (benim < Math.min(...ilan.egitim.map((e) => EGITIM_SIRA.indexOf(e)))) {
        g.push({ alan: 'egitim', sonuc: 'hayir', metin: `İstenen: ${ilan.egitim.map((e) => EGITIM_ADI[e]).join(', ')}` });
      } else if (benim > ust) {
        g.push({ alan: 'egitim', sonuc: 'belirsiz', metin: `Kadrolar ${ilan.egitim.map((e) => EGITIM_ADI[e]).join(', ')} düzeyinde; o düzeyin diplomasıyla başvurulur` });
      } else {
        g.push({ alan: 'egitim', sonuc: 'hayir', metin: `İstenen: ${ilan.egitim.map((e) => EGITIM_ADI[e]).join(', ')}` });
      }
    }
  }

  // KPSS
  if (ilan.kpssGerekli === false) {
    g.push({ alan: 'kpss', sonuc: 'evet', metin: 'KPSS şartı yok' });
  } else if (ilan.kpss.length) {
    const eslesen = ilan.kpss.filter((k) => p.puanlar[k.tur] !== undefined);
    if (!eslesen.length) {
      g.push({ alan: 'kpss', sonuc: 'hayir', metin: `İstenen puan türü: ${ilan.kpss.map((k) => k.tur).join(', ')}` });
    } else {
      const yeten = eslesen.find((k) => k.min === undefined || p.puanlar[k.tur] >= k.min);
      if (yeten) {
        g.push({
          alan: 'kpss',
          sonuc: 'evet',
          metin: yeten.min !== undefined
            ? `KPSS ${yeten.tur}: ${p2(p.puanlar[yeten.tur])} ≥ ${yeten.min} taban`
            : `KPSS ${yeten.tur} puanınla başvurabilirsin`,
        });
      } else {
        const k = eslesen[0];
        g.push({ alan: 'kpss', sonuc: 'hayir', metin: `KPSS ${k.tur}: ${p2(p.puanlar[k.tur])} < ${k.min} taban` });
      }
    }
  } else if (ilan.kpssGerekli) {
    g.push({ alan: 'kpss', sonuc: 'belirsiz', metin: 'KPSS istenir; puan türü ilan metninde' });
  } else {
    g.push({ alan: 'kpss', sonuc: 'belirsiz', metin: 'KPSS şartını ilan metninden kontrol et' });
  }

  // Bölüm
  if (ilan.bolumSarti === 'yok') {
    g.push({ alan: 'bolum', sonuc: 'evet', metin: 'Bölüm şartı yok' });
  } else if (p.bolum.trim()) {
    const metin = katla([detayMetni ?? '', ilan.ozet, ilan.bolumler.join(' '), ilan.unvanlar.join(' ')].join(' '));
    const anahtar = bolumAnahtarlari(p.bolum);
    if (anahtar.some((a) => metin.includes(a))) {
      g.push({ alan: 'bolum', sonuc: 'evet', metin: `${p.bolum} ilanda geçiyor` });
    } else if (detayMetni && ilan.bolumSarti === 'var' && !ilan.egitim.every((e) => e === 'ortaogretim')) {
      g.push({ alan: 'bolum', sonuc: 'hayir', metin: `${p.bolum} istenen bölümler arasında görünmüyor` });
    } else {
      g.push({ alan: 'bolum', sonuc: 'belirsiz', metin: 'Bölüm şartını ilan metninden kontrol et' });
    }
  }

  // İl
  if (p.iller.length) {
    if (!ilan.iller.length) g.push({ alan: 'il', sonuc: 'evet', metin: 'Türkiye geneli / çok ilde kadro' });
    else if (ilan.iller.some((i) => p.iller.includes(i))) g.push({ alan: 'il', sonuc: 'evet', metin: ilan.iller.map((i) => ILLER[i]).join(', ') });
    else g.push({ alan: 'il', sonuc: 'hayir', metin: `Kadro yeri: ${ilan.iller.map((i) => ILLER[i]).join(', ')}` });
  }

  const hayir = g.filter((x) => x.sonuc === 'hayir').length;
  const belirsiz = g.filter((x) => x.sonuc === 'belirsiz').length;
  const evet = g.filter((x) => x.sonuc === 'evet').length;
  const durum: Durum = hayir ? 'degil' : belirsiz ? 'kontrol' : 'uygun';
  const puan = evet * 10 - belirsiz * 3 - hayir * 50 + (ilan.kadro ? Math.min(10, Math.log10(ilan.kadro + 1) * 3) : 0);
  return { durum, puan, gerekceler: g };
}
