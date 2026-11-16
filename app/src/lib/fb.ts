/**
 * Firebase (yerel modüller): bildirim, hesap, forum.
 * google-services.json olmadan derlenen sürümde Firebase yoktur; tüm işlevler
 * sessizce devre dışı kalır ve arayüz ilgili bölümü "yakında" olarak gösterir.
 */
import { getApp } from '@react-native-firebase/app';
import {
  createUserWithEmailAndPassword,
  deleteUser,
  EmailAuthProvider,
  getAuth,
  onAuthStateChanged,
  reauthenticateWithCredential,
  sendPasswordResetEmail,
  signInWithEmailAndPassword,
  signOut,
  updateProfile,
  type User,
} from '@react-native-firebase/auth';
import {
  addDoc,
  collection,
  collectionGroup,
  deleteDoc,
  doc,
  getDoc,
  getDocs,
  getFirestore,
  increment,
  limit,
  onSnapshot,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
  writeBatch,
} from '@react-native-firebase/firestore';
import {
  getInitialNotification,
  getMessaging,
  onMessage,
  onNotificationOpenedApp,
  subscribeToTopic,
  unsubscribeFromTopic,
} from '@react-native-firebase/messaging';
import { PermissionsAndroid, Platform } from 'react-native';

export type Kullanici = { uid: string; ad: string; eposta: string | null };

let hazir: boolean | null = null;
export function firebaseHazir(): boolean {
  if (hazir === null) {
    try {
      getApp();
      hazir = true;
    } catch {
      hazir = false;
    }
  }
  return hazir;
}

// --- Bildirim -------------------------------------------------------------------
export async function bildirimIzniIste(): Promise<boolean> {
  if (Platform.OS !== 'android') return false;
  if (Number(Platform.Version) < 33) return true;
  const r = await PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS);
  return r === PermissionsAndroid.RESULTS.GRANTED;
}

const TUM_KONULAR_ON = 'il_';

/** FCM konularını kullanıcının durumuna göre eşitler (pro / free / il_XX). */
export async function konulariEsitle(opt: { acik: boolean; pro: boolean; iller: string[]; onceki: string[] }): Promise<string[]> {
  if (!firebaseHazir()) return opt.onceki;
  const m = getMessaging();
  const hedef = new Set<string>();
  if (opt.acik) {
    if (opt.pro) {
      hedef.add('pro');
      if (opt.iller.length) opt.iller.forEach((i) => hedef.add(`${TUM_KONULAR_ON}${i}`));
      else hedef.add('il_all');
    } else {
      hedef.add('free');
    }
  }
  const eski = new Set(opt.onceki);
  await Promise.all([
    ...[...hedef].filter((k) => !eski.has(k)).map((k) => subscribeToTopic(m, k).catch(() => {})),
    ...[...eski].filter((k) => !hedef.has(k)).map((k) => unsubscribeFromTopic(m, k).catch(() => {})),
  ]);
  return [...hedef];
}

export type GelenBildirim = { baslik: string; govde: string; ilanId?: string };

export function bildirimDinle(onOn: (b: GelenBildirim) => void, onAcilis: (ilanId?: string) => void): () => void {
  if (!firebaseHazir()) return () => {};
  const m = getMessaging();
  const a = onMessage(m, async (msg) => {
    onOn({ baslik: msg.notification?.title ?? '', govde: msg.notification?.body ?? '', ilanId: msg.data?.ilanId as string | undefined });
  });
  const b = onNotificationOpenedApp(m, (msg) => onAcilis(msg?.data?.ilanId as string | undefined));
  getInitialNotification(m).then((msg) => {
    if (msg) onAcilis(msg.data?.ilanId as string | undefined);
  });
  return () => {
    a();
    b();
  };
}

// --- Hesap ----------------------------------------------------------------------
const donustur = (u: User | null): Kullanici | null =>
  u ? { uid: u.uid, ad: u.displayName || 'Aday', eposta: u.email } : null;

export function oturumDinle(cb: (k: Kullanici | null) => void): () => void {
  if (!firebaseHazir()) {
    cb(null);
    return () => {};
  }
  return onAuthStateChanged(getAuth(), (u) => cb(donustur(u)));
}

