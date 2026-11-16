import { View } from 'react-native';

import { usePalette } from '../theme';

/** Logo işareti: optik form satırları, ilk balon fosforlu sarı. */
export function Isaret({ boyut = 64 }: { boyut?: number }) {
  const c = usePalette();
  const s = boyut / 64;
  return (
    <View style={{ width: boyut, height: boyut, borderRadius: 16 * s, backgroundColor: c.dark ? c.sheet : '#1B1E23', padding: 13 * s, justifyContent: 'space-between' }}>
      {[0, 1, 2].map((i) => (
        <View key={i} style={{ flexDirection: 'row', alignItems: 'center', gap: 6 * s }}>
          <View style={{ width: 9 * s, height: 9 * s, borderRadius: 5 * s, backgroundColor: i === 0 ? '#FFE45A' : 'transparent', borderWidth: i === 0 ? 0 : 1.6 * s, borderColor: '#FFFFFF' }} />
          <View style={{ height: 3 * s, borderRadius: 2, backgroundColor: '#FFFFFF', opacity: i === 0 ? 1 : 0.55, width: (i === 2 ? 18 : 26) * s }} />
        </View>
      ))}
    </View>
  );
}

