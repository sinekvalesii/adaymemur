import { router } from 'expo-router';
import { View } from 'react-native';

import { AltBaslik } from '../components/Ekran';
import { Buton, Yazi } from '../components/ui';
import { space, usePalette } from '../theme';

export default function Bulunamadi() {
  const c = usePalette();
  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <AltBaslik />
      <View style={{ padding: space.xl, gap: space.md }}>
        <Yazi boyut={20} agirlik="bold">Sayfa bulunamadı</Yazi>
        <Buton etiket="Ana sayfaya dön" tur="ikincil" onPress={() => router.replace('/')} />
      </View>
    </View>
  );
}
