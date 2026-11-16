import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { ScrollView, Switch, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { AltBaslik } from '../components/Ekran';
import { Buton, Cip, Girdi, Yazi } from '../components/ui';
import { useIlanlar } from '../lib/api';
import { filtrele } from '../lib/liste';
import { EGITIM_ADI, IL_LISTESI, TUR_ADI } from '../lib/sabitler';
import { filtreDeposu, profilDeposu, useDepo, VARSAYILAN_FILTRE } from '../lib/store';
import type { Egitim, Filtre, Tur } from '../lib/types';
import { space, usePalette } from '../theme';

const PUAN = ['P3', 'P93', 'P94'];

function Baslik({ metin, alt }: { metin: string; alt?: string }) {
  return (
    <View style={{ marginTop: space.xl, marginBottom: space.sm }}>
      <Yazi boyut={16} agirlik="bold">{metin}</Yazi>
      {alt ? <Yazi boyut={13} ton="ink3">{alt}</Yazi> : null}
    </View>
  );
}

export default function FiltreEkrani() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const mevcut = useDepo(filtreDeposu, (s) => s);
  const profil = useDepo(profilDeposu, (s) => s);
  const ilanlar = useIlanlar();
  const [f, setF] = useState<Filtre>(mevcut);
  const [ilAra, setIlAra] = useState('');
  const sonuc = useMemo(() => filtrele(ilanlar, f).length, [ilanlar, f]);

  const degistir = <K extends keyof Filtre>(k: K, v: Filtre[K]) => setF((x) => ({ ...x, [k]: v }));
  const toggle = <T,>(liste: T[], v: T) => (liste.includes(v) ? liste.filter((x) => x !== v) : [...liste, v]);

  const iller = IL_LISTESI.filter((i) => !ilAra || i.ad.toLocaleLowerCase('tr').includes(ilAra.toLocaleLowerCase('tr')));

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <AltBaslik
        baslik="Filtrele"
        kapat
        sag={<Buton etiket="Sıfırla" tur="sade" genis={false} onPress={() => setF({ ...VARSAYILAN_FILTRE, arama: f.arama })} stil={{ height: 40 }} />}
      />
      <ScrollView contentContainerStyle={{ paddingHorizontal: space.lg, paddingBottom: 140 }} keyboardShouldPersistTaps="handled">
        <Baslik metin="Sıralama" />
        <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap' }}>
          <Cip etiket="En yeni" secili={f.siralama === 'yeni'} onPress={() => degistir('siralama', 'yeni')} />
          <Cip etiket="Son başvurusu yakın" secili={f.siralama === 'sonGun'} onPress={() => degistir('siralama', 'sonGun')} />
          <Cip etiket="En çok kadro" secili={f.siralama === 'kadro'} onPress={() => degistir('siralama', 'kadro')} />
        </View>

        <Baslik metin="KPSS puanı" alt="Puan türünü seç; taban puanı senin puanından yüksek olanlar gizlenir." />
        <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap' }}>
          <Cip etiket="Hepsi" secili={!f.puanTuru} onPress={() => setF((x) => ({ ...x, puanTuru: null, minPuan: null }))} />
          {PUAN.map((p) => (
            <Cip
              key={p}
              etiket={`KPSS ${p}`}
              secili={f.puanTuru === p}
              onPress={() => setF((x) => ({ ...x, puanTuru: p, minPuan: x.minPuan ?? profil.puanlar[p] ?? null }))}
            />
          ))}
        </View>
        {f.puanTuru ? (
          <View style={{ marginTop: space.md }}>
            <Girdi
              etiket={`${f.puanTuru} puanın`}
              placeholder="Ör. 72,45"
              keyboardType="decimal-pad"
              value={f.minPuan !== null ? String(f.minPuan).replace('.', ',') : ''}
              onChangeText={(t) => {
                const n = parseFloat(t.replace(',', '.'));
                degistir('minPuan', Number.isFinite(n) ? Math.min(100, n) : null);
              }}
            />
          </View>
        ) : null}

        <Baslik metin="Kadro türü" />
        <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap' }}>
          {(Object.keys(TUR_ADI) as Tur[]).map((t) => (
            <Cip key={t} etiket={TUR_ADI[t]} secili={f.turler.includes(t)} onPress={() => degistir('turler', toggle(f.turler, t))} />
          ))}
        </View>

        <Baslik metin="Öğrenim düzeyi" />
        <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap' }}>
          {(['ortaogretim', 'onlisans', 'lisans', 'yukseklisans', 'doktora'] as Egitim[]).map((e) => (
            <Cip key={e} etiket={EGITIM_ADI[e]} secili={f.egitim.includes(e)} onPress={() => degistir('egitim', toggle(f.egitim, e))} />
          ))}
        </View>

        <Baslik metin="İl" alt="Türkiye geneli kadro açan ilanlar her zaman görünür." />
        {f.iller.length ? (
          <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap', marginBottom: space.md }}>
            {f.iller.map((k) => (
              <Cip key={k} etiket={IL_LISTESI.find((i) => i.kod === k)!.ad} secili ikon="carpi" kucuk onPress={() => degistir('iller', toggle(f.iller, k))} />
            ))}
          </View>
        ) : null}
        <Girdi ikon="ara" placeholder="İl ara" value={ilAra} onChangeText={setIlAra} />
        <View style={{ flexDirection: 'row', gap: 8, flexWrap: 'wrap', marginTop: space.md }}>
          {iller.slice(0, ilAra ? 81 : 24).map((i) => (
            <Cip key={i.kod} etiket={i.ad} kucuk secili={f.iller.includes(i.kod)} onPress={() => degistir('iller', toggle(f.iller, i.kod))} />
          ))}
        </View>

        <View style={{ flexDirection: 'row', alignItems: 'center', marginTop: space.xl, gap: 12 }}>
          <View style={{ flex: 1 }}>
            <Yazi agirlik="semibold">Süresi dolan ilanları göster</Yazi>
            <Yazi boyut={13} ton="ink3">Son 30 güne ait arşiv</Yazi>
          </View>
          <Switch
            value={f.suresiGecenleriGoster}
            onValueChange={(v) => degistir('suresiGecenleriGoster', v)}
            trackColor={{ true: c.formInk, false: c.form }}
            thumbColor={c.sheet}
          />
        </View>
      </ScrollView>
      <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, padding: space.lg, paddingBottom: ins.bottom + space.lg, backgroundColor: c.paper, borderTopWidth: 1, borderTopColor: c.line }}>
        <Buton
          etiket={`${sonuc} ilanı göster`}
          onPress={() => {
            filtreDeposu.set(f);
            router.back();
          }}
        />
      </View>
    </View>
  );
}
