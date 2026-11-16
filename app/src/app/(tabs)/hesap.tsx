import * as Application from 'expo-application';
import { router } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';
import { Alert, Linking, ScrollView, Share, Switch, View } from 'react-native';

import { bildir } from '../../components/Bildirim';
import { SekmeBasligi } from '../../components/Ekran';
import { Ikon } from '../../components/Ikon';
import { Buton, Grup, Kart, Satir, Yazi } from '../../components/ui';
import { reklamGizlilikAyarlari, useReklamGizlilikSecenegi } from '../../lib/ads';
import { LINKS } from '../../lib/config';
import { bildirimIzniIste, cikisYap, firebaseHazir } from '../../lib/fb';
import { tarihUzun } from '../../lib/format';
import { useKullanici } from '../../lib/oturum';
import { proDeposu } from '../../lib/pro';
import { EGITIM_ADI, ILLER, MAKER } from '../../lib/sabitler';
import { ayarDeposu, profilDeposu, useDepo } from '../../lib/store';
import { space, usePalette } from '../../theme';

export default function Hesap() {
  const c = usePalette();
  const pro = useDepo(proDeposu, (s) => s);
  const profil = useDepo(profilDeposu, (s) => s);
  const ayar = useDepo(ayarDeposu, (s) => s);
  const { kullanici } = useKullanici();
  const gizlilikSecenegi = useReklamGizlilikSecenegi();
  const ac = (u: string) => WebBrowser.openBrowserAsync(u, { toolbarColor: c.paper, controlsColor: c.formInk }).catch(() => {});

  const bildirimDegistir = async (v: boolean) => {
    if (v) {
      const izin = await bildirimIzniIste();
      if (!izin) {
        Alert.alert('Bildirim izni kapalı', 'Yeni ilan bildirimleri için telefon ayarlarından izin ver.', [
          { text: 'Vazgeç', style: 'cancel' },
          { text: 'Ayarları aç', onPress: () => Linking.openSettings() },
        ]);
        return;
      }
    }
    ayarDeposu.set({ bildirim: v });
  };

  const profilOzet = profil.tamamlandi
    ? [profil.egitim ? EGITIM_ADI[profil.egitim] : null, profil.bolum || null, ...Object.entries(profil.puanlar).map(([k, v]) => `${k} ${String(v).replace('.', ',')}`)]
        .filter(Boolean)
        .join(' · ')
    : 'Bana Uygun için öğrenim ve puan bilgilerini gir';

  return (
    <ScrollView style={{ flex: 1, backgroundColor: c.paper }} contentContainerStyle={{ paddingBottom: 40 }}>
      <SekmeBasligi baslik="Hesap" />

      <View style={{ paddingHorizontal: space.lg, marginTop: space.sm }}>
        {pro.pro ? (
          <Kart stil={{ backgroundColor: c.dark ? c.sheet : c.ink, borderColor: c.dark ? c.line : c.ink, gap: 6 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
              <Ikon ad="yildiz" renk={c.marker} />
              <Yazi boyut={17} agirlik="heavy" stil={{ color: c.dark ? c.ink : c.paper }}>Aday Memur Pro</Yazi>
            </View>
            <Yazi boyut={13.5} stil={{ color: c.dark ? c.ink2 : c.paper, opacity: c.dark ? 1 : 0.75 }}>
              {pro.bitis ? `${pro.yenileniyor ? 'Yenilenme' : 'Bitiş'}: ${tarihUzun(pro.bitis.slice(0, 10))}` : 'Aktif'}
            </Yazi>
            <Buton etiket="Aboneliği yönet" tur="ikincil" onPress={() => Linking.openURL(LINKS.abonelikYonet)} stil={{ marginTop: 8 }} />
          </Kart>
        ) : (
          <Kart onPress={() => router.push('/pro')} stil={{ backgroundColor: c.dark ? c.sheet : c.ink, borderColor: c.dark ? c.line : c.ink, gap: 6 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
              <Ikon ad="yildiz" renk={c.marker} />
              <Yazi boyut={17} agirlik="heavy" stil={{ color: c.dark ? c.ink : c.paper, flex: 1 }}>Pro’ya geç</Yazi>
              <Ikon ad="sag" boyut={18} renk={c.dark ? c.ink2 : c.paper} />
            </View>
            <Yazi boyut={13.5} stil={{ color: c.dark ? c.ink2 : c.paper, opacity: c.dark ? 1 : 0.78 }}>Sana uygun ilan listesi, anında bildirim, erken erişim ve reklamsız kullanım.</Yazi>
          </Kart>
        )}
      </View>

      <Grup baslik="Profil">
        <Satir ikon="okul" baslik="Öğrenim ve KPSS bilgilerim" alt={profilOzet} onPress={() => router.push('/profil')} />
      </Grup>

      <Grup
        baslik="Bildirimler"
        alt={pro.pro ? 'Pro: yeni ilan yayımlandığı anda bildirim alırsın.' : 'Ücretsiz planda her akşam günün yeni ilanlarının özeti gelir. Anında bildirim Pro’da.'}
      >
        <Satir
          ikon="zil"
          baslik="Yeni ilan bildirimleri"
          sag={<Switch value={ayar.bildirim} onValueChange={bildirimDegistir} trackColor={{ true: c.formInk, false: c.form }} thumbColor={c.sheet} />}
        />
        {pro.pro && ayar.bildirim ? (
          <Satir
            ikon="konum"
            baslik="Bildirim alınacak iller"
            alt={ayar.bildirimIlleri.length ? ayar.bildirimIlleri.map((k) => ILLER[k]).join(', ') : 'Tüm Türkiye'}
            onPress={() => router.push('/bildirim-illeri')}
          />
        ) : null}
      </Grup>

      {firebaseHazir() ? (
        <Grup baslik="Forum hesabı">
          {kullanici ? (
            <>
              <Satir ikon="kisi" baslik={kullanici.ad} alt={kullanici.eposta ?? undefined} />
              <Satir
                ikon="cikis"
                baslik="Çıkış yap"
                onPress={() => cikisYap().then(() => bildir('Çıkış yapıldı')).catch(() => {})}
              />
              <Satir ikon="sil" baslik="Hesabı sil" tehlike onPress={() => router.push('/hesap-sil')} />
            </>
          ) : (
            <Satir ikon="kisi" baslik="Giriş yap veya kayıt ol" alt="Forumda soru sormak için" onPress={() => router.push('/giris')} />
          )}
        </Grup>
      ) : null}

      <Grup baslik="Uygulama">
        <Satir ikon="takvim" baslik="KPSS takvimi ve duyurular" onPress={() => router.push('/gundem')} />
        <Satir ikon="yildiz" baslik="Uygulamayı değerlendir" onPress={() => Linking.openURL(LINKS.playStore)} />
        <Satir ikon="paylas" baslik="Arkadaşına öner" onPress={() => Share.share({ message: `Kamu ilanlarını ve KPSS gündemini tek yerden takip ediyorum: Aday Memur\n${LINKS.playStore}` }).catch(() => {})} />
        {pro.pro ? null : (
          <Satir ikon="yenile" baslik="Satın alımları geri yükle" onPress={() => router.push('/pro')} />
        )}
        {gizlilikSecenegi ? <Satir ikon="kalkan" baslik="Reklam gizlilik ayarları" onPress={reklamGizlilikAyarlari} /> : null}
        {LINKS.destekEposta ? (
          <Satir ikon="sohbet" baslik="Destek" alt={LINKS.destekEposta} onPress={() => Linking.openURL(`mailto:${LINKS.destekEposta}?subject=Aday%20Memur`)} />
        ) : null}
      </Grup>

      <Grup baslik="Yasal">
        <Satir ikon="bilgi" baslik="Veri kaynakları ve sorumluluk" onPress={() => router.push('/hakkinda')} />
        <Satir ikon="kalkan" baslik="Gizlilik politikası" onPress={() => ac(LINKS.gizlilik)} />
        <Satir ikon="belge" baslik="Kullanım koşulları" onPress={() => ac(LINKS.kosullar)} />
      </Grup>

      <View style={{ alignItems: 'center', marginTop: space.xxl, gap: 2 }}>
        <Yazi boyut={12.5} ton="ink3">Aday Memur {Application.nativeApplicationVersion ?? '1.0.0'}</Yazi>
        <Yazi boyut={12.5} ton="ink3">Yapım: {MAKER}</Yazi>
      </View>
    </ScrollView>
  );
}
