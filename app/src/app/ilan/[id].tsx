import { createEventInCalendarAsync } from 'expo-calendar/legacy';
import { router, useLocalSearchParams } from 'expo-router';
import * as WebBrowser from 'expo-web-browser';
import { useEffect, useMemo, useState } from 'react';
import { ActivityIndicator, Platform, Pressable, ScrollView, Share, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { bildir } from '../../components/Bildirim';
import { AltBaslik } from '../../components/Ekran';
import { Ikon, type IkonAdi } from '../../components/Ikon';
import { ilKonum, KurumLogo, kpssEtiketi } from '../../components/IlanKarti';
import { Buton, Cip, Fosforlu, Kart, Rozet, Yazi } from '../../components/ui';
import { ReklamAlani } from '../../lib/ads';
import { ilanMetni, useIlan, useVeri } from '../../lib/api';
import { gun, kalanGun, kalanMetin, sayi, tarihUzun } from '../../lib/format';
import { kaydetDegistir, useKayitli } from '../../lib/kayit';
import { uygunluk } from '../../lib/match';
import { proDeposu } from '../../lib/pro';
import { EGITIM_ADI, KAYNAK_ADI, TUR_ADI } from '../../lib/sabitler';
import { ayarDeposu, kayitDeposu, profilDeposu, useDepo } from '../../lib/store';
import { radius, space, usePalette } from '../../theme';

const BOLUM_METNI = { yok: 'Bölüm şartı yok', var: 'Belirli bölümler', bilinmiyor: 'İlan metninde' } as const;

function Bilgi({ ikon, etiket, deger }: { ikon: IkonAdi; etiket: string; deger: string }) {
  const c = usePalette();
  return (
    <View style={{ width: '50%', paddingVertical: 10, paddingRight: 10, flexDirection: 'row', gap: 10 }}>
      <Ikon ad={ikon} boyut={18} renk={c.ink3} />
      <View style={{ flex: 1 }}>
        <Yazi boyut={12} ton="ink3">{etiket}</Yazi>
        <Yazi boyut={14.5} agirlik="semibold">{deger}</Yazi>
      </View>
    </View>
  );
}

export default function IlanDetay() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const { id } = useLocalSearchParams<{ id: string }>();
  const canli = useIlan(id);
  const kayitliKopya = useDepo(kayitDeposu, (s) => s.kayitlar[id]?.ilan);
  const ilan = canli ?? kayitliKopya;
  const durum = useVeri((v) => v.durum);
  const kayitli = useKayitli(id);
  const pro = useDepo(proDeposu, (s) => s.pro);
  const profil = useDepo(profilDeposu, (s) => s);
  const [metin, setMetin] = useState<string | null | undefined>(undefined);
  const [acik, setAcik] = useState(false);

  useEffect(() => {
    if (!id) return;
    ayarDeposu.set((s) => ({ goruldu: { ...s.goruldu, [id]: 1 } }));
  }, [id]);

  useEffect(() => {
    if (!ilan) return;
    let iptal = false;
    ilanMetni(ilan).then((m) => !iptal && setMetin(m));
    return () => {
      iptal = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ilan?.id]);

  const u = useMemo(() => (ilan && profil.tamamlandi ? uygunluk(ilan, profil, metin ?? undefined) : null), [ilan, profil, metin]);

  if (!ilan) {
    return (
      <View style={{ flex: 1, backgroundColor: c.paper }}>
        <AltBaslik />
        {durum === 'hazir' ? (
          <View style={{ padding: space.xl, gap: 12 }}>
            <Yazi boyut={18} agirlik="bold">İlan yayından kalktı</Yazi>
            <Yazi ton="ink2">Bu ilanın başvuru süresi dolmuş ya da kaynak tarafından kaldırılmış olabilir.</Yazi>
            <Buton etiket="İlanlara dön" tur="ikincil" onPress={() => router.replace('/')} />
          </View>
        ) : (
          <ActivityIndicator style={{ marginTop: 60 }} color={c.ink2} />
        )}
      </View>
    );
  }

  const kalan = kalanMetin(ilan.sonBasvuru);
  const k = kalanGun(ilan.sonBasvuru);
  const tonRenk = { bad: c.bad, warn: c.warn, ok: c.ok, mute: c.ink3 }[kalan.ton];
  const kpss = kpssEtiketi(ilan);
  const egitim = ilan.egitim.map((e) => EGITIM_ADI[e]).join(', ');
  const resmiAc = (url: string) =>
    WebBrowser.openBrowserAsync(url, { toolbarColor: c.paper, controlsColor: c.formInk, showTitle: true }).catch(() => {});

  const takvimeEkle = async () => {
    if (!ilan.sonBasvuru) return;
    try {
      const bas = gun(ilan.sonBasvuru);
      bas.setHours(9, 0, 0, 0);
      const bit = new Date(bas);
      bit.setHours(23, 59, 0, 0);
      await createEventInCalendarAsync({
        title: `Son başvuru: ${ilan.kurum || ilan.baslik}`,
        startDate: bas,
        endDate: bit,
        notes: `${ilan.baslik}\n\nResmî ilan: ${ilan.url}\n\nAday Memur`,
        alarms: [{ relativeOffset: -24 * 60 }, { relativeOffset: -3 * 60 }],
      });
    } catch {
      bildir('Takvim açılamadı', 'Cihazında bir takvim uygulaması olduğundan emin ol.', { ikon: 'takvim' });
    }
  };

  const paylas = () =>
    Share.share({
      message: `${ilan.kurum ? ilan.kurum + ' — ' : ''}${ilan.baslik}\n${ilan.sonBasvuru ? `Son başvuru: ${tarihUzun(ilan.sonBasvuru)}\n` : ''}${ilan.url}`,
    }).catch(() => {});

  const metinGoster = metin && !acik && metin.length > 1400 ? metin.slice(0, 1400).replace(/\s+\S*$/, '') + '…' : metin;

  return (
    <View style={{ flex: 1, backgroundColor: c.paper }}>
      <AltBaslik
        sag={
          <View style={{ flexDirection: 'row' }}>
            <Pressable accessibilityLabel="Paylaş" hitSlop={8} onPress={paylas} style={{ padding: 10 }}>
              <Ikon ad="paylas" renk={c.ink} />
            </Pressable>
            <Pressable accessibilityLabel="Kaydet" hitSlop={8} onPress={() => kaydetDegistir(ilan)} style={{ padding: 10 }}>
              <Ikon ad={kayitli ? 'yerimiDolu' : 'yerimi'} renk={kayitli ? c.formInk : c.ink} />
            </Pressable>
          </View>
        }
      />
      <ScrollView contentContainerStyle={{ paddingHorizontal: space.lg, paddingBottom: ins.bottom + 110 }}>
        <View style={{ flexDirection: 'row', alignItems: 'center', gap: 12, marginTop: 4 }}>
          <KurumLogo ilan={ilan} boyut={44} />
          <View style={{ flex: 1 }}>
            <Yazi boyut={14} agirlik="semibold" ton="ink2">{ilan.kurum || 'Kamu kurumu'}</Yazi>
            <Yazi boyut={12.5} ton="ink3">{KAYNAK_ADI[ilan.kaynak]}{ilan.yayin ? ` · ${tarihUzun(ilan.yayin)}` : ''}</Yazi>
          </View>
        </View>
        <Yazi boyut={24} agirlik="heavy" stil={{ marginTop: space.lg, letterSpacing: -0.5, lineHeight: 30 }} secilebilir>
          {ilan.baslik}
        </Yazi>
        <View style={{ flexDirection: 'row', gap: 6, marginTop: space.md, flexWrap: 'wrap' }}>
          <Rozet metin={TUR_ADI[ilan.tur]} />
          {ilan.kadro ? <Rozet metin={`${sayi(ilan.kadro)} kadro`} ton="mute" /> : null}
        </View>

        {/* Son başvuru */}
        <Kart stil={{ marginTop: space.xl, flexDirection: 'row', alignItems: 'center', gap: 14 }}>
          <View style={{ width: 62, height: 62, borderRadius: 14, backgroundColor: k !== null && k >= 0 ? c.marker : c.form2, alignItems: 'center', justifyContent: 'center' }}>
            {k !== null && k >= 0 ? (
              <>
                <Yazi boyut={24} agirlik="heavy" ton="onMarker" stil={{ lineHeight: 28 }}>{k}</Yazi>
                <Yazi boyut={11} agirlik="semibold" ton="onMarker">gün</Yazi>
              </>
            ) : (
              <Ikon ad="takvim" renk={c.ink3} />
            )}
          </View>
          <View style={{ flex: 1 }}>
            <Yazi boyut={12.5} ton="ink3">Son başvuru</Yazi>
            <Yazi boyut={16.5} agirlik="bold">{ilan.sonBasvuru ? tarihUzun(ilan.sonBasvuru) : 'İlan metninde belirtilmiş'}</Yazi>
            {ilan.sonBasvuru ? (
              <Yazi boyut={13} agirlik="semibold" stil={{ color: tonRenk }}>
                {ilan.basvuruBaslangic ? `${tarihUzun(ilan.basvuruBaslangic)} başladı · ` : ''}
                {kalan.metin}
              </Yazi>
            ) : null}
          </View>
        </Kart>
        {ilan.sonBasvuru && k !== null && k >= 0 && Platform.OS !== 'web' ? (
          <Buton etiket="Takvime ekle" ikon="takvim" tur="ikincil" onPress={takvimeEkle} stil={{ marginTop: space.sm }} />
        ) : null}

        {/* Özet */}
        <Kart stil={{ marginTop: space.md, flexDirection: 'row', flexWrap: 'wrap', paddingVertical: 8 }}>
          <Bilgi ikon="okul" etiket="Öğrenim" deger={egitim || 'İlan metninde'} />
          <Bilgi ikon="grafik" etiket="KPSS" deger={kpss ?? (ilan.kpssGerekli ? 'İstenir' : 'İlan metninde')} />
          <Bilgi ikon="belge" etiket="Bölüm" deger={BOLUM_METNI[ilan.bolumSarti]} />
          <Bilgi ikon="konum" etiket="Kadro yeri" deger={ilKonum(ilan)} />
        </Kart>

        {/* Uygunluk (Pro) */}
        <View style={{ marginTop: space.xl }}>
          <Fosforlu boyut={18}>Bu ilan sana uygun mu?</Fosforlu>
        </View>
        {!pro ? (
          <Kart onPress={() => router.push('/pro')} stil={{ marginTop: space.md, gap: 8 }}>
            <View style={{ flexDirection: 'row', gap: 10, alignItems: 'center' }}>
              <Ikon ad="kilit" renk={c.formInk} />
              <Yazi agirlik="bold" stil={{ flex: 1 }}>Pro ile anında öğren</Yazi>
              <Ikon ad="sag" boyut={18} renk={c.ink3} />
            </View>
            <Yazi boyut={14} ton="ink2">Öğrenim düzeyin, bölümün, KPSS puanın ve il tercihin bu ilanın şartlarıyla tek tek karşılaştırılır.</Yazi>
          </Kart>
        ) : !profil.tamamlandi ? (
          <Kart onPress={() => router.push('/profil')} stil={{ marginTop: space.md, gap: 6 }}>
            <Yazi agirlik="bold">Önce profilini tamamla</Yazi>
            <Yazi boyut={14} ton="ink2">Öğrenim, bölüm ve KPSS puanını gir; uygunluk hesaplansın.</Yazi>
          </Kart>
        ) : u ? (
          <Kart stil={{ marginTop: space.md, gap: 12 }}>
            <View style={{ flexDirection: 'row', alignItems: 'center', gap: 8 }}>
              <Rozet metin={u.durum === 'uygun' ? 'Başvurabilirsin' : u.durum === 'kontrol' ? 'Şartları kontrol et' : 'Uygun görünmüyor'} ton={u.durum === 'uygun' ? 'ok' : u.durum === 'kontrol' ? 'warn' : 'bad'} />
            </View>
            {u.gerekceler.map((g, i) => (
              <View key={i} style={{ flexDirection: 'row', gap: 10, alignItems: 'flex-start' }}>
                <View style={{ width: 22, height: 22, borderRadius: 11, alignItems: 'center', justifyContent: 'center', backgroundColor: g.sonuc === 'evet' ? c.okBg : g.sonuc === 'hayir' ? c.badBg : c.warnBg }}>
                  <Ikon ad={g.sonuc === 'evet' ? 'tik' : g.sonuc === 'hayir' ? 'carpi' : 'bilgi'} boyut={14} kalinlik={2.2} renk={g.sonuc === 'evet' ? c.ok : g.sonuc === 'hayir' ? c.bad : c.warn} />
                </View>
                <Yazi boyut={14.5} stil={{ flex: 1 }}>{g.metin}</Yazi>
              </View>
            ))}
            <Yazi boyut={12} ton="ink3">Otomatik karşılaştırmadır; kesin şartlar için resmî ilan metnini esas al.</Yazi>
          </Kart>
        ) : null}

        {ilan.unvanlar.length ? (
          <>
            <Yazi boyut={16} agirlik="bold" stil={{ marginTop: space.xl, marginBottom: space.sm }}>Kadrolar</Yazi>
            <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: 8 }}>
              {ilan.unvanlar.map((u2) => (
                <Cip key={u2} etiket={u2} kucuk />
              ))}
            </View>
          </>
        ) : null}

        {/* İlan metni */}
        <Yazi boyut={16} agirlik="bold" stil={{ marginTop: space.xl, marginBottom: space.sm }}>İlan metni</Yazi>
        <Kart>
          {metin === undefined ? (
            <View style={{ flexDirection: 'row', gap: 10, alignItems: 'center' }}>
              <ActivityIndicator color={c.ink3} />
              <Yazi ton="ink3">Resmî metin yükleniyor…</Yazi>
            </View>
          ) : metin ? (
            <>
              <Yazi boyut={14.5} ton="ink2" secilebilir stil={{ lineHeight: 22 }}>{metinGoster}</Yazi>
              {metin.length > 1400 ? (
                <Pressable onPress={() => setAcik((x) => !x)} style={{ marginTop: 10 }}>
                  <Yazi agirlik="bold" ton="formInk">{acik ? 'Daha az göster' : 'Devamını oku'}</Yazi>
                </Pressable>
              ) : null}
            </>
          ) : (
            <Yazi ton="ink2">{ilan.ozet || 'Bu ilanın tam metni kurumun resmî sayfasında yayımlanıyor.'}</Yazi>
          )}
        </Kart>

        {ilan.kaynaklar.length > 1 ? (
          <View style={{ marginTop: space.md, gap: 8 }}>
            <Yazi boyut={13} ton="ink3">Bu ilan şu kaynaklarda da yayımlandı:</Yazi>
            {ilan.kaynaklar.slice(1).map((k2) => (
              <Pressable key={k2.url} onPress={() => resmiAc(k2.url)} style={{ flexDirection: 'row', gap: 8, alignItems: 'center' }}>
                <Ikon ad="dis" boyut={16} renk={c.formInk} />
                <Yazi boyut={14} agirlik="semibold" ton="formInk">{KAYNAK_ADI[k2.kaynak]}</Yazi>
              </Pressable>
            ))}
          </View>
        ) : null}

        <View style={{ marginTop: space.xl, flexDirection: 'row', gap: 10, padding: space.md, borderRadius: radius.md, backgroundColor: c.form2 }}>
          <Ikon ad="kalkan" boyut={18} renk={c.formInk} />
          <Yazi boyut={12.5} ton="ink2" stil={{ flex: 1 }}>
            Aday Memur bağımsız bir uygulamadır, hiçbir kamu kurumuyla bağlantısı yoktur. Başvurular yalnızca kurumun resmî kanallarından yapılır; şartlar için resmî ilan metnini esas al.
          </Yazi>
        </View>
        <ReklamAlani />
      </ScrollView>

      <View style={{ position: 'absolute', left: 0, right: 0, bottom: 0, paddingHorizontal: space.lg, paddingTop: space.md, paddingBottom: ins.bottom + space.md, backgroundColor: c.paper, borderTopWidth: 1, borderTopColor: c.line }}>
        <Buton etiket="Resmî ilana git" ikon="dis" onPress={() => resmiAc(ilan.url)} />
      </View>
    </View>
  );
}
