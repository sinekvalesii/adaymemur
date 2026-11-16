import { useEffect, useRef, useState } from 'react';
import { Animated, Pressable, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { font, radius, usePalette } from '../theme';
import { Ikon, type IkonAdi } from './Ikon';

type Mesaj = { id: number; baslik: string; metin?: string; ikon?: IkonAdi; onPress?: () => void };

let dinleyici: ((m: Mesaj) => void) | null = null;
let sayac = 0;

/** Uygulama içi kısa bildirim (toast). */
export function bildir(baslik: string, metin?: string, opt: { ikon?: IkonAdi; onPress?: () => void } = {}) {
  dinleyici?.({ id: ++sayac, baslik, metin, ...opt });
}

export function BildirimKatmani() {
  const c = usePalette();
  const ins = useSafeAreaInsets();
  const [m, setM] = useState<Mesaj | null>(null);
  const y = useRef(new Animated.Value(-160)).current;
  const zaman = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    dinleyici = (yeni) => {
      setM(yeni);
      Animated.spring(y, { toValue: 0, useNativeDriver: true, damping: 18, stiffness: 180 }).start();
      if (zaman.current) clearTimeout(zaman.current);
      zaman.current = setTimeout(() => {
        Animated.timing(y, { toValue: -160, duration: 220, useNativeDriver: true }).start(() => setM(null));
      }, 3800);
    };
    return () => {
      dinleyici = null;
    };
  }, [y]);

  if (!m) return null;
  return (
    <Animated.View pointerEvents="box-none" style={{ position: 'absolute', left: 12, right: 12, top: ins.top + 8, transform: [{ translateY: y }], zIndex: 1000 }}>
      <Pressable
        onPress={() => {
          m.onPress?.();
          Animated.timing(y, { toValue: -160, duration: 180, useNativeDriver: true }).start(() => setM(null));
        }}
        style={{
          flexDirection: 'row',
          gap: 12,
          alignItems: 'center',
          backgroundColor: c.ink,
          borderRadius: radius.lg,
          paddingVertical: 14,
          paddingHorizontal: 16,
          shadowColor: '#000',
          shadowOpacity: 0.25,
          shadowRadius: 18,
          shadowOffset: { width: 0, height: 8 },
          elevation: 8,
        }}
      >
        <View style={{ width: 34, height: 34, borderRadius: 17, backgroundColor: c.marker, alignItems: 'center', justifyContent: 'center' }}>
          <Ikon ad={m.ikon ?? 'zil'} boyut={18} renk={c.onMarker} />
        </View>
        <View style={{ flex: 1 }}>
          <Text style={{ fontFamily: font.bold, fontSize: 15, color: c.paper }} numberOfLines={2}>{m.baslik}</Text>
          {m.metin ? <Text style={{ fontFamily: font.regular, fontSize: 13.5, color: c.paper, opacity: 0.8 }} numberOfLines={2}>{m.metin}</Text> : null}
        </View>
      </Pressable>
    </Animated.View>
  );
}
