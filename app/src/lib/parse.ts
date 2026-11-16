/**
 * Kural tabanlı ilan ayrıştırıcı — pipeline/adaymemur/parse.py ile aynı mantık.
 * Kariyer Kapısı ilan metni yalnızca Türkiye IP'lerine açık olduğundan bu ilanların
 * detayını cihaz çeker ve burada ayrıştırır.
 */
import { katla } from './format';
import type { BolumSarti, Egitim, KpssSart } from './types';

const AYLAR: Record<string, number> = {
  ocak: 1, subat: 2, mart: 3, nisan: 4, mayis: 5, haziran: 6,
  temmuz: 7, agustos: 8, eylul: 9, ekim: 10, kasim: 11, aralik: 12,
};
const AY = Object.keys(AYLAR).join('|');
const D_NUM = String.raw`(\d{1,2})\s?[./-]\s?(\d{1,2})\s?[./-]\s?(\d{4})`;
const D_TXT = String.raw`(\d{1,2})\s+(${AY})\s+(\d{4})`;

const RANGE = [
  new RegExp(String.raw`${D_NUM}\s*(?:-|–|ile|ila|ve)\s*${D_NUM}`, 'g'),
  new RegExp(String.raw`${D_TXT}\s*(?:-|–|ile|ila|ve)\s*${D_TXT}`, 'g'),
  new RegExp(String.raw`(\d{1,2})\s*(?:-|–)\s*(\d{1,2})\s+(${AY})\s+(\d{4})`, 'g'),
  new RegExp(String.raw`(\d{1,2})\s+(${AY})\s*(?:-|–)\s*(\d{1,2})\s+(${AY})\s+(\d{4})`, 'g'),
  new RegExp(String.raw`(\d{1,2})[./](\d{1,2})\s*(?:-|–)\s*(\d{1,2})[./](\d{1,2})[./](\d{4})`, 'g'),
];
const SINGLE = [new RegExp(D_NUM, 'g'), new RegExp(D_TXT, 'g')];

type D = { y: number; m: number; d: number } | null;
const mk = (d: string | number, m: string | number, y: string | number): D => {
  const dd = Number(d), mm = Number(m), yy = Number(y);
  if (yy < 2020 || yy > 2100 || mm < 1 || mm > 12 || dd < 1 || dd > 31) return null;
  const t = new Date(yy, mm - 1, dd);
  if (t.getMonth() !== mm - 1) return null;
  return { y: yy, m: mm, d: dd };
};
const iso = (x: D) => (x ? `${x.y}-${String(x.m).padStart(2, '0')}-${String(x.d).padStart(2, '0')}` : null);
const num = (x: D) => (x ? x.y * 10000 + x.m * 100 + x.d : 0);
const ay = (s: string) => AYLAR[s];

function rangeFrom(g: string[], i: number): [D, D] {
  switch (i) {
    case 0: return [mk(g[1], g[2], g[3]), mk(g[4], g[5], g[6])];
    case 1: return [mk(g[1], ay(g[2]), g[3]), mk(g[4], ay(g[5]), g[6])];
    case 2: return [mk(g[1], ay(g[3]), g[4]), mk(g[2], ay(g[3]), g[4])];
    case 3: return [mk(g[1], ay(g[2]), g[5]), mk(g[3], ay(g[4]), g[5])];
    case 4: return [mk(g[1], g[2], g[5]), mk(g[3], g[4], g[5])];
  }
  return [null, null];
}

