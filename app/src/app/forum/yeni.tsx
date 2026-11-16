import { Redirect, router } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, Pressable, ScrollView, View } from 'react-native';

import { bildir } from '../../components/Bildirim';
import { AltBaslik } from '../../components/Ekran';
import { Ikon } from '../../components/Ikon';
import { Buton, Cip, Girdi, Yazi } from '../../components/ui';
import { LINKS } from '../../lib/config';
import { konuAc } from '../../lib/fb';
import { useKullanici } from '../../lib/oturum';
import { FORUM_KATEGORILERI, uygunsuzMu } from '../../lib/sabitler';
import { ayarDeposu, useDepo } from '../../lib/store';
import { space, usePalette } from '../../theme';

export default function YeniKonu() {
  const c = usePalette();
  const { kullanici, bilindi } = useKullanici();
  const onay = useDepo(ayarDeposu, (s) => s.forumKosullariOnay);
  const [baslik, setBaslik] = useState('');
  const [metin, setMetin] = useState('');
  const [kategori, setKategori] = useState('KPSS');
  const [kurallar, setKurallar] = useState(onay);
  const [bekle, setBekle] = useState(false);

  if (bilindi && !kullanici) return <Redirect href="/giris" />;

  const gecerli = baslik.trim().length >= 8 && metin.trim().length >= 10 && kurallar;

  const gonder = async () => {
    if (uygunsuzMu(`${baslik} ${metin}`)) {
      bildir('Mesajın uygunsuz ifade içeriyor', 'Lütfen forum kurallarına uygun yaz.', { ikon: 'uyari' });
      return;
    }
    setBekle(true);
    try {
      ayarDeposu.set({ forumKosullariOnay: true });
      const id = await konuAc(baslik, metin, kategori);
      router.replace(`/forum/${id}`);
    } catch {
      bildir('Konu açılamadı', 'Bağlantını kontrol edip tekrar dene.');
      setBekle(false);
    }
  };

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.paper }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <AltBaslik baslik="Yeni soru" kapat />
      <ScrollView contentContainerStyle={{ padding: space.lg, gap: space.lg }} keyboardShouldPersistTaps="handled">
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          {FORUM_KATEGORILERI.map((k) => (
            <Cip key={k} etiket={k} secili={kategori === k} onPress={() => setKategori(k)} kucuk />
          ))}
        </View>
        <Girdi etiket="Başlık" placeholder="Sorunu kısaca özetle" value={baslik} onChangeText={setBaslik} maxLength={140} />
        <Girdi etiket="Ayrıntı" placeholder="Durumunu ve sorunu anlat" value={metin} onChangeText={setMetin} multiline maxLength={4000} />
        <Pressable onPress={() => setKurallar((x) => !x)} style={{ flexDirection: 'row', gap: 12, alignItems: 'flex-start' }}>
          <View style={{ width: 24, height: 24, borderRadius: 6, borderWidth: 1.5, borderColor: kurallar ? c.ink : c.ink3, backgroundColor: kurallar ? c.ink : 'transparent', alignItems: 'center', justifyContent: 'center', marginTop: 1 }}>
            {kurallar ? <Ikon ad="tik" boyut={16} renk={c.paper} kalinlik={2.4} /> : null}
          </View>
          <Yazi boyut={14} ton="ink2" stil={{ flex: 1 }}>
            Hakaret, kişisel bilgi ve reklam içermeyen paylaşım yapacağımı; kurallara aykırı içeriklerin kaldırılabileceğini kabul ediyorum.
          </Yazi>
        </Pressable>
        <Pressable onPress={() => WebBrowser.openBrowserAsync(LINKS.forumKurallari).catch(() => {})}>
          <Yazi boyut={13.5} agirlik="semibold" ton="formInk">Forum kurallarını oku</Yazi>
        </Pressable>
        <Buton etiket="Yayınla" onPress={gonder} pasif={!gecerli} yukleniyor={bekle} />
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
