import * as Haptics from 'expo-haptics';
import { type ReactNode } from 'react';
import {
  ActivityIndicator,
  Platform,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  View,
  type StyleProp,
  type TextInputProps,
  type TextStyle,
  type ViewStyle,
} from 'react-native';

import { font, radius, space, usePalette, type Palette } from '../theme';
import { Ikon, type IkonAdi } from './Ikon';

export const dokun = () => {
  if (Platform.OS !== 'web') Haptics.selectionAsync().catch(() => {});
};

// --- Yazı -----------------------------------------------------------------------
type Ton = 'ink' | 'ink2' | 'ink3' | 'formInk' | 'ok' | 'bad' | 'warn' | 'onMarker';

export function Yazi({
  children,
  boyut = 15,
  agirlik = 'regular',
  ton = 'ink',
  stil,
  satir,
  secilebilir,
}: {
  children: ReactNode;
  boyut?: number;
  agirlik?: keyof typeof font;
  ton?: Ton;
  stil?: StyleProp<TextStyle>;
  satir?: number;
  secilebilir?: boolean;
}) {
  const c = usePalette();
  return (
    <Text
      numberOfLines={satir}
      selectable={secilebilir}
      style={[{ fontFamily: font[agirlik], fontSize: boyut, color: c[ton], lineHeight: Math.round(boyut * 1.38) }, stil]}
    >
      {children}
    </Text>
  );
}

/** Fosforlu kalem vurgusu: metnin alt yarısının arkasında sarı şerit. */
export function Fosforlu({ children, boyut = 15, agirlik = 'bold' as keyof typeof font }: { children: ReactNode; boyut?: number; agirlik?: keyof typeof font }) {
  const c = usePalette();
  return (
    <View style={{ alignSelf: 'flex-start' }}>
      <View
        style={{
          position: 'absolute',
          left: -3,
          right: -3,
          bottom: boyut * 0.08,
          height: boyut * 0.58,
          backgroundColor: c.marker,
          borderRadius: 3,
          transform: [{ rotate: '-0.6deg' }],
        }}
      />
      <Text style={{ fontFamily: font[agirlik], fontSize: boyut, color: c.dark ? c.ink : c.onMarker, lineHeight: Math.round(boyut * 1.3) }}>{children}</Text>
    </View>
  );
}

// --- Kart / yüzey ---------------------------------------------------------------
export function Kart({ children, stil, onPress, dolgu = space.lg }: { children: ReactNode; stil?: StyleProp<ViewStyle>; onPress?: () => void; dolgu?: number }) {
  const c = usePalette();
  const base: ViewStyle = {
    backgroundColor: c.sheet,
    borderRadius: radius.lg,
    padding: dolgu,
    borderWidth: StyleSheet.hairlineWidth,
    borderColor: c.line,
  };
  if (!onPress) return <View style={[base, stil]}>{children}</View>;
  return (
    <Pressable
      onPress={onPress}
      android_ripple={{ color: c.form2, borderless: false }}
      style={({ pressed }) => [base, stil, pressed && Platform.OS !== 'android' && { opacity: 0.85 }]}
    >
      {children}
    </Pressable>
  );
}

export function Ayrac({ bosluk = 0 }: { bosluk?: number }) {
  const c = usePalette();
  return <View style={{ height: StyleSheet.hairlineWidth, backgroundColor: c.line, marginVertical: bosluk }} />;
}

// --- Çip / rozet ----------------------------------------------------------------
export function Cip({
  etiket,
  secili,
  onPress,
  ikon,
  kucuk,
}: {
  etiket: string;
  secili?: boolean;
  onPress?: () => void;
  ikon?: IkonAdi;
  kucuk?: boolean;
}) {
  const c = usePalette();
  return (
    <Pressable
      onPress={
        onPress
          ? () => {
              dokun();
              onPress();
            }
          : undefined
      }
      hitSlop={4}
      style={{
        flexDirection: 'row',
        alignItems: 'center',
        gap: 6,
        paddingHorizontal: kucuk ? 10 : 14,
        height: kucuk ? 30 : 36,
        borderRadius: radius.pill,
        backgroundColor: secili ? c.ink : c.sheet,
        borderWidth: 1,
        borderColor: secili ? c.ink : c.line,
      }}
    >
      {ikon ? <Ikon ad={ikon} boyut={16} renk={secili ? c.paper : c.ink2} /> : null}
      <Text style={{ fontFamily: font.semibold, fontSize: kucuk ? 13 : 14, color: secili ? c.paper : c.ink }}>{etiket}</Text>
    </Pressable>
  );
}

