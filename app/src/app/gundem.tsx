import * as WebBrowser from 'expo-web-browser';
import { useMemo } from 'react';
import { Pressable, ScrollView, View } from 'react-native';

import { AltBaslik } from '../components/Ekran';
import { Ikon } from '../components/Ikon';
import { Kart, Rozet, Yazi } from '../components/ui';
import { useVeri } from '../lib/api';
import { gunAdi, kalanGun, tarihKisa, tarihUzun } from '../lib/format';
import type { TakvimOgesi } from '../lib/types';
import { space, usePalette } from '../theme';

const TIP: Record<TakvimOgesi['tip'], { ad: string; ton: 'form' | 'marker' | 'ok' | 'warn' }> = {
  basvuru: { ad: 'Başvuru', ton: 'form' },
  sinav: { ad: 'Sınav', ton: 'marker' },
  sonuc: { ad: 'Sonuç', ton: 'ok' },
  tercih: { ad: 'Tercih', ton: 'warn' },
};

export default function Gundem() {
  const c = usePalette();
  const takvim = useVeri((v) => v.takvim);
  const duyurular = useVeri((v) => v.duyurular);
  const [yaklasan, gecmis] = useMemo(() => {
    const o = [...(takvim?.ogeler ?? [])].sort((a, b) => a.tarih.localeCompare(b.tarih));
    return [o.filter((x) => (kalanGun(x.bitis ?? x.tarih) ?? -1) >= 0), o.filter((x) => (kalanGun(x.bitis ?? x.tarih) ?? -1) < 0).reverse()];
  }, [takvim]);

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <AltBaslik baslik="KPSS gündemi" />
      <ScrollView contentContainerStyle={{ padding: space.lg, paddingBottom: 40, gap: space.md }}>
        {duyurular.length ? (
          <>
            <Yazi boyut={12} agirlik="bold" ton="ink3" stil={{ letterSpacing: 0.9 }}>DUYURULAR</Yazi>
            {duyurular.map((d) => (
              <Kart key={d.id} onPress={d.url ? () => WebBrowser.openBrowserAsync(d.url!).catch(() => {}) : undefined} stil={{ gap: 6, borderColor: d.vurgulu ? c.marker : c.line, borderWidth: d.vurgulu ? 2 : undefined }}>
                <Yazi boyut={12.5} ton="ink3">{tarihUzun(d.tarih)}</Yazi>
                <Yazi agirlik="bold" boyut={16}>{d.baslik}</Yazi>
                <Yazi boyut={14} ton="ink2">{d.metin}</Yazi>
                {d.url ? (
                  <View style={{ flexDirection: 'row', gap: 6, alignItems: 'center', marginTop: 2 }}>
                    <Ikon ad="dis" boyut={15} renk={c.formInk} />
                    <Yazi boyut={13.5} agirlik="semibold" ton="formInk">Kaynağı aç</Yazi>
                  </View>
                ) : null}
              </Kart>
            ))}
          </>
        ) : null}

        <Yazi boyut={12} agirlik="bold" ton="ink3" stil={{ letterSpacing: 0.9, marginTop: space.md }}>YAKLAŞAN TARİHLER</Yazi>
        {yaklasan.length ? (
          <Kart dolgu={0}>
            {yaklasan.map((o, i) => {
              const k = kalanGun(o.tarih) ?? 0;
              return (
                <View key={o.id} style={{ flexDirection: 'row', gap: 14, padding: space.lg, borderTopWidth: i ? 1 : 0, borderTopColor: c.line }}>
                  <View style={{ width: 52, alignItems: 'center' }}>
                    <Yazi boyut={20} agirlik="heavy" stil={{ lineHeight: 24 }}>{tarihKisa(o.tarih).split(' ')[0]}</Yazi>
                    <Yazi boyut={12} agirlik="semibold" ton="ink3">{tarihKisa(o.tarih).split(' ')[1]}</Yazi>
                  </View>
                  <View style={{ flex: 1, gap: 4 }}>
                    <View style={{ flexDirection: 'row', gap: 6, alignItems: 'center' }}>
                      <Rozet metin={TIP[o.tip].ad} ton={TIP[o.tip].ton} />
                      <Yazi boyut={12.5} ton="ink3">{o.sinav}</Yazi>
                    </View>
                    <Yazi agirlik="bold">{o.baslik}</Yazi>
                    <Yazi boyut={13} ton="ink2">
                      {o.bitis ? `${tarihUzun(o.tarih)} – ${tarihUzun(o.bitis)}` : `${gunAdi(o.tarih)}`}
                      {k > 0 ? ` · ${k} gün kaldı` : k === 0 ? ' · bugün' : ' · devam ediyor'}
                    </Yazi>
                    {o.not ? <Yazi boyut={12.5} ton="ink3">{o.not}</Yazi> : null}
                  </View>
                </View>
              );
            })}
          </Kart>
        ) : (
          <Yazi ton="ink3">Takvim yükleniyor ya da yaklaşan tarih yok.</Yazi>
        )}

        {gecmis.length ? (
          <>
            <Yazi boyut={12} agirlik="bold" ton="ink3" stil={{ letterSpacing: 0.9, marginTop: space.md }}>GEÇMİŞ</Yazi>
            {gecmis.slice(0, 6).map((o) => (
              <Yazi key={o.id} boyut={13.5} ton="ink3">{tarihUzun(o.tarih)} · {o.baslik}</Yazi>
            ))}
          </>
        ) : null}

        {takvim ? (
          <Pressable onPress={() => WebBrowser.openBrowserAsync(takvim.kaynakUrl).catch(() => {})} style={{ marginTop: space.lg, gap: 4 }}>
            <Yazi boyut={12.5} ton="ink3">{takvim.uyari}</Yazi>
            <Yazi boyut={13} agirlik="semibold" ton="formInk">Kaynak: {takvim.kaynak}</Yazi>
          </Pressable>
        ) : null}
      </ScrollView>
    </View>
  );
}