const HATA: Record<string, string> = {
  'auth/invalid-email': 'E-posta adresi geçersiz.',
  'auth/user-not-found': 'E-posta veya şifre hatalı.',
  'auth/wrong-password': 'E-posta veya şifre hatalı.',
  'auth/invalid-credential': 'E-posta veya şifre hatalı.',
  'auth/email-already-in-use': 'Bu e-posta ile zaten bir hesap var.',
  'auth/weak-password': 'Şifre en az 6 karakter olmalı.',
  'auth/too-many-requests': 'Çok fazla deneme yapıldı. Biraz sonra tekrar dene.',
  'auth/network-request-failed': 'Bağlantı hatası. İnternetini kontrol et.',
  'auth/requires-recent-login': 'Güvenlik için şifreni tekrar girmen gerekiyor.',
};

export function hataMetni(e: unknown): string {
  const kod = (e as { code?: string })?.code ?? '';
  return HATA[kod] ?? 'İşlem tamamlanamadı. Tekrar dene.';
}

export async function girisYap(eposta: string, sifre: string) {
  await signInWithEmailAndPassword(getAuth(), eposta.trim(), sifre);
}

export async function kayitOl(ad: string, eposta: string, sifre: string) {
  const c = await createUserWithEmailAndPassword(getAuth(), eposta.trim(), sifre);
  await updateProfile(c.user, { displayName: ad.trim() });
  await setDoc(doc(getFirestore(), 'kullanicilar', c.user.uid), { ad: ad.trim(), olusturma: serverTimestamp() });
}

export async function sifreSifirla(eposta: string) {
  await sendPasswordResetEmail(getAuth(), eposta.trim());
}

export async function cikisYap() {
  await signOut(getAuth());
}

/** Hesabı ve kullanıcının forumdaki tüm içeriğini kalıcı olarak siler. */
export async function hesabiSil(sifre: string) {
  const auth = getAuth();
  const u = auth.currentUser;
  if (!u || !u.email) throw new Error('oturum yok');
  await reauthenticateWithCredential(u, EmailAuthProvider.credential(u.email, sifre));
  const db = getFirestore();
  const [konular, yorumlar, raporlar] = await Promise.all([
    getDocs(query(collection(db, 'konular'), where('yazarId', '==', u.uid))),
    getDocs(query(collectionGroup(db, 'yorumlar'), where('yazarId', '==', u.uid))),
    getDocs(query(collection(db, 'raporlar'), where('raporlayan', '==', u.uid))),
  ]);
  const refs = [
    ...yorumlar.docs.map((d) => d.ref),
    ...konular.docs.map((d) => d.ref),
    ...raporlar.docs.map((d) => d.ref),
    doc(db, 'kullanicilar', u.uid),
  ];
  for (let i = 0; i < refs.length; i += 400) {
    const batch = writeBatch(db);
    refs.slice(i, i + 400).forEach((r) => batch.delete(r));
    await batch.commit();
  }
  await deleteUser(u);
}

// --- Forum ----------------------------------------------------------------------
export interface Konu {
  id: string;
  baslik: string;
  metin: string;
  kategori: string;
  yazarId: string;
  yazarAd: string;
  olusturma: number;
  sonHareket: number;
  yorumSayisi: number;
  raporSayisi: number;
}

export interface Yorum {
  id: string;
  metin: string;
  yazarId: string;
  yazarAd: string;
  olusturma: number;
  raporSayisi: number;
}

const ms = (v: unknown): number => {
  const t = v as { toMillis?: () => number } | null;
  return t?.toMillis ? t.toMillis() : Date.now();
};

export function konulariDinle(kategori: string | null, cb: (k: Konu[]) => void, hata: (e: unknown) => void) {
  if (!firebaseHazir()) return () => {};
  const db = getFirestore();
  const q = kategori
    ? query(collection(db, 'konular'), where('kategori', '==', kategori), orderBy('sonHareket', 'desc'), limit(100))
    : query(collection(db, 'konular'), orderBy('sonHareket', 'desc'), limit(100));
  return onSnapshot(
    q,
    (snap) => {
      cb(
        snap.docs
          .filter((d) => !d.data().gizli)
          .map((d) => {
            const x = d.data();
            return {
              id: d.id,
              baslik: x.baslik,
              metin: x.metin,
              kategori: x.kategori,
              yazarId: x.yazarId,
              yazarAd: x.yazarAd,
              olusturma: ms(x.olusturma),
              sonHareket: ms(x.sonHareket),
              yorumSayisi: x.yorumSayisi ?? 0,
              raporSayisi: x.raporSayisi ?? 0,
            } as Konu;
          }),
      );
    },
    hata,
  );
}

