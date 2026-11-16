import { router } from 'expo-router';
import { useState } from 'react';
import { ScrollView, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AltBaslik } from '../components/Ekran';
import { Buton, Cip, Girdi, Yazi } from '../components/ui';
import { IL_LISTESI } from '../lib/sabitler';
import { ayarDeposu, useDepo } from '../lib/store';
import { space, usePalette } from '../theme';

const AZAMI = 3;

export default function BildirimIlleri() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const mevcut = useDepo(ayarDeposu, (s) => s.bildirimIlleri);
  const [secim, setSecim] = useState<string[]>(mevcut);
  const [ara, setAra] = useState('');
  const liste = IL_LISTESI.filter((i) => !ara || i.ad.toLocaleLowerCase('tr').includes(ara.toLocaleLowerCase('tr')));

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <AltBaslik baslik="Bildirim illeri" />
      <ScrollView contentContainerStyle={{ padding: space.lg, paddingBottom: 140, gap: space.md }} keyboardShouldPersistTaps="handled">
        <Yazi ton="ink2">
          En fazla {AZAMI} il seçebilirsin. Türkiye geneli kadro açan ilanlar her durumda bildirilir. Hiç seçmezsen tüm iller için bildirim alırsın.
        </Yazi>
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          <Cip etiket="Tüm Türkiye" secili={!secim.length} onPress={() => setSecim([])} />
          {secim.map((k) => (
            <Cip key={k} etiket={IL_LISTESI.find((i) => i.kod === k)!.ad} secili ikon="carpi" onPress={() => setSecim(secim.filter((x) => x !== k))} />
          ))}
        </View>
        <Girdi ikon="ara" placeholder="İl ara" value={ara} onChangeText={setAra} />
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          {liste.map((i) => (
            <Cip
              key={i.kod}
              kucuk
              etiket={i.ad}
              secili={secim.includes(i.kod)}
              onPress={() =>
                setSecim((s) => (s.includes(i.kod) ? s.filter((x) => x !== i.kod) : s.length >= AZAMI ? s : [...s, i.kod]))
              }
            />
          ))}
        </View>
      </ScrollView>
      <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: space.lg, paddingBottom: ins.bottom + space.lg, backgroundColor: c.paper, borderTopWidth: 1, borderTopColor: c.line }}>
        <Buton
          etiket="Kaydet"
          onPress={() => {
            ayarDeposu.set({ bildirimIlleri: secim });
            router.back();
          }}
        />
      </View>
    </View>
  );
}
