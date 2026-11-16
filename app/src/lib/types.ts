export type Kaynak = 'kariyerkapisi' | 'ilangov' | 'iskur';
export type Tur = 'memur' | 'sozlesmeli' | 'isci' | 'akademik' | 'kolluk' | 'diger';
export type Egitim = 'ilkogretim' | 'ortaogretim' | 'onlisans' | 'lisans' | 'yukseklisans' | 'doktora';
export type BolumSarti = 'yok' | 'var' | 'bilinmiyor';

export interface KpssSart {
  tur: string; // P3, P93, P94 ...
  min?: number;
}

export interface Ilan {
  id: string;
  kaynak: Kaynak;
  baslik: string;
  kurum: string;
  tur: Tur;
  iller: string[];
  egitim: Egitim[];
  kpss: KpssSart[];
  kpssGerekli: boolean | null;
  bolumSarti: BolumSarti;
  bolumler: string[];
  kadro: number | null;
  unvanlar: string[];
  basvuruBaslangic: string | null;
  sonBasvuru: string | null;
  yayin: string | null;
  eklenme: string;
  url: string;
  logo: string | null;
  ozet: string;
  detay: boolean;
  kaynaklar: { kaynak: Kaynak; url: string }[];
}

export interface IlanlarYanit {
  surum: number;
  guncelleme: string;
  adet: number;
  ilanlar: Ilan[];
}

export interface TakvimOgesi {
  id: string;
  sinav: string;
  baslik: string;
  tarih: string; // ISO
  bitis?: string;
  tip: 'basvuru' | 'sinav' | 'sonuc' | 'tercih';
  not?: string;
}

export interface Takvim {
  guncelleme: string;
  kaynak: string;
  kaynakUrl: string;
  uyari: string;
  ogeler: TakvimOgesi[];
}

export interface Duyuru {
  id: string;
  baslik: string;
  metin: string;
  tarih: string;
  url?: string;
  vurgulu?: boolean;
}

export interface Profil {
  egitim: Egitim | null;
  bolum: string;
  puanlar: Record<string, number>; // { P3: 78.4 }
  iller: string[]; // plaka; boş = tüm Türkiye
  turler: Tur[]; // ilgilenilen türler; boş = hepsi
  tamamlandi: boolean;
}

export interface Filtre {
  arama: string;
  iller: string[];
  turler: Tur[];
  egitim: Egitim[];
  puanTuru: string | null;
  minPuan: number | null; // kullanıcının puanı: taban puanı bunun altında olanlar
  suresiGecenleriGoster: boolean;
  siralama: 'yeni' | 'sonGun' | 'kadro';
}
