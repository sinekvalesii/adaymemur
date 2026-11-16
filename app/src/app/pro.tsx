import { router } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { bildir } from '../components/Bildirim';
import { Ikon, type IkonAdi } from '../components/Ikon';
import { Buton, Fosforlu, IkonButon, Rozet, Yazi } from '../components/ui';
import { ERKEN_ERISIM_DK, LINKS, UCRETSIZ_KAYIT_LIMITI } from '../lib/config';
import { aboneligeHazir, geriYukle, paketleriGetir, proDeposu, satinAl, type Paket } from '../lib/pro';
import { useDepo } from '../lib/store';
import { radius, space, usePalette } from '../theme';

const AVANTAJLAR: { ikon: IkonAdi; baslik: string; metin: string }[] = [
  { ikon: 'hedef', baslik: 'Sana özel ilan listesi', metin: 'Öğrenim, bölüm, KPSS puanı ve il tercihine göre başvurabileceğin ilanlar; her biri için neden uygun olduğu.' },
  { ikon: 'zil', baslik: 'Anında bildirim', metin: 'Yeni ilan yayımlandığı an, seçtiğin illere göre. Ücretsiz planda günlük özet gelir.' },
  { ikon: 'saat', baslik: 'Erken erişim', metin: ERKEN_ERISIM_DK ? `Yeni ilanları herkesten ${ERKEN_ERISIM_DK} dakika önce gör.` : 'Yeni ilanları ilk gören ol.' },
  { ikon: 'yerimi', baslik: 'Sınırsız kayıt', metin: `Ücretsiz planda ${UCRETSIZ_KAYIT_LIMITI} ilan; Pro’da sınırsız.` },
  { ikon: 'kalkan', baslik: 'Reklamsız', metin: 'Uygulamanın her yerinde reklamsız kullanım.' },
];

