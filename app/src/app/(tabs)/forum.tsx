import { FlashList } from '@shopify/flash-list';
import { router } from 'expo-router';
import { useEffect, useState } from 'react';
import { Pressable, ScrollView, View } from 'react-native';

import { SekmeBasligi } from '../../components/Ekran';
import { Ikon } from '../../components/Ikon';
import { Bos, Cip, Kart, Rozet, Yazi } from '../../components/ui';
import { firebaseHazir, konulariDinle, type Konu } from '../../lib/fb';
import { goreli } from '../../lib/format';
import { FORUM_KATEGORILERI as KATEGORILER } from '../../lib/sabitler';
import { ayarDeposu, useDepo } from '../../lib/store';
import { font, space, usePalette } from '../../theme';

export default function Forum() {
  const c = usePalette();
  const [kategori, setKategori] = useState<string | null>(null);
  const [konular, setKonular] = useState<Konu[] | null>(null);
  const [hata, setHata] = useState(false);
  const engelli = useDepo(ayarDeposu, (s) => s.engellenenler);
  const aktif = firebaseHazir();

  useEffect(() => {
    if (!aktif) return;
    setKonular(null);
    return konulariDinle(kategori, setKonular, () => setHata(true));
  }, [kategori, aktif]);

  const gorunen = (konular ?? []).filter((k) => !engelli.includes(k.yazarId) && k.raporSayisi < 3);

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <FlashList
        data={gorunen}
        keyExtractor={(k) => k.id}
        contentContainerStyle={{ paddingBottom: 100 }}
        ListHeaderComponent={
          <View>
            <SekmeBasligi baslik="Forum" alt="Atama, tayin, maaş ve KPSS soruları" />
            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ paddingHorizontal: space.lg, gap: 8, paddingVertical: space.md }}>
              <Cip etiket="Tümü" secili={!kategori} onPress={() => setKategori(null)} />
              {KATEGORILER.map((k) => (
                <Cip key={k} etiket={k} secili={kategori === k} onPress={() => setKategori(k)} />
              ))}
            </ScrollView>
          </View>
        }
        renderItem={({ item }) => (
          <View style={{ paddingHorizontal: space.lg, paddingBottom: space.md }}>
            <Kart onPress={() => router.push(`/forum/${item.id}`)} stil={{ gap: 8 }}>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                <Rozet metin={item.kategori} />
                <Yazi boyut={12.5} ton="ink3">{item.yazarAd} · {goreli(new Date(item.sonHareket).toISOString())}</Yazi>
              </View>
              <Yazi boyut={16} agirlik="bold" satir={2}>{item.baslik}</Yazi>
              <Yazi boyut={14} ton="ink2" satir={2}>{item.metin}</Yazi>
              <View style={{ flexDirection: 'row', alignItems: 'center', gap: 6 }}>
                <Ikon ad="sohbet" boyut={16} renk={c.ink3} />
                <Yazi boyut={13} ton="ink3">{item.yorumSayisi} yanıt</Yazi>
              </View>
            </Kart>
          </View>
        )}
        ListEmptyComponent={
          !aktif ? (
            <Bos ikon="sohbet" baslik="Forum yakında açılıyor" metin="Adayların birbirine sorular sorabildiği bölüm hazırlanıyor." />
          ) : hata ? (
            <Bos ikon="uyari" baslik="Forum yüklenemedi" metin="Bağlantını kontrol edip tekrar dene." />
          ) : konular === null ? (
            <Yazi ton="ink3" stil={{ textAlign: 'center', marginTop: 40 }}>Yükleniyor…</Yazi>
          ) : (
            <Bos ikon="sohbet" baslik="Bu kategoride henüz konu yok" metin="İlk soruyu sen sor." />
          )
        }
      />
      {aktif ? (
        <Pressable
          accessibilityLabel="Yeni konu aç"
          onPress={() => router.push('/forum/yeni')}
          style={({ pressed }) => ({
            position: 'absolute',
            right: space.lg,
            bottom: space.lg,
            height: 54,
            paddingHorizontal: 20,
            borderRadius: 27,
            backgroundColor: c.marker,
            flexDirection: 'row',
            alignItems: 'center',
            gap: 8,
            transform: [{ scale: pressed ? 0.97 : 1 }],
            shadowColor: '#000',
            shadowOpacity: 0.18,
            shadowRadius: 12,
            shadowOffset: { width: 0, height: 6 },
            elevation: 5,
          })}
        >
          <Ikon ad="kalem" boyut={20} renk={c.onMarker} />
          <Yazi agirlik="bold" ton="onMarker" stil={{ fontFamily: font.bold }}>Soru sor</Yazi>
        </Pressable>
      ) : null}
    </View>
  );
}
