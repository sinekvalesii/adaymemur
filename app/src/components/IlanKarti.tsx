import { Image } from 'expo-image';
import { memo } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { kalanMetin, sayi } from '../lib/format';
import type { Uygunluk } from '../lib/match';
import { EGITIM_ADI, ILLER, TUR_ADI } from '../lib/sabitler';
import type { Ilan } from '../lib/types';
import { font, radius, space, usePalette } from '../theme';
import { Ikon } from './Ikon';
import { dokun, Rozet, Yazi } from './ui';

export function kurumKisaltma(kurum: string) {
  const k = kurum.replace(/T\.C\.\s*/i, '').split(/\s+/).filter((w) => w.length > 2 && !/^(ve|ile|bakanlığı|genel|müdürlüğü)$/i.test(w));
  return (k[0]?.[0] ?? 'K').toLocaleUpperCase('tr') + (k[1]?.[0] ?? '').toLocaleUpperCase('tr');
}

export function KurumLogo({ ilan, boyut = 34 }: { ilan: Ilan; boyut?: number }) {
  const c = usePalette();
  return (
    <View style={{ width: boyut, height: boyut, borderRadius: 9, backgroundColor: c.form2, alignItems: 'center', justifyContent: 'center', overflow: 'hidden' }}>
      {ilan.logo ? (
        <Image source={{ uri: ilan.logo }} style={{ width: boyut - 6, height: boyut - 6 }} contentFit="contain" transition={150} />
      ) : (
        <Text style={{ fontFamily: font.bold, fontSize: boyut * 0.36, color: c.formInk }}>{kurumKisaltma(ilan.kurum || ilan.baslik)}</Text>
      )}
    </View>
  );
}

export function ilKonum(ilan: Ilan) {
  if (!ilan.iller.length) return 'Türkiye geneli';
  if (ilan.iller.length > 2) return `${ILLER[ilan.iller[0]]} +${ilan.iller.length - 1}`;
  return ilan.iller.map((i) => ILLER[i]).join(', ');
}

export function kpssEtiketi(ilan: Ilan): string | null {
  if (ilan.kpssGerekli === false) return 'KPSS şartsız';
  if (!ilan.kpss.length) return null;
  const k = ilan.kpss.slice(0, 2).map((x) => x.tur).join(' · ');
  const min = ilan.kpss.find((x) => x.min !== undefined)?.min;
  return min ? `${k} ≥ ${min}` : k + (ilan.kpss.length > 2 ? ' +' : '');
}

interface Props {
  ilan: Ilan;
  onPress: () => void;
  kayitli: boolean;
  onKaydet: () => void;
  yeni?: boolean;
  kilitDk?: number | null;
  uygunluk?: Uygunluk;
}

function IlanKartiTemel({ ilan, onPress, kayitli, onKaydet, yeni, kilitDk, uygunluk }: Props) {
  const c = usePalette();
  const kalan = kalanMetin(ilan.sonBasvuru);
  const tonRenk = { bad: c.bad, warn: c.warn, ok: c.ok, mute: c.ink3 }[kalan.ton];
  const kpss = kpssEtiketi(ilan);
  const egitim = ilan.egitim.filter((e) => e !== 'ilkogretim').slice(0, 3).map((e) => EGITIM_ADI[e]).join(' · ');

  return (
    <Pressable
      onPress={onPress}
      android_ripple={{ color: c.form2 }}
      style={({ pressed }) => [
        s.kart,
        { backgroundColor: c.sheet, borderColor: c.line },
        pressed && { opacity: 0.92 },
      ]}
    >
      <View style={s.ust}>
        <KurumLogo ilan={ilan} />
        <View style={{ flex: 1 }}>
          <Yazi boyut={13} agirlik="semibold" ton="ink2" satir={1}>{ilan.kurum || 'Kamu kurumu'}</Yazi>
          <Yazi boyut={12} ton="ink3" satir={1}>{ilKonum(ilan)}</Yazi>
        </View>
        {yeni ? <Rozet metin="Yeni" ton="marker" /> : null}
        <Pressable
          hitSlop={10}
          accessibilityLabel={kayitli ? 'Kaydedilenlerden çıkar' : 'Kaydet'}
          onPress={() => {
            dokun();
            onKaydet();
          }}
          style={{ padding: 4, marginRight: -4 }}
        >
          <Ikon ad={kayitli ? 'yerimiDolu' : 'yerimi'} boyut={22} renk={kayitli ? c.formInk : c.ink3} />
        </Pressable>
      </View>

      <Yazi boyut={16.5} agirlik="bold" satir={2} stil={{ marginTop: 10, letterSpacing: -0.15 }}>{ilan.baslik}</Yazi>

      <View style={s.rozetler}>
        <Rozet metin={TUR_ADI[ilan.tur]} />
        {ilan.kadro ? <Rozet metin={`${sayi(ilan.kadro)} kadro`} ton="mute" /> : null}
        {kpss ? <Rozet metin={kpss} ton="mute" /> : null}
        {egitim ? <Rozet metin={egitim} ton="mute" /> : null}
      </View>

      <View style={[s.alt, { borderTopColor: c.line }]}>
        <Ikon ad="saat" boyut={16} renk={tonRenk} />
        <Text style={{ fontFamily: font.semibold, fontSize: 13, color: tonRenk, flex: 1 }}>{kalan.metin}</Text>
        {uygunluk ? <UygunlukRozeti u={uygunluk} /> : null}
      </View>

      {kilitDk ? (
        <View style={[StyleSheet.absoluteFill, s.kilit, { backgroundColor: c.dark ? 'rgba(23,26,31,0.94)' : 'rgba(255,255,255,0.94)' }]}>
          <View style={{ width: 40, height: 40, borderRadius: 20, backgroundColor: c.marker, alignItems: 'center', justifyContent: 'center' }}>
            <Ikon ad="kilit" boyut={20} renk={c.onMarker} />
          </View>
          <Yazi agirlik="bold" stil={{ textAlign: 'center' }}>Yeni ilan — Pro üyelere açıldı</Yazi>
          <Yazi boyut={13} ton="ink2" stil={{ textAlign: 'center' }}>{kilitDk} dk sonra herkese açılacak</Yazi>
        </View>
      ) : null}
    </Pressable>
  );
}

export function UygunlukRozeti({ u }: { u: Uygunluk }) {
  if (u.durum === 'uygun') return <Rozet metin="Uygunsun" ton="ok" />;
  if (u.durum === 'kontrol') return <Rozet metin="Şartları kontrol et" ton="warn" />;
  return <Rozet metin="Uygun değil" ton="bad" />;
}

export const IlanKarti = memo(IlanKartiTemel);

const s = StyleSheet.create({
  kart: {
    borderRadius: radius.lg,
    borderWidth: StyleSheet.hairlineWidth,
    padding: space.lg,
    paddingBottom: 0,
    overflow: 'hidden',
  },
  ust: { flexDirection: 'row', alignItems: 'center', gap: 10 },
  rozetler: { flexDirection: 'row', flexWrap: 'wrap', gap: 6, marginTop: 10 },
  alt: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: 14,
    paddingVertical: 12,
    borderTopWidth: StyleSheet.hairlineWidth,
  },
  kilit: { alignItems: 'center', justifyContent: 'center', gap: 6, padding: space.lg, borderRadius: radius.lg },
});