export default function ProEkrani() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const pro = useDepo(proDeposu, (s) => s.pro);
  const [paketler, setPaketler] = useState<Paket[] | null>(null);
  const [secili, setSecili] = useState<string | null>(null);
  const [bekle, setBekle] = useState(false);
  const hazir = aboneligeHazir();

  useEffect(() => {
    if (!hazir) {
      setPaketler([]);
      return;
    }
    paketleriGetir()
      .then((p) => {
        const sirali = [...p].sort((a, b) => (a.tip === 'yillik' ? -1 : b.tip === 'yillik' ? 1 : 0));
        setPaketler(sirali);
        setSecili(sirali[0]?.id ?? null);
      })
      .catch(() => setPaketler([]));
  }, [hazir]);

  const al = async () => {
    const p = paketler?.find((x) => x.id === secili);
    if (!p) return;
    setBekle(true);
    try {
      const r = await satinAl(p);
      if (r === 'tamam') {
        bildir('Pro’ya hoş geldin', 'Tüm özellikler açıldı.', { ikon: 'yildiz' });
        router.back();
      }
    } catch {
      bildir('Satın alma tamamlanamadı', 'Ödemen alınmadıysa tekrar deneyebilirsin.', { ikon: 'uyari' });
    } finally {
      setBekle(false);
    }
  };

  const yukle = async () => {
    setBekle(true);
    try {
      const ok = await geriYukle();
      bildir(ok ? 'Pro aboneliğin geri yüklendi' : 'Bu Google hesabında etkin abonelik bulunamadı');
      if (ok) router.back();
    } catch {
      bildir('Geri yükleme başarısız', 'Bağlantını kontrol edip tekrar dene.');
    } finally {
      setBekle(false);
    }
  };

  const sec = paketler?.find((x) => x.id === secili);

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <ScrollView contentContainerStyle={{ paddingTop: ins.top + 8, paddingHorizontal: space.lg, paddingBottom: 230 }}>
        <View style={{ alignItems: 'flex-end' }}>
          <IkonButon ad="carpi" etiket="Kapat" onPress={() => router.back()} />
        </View>
        <Rozet metin="ADAY MEMUR PRO" ton="marker" />
        <Yazi boyut={30} agirlik="heavy" stil={{ letterSpacing: -0.9, lineHeight: 36, marginTop: space.md }}>
          Başvurabileceğin ilanı
        </Yazi>
        <Fosforlu boyut={30} agirlik="heavy">ilk sen gör.</Fosforlu>

        <View style={{ marginTop: space.xl, gap: space.lg }}>
          {AVANTAJLAR.map((a) => (
            <View key={a.baslik} style={{ flexDirection: 'row', gap: 14 }}>
              <View style={{ width: 40, height: 40, borderRadius: 12, backgroundColor: c.form2, alignItems: 'center', justifyContent: 'center' }}>
                <Ikon ad={a.ikon} renk={c.formInk} />
              </View>
              <View style={{ flex: 1 }}>
                <Yazi agirlik="bold">{a.baslik}</Yazi>
                <Yazi boyut={14} ton="ink2">{a.metin}</Yazi>
              </View>
            </View>
          ))}
        </View>

        {pro ? (
          <View style={{ marginTop: space.xl, padding: space.lg, borderRadius: radius.lg, backgroundColor: c.okBg }}>
            <Yazi agirlik="bold" ton="ok">Pro aboneliğin etkin. Teşekkürler!</Yazi>
          </View>
        ) : paketler === null ? (
          <ActivityIndicator style={{ marginTop: space.xl }} color={c.ink2} />
        ) : paketler.length === 0 ? (
          <View style={{ marginTop: space.xl, padding: space.lg, borderRadius: radius.lg, backgroundColor: c.form2, gap: 4 }}>
            <Yazi agirlik="bold">Abonelik seçenekleri yüklenemedi</Yazi>
            <Yazi boyut={14} ton="ink2">Google Play bağlantısını kontrol edip birazdan tekrar dene.</Yazi>
          </View>
        ) : (
          <View style={{ marginTop: space.xl, gap: space.md }}>
            {paketler.map((p) => {
              const aktif = p.id === secili;
              return (
                <Pressable
                  key={p.id}
                  onPress={() => setSecili(p.id)}
                  style={{ borderRadius: radius.lg, borderWidth: 2, borderColor: aktif ? c.ink : c.line, backgroundColor: c.sheet, padding: space.lg, flexDirection: 'row', alignItems: 'center', gap: 14 }}
                >
                  <View style={{ width: 22, height: 22, borderRadius: 11, borderWidth: 2, borderColor: aktif ? c.ink : c.ink3, alignItems: 'center', justifyContent: 'center' }}>
                    {aktif ? <View style={{ width: 10, height: 10, borderRadius: 5, backgroundColor: c.ink }} /> : null}
                  </View>
                  <View style={{ flex: 1 }}>
                    <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
                      <Yazi agirlik="bold" boyut={16}>{p.tip === 'yillik' ? 'Yıllık' : p.tip === 'aylik' ? 'Aylık' : p.id}</Yazi>
                      {p.tip === 'yillik' ? <Rozet metin="En avantajlı" ton="marker" /> : null}
                    </View>
                    <Yazi boyut={13.5} ton="ink2">{p.deneme ? `${p.deneme}, sonra ` : ''}{p.fiyat}{p.tip === 'yillik' ? '/yıl' : p.tip === 'aylik' ? '/ay' : ''}</Yazi>
                  </View>
                  {p.aylikKarsilik ? <Yazi boyut={13} agirlik="semibold" ton="ink3">{p.aylikKarsilik}/ay</Yazi> : null}
                </Pressable>
              );
            })}
          </View>
        )}
      </ScrollView>

      {!pro ? (
        <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, paddingHorizontal: space.lg, paddingTop: space.md, paddingBottom: ins.bottom + space.md, backgroundColor: c.paper, borderTopWidth: 1, borderTopColor: c.line, gap: 8 }}>
          <Buton etiket={sec?.deneme ? 'Ücretsiz denemeyi başlat' : 'Pro’ya geç'} onPress={al} yukleniyor={bekle} pasif={!sec} />
          <Yazi boyut={11.5} ton="ink3" stil={{ textAlign: 'center' }}>
            {sec ? `${sec.deneme ? `${sec.deneme} deneme bitince ` : ''}${sec.fiyat} ${sec.tip === 'yillik' ? 'yıllık' : 'aylık'} olarak Google Play hesabından tahsil edilir. ` : ''}
            Abonelik, dönem bitmeden en az 24 saat önce iptal edilmezse otomatik yenilenir; Google Play › Abonelikler’den istediğin zaman iptal edebilirsin.
          </Yazi>
          <View style={{ flexDirection: 'row', justifyContent: 'center', gap: 18 }}>
            <Pressable onPress={yukle} hitSlop={6}><Yazi boyut={12.5} agirlik="semibold" ton="formInk">Geri yükle</Yazi></Pressable>
            <Pressable onPress={() => WebBrowser.openBrowserAsync(LINKS.kosullar).catch(() => {})} hitSlop={6}><Yazi boyut={12.5} agirlik="semibold" ton="formInk">Koşullar</Yazi></Pressable>
            <Pressable onPress={() => WebBrowser.openBrowserAsync(LINKS.gizlilik).catch(() => {})} hitSlop={6}><Yazi boyut={12.5} agirlik="semibold" ton="formInk">Gizlilik</Yazi></Pressable>
          </View>
        </View>
      ) : null}
    </View>
  );
}
