import { router } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';
import { useState } from 'react';
import { KeyboardAvoidingView, Platform, Pressable, ScrollView, View } from 'react-native';

import { bildir } from '../components/Bildirim';
import { AltBaslik } from '../components/Ekran';
import { Buton, Cip, Fosforlu, Girdi, Yazi } from '../components/ui';
import { LINKS } from '../lib/config';
import { firebaseHazir, girisYap, hataMetni, kayitOl, sifreSifirla } from '../lib/fb';
import { space, usePalette } from '../theme';

export default function Giris() {
  const c = usePalette();
  const [mod, setMod] = useState<'giris' | 'kayit'>('giris');
  const [ad, setAd] = useState('');
  const [eposta, setEposta] = useState('');
  const [sifre, setSifre] = useState('');
  const [hata, setHata] = useState<string | null>(null);
  const [bekle, setBekle] = useState(false);

  const gonder = async () => {
    setHata(null);
    if (mod === 'kayit' && ad.trim().length < 2) return setHata('Forumda görünecek adını yaz.');
    setBekle(true);
    try {
      if (mod === 'giris') await girisYap(eposta, sifre);
      else await kayitOl(ad, eposta, sifre);
      bildir(mod === 'giris' ? 'Giriş yapıldı' : 'Hesabın oluşturuldu', undefined, { ikon: 'tik' });
      router.back();
    } catch (e) {
      setHata(hataMetni(e));
    } finally {
      setBekle(false);
    }
  };

  const sifirla = async () => {
    if (!eposta.includes('@')) return setHata('Önce e-posta adresini yaz.');
    try {
      await sifreSifirla(eposta);
      bildir('Şifre sıfırlama bağlantısı gönderildi', 'E-posta kutunu kontrol et.');
    } catch (e) {
      setHata(hataMetni(e));
    }
  };

  if (!firebaseHazir()) {
    return (
      <View style={{ flex: 1, backgroundColor: c.paper }}>
        <AltBaslik kapat />
        <Yazi ton="ink2" stil={{ padding: space.xl }}>Hesap özellikleri yakında açılacak.</Yazi>
      </View>
    );
  }

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.paper }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <AltBaslik kapat />
      <ScrollView contentContainerStyle={{ padding: space.lg, gap: space.lg }} keyboardShouldPersistTaps="handled">
        <Fosforlu boyut={26} agirlik="heavy">{mod === 'giris' ? 'Tekrar hoş geldin' : 'Hesap oluştur'}</Fosforlu>
        <Yazi ton="ink2">Hesap yalnızca forumda soru sormak ve yanıt yazmak için gerekir. İlanlar ve Bana Uygun hesapsız çalışır.</Yazi>
        <View style={{ flexDirection: 'row', gap: 8 }}>
          <Cip etiket="Giriş yap" secili={mod === 'giris'} onPress={() => setMod('giris')} />
          <Cip etiket="Kayıt ol" secili={mod === 'kayit'} onPress={() => setMod('kayit')} />
        </View>
        {mod === 'kayit' ? <Girdi etiket="Görünen ad" placeholder="Ör. Ayşe K." value={ad} onChangeText={setAd} maxLength={30} autoComplete="name" /> : null}
        <Girdi etiket="E-posta" placeholder="ornek@eposta.com" value={eposta} onChangeText={setEposta} keyboardType="email-address" autoCapitalize="none" autoComplete="email" />
        <Girdi etiket="Şifre" placeholder="En az 6 karakter" value={sifre} onChangeText={setSifre} secureTextEntry autoComplete={mod === 'giris' ? 'password' : 'new-password'} hata={hata} />
        <Buton etiket={mod === 'giris' ? 'Giriş yap' : 'Hesap oluştur'} onPress={gonder} yukleniyor={bekle} pasif={!eposta || sifre.length < 6} />
        {mod === 'giris' ? <Buton etiket="Şifremi unuttum" tur="sade" onPress={sifirla} /> : null}
        <Pressable onPress={() => WebBrowser.openBrowserAsync(LINKS.kosullar).catch(() => {})}>
          <Yazi boyut={12.5} ton="ink3" stil={{ textAlign: 'center' }}>
            Devam ederek Kullanım Koşulları ve Gizlilik Politikası’nı kabul etmiş olursun.
          </Yazi>
        </Pressable>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}