export function tarihler(text: string): { baslangic: string | null; son: string | null } {
  const t = katla(text);
  const aday: { s: number; b: D; e: D }[] = [];
  const skor = (pos: number, end: number) => {
    const once = t.slice(Math.max(0, pos - 160), pos);
    const sonra = t.slice(end, end + 80);
    let s = 0;
    if (/basvuru|basvurular|son gun|tarihleri arasinda|saat 23/.test(once)) s += 3;
    if (/tarihleri arasinda|arasinda|kadar|son gun/.test(sonra)) s += 2;
    if (/sinav|mulakat|sozlu|yazili|sonuc|ilan edil|yayim/.test(once.slice(-60))) s -= 2;
    return s;
  };
  RANGE.forEach((rx, i) => {
    rx.lastIndex = 0;
    for (const m of t.matchAll(rx)) {
      const [b, e] = rangeFrom(m as unknown as string[], i);
      if (e && (!b || num(b) <= num(e))) aday.push({ s: skor(m.index!, m.index! + m[0].length) + 1, b, e });
    }
  });
  if (!aday.length) {
    SINGLE.forEach((rx) => {
      for (const m of t.matchAll(rx)) {
        const mm = /^\d+$/.test(m[2]) ? m[2] : ay(m[2]);
        const d = mk(m[1], mm, m[3]);
        const s = skor(m.index!, m.index! + m[0].length);
        if (d && s >= 3) aday.push({ s, b: null, e: d });
      }
    });
  }
  if (!aday.length) return { baslangic: null, son: null };
  aday.sort((a, b) => b.s - a.s || num(a.e) - num(b.e));
  return { baslangic: iso(aday[0].b), son: iso(aday[0].e) };
}

export function kpss(text: string): { kpss: KpssSart[]; gerekli: boolean | null } {
  const t = katla(text);
  const turler: string[] = [];
  for (const m of t.matchAll(/\bkpss\s*[-_]?\s*p\s*[-_]?\s*(\d{1,3})\b|\b(?:puan turu|puan turleri)\s*:?\s*(?:kpss)?\s*p\s?(\d{1,3})\b/g)) {
    const n = Number(m[1] ?? m[2]);
    if (n >= 1 && n <= 125 && !turler.includes(`P${n}`)) turler.push(`P${n}`);
  }
  if (!turler.length && t.includes('kpss')) {
    for (const m of t.matchAll(/\bp\s?(3|93|94|\d{1,2})\b/g)) {
      const ctx = t.slice(Math.max(0, m.index! - 60), m.index! + m[0].length + 30);
      if (ctx.includes('puan') && !turler.includes(`P${Number(m[1])}`)) turler.push(`P${Number(m[1])}`);
    }
  }
  const minRx = [
    /(\d{2,3})(?:[.,]\d+)?\s*(?:\(\w+\)\s*)?(?:ve|veya)\s*(?:daha\s*)?(?:uzeri|ustu|yuksek)/g,
    /en\s*az\s*(\d{2,3})(?:[.,]\d+)?\s*(?:\(\w+\)\s*)?puan/g,
    /(\d{2,3})(?:[.,]\d+)?\s*puan(?:in| ve)?\s*(?:ve\s*)?uzeri/g,
    /(?:taban|asgari|minimum)\s*(?:kpss\s*)?puan[a-z]*\s*:?\s*(\d{2,3})/g,
  ];
  const minler: number[] = [];
  for (const rx of minRx) {
    for (const m of t.matchAll(rx)) {
      const ctx = t.slice(Math.max(0, m.index! - 120), m.index! + m[0].length + 40);
      const v = Number(m[1]);
      if ((ctx.includes('kpss') || ctx.includes('puan')) && v >= 40 && v <= 100) minler.push(v);
    }
  }
  const taban = minler.length ? Math.min(...minler) : undefined;
  let gerekli: boolean | null = null;
  if (/kpss\s*(?:puan[a-z]*\s*)?(?:sarti\s*)?(?:aranmaz|aranmayacak|sarti yoktur|sarti bulunmamaktadir)/.test(t)) gerekli = false;
  else if (turler.length || /kpss\s*\(?b\)?\s*grubu|kpss puan/.test(t)) gerekli = true;
  return { kpss: turler.map((tur) => (taban !== undefined ? { tur, min: taban } : { tur })), gerekli };
}

const SIRA: Egitim[] = ['ilkogretim', 'ortaogretim', 'onlisans', 'lisans', 'yukseklisans', 'doktora'];

