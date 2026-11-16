import {
  Archivo_400Regular,
  Archivo_500Medium,
  Archivo_600SemiBold,
  Archivo_700Bold,
  Archivo_800ExtraBold,
  useFonts,
} from '@expo-google-fonts/archivo';
import { router, Stack } from 'expo-router';
import * as SplashScreen from 'expo-splash-screen';
import { StatusBar } from 'expo-status-bar';
import { useEffect, useState } from 'react';
import { View } from 'react-native';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { BildirimKatmani, bildir } from '../components/Bildirim';
import { reklamlariBaslat } from '../lib/ads';
import { verileriBaslat } from '../lib/api';
import { bildirimDinle, konulariEsitle } from '../lib/fb';
import { oturumuBaslat } from '../lib/oturum';
import { aboneligiBaslat, proDeposu } from '../lib/pro';
import { ayarDeposu, tumDepolariYukle, useDepo } from '../lib/store';
import { usePalette } from '../theme';

SplashScreen.preventAutoHideAsync().catch(() => {});

export const unstable_settings = { anchor: '(tabs)' };

function BildirimEsitleyici() {
  const pro = useDepo(proDeposu, (s) => s.pro);
  const acik = useDepo(ayarDeposu, (s) => s.bildirim);
  const iller = useDepo(ayarDeposu, (s) => s.bildirimIlleri.join(','));
  useEffect(() => {
    const onceki = ayarDeposu.get().fcmKonular;
    konulariEsitle({ acik, pro, iller: iller ? iller.split(',') : [], onceki })
      .then((fcmKonular) => ayarDeposu.set({ fcmKonular }))
      .catch(() => {});
  }, [pro, acik, iller]);

  useEffect(
    () =>
      bildirimDinle(
        (b) => bildir(b.baslik, b.govde, { ikon: 'zil', onPress: b.ilanId ? () => router.push(`/ilan/${b.ilanId}`) : undefined }),
        (ilanId) => {
          if (ilanId) setTimeout(() => router.push(`/ilan/${ilanId}`), 300);
        },
      ),
    [],
  );
  return null;
}

export default function KokDuzen() {
  const c = usePalette();
  const [fontlar] = useFonts({ Archivo_400Regular, Archivo_500Medium, Archivo_600SemiBold, Archivo_700Bold, Archivo_800ExtraBold });
  const [depolar, setDepolar] = useState(false);

  useEffect(() => {
    // Pro durumu depolarla birlikte yüklenir; bildirim konuları ilk açılışta gereksiz yere değişmesin
    Promise.all([tumDepolariYukle(), proDeposu.yukle()]).then(() => {
      ayarDeposu.set((s) => ({ acilisSayisi: s.acilisSayisi + 1 }));
      setDepolar(true);
      aboneligiBaslat();
    });
    verileriBaslat();
    const kapat = oturumuBaslat();
    return kapat;
  }, []);

  const hazir = fontlar && depolar;
  useEffect(() => {
    if (hazir) {
      SplashScreen.hideAsync().catch(() => {});
      // Reklam onay formu açılışta ilk ekranın üstüne binmesin
      setTimeout(() => reklamlariBaslat(), 1500);
    }
  }, [hazir]);

  if (!hazir) return <View style={{ flex: 1, backgroundColor: c.paper }} />;

  return (
    <SafeAreaProvider>
      <StatusBar style={c.dark ? 'light' : 'dark'} />
      <Stack screenOptions={{ headerShown: false, contentStyle: { backgroundColor: c.paper }, animation: 'slide_from_right' }}>
        <Stack.Screen name="(tabs)" />
        <Stack.Screen name="karsilama" options={{ animation: 'fade', gestureEnabled: false }} />
        <Stack.Screen name="filtre" options={{ presentation: 'modal', animation: 'slide_from_bottom' }} />
        <Stack.Screen name="pro" options={{ presentation: 'modal', animation: 'slide_from_bottom' }} />
        <Stack.Screen name="giris" options={{ presentation: 'modal', animation: 'slide_from_bottom' }} />
        <Stack.Screen name="forum/yeni" options={{ presentation: 'modal', animation: 'slide_from_bottom' }} />
      </Stack>
      <BildirimEsitleyici />
      <BildirimKatmani />
    </SafeAreaProvider>
  );
}