export function konuDinle(id: string, cb: (k: Konu | null) => void) {
  if (!firebaseHazir()) return () => {};
  return onSnapshot(doc(getFirestore(), 'konular', id), (d) => {
    const x = d.data();
    if (!x || x.gizli) return cb(null);
    cb({
      id: d.id,
      baslik: x.baslik,
      metin: x.metin,
      kategori: x.kategori,
      yazarId: x.yazarId,
      yazarAd: x.yazarAd,
      olusturma: ms(x.olusturma),
      sonHareket: ms(x.sonHareket),
      yorumSayisi: x.yorumSayisi ?? 0,
      raporSayisi: x.raporSayisi ?? 0,
    });
  });
}

export function yorumlariDinle(konuId: string, cb: (y: Yorum[]) => void) {
  if (!firebaseHazir()) return () => {};
  const q = query(collection(getFirestore(), 'konular', konuId, 'yorumlar'), orderBy('olusturma', 'asc'), limit(300));
  return onSnapshot(q, (snap) =>
    cb(
      snap.docs
        .filter((d) => !d.data().gizli)
        .map((d) => {
          const x = d.data();
          return { id: d.id, metin: x.metin, yazarId: x.yazarId, yazarAd: x.yazarAd, olusturma: ms(x.olusturma), raporSayisi: x.raporSayisi ?? 0 };
        }),
    ),
  );
}

function aktifKullanici() {
  const u = getAuth().currentUser;
  if (!u) throw Object.assign(new Error('giriş gerekli'), { code: 'auth/requires-login' });
  return u;
}

export async function konuAc(baslik: string, metin: string, kategori: string): Promise<string> {
  const u = aktifKullanici();
  const r = await addDoc(collection(getFirestore(), 'konular'), {
    baslik: baslik.trim(),
    metin: metin.trim(),
    kategori,
    yazarId: u.uid,
    yazarAd: u.displayName || 'Aday',
    olusturma: serverTimestamp(),
    sonHareket: serverTimestamp(),
    yorumSayisi: 0,
    raporSayisi: 0,
    gizli: false,
  });
  return r.id;
}

export async function yorumYaz(konuId: string, metin: string) {
  const u = aktifKullanici();
  const db = getFirestore();
  await addDoc(collection(db, 'konular', konuId, 'yorumlar'), {
    metin: metin.trim(),
    yazarId: u.uid,
    yazarAd: u.displayName || 'Aday',
    olusturma: serverTimestamp(),
    raporSayisi: 0,
    gizli: false,
  });
  await updateDoc(doc(db, 'konular', konuId), { yorumSayisi: increment(1), sonHareket: serverTimestamp() });
}

export async function icerikSil(konuId: string, yorumId?: string) {
  const db = getFirestore();
  if (yorumId) {
    await deleteDoc(doc(db, 'konular', konuId, 'yorumlar', yorumId));
    await updateDoc(doc(db, 'konular', konuId), { yorumSayisi: increment(-1) });
  } else {
    await deleteDoc(doc(db, 'konular', konuId));
  }
}

export async function raporla(konuId: string, sebep: string, yorumId?: string) {
  const u = aktifKullanici();
  const db = getFirestore();
  const hedef = yorumId ? `konular/${konuId}/yorumlar/${yorumId}` : `konular/${konuId}`;
  const raporRef = doc(db, 'raporlar', `${u.uid}_${hedef.replace(/\//g, '_')}`);
  if ((await getDoc(raporRef)).exists()) return; // aynı içerik bir kez raporlanır
  const ref = yorumId ? doc(db, 'konular', konuId, 'yorumlar', yorumId) : doc(db, 'konular', konuId);
  // Rapor ve sayaç tek işlemde: kurallar sayacın yalnızca yeni raporla artmasına izin verir
  const batch = writeBatch(db);
  batch.set(raporRef, { hedef, sebep, raporlayan: u.uid, olusturma: serverTimestamp() });
  batch.update(ref, { raporSayisi: increment(1) });
  await batch.commit();
}
