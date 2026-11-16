import { useSyncExternalStore } from 'react';

import { oturumDinle, type Kullanici } from './fb';

let kullanici: Kullanici | null = null;
let bilindi = false;
const dinleyici = new Set<() => void>();

export function oturumuBaslat() {
  return oturumDinle((k) => {
    kullanici = k;
    bilindi = true;
    dinleyici.forEach((f) => f());
  });
}

export function useKullanici(): { kullanici: Kullanici | null; bilindi: boolean } {
  const k = useSyncExternalStore(
    (f) => {
      dinleyici.add(f);
      return () => dinleyici.delete(f);
    },
    () => kullanici,
    () => kullanici,
  );
  return { kullanici: k, bilindi };
}
