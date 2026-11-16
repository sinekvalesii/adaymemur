import { FlashList } from '@shopify/flash-list';
import { router } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { View } from 'react-native';

import { SekmeBasligi } from '../../components/Ekran';
import { Ikon } from '../../components/Ikon';
import { IlanKarti } from '../../components/IlanKarti';
import { Bos, Buton, Cip, Fosforlu, Kart, Yazi } from '../../components/ui';
import { ilanMetni, useIlanlar } from '../../lib/api';
import { kalanGun } from '../../lib/format';
import { kaydetDegistir, useKayitIdleri } from '../../lib/kayit';
import { uygunluk, type Uygunluk } from '../../lib/match';
import { proDeposu } from '../../lib/pro';
import { EGITIM_ADI } from '../../lib/sabitler';
import { profilDeposu, useDepo } from '../../lib/store';
import type { Ilan } from '../../lib/types';
import { space, usePalette } from '../../theme';

type Sekme = 'uygun' | 'kontrol';

export default function BanaUygun() {
  const c = usePalette();
  const ilanlar = useIlanlar();
  const profil = useDepo(profilDeposu, (s) => s);
  const pro = useDepo(proDeposu, (s) => s.pro);
  const kayitlar = useKayitIdleri();
  const [sekme, setSekme] = useState<Sekme>('uygun');

  const sonuc = useMemo(() => {
    if (!profil.tamamlandi) return { uygun: [], kontrol: [] } as Record<Sekme, { ilan: Ilan; u: Uygunluk }[]>;
    const aktif = ilanlar.filter((i) => {
      const k = kalanGun(i.sonBasvuru);
      return k === null || k >= 0;
    });
    const hepsi = aktif.map((ilan) => ({ ilan, u: uygunluk(ilan, profil) }));
    const sirala = (a: { u: Uygunluk; ilan: Ilan }, b: { u: Uygunluk; ilan: Ilan }) =>
      b.u.puan - a.u.puan || (a.ilan.sonBasvuru ?? '9').localeCompare(b.ilan.sonBasvuru ?? '9');
    return {
      uygun: hepsi.filter((x) => x.u.durum === 'uygun').sort(sirala),
      kontrol: hepsi.filter((x) => x.u.durum === 'kontrol').sort(sirala),
    };
  }, [ilanlar, profil]);

  // Pro: Kariyer Kapısı ilanlarının metnini arka planda çekip şartları netleştir
  useEffect(() => {
    if (!pro || !profil.tamamlandi) return;
    let iptal = false;
    const hedef = sonuc.kontrol
      .map((x) => x.ilan)
      .filter((i) => i.kaynak === 'kariyerkapisi' && !i.detay && !i.egitim.length)
      .slice(0, 20);
    (async () => {
      for (const i of hedef) {
        if (iptal) break;
        await ilanMetni(i);
      }
    })();
    return () => {
      iptal = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pro, profil.tamamlandi, sonuc.kontrol.length]);

  if (!profil.tamamlandi) {
    return (
      <View style={{ flex: 1, backgroundColor: c.paper }}>
        <SekmeBasligi baslik="Bana Uygun" alt="Kişisel ilan eşleştirme" />
        <View style={{ padding: space.lg, gap: space.lg }}>
          <Kart stil={{ gap: 12 }}>
            <Fosforlu boyut={20}>Sana uygun ilanları tek listede gör</Fosforlu>
            <Yazi ton="ink2">Öğrenim düzeyin, bölümün, KPSS puanın ve il tercihin her ilanın şartlarıyla karşılaştırılır. Başvurabileceğin ilanlar öne çıkar.</Yazi>
            {['Öğrenim ve bölüm uyumu', 'KPSS puan türü ve taban puan', 'Kadro yeri ve son başvuru'].map((m) => (
              <View key={m} style={{ flexDirection: 'row', gap: 10, alignItems: 'center' }}>
                <Ikon ad="tik" boyut={18} renk={c.ok} kalinlik={2.2} />
                <Yazi>{m}</Yazi>
              </View>
            ))}
            <Buton etiket="Profilimi oluştur" onPress={() => router.push('/profil')} stil={{ marginTop: 6 }} />
          </Kart>
        </View>
      </View>
    );
  }

  const liste = sonuc[sekme];
  const ozet = `${profil.egitim ? EGITIM_ADI[profil.egitim] : ''}${profil.bolum ? ` · ${profil.bolum}` : ''}${Object.entries(profil.puanlar)
    .map(([k, v]) => ` · ${k} ${String(v).replace('.', ',')}`)
    .join('')}`;

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <FlashList
        data={pro ? liste : []}
        keyExtractor={(x) => x.ilan.id}
        contentContainerStyle={{ paddingBottom: 24 }}
        ListHeaderComponent={
          <View>
            <SekmeBasligi baslik="Bana Uygun" alt={ozet} sag={<Buton etiket="Düzenle" tur="sade" genis={false} onPress={() => router.push('/profil')} stil={{ height: 40 }} />} />
            <View style={{ flexDirection: 'row', gap: 8, paddingHorizontal: space.lg, paddingVertical: space.md }}>
              <Cip etiket={`Uygun · ${sonuc.uygun.length}`} secili={sekme === 'uygun'} onPress={() => setSekme('uygun')} />
              <Cip etiket={`Kontrol et · ${sonuc.kontrol.length}`} secili={sekme === 'kontrol'} onPress={() => setSekme('kontrol')} />
            </View>
            {!pro ? (
              <View style={{ paddingHorizontal: space.lg }}>
                <Kart stil={{ gap: 12, alignItems: 'flex-start' }}>
                  <View style={{ width: 44, height: 44, borderRadius: 22, backgroundColor: c.marker, alignItems: 'center', justifyContent: 'center' }}>
                    <Ikon ad="kilit" renk={c.onMarker} />
                  </View>
                  <Yazi boyut={20} agirlik="heavy" stil={{ letterSpacing: -0.4 }}>
                    {sonuc.uygun.length ? `${sonuc.uygun.length} ilan profilinle eşleşiyor` : 'Eşleşmeleri Pro ile gör'}
                  </Yazi>
                  <Yazi ton="ink2">
                    {sonuc.kontrol.length
                      ? `${sonuc.kontrol.length} ilan için de şartları birlikte kontrol edelim. `
                      : ''}
                    Kişisel listeni, her ilanın neden uygun olduğunu ve yeni eşleşmelerde anında bildirimi Pro ile aç.
                  </Yazi>
                  <Buton etiket="Pro’yu incele" onPress={() => router.push('/pro')} />
                </Kart>
              </View>
            ) : null}
          </View>
        }
        renderItem={({ item }) => (
          <View style={{ paddingHorizontal: space.lg, paddingBottom: space.md }}>
            <IlanKarti
              ilan={item.ilan}
              uygunluk={item.u}
              kayitli={!!kayitlar[item.ilan.id]}
              onKaydet={() => kaydetDegistir(item.ilan)}
              onPress={() => router.push(`/ilan/${item.ilan.id}`)}
            />
          </View>
        )}
        ListEmptyComponent={
          pro ? (
            <Bos
              ikon="hedef"
              baslik={sekme === 'uygun' ? 'Şu an tam eşleşen ilan yok' : 'Kontrol edilecek ilan yok'}
              metin="Yeni ilanlar geldikçe bu liste güncellenir. Bildirimler açıksa eşleşmeleri kaçırmazsın."
            />
          ) : null
        }
      />
    </View>
  );
}