export function egitim(text: string, k: KpssSart[] = []): Egitim[] {
  const t = katla(text);
  const f = new Set<Egitim>();
  if (/\b(ortaogretim|lise|meslek lisesi|lise ve dengi)\b/.test(t)) f.add('ortaogretim');
  if (/\bon\s?lisans\b|\bonlisans\b|meslek yuksekokulu/.test(t)) f.add('onlisans');
  for (const m of t.matchAll(/\blisans\b/g)) {
    if (!/(on\s?|yuksek\s?)$/.test(t.slice(Math.max(0, m.index! - 8), m.index!))) {
      f.add('lisans');
      break;
    }
  }
  if (/yuksek\s?lisans|master/.test(t)) f.add('yukseklisans');
  if (/\bdoktora\b|doktor ogretim uyesi/.test(t)) f.add('doktora');
  if (/\bilkogretim\b|\bilkokul\b|\bortaokul\b/.test(t)) f.add('ilkogretim');
  for (const x of k) {
    const n = Number(x.tur.slice(1));
    if (n === 3) f.add('lisans');
    else if (n === 93) f.add('onlisans');
    else if (n === 94) f.add('ortaogretim');
    else if ((n >= 1 && n <= 48) || (n >= 100 && n <= 125)) f.add('lisans');
  }
  return SIRA.filter((e) => f.has(e));
}

export function bolumSarti(text: string): BolumSarti {
  const t = katla(text);
  if (
    /herhangi bir (?:lisans|on\s?lisans|onlisans|yuksekogretim|ortaogretim|lise)?\s*(?:programindan|bolumunden|bolumden)|tum bolumler|tum lisans|her bolum|bolum (?:sarti|sinirlamasi) (?:aranmaz|yoktur|bulunmamaktadir)|bolum ayrimi (?:yapilmaksizin|yoktur)/.test(t)
  )
    return 'yok';
  if (/bolum(?:u|unden|lerinden|lerin|leri)\b|muhendisligi|fakultesi|programlarindan/.test(t)) return 'var';
  return 'bilinmiyor';
}

export function kadro(text: string): number | null {
  const t = katla(text);
  const rxs = [
    /toplam\s*(\d{1,3}(?:\.\d{3})*|\d+)\s*\(?[a-z ]{0,20}\)?\s*(?:adet\s*)?(?:sozlesmeli |surekli |kadrolu |gecici |daimi )?(?:personel|isci|memur|kadro|pozisyon|ogretim|uzman|aday)/,
    /(\d{1,3}(?:\.\d{3})*|\d+)\s*(?:adet\s*|kisi\s*|\(.{1,12}\)\s*)?(?:sozlesmeli |surekli |kadrolu |gecici |daimi )?[a-z ]{0,40}?(?:personel|isci|memur)\s*(?:alacak|alinacak|alimi|istihdam)/,
    /toplam\s*(\d{1,3}(?:\.\d{3})*|\d+)\s*(?:adet\s*|kisi\s*)?[a-z ]{0,40}?(?:alinacak|alimi yapilacak|istihdam edilecek)/,
  ];
  for (const rx of rxs) {
    const m = t.match(rx);
    if (m) {
      const n = Number(m[1].replace(/\./g, ''));
      if (n > 0 && n < 100000) return n;
    }
  }
  return null;
}

export interface Ayristirma {
  kpss: KpssSart[];
  kpssGerekli: boolean | null;
  egitim: Egitim[];
  bolumSarti: BolumSarti;
  kadro: number | null;
  basvuruBaslangic: string | null;
  sonBasvuru: string | null;
}

export function ayristir(baslik: string, metin: string): Ayristirma {
  const tam = `${baslik}\n${metin}`;
  const k = kpss(tam);
  const t = tarihler(metin || baslik);
  return {
    kpss: k.kpss,
    kpssGerekli: k.gerekli,
    egitim: egitim(tam, k.kpss),
    bolumSarti: metin ? bolumSarti(metin) : 'bilinmiyor',
    kadro: kadro(tam),
    basvuruBaslangic: t.baslangic,
    sonBasvuru: t.son,
  };
}
