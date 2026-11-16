import { Redirect, Tabs } from 'expo-router';
import { View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Ikon, type IkonAdi } from '../../components/Ikon';
import { ayarDeposu, useDepo } from '../../lib/store';
import { font, usePalette } from '../../theme';

function SekmeIkonu({ ad, odak }: { ad: IkonAdi; odak: boolean }) {
  const c = usePalette();
  return (
    <View
      style={{
        width: 58,
        height: 30,
        borderRadius: 15,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: odak ? c.marker : 'transparent',
      }}
    >
      <Ikon ad={ad} boyut={22} renk={odak ? c.onMarker : c.ink3} kalinlik={odak ? 2 : 1.75} />
    </View>
  );
}

export default function SekmeDuzeni() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const goruldu = useDepo(ayarDeposu, (s) => s.karsilamaGoruldu);
  if (!goruldu) return <Redirect href="/karsilama" />;

  return (
    <Tabs
      screenOptions={{
        headerShown: false,
        sceneStyle: { backgroundColor: c.paper },
        tabBarActiveTintColor: c.ink,
        tabBarInactiveTintColor: c.ink3,
        tabBarStyle: {
          backgroundColor: c.sheet,
          borderTopColor: c.line,
          height: 70 + ins.bottom,
          paddingTop: 7,
          paddingBottom: ins.bottom + 8,
        },
        tabBarIconStyle: { height: 30 },
        tabBarLabelStyle: { fontFamily: font.semibold, fontSize: 11.5, marginTop: 3, lineHeight: 14 },
      }}
    >
      <Tabs.Screen name="index" options={{ title: 'İlanlar', tabBarIcon: ({ focused }) => <SekmeIkonu ad="liste" odak={focused} /> }} />
      <Tabs.Screen name="uygun" options={{ title: 'Bana Uygun', tabBarIcon: ({ focused }) => <SekmeIkonu ad="hedef" odak={focused} /> }} />
      <Tabs.Screen name="kaydedilenler" options={{ title: 'Kaydedilen', tabBarIcon: ({ focused }) => <SekmeIkonu ad="yerimi" odak={focused} /> }} />
      <Tabs.Screen name="forum" options={{ title: 'Forum', tabBarIcon: ({ focused }) => <SekmeIkonu ad="sohbet" odak={focused} /> }} />
      <Tabs.Screen name="hesap" options={{ title: 'Hesap', tabBarIcon: ({ focused }) => <SekmeIkonu ad="kisi" odak={focused} /> }} />
    </Tabs>
  );
}
