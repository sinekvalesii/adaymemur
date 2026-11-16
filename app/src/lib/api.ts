import AsyncStorage from '@react-native-async-storage/async-storage';
import { useEffect, useMemo, useSyncExternalStore } from 'react';

import { DATA_URL } from './config';
import { ayristir } from './parse';
import type { Duyuru, Ilan, IlanlarYanit, Takvim } from './types';

const ONBELLEK = 'am.ilanlar.v1';
const ZENGIN = 'am.zengin.v1';

type Durum = 'bos' | 'yukleniyor' | 'hazir' | 'hata';

interface VeriDurumu {
  durum: Durum;
  ilanlar: Ilan[];
  guncelleme: string | null;
  hata: string | null;
  yenileniyor: boolean;
  takvim: Takvim | null;
  duyurular: Duyuru[];
  zengin: Record<string, Partial<Ilan>>; // cihazda zenginleştirilen alanlar
}

let s: VeriDurumu = {
  durum: 'bos',
  ilanlar: [],
  guncelleme: null,
  hata: null,
  yenileniyor: false,
  takvim: null,
  duyurular: [],
  zengin: {},
};
const dinleyici = new Set<() => void>();
const set = (p: Partial<VeriDurumu>) => {
  s = { ...s, ...p };
  dinleyici.forEach((f) => f());
};

async function getJson<T>(url: string, timeoutMs = 20000): Promise<T> {
  const ctl = new AbortController();
  const t = setTimeout(() => ctl.abort(), timeoutMs);
  try {
    const r = await fetch(`${url}${url.includes('?') ? '&' : '?'}t=${Math.floor(Date.now() / 60000)}`, {
      signal: ctl.signal,
      headers: { Accept: 'application/json' },
    });
    if (!r.ok) throw new Error(`HTTP ${r.status}`);
    return (await r.json()) as T;
  } finally {
    clearTimeout(t);
  }
}

let ilkYukleme: Promise<void> | null = null;

export function verileriBaslat() {
  if (!ilkYukleme) {
    ilkYukleme = (async () => {
      try {
        const [ham, zengin] = await Promise.all([AsyncStorage.getItem(ONBELLEK), AsyncStorage.getItem(ZENGIN)]);
        if (zengin) set({ zengin: JSON.parse(zengin) });
        if (ham) {
          const v = JSON.parse(ham) as IlanlarYanit & { takvim?: Takvim; duyurular?: Duyuru[] };
          set({ durum: 'hazir', ilanlar: v.ilanlar, guncelleme: v.guncelleme, takvim: v.takvim ?? null, duyurular: v.duyurular ?? [] });
        }
      } catch {
        // önbellek okunamadı
      }
      await yenile();
    })();
  }
  return ilkYukleme;
}

export async function yenile() {
  if (s.yenileniyor) return;
  set({ yenileniyor: true, durum: s.ilanlar.length ? s.durum : 'yukleniyor' });
  try {
    const [v, takvim, duy] = await Promise.all([
      getJson<IlanlarYanit>(`${DATA_URL}/ilanlar.json`),
      getJson<Takvim>(`${DATA_URL}/takvim.json`).catch(() => s.takvim),
      getJson<{ duyurular: Duyuru[] }>(`${DATA_URL}/duyurular.json`).catch(() => ({ duyurular: s.duyurular })),
    ]);
    set({ durum: 'hazir', ilanlar: v.ilanlar, guncelleme: v.guncelleme, hata: null, takvim, duyurular: duy?.duyurular ?? [] });
    AsyncStorage.setItem(ONBELLEK, JSON.stringify({ ...v, takvim, duyurular: duy?.duyurular ?? [] })).catch(() => {});
  } catch (e) {
    set({ durum: s.ilanlar.length ? 'hazir' : 'hata', hata: 'İlanlar şu an yüklenemedi. Bağlantını kontrol edip tekrar dene.' });
  } finally {
    set({ yenileniyor: false });
  }
}

export function useVeri<R>(sec: (v: VeriDurumu) => R): R {
  return useSyncExternalStore(
    (f) => {
      dinleyici.add(f);
      return () => dinleyici.delete(f);
    },
    () => sec(s),
    () => sec(s),
  );
}

