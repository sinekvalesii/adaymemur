import { FlashList } from '@shopify/flash-list';
import { router } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { Pressable, RefreshControl, ScrollView, Text, View } from 'react-native';

import { SekmeBasligi } from '../../components/Ekran';
import { GundemKarti } from '../../components/Gundem';
import { Ikon } from '../../components/Ikon';
import { IlanKarti } from '../../components/IlanKarti';
import { Bos, Buton, Cip, Girdi, IskeletKart } from '../../components/ui';
import { ReklamAlani } from '../../lib/ads';
import { useIlanlar, useVeri, yenile } from '../../lib/api';
import { goreli, sayi } from '../../lib/format';
import { kaydetDegistir, useKayitIdleri } from '../../lib/kayit';
import { aktifFiltreSayisi, filtrele, kilitDakika } from '../../lib/liste';
import { proDeposu } from '../../lib/pro';
import { ayarDeposu, filtreDeposu, useDepo, VARSAYILAN_FILTRE } from '../../lib/store';
import type { Ilan, Tur } from '../../lib/types';
import { font, radius, space, usePalette } from '../../theme';

const HIZLI: { tur: Tur; ad: string }[] = [
  { tur: 'memur', ad: 'Memur' },
  { tur: 'sozlesmeli', ad: 'Sözleşmeli' },
  { tur: 'isci', ad: 'İşçi' },
  { tur: 'akademik', ad: 'Akademik' },
  { tur: 'kolluk', ad: 'Askerî / Kolluk' },
];

type Satir = { tip: 'ilan'; ilan: Ilan } | { tip: 'reklam'; id: string };

