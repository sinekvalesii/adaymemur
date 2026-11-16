import { router } from 'expo-router';
import { Pressable, View } from 'react-native';

import { useVeri } from '../lib/api';
import { kalanGun, tarihUzun } from '../lib/format';
import { radius, space, usePalette } from '../theme';
import { Ikon } from './Ikon';
import { Yazi } from './ui';

/** İlanlar ekranının üstünde: en yakın KPSS / ÖSYM tarihi ve öne çıkan duyuru. */
export function GundemKarti() {
  const c = usePalette();
  const takvim = useVeri((v) => v.takvim);
  const duyuru = useVeri((v) => v.duyurular.find((d) => d.vurgulu));
  const siradaki = takvim?.ogeler
    .filter((o) => (kalanGun(o.bitis ?? o.tarih) ?? -1) >= 0)
    .sort((a, b) => a.tarih.localeCompare(b.tarih))[0];
  if (!siradaki && !duyuru) return null;
  const k = siradaki ? kalanGun(siradaki.tarih) : null;
  return (
    <Pressable
      onPress={() => router.push('/gundem')}
      style={({ pressed }) => ({
        marginHorizontal: space.lg,
        marginBottom: space.md,
        borderRadius: radius.lg,
        backgroundColor: c.dark ? c.sheet : c.ink,
        borderWidth: c.dark ? 1 : 0,
        borderColor: c.line,
        padding: space.lg,
        flexDirection: 'row',
        alignItems: 'center',
        gap: 14,
        opacity: pressed ? 0.92 : 1,
      })}
    >
      {siradaki ? (
        <View style={{ width: 58, alignItems: 'center', paddingVertical: 6, borderRadius: 12, backgroundColor: c.marker }}>
          <Yazi boyut={22} agirlik="heavy" ton="onMarker" stil={{ lineHeight: 26 }}>{k !== null && k > 0 ? k : '!'}</Yazi>
          <Yazi boyut={11} agirlik="semibold" ton="onMarker">{k !== null && k > 0 ? 'gün' : 'bugün'}</Yazi>
        </View>
      ) : null}
      <View style={{ flex: 1, gap: 2 }}>
        <Yazi boyut={11.5} agirlik="bold" stil={{ color: c.marker, letterSpacing: 0.8 }}>KPSS GÜNDEMİ</Yazi>
        <Yazi boyut={15} agirlik="bold" stil={{ color: c.dark ? c.ink : c.paper }} satir={2}>
          {siradaki ? siradaki.baslik : duyuru!.baslik}
        </Yazi>
        <Yazi boyut={12.5} stil={{ color: c.dark ? c.ink2 : c.paper, opacity: c.dark ? 1 : 0.72 }} satir={1}>
          {siradaki ? `${tarihUzun(siradaki.tarih)}${siradaki.bitis ? ` – ${tarihUzun(siradaki.bitis)}` : ''}` : 'Duyuruyu oku'}
        </Yazi>
      </View>
      <Ikon ad="sag" boyut={18} renk={c.dark ? c.ink2 : c.paper} />
    </Pressable>
  );
}