/** Cihazda zenginleştirilmiş alanlarla birleştirilmiş ilan. */
export function birlestir(i: Ilan, z: Record<string, Partial<Ilan>>): Ilan {
  const ek = z[i.id];
  if (!ek) return i;
  const out = { ...i };
  (Object.keys(ek) as (keyof Ilan)[]).forEach((k) => {
    const v = ek[k];
    const mevcut = i[k];
    const bos = mevcut === null || mevcut === '' || (Array.isArray(mevcut) && !mevcut.length) || mevcut === 'bilinmiyor';
    if (v !== undefined && v !== null && bos) (out as Record<string, unknown>)[k] = v;
  });
  return out;
}

export function useIlanlar(): Ilan[] {
  const ilanlar = useVeri((v) => v.ilanlar);
  const zengin = useVeri((v) => v.zengin);
  return useMemo(() => (Object.keys(zengin).length ? ilanlar.map((i) => birlestir(i, zengin)) : ilanlar), [ilanlar, zengin]);
}

export function useIlan(id: string): Ilan | undefined {
  const ilan = useVeri((v) => v.ilanlar.find((i) => i.id === id));
  const zengin = useVeri((v) => v.zengin[id]);
  return useMemo(() => (ilan && zengin ? birlestir(ilan, { [id]: zengin }) : ilan), [ilan, zengin, id]);
}

export function useVeriBaslat() {
  useEffect(() => {
    verileriBaslat();
  }, []);
}

// --- İlan metni ---------------------------------------------------------------
const metinOnbellek = new Map<string, string>();

const KK_API = 'https://api.kariyerkapisi.gov.tr/api';

function temizle(html: string): string {
  return html
    .replace(/<\s*br\s*\/?>/gi, '\n')
    .replace(/<\/(p|div|li|tr|h\d)>/gi, '\n')
    .replace(/<[^>]+>/g, ' ')
    .replace(/\[\/?[a-z0-9=#]+\]/gi, ' ')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/[ \t]+/g, ' ')
    .replace(/\n\s*\n+/g, '\n\n')
    .trim();
}

async function kariyerKapisiMetni(guid: string): Promise<string | null> {
  const body = JSON.stringify({ ilanGuid: guid });
  const headers = { 'Content-Type': 'application/json', Accept: 'application/json' };
  const ctl = new AbortController();
  const t = setTimeout(() => ctl.abort(), 15000);
  try {
    const [a, b] = await Promise.all([
      fetch(`${KK_API}/ilan/GetIlanPreviewPublic`, { method: 'POST', body, headers, signal: ctl.signal }).then((r) => (r.ok ? r.json() : null)),
      fetch(`${KK_API}/altilan/GetAltIlanInfoByIlanIdPublic`, { method: 'POST', body, headers, signal: ctl.signal })
        .then((r) => (r.ok ? r.json() : []))
        .catch(() => []),
    ]);
    const parcalar = [a?.ilanMetni ?? ''];
    if (Array.isArray(b)) for (const x of b) parcalar.push(`${x?.ilanBaslik ?? ''}\n${x?.ilanMetni ?? ''}`);
    const metin = temizle(parcalar.join('\n\n'));
    return metin.length > 120 ? metin : null;
  } catch {
    return null;
  } finally {
    clearTimeout(t);
  }
}

/**
 * İlanın tam metni. Önce yayınlanmış metin dosyası; Kariyer Kapısı ilanında yoksa
 * cihazdan resmî servise sorulur ve sonuç ayrıştırılıp ilan alanlarına eklenir.
 */
export async function ilanMetni(ilan: Ilan): Promise<string | null> {
  if (metinOnbellek.has(ilan.id)) return metinOnbellek.get(ilan.id)!;
  let metin: string | null = null;
  if (ilan.detay) {
    try {
      const r = await getJson<{ metin: string }>(`${DATA_URL}/ilan/${encodeURIComponent(ilan.id)}.json`);
      metin = r.metin;
    } catch {
      metin = null;
    }
  }
  if (!metin && ilan.kaynak === 'kariyerkapisi') {
    metin = await kariyerKapisiMetni(ilan.id.replace(/^kk-/, ''));
    if (metin) {
      const p = ayristir(ilan.baslik, metin);
      const ek: Partial<Ilan> = { ...p, ozet: metin.slice(0, 320) };
      const zengin = { ...s.zengin, [ilan.id]: ek };
      // önbelleği sınırlı tut
      const anahtarlar = Object.keys(zengin);
      if (anahtarlar.length > 300) anahtarlar.slice(0, anahtarlar.length - 300).forEach((k) => delete zengin[k]);
      set({ zengin });
      AsyncStorage.setItem(ZENGIN, JSON.stringify(zengin)).catch(() => {});
    }
  }
  if (metin) metinOnbellek.set(ilan.id, metin);
  return metin;
}