export default function Ilanlar() {
  const c = usePalette();
  const ilanlar = useIlanlar();
  const durum = useVeri((v) => v.durum);
  const guncelleme = useVeri((v) => v.guncelleme);
  const yenileniyor = useVeri((v) => v.yenileniyor);
  const hata = useVeri((v) => v.hata);
  const filtre = useDepo(filtreDeposu, (s) => s);
  const pro = useDepo(proDeposu, (s) => s.pro);
  const goruldu = useDepo(ayarDeposu, (s) => s.goruldu);
  const kayitlar = useKayitIdleri();
  const [arama, setArama] = useState(filtre.arama);
  const [, tik] = useState(0);

  // Erken erişim sayaçları için dakikada bir yenile
  useEffect(() => {
    const t = setInterval(() => tik((x) => x + 1), 60000);
    return () => clearInterval(t);
  }, []);
  useEffect(() => {
    const t = setTimeout(() => filtreDeposu.set({ arama }), 200);
    return () => clearTimeout(t);
  }, [arama]);

  const sonuc = useMemo(() => filtrele(ilanlar, filtre), [ilanlar, filtre]);
  const satirlar = useMemo<Satir[]>(() => {
    const out: Satir[] = [];
    sonuc.forEach((ilan, i) => {
      out.push({ tip: 'ilan', ilan });
      if (!pro && i > 0 && i % 8 === 4) out.push({ tip: 'reklam', id: `r${i}` });
    });
    return out;
  }, [sonuc, pro]);
  const filtreSayisi = aktifFiltreSayisi(filtre);
  const gun = Date.now() - 24 * 3600000;

  const ustBilgi = (
    <View>
      <SekmeBasligi
        baslik="Kamu İlanları"
        alt={guncelleme ? `${sayi(sonuc.length)} ilan · ${goreli(guncelleme)} güncellendi` : 'Resmî kaynaklardan güncel ilanlar'}
      />
      <View style={{ paddingHorizontal: space.lg, flexDirection: 'row', gap: 10, marginTop: 4 }}>
        <View style={{ flex: 1 }}>
          <Girdi
            ikon="ara"
            placeholder="Kurum, unvan veya il ara"
            value={arama}
            onChangeText={setArama}
            returnKeyType="search"
            autoCorrect={false}
            clearButtonMode="while-editing"
          />
        </View>
        <Pressable
          accessibilityLabel="Filtreler"
          onPress={() => router.push('/filtre')}
          style={{ width: 50, height: 50, borderRadius: radius.md, backgroundColor: filtreSayisi ? c.ink : c.sheet, borderWidth: 1, borderColor: filtreSayisi ? c.ink : c.line, alignItems: 'center', justifyContent: 'center' }}
        >
          <Ikon ad="filtre" renk={filtreSayisi ? c.paper : c.ink} />
          {filtreSayisi ? (
            <View style={{ position: 'absolute', top: -5, right: -5, minWidth: 20, height: 20, borderRadius: 10, backgroundColor: c.marker, alignItems: 'center', justifyContent: 'center', paddingHorizontal: 5 }}>
              <Text style={{ fontFamily: font.bold, fontSize: 11, color: c.onMarker }}>{filtreSayisi}</Text>
            </View>
          ) : null}
        </Pressable>
      </View>
      <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ paddingHorizontal: space.lg, gap: 8, paddingVertical: space.md }}>
        <Cip etiket="Tümü" secili={!filtre.turler.length} onPress={() => filtreDeposu.set({ turler: [] })} />
        {HIZLI.map((h) => (
          <Cip
            key={h.tur}
            etiket={h.ad}
            secili={filtre.turler.includes(h.tur)}
            onPress={() =>
              filtreDeposu.set((s) => ({ turler: s.turler.includes(h.tur) ? s.turler.filter((t) => t !== h.tur) : [...s.turler, h.tur] }))
            }
          />
        ))}
      </ScrollView>
      <GundemKarti />
    </View>
  );

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <FlashList
        data={durum === 'hazir' ? satirlar : []}
        keyExtractor={(s) => (s.tip === 'ilan' ? s.ilan.id : s.id)}
        getItemType={(s) => s.tip}
        ListHeaderComponent={ustBilgi}
        keyboardDismissMode="on-drag"
        keyboardShouldPersistTaps="handled"
        contentContainerStyle={{ paddingBottom: 24 }}
        refreshControl={<RefreshControl refreshing={yenileniyor && durum === 'hazir'} onRefresh={yenile} tintColor={c.ink2} colors={[c.formInk]} />}
        renderItem={({ item }) =>
          item.tip === 'reklam' ? (
            <View style={{ paddingHorizontal: space.lg }}>
              <ReklamAlani yer="listeArasi" />
            </View>
          ) : (
            <View style={{ paddingHorizontal: space.lg, paddingBottom: space.md }}>
              <IlanKarti
                ilan={item.ilan}
                kayitli={!!kayitlar[item.ilan.id]}
                onKaydet={() => kaydetDegistir(item.ilan)}
                yeni={!goruldu[item.ilan.id] && new Date(item.ilan.eklenme).getTime() > gun}
                kilitDk={kilitDakika(item.ilan, pro)}
                onPress={() => (kilitDakika(item.ilan, pro) ? router.push('/pro') : router.push(`/ilan/${item.ilan.id}`))}
              />
            </View>
          )
        }
        ListEmptyComponent={
          durum === 'yukleniyor' || durum === 'bos' ? (
            <View style={{ paddingHorizontal: space.lg, gap: space.md }}>
              <IskeletKart />
              <IskeletKart />
              <IskeletKart />
            </View>
          ) : durum === 'hata' ? (
            <Bos ikon="uyari" baslik="Bağlantı kurulamadı" metin={hata ?? undefined} eylem={<Buton etiket="Tekrar dene" ikon="yenile" onPress={yenile} />} />
          ) : (
            <Bos
              ikon="ara"
              baslik="Bu filtrelere uyan ilan yok"
              metin="Filtreleri gevşetmeyi ya da aramayı değiştirmeyi dene."
              eylem={
                <Buton
                  etiket="Filtreleri temizle"
                  tur="ikincil"
                  onPress={() => {
                    setArama('');
                    filtreDeposu.set({ ...VARSAYILAN_FILTRE, siralama: filtre.siralama });
                  }}
                />
              }
            />
          )
        }
      />
      <ReklamAlani />
    </View>
  );
}
