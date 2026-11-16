import { router } from 'expo-router';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { bildir } from '../components/Bildirim';
import { AltBaslik } from '../components/Ekran';
import { ProfilFormu } from '../components/ProfilFormu';
import { Buton, Yazi } from '../components/ui';
import { profilDeposu, useDepo } from '../lib/store';
import type { Profil } from '../lib/types';
import { space, usePalette } from '../theme';

export default function ProfilEkrani() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const mevcut = useDepo(profilDeposu, (s) => s);
  const [p, setP] = useState<Profil>(mevcut);

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.paper }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <AltBaslik baslik="Öğrenim ve KPSS bilgilerim" />
      <ScrollView contentContainerStyle={{ padding: space.lg, paddingBottom: 140 }} keyboardShouldPersistTaps="handled">
        <Yazi ton="ink2" stil={{ marginBottom: space.lg }}>
          Bu bilgiler yalnızca cihazında saklanır ve ilanlarla eşleştirme için kullanılır.
        </Yazi>
        <ProfilFormu deger={p} degistir={setP} />
      </ScrollView>
      <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: space.lg, paddingBottom: ins.bottom + space.lg, backgroundColor: c.paper, borderTopWidth: 1, borderTopColor: c.line }}>
        <Buton
          etiket="Kaydet"
          pasif={!p.egitim}
          onPress={() => {
            profilDeposu.set({ ...p, tamamlandi: true });
            bildir('Profilin kaydedildi', 'Bana Uygun listesi güncellendi.', { ikon: 'tik' });
            router.back();
          }}
        />
      </View>
    </KeyboardAvoidingView>
  );
}
