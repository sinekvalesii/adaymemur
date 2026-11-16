import { FlashList } from '@shopify/flash-list';
import { router } from 'expo-router';
import { useMemo } from 'react';
import { View } from 'react-native';

import { SekmeBasligi } from '../../components/Ekran';
import { IlanKarti } from '../../components/IlanKarti';
import { Bos, Buton, Yazi } from '../../components/ui';
import { useIlanlar } from '../../lib/api';
import { UCRETSIZ_KAYIT_LIMITI } from '../../lib/config';
import { kalanGun } from '../../lib/format';
import { kaydetDegistir } from '../../lib/kayit';
import { proDeposu } from '../../lib/pro';
import { kayitDeposu, useDepo } from '../../lib/store';
import type { Ilan } from '../../lib/types';
import { space, usePalette } from '../../theme';

type Satir = { tip: 'baslik'; metin: string } | { tip: 'ilan'; ilan: Ilan };

export default function Kaydedilenler() {
  const c = usePalette();
  const kayitlar = useDepo(kayitDeposu, (s) => s.kayitlar);
  const ilanlar = useIlanlar();
  const pro = useDepo(proDeposu, (s) => s.pro);

  const satirlar = useMemo<Satir[]>(() => {
    const canli = new Map(ilanlar.map((i) => [i.id, i]));
    const hepsi = Object.values(kayitlar).map((k) => canli.get(k.ilan.id) ?? k.ilan);
    const aktif = hepsi.filter((i) => (kalanGun(i.sonBasvuru) ?? 0) >= 0).sort((a, b) => (a.sonBasvuru ?? '9').localeCompare(b.sonBasvuru ?? '9'));
    const biten = hepsi.filter((i) => (kalanGun(i.sonBasvuru) ?? 0) < 0);
    const out: Satir[] = [];
    if (aktif.length) {
      out.push({ tip: 'baslik', metin: 'Başvurusu açık' });
      aktif.forEach((ilan) => out.push({ tip: 'ilan', ilan }));
    }
    if (biten.length) {
      out.push({ tip: 'baslik', metin: 'Süresi dolanlar' });
      biten.forEach((ilan) => out.push({ tip: 'ilan', ilan }));
    }
    return out;
  }, [kayitlar, ilanlar]);
  const adet = Object.keys(kayitlar).length;

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <FlashList
        data={satirlar}
        getItemType={(s) => s.tip}
        keyExtractor={(s) => (s.tip === 'ilan' ? s.ilan.id : s.metin)}
        contentContainerStyle={{ paddingBottom: 24 }}
        ListHeaderComponent={
          <SekmeBasligi
            baslik="Kaydedilenler"
            alt={adet ? (pro ? `${adet} ilan · son başvuruya göre sıralı` : `${adet}/${UCRETSIZ_KAYIT_LIMITI} ilan kaydedildi`) : 'Takip ettiğin ilanlar'}
          />
        }
        renderItem={({ item }) =>
          item.tip === 'baslik' ? (
            <Yazi boyut={12} agirlik="bold" ton="ink3" stil={{ marginHorizontal: space.lg, marginTop: space.md, marginBottom: space.sm, letterSpacing: 0.9 }}>
              {item.metin.toLocaleUpperCase('tr')}
            </Yazi>
          ) : (
            <View style={{ paddingHorizontal: space.lg, paddingBottom: space.md }}>
              <IlanKarti ilan={item.ilan} kayitli onKaydet={() => kaydetDegistir(item.ilan)} onPress={() => router.push(`/ilan/${item.ilan.id}`)} />
            </View>
          )
        }
        ListEmptyComponent={
          <Bos
            ikon="yerimi"
            baslik="Henüz ilan kaydetmedin"
            metin="İlan kartındaki işarete dokun; son başvuru tarihleri burada sıralansın."
            eylem={<Buton etiket="İlanlara göz at" tur="ikincil" onPress={() => router.navigate('/')} />}
          />
        }
      />
    </View>
  );
}
