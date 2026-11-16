import { router } from 'expo-router';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, ScrollView, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Ikon, type IkonAdi } from '../components/Ikon';
import { Isaret } from '../components/Isaret';
import { ProfilFormu } from '../components/ProfilFormu';
import { Buton, Fosforlu, Yazi } from '../components/ui';
import { bildirimIzniIste } from '../lib/fb';
import { ayarDeposu, profilDeposu } from '../lib/store';
import type { Profil } from '../lib/types';
import { radius, space, usePalette } from '../theme';

function Madde({ ikon, baslik, metin }: { ikon: IkonAdi; baslik: string; metin: string }) {
  const c = usePalette();
  return (
    <View style={{ flexDirection: 'row', gap: 14 }}>
      <View style={{ width: 42, height: 42, borderRadius: 12, backgroundColor: c.form2, alignItems: 'center', justifyContent: 'center' }}>
        <Ikon ad={ikon} renk={c.formInk} />
      </View>
      <View style={{ flex: 1, gap: 2 }}>
        <Yazi agirlik="bold">{baslik}</Yazi>
        <Yazi boyut={14} ton="ink2">{metin}</Yazi>
      </View>
    </View>
  );
}

export default function Karsilama() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const [adim, setAdim] = useState<0 | 1>(0);
  const [p, setP] = useState<Profil>(profilDeposu.get());

  const bitir = async (profilKaydet: boolean) => {
    if (profilKaydet && p.egitim) profilDeposu.set({ ...p, tamamlandi: true });
    await bildirimIzniIste().catch(() => false);
    ayarDeposu.set({ karsilamaGoruldu: true });
    router.replace('/');
  };

  if (adim === 0) {
    return (
      <View style={{ flex: 1, backgroundColor: c.paper, paddingTop: ins.top + space.xxl, paddingBottom: ins.bottom + space.lg, paddingHorizontal: space.xl }}>
        <Isaret />
        <View style={{ marginTop: space.xxl, gap: 10 }}>
          <Yazi boyut={34} agirlik="heavy" stil={{ letterSpacing: -1, lineHeight: 40 }}>Kamu ilanları,</Yazi>
          <Fosforlu boyut={34} agirlik="heavy">tek yerde.</Fosforlu>
          <Yazi boyut={16} ton="ink2" stil={{ marginTop: 6 }}>
            Kariyer Kapısı, ilan.gov.tr ve İŞKUR’daki kamu personel alımları; KPSS şartlarıyla özetlenmiş halde.
          </Yazi>
        </View>
        <View style={{ marginTop: space.xxl, gap: space.lg }}>
          <Madde ikon="zil" baslik="Yeni ilanı ilk sen gör" metin="İl ve kadro tercihine göre bildirim." />
          <Madde ikon="grafik" baslik="KPSS puanına göre süz" metin="Puan türü ve taban puanla filtrele." />
          <Madde ikon="takvim" baslik="Son başvuruyu kaçırma" metin="Kaydet, takvimine ekle, geri sayımı izle." />
        </View>
        <View style={{ flex: 1 }} />
        <View style={{ padding: space.md, borderRadius: radius.md, backgroundColor: c.form2, flexDirection: 'row', gap: 10, marginBottom: space.lg }}>
          <Ikon ad="kalkan" boyut={18} renk={c.formInk} />
          <Yazi boyut={12.5} ton="ink2" stil={{ flex: 1 }}>Bağımsız bir uygulamadır; hiçbir kamu kurumuyla bağlantısı yoktur. Başvurular resmî kanallardan yapılır.</Yazi>
        </View>
        <Buton etiket="Başla" onPress={() => setAdim(1)} />
      </View>
    );
  }

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.paper }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <ScrollView contentContainerStyle={{ paddingTop: ins.top + space.xl, paddingHorizontal: space.lg, paddingBottom: 160 }} keyboardShouldPersistTaps="handled">
        <Yazi boyut={13} agirlik="bold" ton="ink3" stil={{ letterSpacing: 0.9 }}>PROFİLİN</Yazi>
        <Yazi boyut={26} agirlik="heavy" stil={{ letterSpacing: -0.6, marginTop: 4 }}>Sana uygun ilanları bulalım</Yazi>
        <Yazi ton="ink2" stil={{ marginTop: 6, marginBottom: space.xl }}>Bilgiler yalnızca cihazında kalır. Sonra Hesap sekmesinden değiştirebilirsin.</Yazi>
        <ProfilFormu deger={p} degistir={setP} />
      </ScrollView>
      <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: space.lg, paddingBottom: ins.bottom + space.lg, backgroundColor: c.paper, borderTopWidth: 1, borderTopColor: c.line, gap: 6 }}>
        <Buton etiket="Devam" pasif={!p.egitim} onPress={() => bitir(true)} />
        <Buton etiket="Şimdilik geç" tur="sade" onPress={() => bitir(false)} />
      </View>
    </KeyboardAvoidingView>
  );
}
