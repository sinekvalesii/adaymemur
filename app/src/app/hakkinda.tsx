import * as WebBrowser from 'expo-web-browser';
import { ScrollView, View } from 'react-native';

import { AltBaslik } from '../components/Ekran';
import { Isaret } from '../components/Isaret';
import { Grup, Satir, Yazi } from '../components/ui';
import { useVeri } from '../lib/api';
import { goreli } from '../lib/format';
import { MAKER } from '../lib/sabitler';
import { space, usePalette } from '../theme';

const KAYNAKLAR = [
  { ad: 'Cumhurbaşkanlığı Kariyer Kapısı', alt: 'kariyerkapisi.gov.tr — kamu işe alım ilanları (resmî RSS)', url: 'https://kariyerkapisi.gov.tr/isealim' },
  { ad: 'Basın İlan Kurumu', alt: 'ilan.gov.tr — personel alımı ilanları', url: 'https://www.ilan.gov.tr' },
  { ad: 'Türkiye İş Kurumu', alt: 'esube.iskur.gov.tr — kamu işyeri iş ilanları', url: 'https://esube.iskur.gov.tr' },
  { ad: 'ÖSYM', alt: 'osym.gov.tr — sınav takvimi', url: 'https://www.osym.gov.tr' },
];

export default function Hakkinda() {
  const c = usePalette();
  const guncelleme = useVeri((v) => v.guncelleme);
  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <AltBaslik baslik="Veri kaynakları ve sorumluluk" />
      <ScrollView contentContainerStyle={{ paddingBottom: 40 }}>
        <View style={{ alignItems: 'center', paddingVertical: space.xl, gap: 10 }}>
          <Isaret boyut={56} />
          <Yazi boyut={20} agirlik="heavy">Aday Memur</Yazi>
          <Yazi boyut={13} ton="ink3">{guncelleme ? `Veriler ${goreli(guncelleme)} güncellendi` : 'Kamu ilanları takip uygulaması'}</Yazi>
        </View>
        <Grup baslik="Bilgilerin kaynağı" alt="Sosyal medya, haber siteleri ve forumlar kaynak olarak kullanılmaz. İlanlar yaklaşık 30 dakikada bir resmî kaynaklardan otomatik derlenir.">
          {KAYNAKLAR.map((k) => (
            <Satir key={k.ad} ikon="dis" baslik={k.ad} alt={k.alt} onPress={() => WebBrowser.openBrowserAsync(k.url).catch(() => {})} />
          ))}
        </Grup>
        <View style={{ padding: space.lg, marginTop: space.md, gap: 10 }}>
          <Yazi boyut={16} agirlik="bold">Sorumluluk reddi</Yazi>
          <Yazi ton="ink2" boyut={14.5}>
            Aday Memur bağımsız bir uygulamadır; T.C. Cumhurbaşkanlığı, ÖSYM, İŞKUR, Basın İlan Kurumu veya başka bir kamu kurumuyla bağlantısı, ortaklığı ya da yetkilendirmesi yoktur. Uygulama başvuru almaz ve hiçbir kurum adına işlem yapmaz.
          </Yazi>
          <Yazi ton="ink2" boyut={14.5}>
            Kadro, puan türü, taban puan ve tarih gibi özet bilgiler ilan metinlerinden otomatik olarak çıkarılır ve hata içerebilir. Başvuru öncesinde şartları daima kurumun resmî ilan metninden doğrula.
          </Yazi>
          <Yazi ton="ink3" boyut={13} stil={{ marginTop: space.md }}>Yapım: {MAKER}</Yazi>
        </View>
      </ScrollView>
    </View>
  );
}
