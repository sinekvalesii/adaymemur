/**
 * Aday Memur Pro — Google Play aboneliği (RevenueCat üzerinden).
 * Anahtar tanımlı değilse abonelik ekranı "yakında" durumunda açılır.
 */
import { Platform } from 'react-native';
import Purchases, { LOG_LEVEL, type CustomerInfo, type PurchasesPackage } from 'react-native-purchases';

import { PRO_ENTITLEMENT, REVENUECAT_ANDROID_KEY } from './config';
import { kaliciDepo } from './store';

export interface Paket {
  id: string;
  tip: 'aylik' | 'yillik' | 'diger';
  fiyat: string;
  aylikKarsilik: string | null;
  deneme: string | null;
  ham: PurchasesPackage;
}

export const proDeposu = kaliciDepo<{ pro: boolean; bitis: string | null; yenileniyor: boolean }>('am.pro.v1', {
  pro: false,
  bitis: null,
  yenileniyor: false,
});

let kuruldu = false;
export const aboneligeHazir = () => kuruldu;

function uygula(ci: CustomerInfo) {
  const e = ci.entitlements.active[PRO_ENTITLEMENT];
  proDeposu.set({ pro: !!e, bitis: e?.expirationDate ?? null, yenileniyor: !!e?.willRenew });
}

export async function aboneligiBaslat() {
  if (kuruldu || !REVENUECAT_ANDROID_KEY || Platform.OS !== 'android') return;
  try {
    if (__DEV__) Purchases.setLogLevel(LOG_LEVEL.WARN);
    Purchases.configure({ apiKey: REVENUECAT_ANDROID_KEY });
    kuruldu = true;
    Purchases.addCustomerInfoUpdateListener(uygula);
    uygula(await Purchases.getCustomerInfo());
  } catch {
    // Mağaza erişilemedi: son bilinen durum (önbellek) geçerli kalır
  }
}

function sure(p: PurchasesPackage): Paket['tip'] {
  if (p.packageType === 'ANNUAL') return 'yillik';
  if (p.packageType === 'MONTHLY') return 'aylik';
  return 'diger';
}

export async function paketleriGetir(): Promise<Paket[]> {
  if (!kuruldu) return [];
  const o = await Purchases.getOfferings();
  const pk = o.current?.availablePackages ?? [];
  return pk.map((p) => {
    const tip = sure(p);
    const prod = p.product;
    const aylik = tip === 'yillik' && prod.price ? `${(prod.price / 12).toLocaleString('tr-TR', { maximumFractionDigits: 2, minimumFractionDigits: 2 })} ${prod.currencyCode === 'TRY' ? '₺' : prod.currencyCode}` : null;
    const intro = prod.introPrice;
    const deneme = intro && intro.price === 0 ? `${intro.periodNumberOfUnits} ${intro.periodUnit === 'DAY' ? 'gün' : intro.periodUnit === 'WEEK' ? 'hafta' : 'ay'} ücretsiz` : null;
    return { id: p.identifier, tip, fiyat: prod.priceString, aylikKarsilik: aylik, deneme, ham: p };
  });
}

export async function satinAl(p: Paket): Promise<'tamam' | 'iptal'> {
  try {
    const r = await Purchases.purchasePackage(p.ham);
    uygula(r.customerInfo);
    return 'tamam';
  } catch (e) {
    if ((e as { userCancelled?: boolean }).userCancelled) return 'iptal';
    throw e;
  }
}

export async function geriYukle(): Promise<boolean> {
  if (!kuruldu) return false;
  const ci = await Purchases.restorePurchases();
  uygula(ci);
  return !!ci.entitlements.active[PRO_ENTITLEMENT];
}
