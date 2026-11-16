import type { Egitim, Tur } from './types';

export const ILLER: Record<string, string> = {
  '01': 'Adana', '02': 'Adıyaman', '03': 'Afyonkarahisar', '04': 'Ağrı', '05': 'Amasya',
  '06': 'Ankara', '07': 'Antalya', '08': 'Artvin', '09': 'Aydın', '10': 'Balıkesir',
  '11': 'Bilecik', '12': 'Bingöl', '13': 'Bitlis', '14': 'Bolu', '15': 'Burdur',
  '16': 'Bursa', '17': 'Çanakkale', '18': 'Çankırı', '19': 'Çorum', '20': 'Denizli',
  '21': 'Diyarbakır', '22': 'Edirne', '23': 'Elazığ', '24': 'Erzincan', '25': 'Erzurum',
  '26': 'Eskişehir', '27': 'Gaziantep', '28': 'Giresun', '29': 'Gümüşhane', '30': 'Hakkari',
  '31': 'Hatay', '32': 'Isparta', '33': 'Mersin', '34': 'İstanbul', '35': 'İzmir',
  '36': 'Kars', '37': 'Kastamonu', '38': 'Kayseri', '39': 'Kırklareli', '40': 'Kırşehir',
  '41': 'Kocaeli', '42': 'Konya', '43': 'Kütahya', '44': 'Malatya', '45': 'Manisa',
  '46': 'Kahramanmaraş', '47': 'Mardin', '48': 'Muğla', '49': 'Muş', '50': 'Nevşehir',
  '51': 'Niğde', '52': 'Ordu', '53': 'Rize', '54': 'Sakarya', '55': 'Samsun',
  '56': 'Siirt', '57': 'Sinop', '58': 'Sivas', '59': 'Tekirdağ', '60': 'Tokat',
  '61': 'Trabzon', '62': 'Tunceli', '63': 'Şanlıurfa', '64': 'Uşak', '65': 'Van',
  '66': 'Yozgat', '67': 'Zonguldak', '68': 'Aksaray', '69': 'Bayburt', '70': 'Karaman',
  '71': 'Kırıkkale', '72': 'Batman', '73': 'Şırnak', '74': 'Bartın', '75': 'Ardahan',
  '76': 'Iğdır', '77': 'Yalova', '78': 'Karabük', '79': 'Kilis', '80': 'Osmaniye',
  '81': 'Düzce',
};

export const IL_LISTESI = Object.entries(ILLER)
  .map(([kod, ad]) => ({ kod, ad }))
  .sort((a, b) => a.ad.localeCompare(b.ad, 'tr'));

export const TUR_ADI: Record<Tur, string> = {
  memur: 'Memur',
  sozlesmeli: 'Sözleşmeli',
  isci: 'İşçi',
  akademik: 'Akademik',
  kolluk: 'Askerî / Kolluk',
  diger: 'Diğer',
};

export const EGITIM_ADI: Record<Egitim, string> = {
  ilkogretim: 'İlköğretim',
  ortaogretim: 'Ortaöğretim',
  onlisans: 'Önlisans',
  lisans: 'Lisans',
  yukseklisans: 'Yüksek lisans',
  doktora: 'Doktora',
};

export const EGITIM_SIRA: Egitim[] = ['ilkogretim', 'ortaogretim', 'onlisans', 'lisans', 'yukseklisans', 'doktora'];

/** Profil kurulumunda sunulan puan türleri. Lisans alan puanları için serbest giriş de var. */
export const PUAN_TURLERI: { kod: string; ad: string; egitim: Egitim }[] = [
  { kod: 'P94', ad: 'KPSS P94 · Ortaöğretim', egitim: 'ortaogretim' },
  { kod: 'P93', ad: 'KPSS P93 · Önlisans', egitim: 'onlisans' },
  { kod: 'P3', ad: 'KPSS P3 · Lisans (GY-GK)', egitim: 'lisans' },
];

export const KAYNAK_ADI = {
  kariyerkapisi: 'Kariyer Kapısı',
  ilangov: 'ilan.gov.tr',
  iskur: 'İŞKUR',
} as const;

export const MAKER = 'selcukyus';

export const FORUM_KATEGORILERI = ['KPSS', 'Atama', 'Tayin', 'Maaş', 'Mülakat', 'Genel'];

const H = '[a-zçğıöşü]';
const UYGUNSUZ = new RegExp(
  `(?:^|[^a-zçğıöşü])(amk|aq|amq|orospu${H}*|piç${H}*|siktir${H}*|sik(?:er|ik|tim|eyim)${H}*|yarra[kğ]${H}*|ananı|pezevenk${H}*|kahpe${H}*|götveren${H}*|ibne${H}*|şerefsiz${H}*)(?=$|[^a-zçğıöşü])`,
);

/** Forum içeriği için temel uygunsuz ifade denetimi. */
export function uygunsuzMu(metin: string): boolean {
  return UYGUNSUZ.test(metin.toLocaleLowerCase('tr'));
}
