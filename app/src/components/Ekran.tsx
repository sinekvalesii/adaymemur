import { router } from 'expo-router';
import { type ReactNode } from 'react';
import { View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { space, usePalette } from '../theme';
import { IkonButon, Yazi } from './ui';

/** Alt sayfalar için geri düğmeli başlık. */
export function AltBaslik({ baslik, sag, kapat }: { baslik?: string; sag?: ReactNode; kapat?: boolean }) {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  return (
    <View
      style={{
        paddingTop: ins.top + 4,
        paddingHorizontal: space.sm,
        paddingBottom: 4,
        flexDirection: 'row',
        alignItems: 'center',
        backgroundColor: c.paper,
        gap: 4,
      }}
    >
      <IkonButon ad={kapat ? 'carpi' : 'sol'} etiket={kapat ? 'Kapat' : 'Geri'} onPress={() => (router.canGoBack() ? router.back() : router.replace('/'))} />
      <View style={{ flex: 1 }}>
        {baslik ? <Yazi boyut={17} agirlik="bold" satir={1}>{baslik}</Yazi> : null}
      </View>
      {sag}
    </View>
  );
}

/** Sekme ekranları için büyük başlık. */
export function SekmeBasligi({ baslik, alt, sag }: { baslik: string; alt?: string; sag?: ReactNode }) {
  const ins = useSafeAreaInsets();
  return (
    <View style={{ paddingTop: ins.top + 10, paddingHorizontal: space.lg, paddingBottom: space.sm, flexDirection: 'row', alignItems: 'flex-end', gap: 8 }}>
      <View style={{ flex: 1 }}>
        <Yazi boyut={30} agirlik="heavy" stil={{ letterSpacing: -0.8, lineHeight: 36 }}>{baslik}</Yazi>
        {alt ? <Yazi boyut={13.5} ton="ink3" stil={{ marginTop: 2 }}>{alt}</Yazi> : null}
      </View>
      {sag}
    </View>
  );
}