export function Rozet({ metin, ton = 'form' }: { metin: string; ton?: 'form' | 'ok' | 'bad' | 'warn' | 'marker' | 'mute' }) {
  const c = usePalette();
  const m: Record<string, [string, string]> = {
    form: [c.form2, c.formInk],
    ok: [c.okBg, c.ok],
    bad: [c.badBg, c.bad],
    warn: [c.warnBg, c.warn],
    marker: [c.marker, c.onMarker],
    mute: [c.form2, c.ink3],
  };
  const [bg, fg] = m[ton];
  return (
    <View style={{ backgroundColor: bg, paddingHorizontal: 8, paddingVertical: 3, borderRadius: 6, alignSelf: 'flex-start' }}>
      <Text style={{ fontFamily: font.semibold, fontSize: 12, color: fg, letterSpacing: 0.1 }}>{metin}</Text>
    </View>
  );
}

// --- Buton ----------------------------------------------------------------------
export function Buton({
  etiket,
  onPress,
  tur = 'ana',
  ikon,
  yukleniyor,
  pasif,
  stil,
  genis = true,
}: {
  etiket: string;
  onPress: () => void;
  tur?: 'ana' | 'ikincil' | 'sade' | 'tehlike';
  ikon?: IkonAdi;
  yukleniyor?: boolean;
  pasif?: boolean;
  stil?: StyleProp<ViewStyle>;
  genis?: boolean;
}) {
  const c = usePalette();
  const renk: Record<string, { bg: string; fg: string; br: string }> = {
    ana: { bg: c.marker, fg: c.onMarker, br: c.marker },
    ikincil: { bg: c.sheet, fg: c.ink, br: c.line },
    sade: { bg: 'transparent', fg: c.formInk, br: 'transparent' },
    tehlike: { bg: c.badBg, fg: c.bad, br: c.badBg },
  };
  const r = renk[tur];
  const kapali = pasif || yukleniyor;
  return (
    <Pressable
      onPress={() => {
        if (kapali) return;
        dokun();
        onPress();
      }}
      style={({ pressed }) => [
        {
          height: 52,
          borderRadius: radius.md,
          backgroundColor: r.bg,
          borderWidth: 1,
          borderColor: r.br,
          alignItems: 'center',
          justifyContent: 'center',
          flexDirection: 'row',
          gap: 8,
          paddingHorizontal: space.lg,
          opacity: kapali ? 0.55 : pressed ? 0.88 : 1,
          alignSelf: genis ? 'stretch' : 'flex-start',
          transform: [{ scale: pressed && !kapali ? 0.985 : 1 }],
        },
        stil,
      ]}
    >
      {yukleniyor ? <ActivityIndicator color={r.fg} /> : ikon ? <Ikon ad={ikon} boyut={20} renk={r.fg} /> : null}
      <Text style={{ fontFamily: font.bold, fontSize: 16, color: r.fg }}>{etiket}</Text>
    </Pressable>
  );
}

export function IkonButon({ ad, onPress, etiket, renk }: { ad: IkonAdi; onPress: () => void; etiket: string; renk?: string }) {
  const c = usePalette();
  return (
    <Pressable
      accessibilityLabel={etiket}
      accessibilityRole="button"
      hitSlop={8}
      onPress={() => {
        dokun();
        onPress();
      }}
      style={({ pressed }) => ({
        width: 42,
        height: 42,
        borderRadius: 21,
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: pressed ? c.form2 : 'transparent',
      })}
    >
      <Ikon ad={ad} renk={renk ?? c.ink} />
    </Pressable>
  );
}

// --- Liste satırı ---------------------------------------------------------------
export function Satir({
  ikon,
  baslik,
  alt,
  sag,
  onPress,
  tehlike,
}: {
  ikon?: IkonAdi;
  baslik: string;
  alt?: string;
  sag?: ReactNode;
  onPress?: () => void;
  tehlike?: boolean;
}) {
  const c = usePalette();
  return (
    <Pressable
      onPress={onPress}
      disabled={!onPress}
      android_ripple={{ color: c.form2 }}
      style={({ pressed }) => ({
        flexDirection: 'row',
        alignItems: 'center',
        gap: 14,
        paddingVertical: 14,
        paddingHorizontal: space.lg,
        backgroundColor: pressed && Platform.OS !== 'android' ? c.form2 : 'transparent',
      })}
    >
      {ikon ? <Ikon ad={ikon} renk={tehlike ? c.bad : c.ink2} /> : null}
      <View style={{ flex: 1 }}>
        <Yazi agirlik="medium" ton={tehlike ? 'bad' : 'ink'}>{baslik}</Yazi>
        {alt ? <Yazi boyut={13} ton="ink3">{alt}</Yazi> : null}
      </View>
      {sag ?? (onPress ? <Ikon ad="sag" boyut={18} renk={c.ink3} /> : null)}
    </Pressable>
  );
}

