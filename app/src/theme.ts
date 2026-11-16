import { useColorScheme } from 'react-native';

/**
 * Aday Memur tasarım dili — sinalish ailesinin paleti.
 * paper: zemin · sheet: kart · marker: fosforlu vurgu · form: optik form mavisi
 */
const light = {
  paper: '#F4F6F9',
  sheet: '#FFFFFF',
  ink: '#1B1E23',
  ink2: '#4A505B',
  ink3: '#767D89',
  line: '#E3E7ED',
  form: '#C3D0E0',
  form2: '#E4EAF2',
  formInk: '#335C8C',
  marker: '#FFE45A',
  onMarker: '#1B1E23',
  ok: '#15784A',
  okBg: '#E0F1E7',
  bad: '#BF3A30',
  badBg: '#F9E3E0',
  warn: '#8F6400',
  warnBg: '#FFF2CC',
  scrim: 'rgba(17, 20, 26, 0.46)',
  shadow: 'rgba(27, 30, 35, 0.16)',
};

const dark: typeof light = {
  paper: '#0F1115',
  sheet: '#171A1F',
  ink: '#ECEEF1',
  ink2: '#B5BBC5',
  ink3: '#8A919D',
  line: '#262B33',
  form: '#323944',
  form2: '#1F242C',
  formInk: '#96B8DE',
  marker: '#F1D23A',
  onMarker: '#15171B',
  ok: '#52C68C',
  okBg: '#15301F',
  bad: '#F3786C',
  badBg: '#3A1C19',
  warn: '#E8B84E',
  warnBg: '#33290F',
  scrim: 'rgba(0, 0, 0, 0.62)',
  shadow: 'rgba(0, 0, 0, 0.6)',
};

export type Palette = typeof light;

export const font = {
  regular: 'Archivo_400Regular',
  medium: 'Archivo_500Medium',
  semibold: 'Archivo_600SemiBold',
  bold: 'Archivo_700Bold',
  heavy: 'Archivo_800ExtraBold',
};

export const space = { xs: 4, sm: 8, md: 12, lg: 16, xl: 20, xxl: 28 };
export const radius = { sm: 8, md: 12, lg: 16, pill: 999 };

export function usePalette(): Palette & { dark: boolean } {
  const scheme = useColorScheme();
  return scheme === 'dark' ? { ...dark, dark: true } : { ...light, dark: false };
}

export const palettes = { light, dark };
