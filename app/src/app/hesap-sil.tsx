import { router } from 'expo-router';
import { useState } from 'react';
import { ScrollView, View } from 'react-native';

import { bildir } from '../components/Bildirim';
import { AltBaslik } from '../components/Ekran';
import { Buton, Girdi, Kart, Yazi } from '../components/ui';
import { hataMetni, hesabiSil } from '../lib/fb';
import { useKullanici } from '../lib/oturum';
import { space, usePalette } from '../theme';

export default function HesapSil() {
  const c = usePalette();
  const { kullanici } = useKullanici();
  const [sifre, setSifre] = useState('');
  const [hata, setHata] = useState<string | null>(null);
  const [bekle, setBekle] = useState(false);

  const sil = async () => {
    setBekle(true);
    setHata(null);
    try {
      await hesabiSil(sifre);
      bildir('Hesabın silindi', 'Forumdaki tüm gönderilerin kaldırıldı.');
      router.replace('/hesap');
    } catch (e) {
      setHata(hataMetni(e));
      setBekle(false);
    }
  };

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <AltBaslik baslik="Hesabı sil" />
      <ScrollView contentContainerStyle={{ padding: space.lg, gap: space.lg }} keyboardShouldPersistTaps="handled">
        <Kart stil={{ gap: 8, backgroundColor: c.badBg, borderColor: c.badBg }}>
          <Yazi agirlik="bold" ton="bad">Bu işlem geri alınamaz</Yazi>
          <Yazi boyut={14} ton="ink2">
            {kullanici?.eposta ?? 'Hesabın'} kalıcı olarak silinir; forumda açtığın konular ve yazdığın tüm yanıtlar kaldırılır. Pro aboneliğin Google Play hesabına bağlıdır ve bu işlemle iptal olmaz; iptal için Play Store → Abonelikler’i kullan.
          </Yazi>
        </Kart>
        <Girdi etiket="Onay için şifren" secureTextEntry value={sifre} onChangeText={setSifre} hata={hata} autoComplete="password" />
        <Buton etiket="Hesabımı kalıcı olarak sil" tur="tehlike" onPress={sil} yukleniyor={bekle} pasif={sifre.length < 6} />
      </ScrollView>
    </View>
  );
}