export function Grup({ baslik, children, alt }: { baslik?: string; children: ReactNode; alt?: string }) {
  const c = usePalette();
  return (
    <View style={{ marginTop: space.xl }}>
      {baslik ? (
        <Text style={{ fontFamily: font.bold, fontSize: 12, letterSpacing: 0.9, color: c.ink3, marginBottom: 8, marginHorizontal: space.lg, textTransform: 'uppercase' }}>
          {baslik}
        </Text>
      ) : null}
      <View style={{ backgroundColor: c.sheet, borderRadius: radius.lg, marginHorizontal: space.lg, overflow: 'hidden', borderWidth: StyleSheet.hairlineWidth, borderColor: c.line }}>
        {children}
      </View>
      {alt ? <Yazi boyut={12.5} ton="ink3" stil={{ marginHorizontal: space.lg + 4, marginTop: 8 }}>{alt}</Yazi> : null}
    </View>
  );
}

// --- Girdi ----------------------------------------------------------------------
export function Girdi(props: TextInputProps & { etiket?: string; ikon?: IkonAdi; hata?: string | null }) {
  const c = usePalette();
  const { etiket, ikon, hata, style, ...rest } = props;
  return (
    <View style={{ gap: 6 }}>
      {etiket ? <Yazi boyut={13} agirlik="semibold" ton="ink2">{etiket}</Yazi> : null}
      <View
        style={{
          flexDirection: 'row',
          alignItems: rest.multiline ? 'flex-start' : 'center',
          gap: 10,
          backgroundColor: c.sheet,
          borderRadius: radius.md,
          borderWidth: 1,
          borderColor: hata ? c.bad : c.line,
          paddingHorizontal: 14,
          minHeight: 50,
          paddingVertical: rest.multiline ? 12 : 0,
        }}
      >
        {ikon ? <Ikon ad={ikon} boyut={19} renk={c.ink3} /> : null}
        <TextInput
          placeholderTextColor={c.ink3}
          selectionColor={c.formInk}
          style={[{ flex: 1, fontFamily: font.regular, fontSize: 16, color: c.ink, paddingVertical: 10 }, rest.multiline && { minHeight: 110, textAlignVertical: 'top' }, style]}
          {...rest}
        />
      </View>
      {hata ? <Yazi boyut={13} ton="bad">{hata}</Yazi> : null}
    </View>
  );
}

// --- Durum ekranları ------------------------------------------------------------
export function Bos({ ikon, baslik, metin, eylem }: { ikon: IkonAdi; baslik: string; metin?: string; eylem?: ReactNode }) {
  const c = usePalette();
  return (
    <View style={{ alignItems: 'center', paddingHorizontal: 36, paddingVertical: 56, gap: 10 }}>
      <View style={{ width: 64, height: 64, borderRadius: 32, backgroundColor: c.form2, alignItems: 'center', justifyContent: 'center', marginBottom: 6 }}>
        <Ikon ad={ikon} boyut={28} renk={c.formInk} />
      </View>
      <Yazi boyut={18} agirlik="bold" stil={{ textAlign: 'center' }}>{baslik}</Yazi>
      {metin ? <Yazi ton="ink2" stil={{ textAlign: 'center' }}>{metin}</Yazi> : null}
      {eylem ? <View style={{ marginTop: 12, alignSelf: 'stretch' }}>{eylem}</View> : null}
    </View>
  );
}

export function IskeletKart() {
  const c = usePalette();
  const b = { backgroundColor: c.form2, borderRadius: 6 };
  return (
    <View style={{ backgroundColor: c.sheet, borderRadius: radius.lg, padding: space.lg, gap: 10, borderWidth: StyleSheet.hairlineWidth, borderColor: c.line }}>
      <View style={[b, { width: '40%', height: 12 }]} />
      <View style={[b, { width: '92%', height: 18 }]} />
      <View style={[b, { width: '70%', height: 18 }]} />
      <View style={{ flexDirection: 'row', gap: 8, marginTop: 4 }}>
        <View style={[b, { width: 64, height: 22 }]} />
        <View style={[b, { width: 84, height: 22 }]} />
      </View>
    </View>
  );
}

export const stilYardim = (c: Palette) =>
  StyleSheet.create({
    ekran: { flex: 1, backgroundColor: c.paper },
  });
