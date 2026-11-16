import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useState } from 'react';
import { ActionSheetIOS, Alert, KeyboardAvoidingView, Platform, Pressable, ScrollView, TextInput, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { bildir } from '../../components/Bildirim';
import { AltBaslik } from '../../components/Ekran';
import { Ikon } from '../../components/Ikon';
import { Kart, Rozet, Yazi } from '../../components/ui';
import { icerikSil, konuDinle, raporla, yorumlariDinle, yorumYaz, type Konu, type Yorum } from '../../lib/fb';
import { goreli } from '../../lib/format';
import { useKullanici } from '../../lib/oturum';
import { uygunsuzMu } from '../../lib/sabitler';
import { ayarDeposu, useDepo } from '../../lib/store';
import { font, radius, space, usePalette } from '../../theme';

const SEBEPLER = ['Hakaret veya taciz', 'Spam / reklam', 'Yanıltıcı bilgi', 'Kişisel bilgi paylaşımı', 'Diğer'];

function secenekSor(baslik: string, secenekler: string[], sec: (i: number) => void) {
  if (Platform.OS === 'ios') {
    ActionSheetIOS.showActionSheetWithOptions({ title: baslik, options: [...secenekler, 'Vazgeç'], cancelButtonIndex: secenekler.length }, (i) => {
      if (i < secenekler.length) sec(i);
    });
    return;
  }
  Alert.alert(baslik, undefined, [...secenekler.map((s, i) => ({ text: s, onPress: () => sec(i) })), { text: 'Vazgeç', style: 'cancel' as const }], { cancelable: true });
}

export default function KonuEkrani() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const { id } = useLocalSearchParams<{ id: string }>();
  const { kullanici } = useKullanici();
  const engelli = useDepo(ayarDeposu, (s) => s.engellenenler);
  const [konu, setKonu] = useState<Konu | null | undefined>(undefined);
  const [yorumlar, setYorumlar] = useState<Yorum[]>([]);
  const [metin, setMetin] = useState('');
  const [gonderiliyor, setGonderiliyor] = useState(false);

  useEffect(() => konuDinle(id, setKonu), [id]);
  useEffect(() => yorumlariDinle(id, setYorumlar), [id]);

  const girisGerekli = () => {
    router.push('/giris');
  };

  const menu = (hedef: { yazarId: string; yazarAd: string; yorumId?: string }) => {
    if (!kullanici) return girisGerekli();
    const benim = hedef.yazarId === kullanici.uid;
    const secenekler = benim ? ['Sil'] : ['Raporla', `${hedef.yazarAd} kullanıcısını engelle`];
    secenekSor(benim ? 'Gönderin' : 'Bu içerik', secenekler, (i) => {
      if (benim) {
        Alert.alert('Silinsin mi?', 'Bu işlem geri alınamaz.', [
          { text: 'Vazgeç', style: 'cancel' },
          {
            text: 'Sil',
            style: 'destructive',
            onPress: () =>
              icerikSil(id, hedef.yorumId)
                .then(() => {
                  bildir('Silindi');
                  if (!hedef.yorumId) router.back();
                })
                .catch(() => bildir('Silinemedi')),
          },
        ]);
      } else if (i === 0) {
        secenekSor('Neden raporluyorsun?', SEBEPLER, (j) =>
          raporla(id, SEBEPLER[j], hedef.yorumId)
            .then(() => bildir('Raporun alındı', 'İçerik incelenecek. Teşekkürler.', { ikon: 'bayrak' }))
            .catch(() => bildir('Rapor gönderilemedi')),
        );
      } else {
        ayarDeposu.set((s) => ({ engellenenler: [...new Set([...s.engellenenler, hedef.yazarId])] }));
        bildir(`${hedef.yazarAd} engellendi`, 'Gönderilerini artık görmeyeceksin.', { ikon: 'engel' });
        if (!hedef.yorumId) router.back();
      }
    });
  };

  const gonder = async () => {
    if (!kullanici) return girisGerekli();
    const t = metin.trim();
    if (t.length < 2) return;
    if (uygunsuzMu(t)) {
      bildir('Mesajın uygunsuz ifade içeriyor', 'Lütfen forum kurallarına uygun yaz.', { ikon: 'uyari' });
      return;
    }
    setGonderiliyor(true);
    try {
      await yorumYaz(id, t);
      setMetin('');
    } catch {
      bildir('Gönderilemedi', 'Bağlantını kontrol edip tekrar dene.');
    } finally {
      setGonderiliyor(false);
    }
  };

  if (konu === null) {
    return (
      <View style={{ flex: 1, backgroundColor: c.paper }}>
        <AltBaslik baslik="Forum" />
        <Yazi ton="ink2" stil={{ padding: space.xl }}>Bu konu kaldırılmış.</Yazi>
      </View>
    );
  }

  const gorunen = yorumlar.filter((y) => !engelli.includes(y.yazarId) && y.raporSayisi < 3);

  return (
    <KeyboardAvoidingView style={{ flex: 1, backgroundColor: c.paper }} behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
      <AltBaslik baslik={konu?.kategori ?? 'Forum'} sag={konu ? <Pressable hitSlop={8} onPress={() => menu({ yazarId: konu.yazarId, yazarAd: konu.yazarAd })} style={{ padding: 10 }}><Ikon ad="bayrak" renk={c.ink2} /></Pressable> : null} />
      <ScrollView contentContainerStyle={{ padding: space.lg, paddingBottom: 24, gap: space.md }} keyboardShouldPersistTaps="handled">
        {konu ? (
          <View style={{ gap: 10, marginBottom: space.sm }}>
            <Yazi boyut={22} agirlik="heavy" stil={{ letterSpacing: -0.4, lineHeight: 28 }}>{konu.baslik}</Yazi>
            <Yazi boyut={13} ton="ink3">{konu.yazarAd} · {goreli(new Date(konu.olusturma).toISOString())}</Yazi>
            <Yazi boyut={15.5} ton="ink2" secilebilir stil={{ lineHeight: 23 }}>{konu.metin}</Yazi>
          </View>
        ) : null}
        <Yazi boyut={12} agirlik="bold" ton="ink3" stil={{ letterSpacing: 0.9 }}>{`${gorunen.length} YANIT`}</Yazi>
        {gorunen.map((y) => (
          <Kart key={y.id} stil={{ gap: 6 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
              <Yazi boyut={13.5} agirlik="bold">{y.yazarAd}</Yazi>
              {konu && y.yazarId === konu.yazarId ? <Rozet metin="Konu sahibi" ton="marker" /> : null}
              <Yazi boyut={12.5} ton="ink3" stil={{ flex: 1 }}>{goreli(new Date(y.olusturma).toISOString())}</Yazi>
              <Pressable hitSlop={10} onPress={() => menu({ yazarId: y.yazarId, yazarAd: y.yazarAd, yorumId: y.id })}>
                <Yazi ton="ink3" agirlik="bold">···</Yazi>
              </Pressable>
            </View>
            <Yazi boyut={15} secilebilir stil={{ lineHeight: 22 }}>{y.metin}</Yazi>
          </Kart>
        ))}
      </ScrollView>
      <View style={{ flexDirection: 'row', alignItems: 'flex-end', gap: 10, paddingHorizontal: space.lg, paddingTop: space.sm, paddingBottom: ins.bottom + space.sm, borderTopWidth: 1, borderTopColor: c.line, backgroundColor: c.sheet }}>
        <TextInput
          value={metin}
          onChangeText={setMetin}
          onFocus={() => !kullanici && girisGerekli()}
          placeholder={kullanici ? 'Yanıt yaz' : 'Yanıt yazmak için giriş yap'}
          placeholderTextColor={c.ink3}
          multiline
          maxLength={2000}
          style={{ flex: 1, minHeight: 44, maxHeight: 120, borderRadius: radius.md, backgroundColor: c.paper, paddingHorizontal: 14, paddingVertical: 11, fontFamily: font.regular, fontSize: 15.5, color: c.ink }}
        />
        <Pressable
          accessibilityLabel="Gönder"
          onPress={gonder}
          disabled={gonderiliyor || metin.trim().length < 2}
          style={{ width: 44, height: 44, borderRadius: 22, backgroundColor: metin.trim().length >= 2 ? c.marker : c.form2, alignItems: 'center', justifyContent: 'center' }}
        >
          <Ikon ad="sag" renk={metin.trim().length >= 2 ? c.onMarker : c.ink3} kalinlik={2.4} />
        </Pressable>
      </View>
    </KeyboardAvoidingView>
  );
}
