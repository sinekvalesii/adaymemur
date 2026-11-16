import { useState } from 'react';
import { View } from 'react-native';

import { EGITIM_ADI, IL_LISTESI, PUAN_TURLERI } from '../lib/sabitler';
import type { Egitim, Profil } from '../lib/types';
import { space } from '../theme';
import { Cip, Girdi, Yazi } from './ui';

const DUZEYLER: Egitim[] = ['ortaogretim', 'onlisans', 'lisans', 'yukseklisans', 'doktora'];

/** Bana Uygun profili: öğrenim, bölüm, KPSS puanları, iller. */
export function ProfilFormu({ deger, degistir }: { deger: Profil; degistir: (p: Profil) => void }) {
  const [ilAra, setIlAra] = useState('');
  const [ekPuan, setEkPuan] = useState('');
  const [ekDeger, setEkDeger] = useState('');
  const set = (p: Partial<Profil>) => degistir({ ...deger, ...p });

  const puanYaz = (kod: string, t: string) => {
    const n = parseFloat(t.replace(',', '.'));
    const puanlar = { ...deger.puanlar };
    if (Number.isFinite(n) && n > 0) puanlar[kod] = Math.min(100, Math.round(n * 1000) / 1000);
    else delete puanlar[kod];
    set({ puanlar });
  };

  const uygunTurler = PUAN_TURLERI.filter((p) => {
    if (!deger.egitim) return true;
    const sira = ['ortaogretim', 'onlisans', 'lisans', 'yukseklisans', 'doktora'];
    return sira.indexOf(p.egitim) <= sira.indexOf(deger.egitim);
  });
  const ekKodlar = Object.keys(deger.puanlar).filter((k) => !PUAN_TURLERI.some((p) => p.kod === k));
  const iller = IL_LISTESI.filter((i) => !ilAra || i.ad.toLocaleLowerCase('tr').includes(ilAra.toLocaleLowerCase('tr')));

  return (
    <View style={{ gap: space.xl }}>
      <View style={{ gap: space.sm }}>
        <Yazi boyut={16} agirlik="bold">En son mezuniyetin</Yazi>
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          {DUZEYLER.map((e) => (
            <Cip key={e} etiket={EGITIM_ADI[e]} secili={deger.egitim === e} onPress={() => set({ egitim: e })} />
          ))}
        </View>
      </View>

      {deger.egitim && deger.egitim !== 'ortaogretim' ? (
        <Girdi
          etiket="Bölümün"
          placeholder="Ör. Lojistik, İşletme, Bilgisayar Mühendisliği"
          value={deger.bolum}
          onChangeText={(bolum) => set({ bolum })}
          autoCapitalize="words"
          maxLength={60}
        />
      ) : null}

      <View style={{ gap: space.md }}>
        <View>
          <Yazi boyut={16} agirlik="bold">KPSS puanların</Yazi>
          <Yazi boyut={13} ton="ink3">Geçerli sınav puanlarını gir. Bilmiyorsan boş bırakabilirsin.</Yazi>
        </View>
        {uygunTurler.map((p) => (
          <Girdi
            key={p.kod}
            etiket={p.ad}
            placeholder="Ör. 74,512"
            keyboardType="decimal-pad"
            defaultValue={deger.puanlar[p.kod] !== undefined ? String(deger.puanlar[p.kod]).replace('.', ',') : ''}
            onChangeText={(t) => puanYaz(p.kod, t)}
          />
        ))}
        {ekKodlar.map((k) => (
          <Girdi
            key={k}
            etiket={`KPSS ${k}`}
            keyboardType="decimal-pad"
            defaultValue={String(deger.puanlar[k]).replace('.', ',')}
            onChangeText={(t) => puanYaz(k, t)}
          />
        ))}
        {deger.egitim === 'lisans' || deger.egitim === 'yukseklisans' || deger.egitim === 'doktora' ? (
          <View style={{ flexDirection: 'row', gap: 8, alignItems: 'flex-end' }}>
            <View style={{ flex: 1 }}>
              <Girdi etiket="A grubu alan puanı" placeholder="Tür, ör. P48" autoCapitalize="characters" value={ekPuan} onChangeText={setEkPuan} maxLength={6} />
            </View>
            <View style={{ flex: 1 }}>
              <Girdi placeholder="Puan" keyboardType="decimal-pad" value={ekDeger} onChangeText={setEkDeger} />
            </View>
            <View style={{ paddingBottom: 7 }}>
              <Cip
                etiket="Ekle"
                ikon="arti"
                onPress={() => {
                  const m = ekPuan.toUpperCase().replace(/\s|KPSS|-/g, '').match(/^P?(\d{1,3})$/);
                  const n = parseFloat(ekDeger.replace(',', '.'));
                  if (m && Number.isFinite(n) && n > 0) {
                    set({ puanlar: { ...deger.puanlar, [`P${Number(m[1])}`]: Math.min(100, n) } });
                    setEkPuan('');
                    setEkDeger('');
                  }
                }}
              />
            </View>
          </View>
        ) : null}
      </View>

      <View style={{ gap: space.sm }}>
        <View>
          <Yazi boyut={16} agirlik="bold">Çalışmak istediğin iller</Yazi>
          <Yazi boyut={13} ton="ink3">Boş bırakırsan tüm Türkiye’deki ilanlar değerlendirilir.</Yazi>
        </View>
        {deger.iller.length ? (
          <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
            {deger.iller.map((k) => (
              <Cip key={k} etiket={IL_LISTESI.find((i) => i.kod === k)!.ad} secili ikon="carpi" kucuk onPress={() => set({ iller: deger.iller.filter((x) => x !== k) })} />
            ))}
          </View>
        ) : null}
        <Girdi ikon="ara" placeholder="İl ara" value={ilAra} onChangeText={setIlAra} />
        <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
          {iller.slice(0, ilAra ? 81 : 18).map((i) => (
            <Cip
              key={i.kod}
              kucuk
              etiket={i.ad}
              secili={deger.iller.includes(i.kod)}
              onPress={() => set({ iller: deger.iller.includes(i.kod) ? deger.iller.filter((x) => x !== i.kod) : [...deger.iller, i.kod] })}
            />
          ))}
        </View>
      </View>
    </View>
  );
}
