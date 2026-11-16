import assert from 'node:assert/strict';
import { test } from 'node:test';

import { ayristir, tarihler } from '../src/lib/parse';
import { uygunluk } from '../src/lib/match';
import { filtrele } from '../src/lib/liste';
import type { Ilan, Profil } from '../src/lib/types';

const ILAN_1 = `T.C. AİLE VE SOSYAL HİZMETLER BAKANLIĞI SÖZLEŞMELİ PERSONEL ALIM İLANI
2024 yılı KPSS (B) grubu puan sırası esas alınarak toplam 3000 sözleşmeli personel alımı yapılacaktır.
Büro Personeli KPSSP3 (Lisans) 1200 adet; Koruma ve Güvenlik Görevlisi KPSSP94 (Ortaöğretim) 800 adet;
Destek Personeli KPSSP93 (Önlisans) 1000 adet. Herhangi bir lisans programından mezun olmak.
a) Adaylar başvurularını 15/12/2025 - 26/12/2025 tarihleri arasında son gün saat 23:59'a kadar yapacaklardır.
Sözlü sınav 12.01.2026 tarihinde yapılacaktır.`;

test('Python ayrıştırıcısıyla aynı sonuç (ILAN_1)', () => {
  const p = ayristir('Sözleşmeli Personel Alımı', ILAN_1);
  assert.deepEqual(p.kpss.map((k) => k.tur), ['P3', 'P94', 'P93']);
  assert.equal(p.kpssGerekli, true);
  assert.deepEqual(p.egitim, ['ortaogretim', 'onlisans', 'lisans']);
  assert.equal(p.bolumSarti, 'yok');
  assert.equal(p.kadro, 3000);
  assert.equal(p.basvuruBaslangic, '2025-12-15');
  assert.equal(p.sonBasvuru, '2025-12-26');
});

test('metinsel tarih aralığı ve KPSS şartsız', () => {
  const p = ayristir('Bilişim', 'Bilgisayar Mühendisliği bölümlerinden mezun olmak. KPSS şartı aranmaz. Başvurular 1 Nisan 2026 - 15 Nisan 2026 tarihleri arasında alınacaktır.');
  assert.equal(p.kpssGerekli, false);
  assert.equal(p.bolumSarti, 'var');
  assert.equal(p.sonBasvuru, '2026-04-15');
});

test('kısa aralık ve taban puan', () => {
  const p = ayristir('İşçi', 'KPSS P93 puan türünden 70 ve üzeri puan almış olmak. Başvurular 20 - 30 Nisan 2026 tarihleri arasında yapılacaktır.');
  assert.deepEqual(p.kpss, [{ tur: 'P93', min: 70 }]);
  assert.equal(p.sonBasvuru, '2026-04-30');
});

test('sınav tarihi son başvuru sanılmaz', () => {
  assert.deepEqual(tarihler('Yazılı sınav 10.02.2026 tarihinde yapılacaktır. Başvurular 01.02.2026 tarihine kadar alınacaktır.'), { baslangic: null, son: '2026-02-01' });
});

const ilan = (o: Partial<Ilan>): Ilan => ({
  id: 'x', kaynak: 'ilangov', baslik: 'Zabıt Katibi', kurum: 'Adalet Bakanlığı', tur: 'memur', iller: [], egitim: ['lisans'],
  kpss: [{ tur: 'P3', min: 70 }], kpssGerekli: true, bolumSarti: 'yok', bolumler: [], kadro: 100, unvanlar: [],
  basvuruBaslangic: null, sonBasvuru: '2099-01-01', yayin: null, eklenme: new Date().toISOString(), url: '', logo: null,
  ozet: '', detay: false, kaynaklar: [], ...o,
});
const profil: Profil = { egitim: 'lisans', bolum: 'Lojistik', puanlar: { P3: 74.2 }, iller: ['06'], turler: [], tamamlandi: true };

test('uygunluk: uygun / puan yetersiz / il dışı', () => {
  assert.equal(uygunluk(ilan({}), profil).durum, 'uygun');
  assert.equal(uygunluk(ilan({ kpss: [{ tur: 'P3', min: 80 }] }), profil).durum, 'degil');
  assert.equal(uygunluk(ilan({ iller: ['35'] }), profil).durum, 'degil');
  assert.equal(uygunluk(ilan({ bolumSarti: 'var', ozet: 'Lojistik bölümünden mezun' }), profil).durum, 'uygun');
  assert.equal(uygunluk(ilan({ bolumSarti: 'var' }), profil).durum, 'kontrol');
  assert.equal(uygunluk(ilan({ sonBasvuru: '2000-01-01' }), profil).durum, 'degil');
});

test('filtre: arama, puan, süresi dolan', () => {
  const liste = [ilan({ id: 'a' }), ilan({ id: 'b', kpss: [{ tur: 'P94' }], baslik: 'Koruma Güvenlik' }), ilan({ id: 'c', sonBasvuru: '2000-01-01' })];
  const f = { arama: '', iller: [], turler: [], egitim: [], puanTuru: null, minPuan: null, suresiGecenleriGoster: false, siralama: 'yeni' as const };
  assert.deepEqual(filtrele(liste, f).map((i) => i.id), ['a', 'b']);
  assert.deepEqual(filtrele(liste, { ...f, arama: 'güvenlik' }).map((i) => i.id), ['b']);
  assert.deepEqual(filtrele(liste, { ...f, puanTuru: 'P3', minPuan: 65 }).map((i) => i.id), []);
  assert.deepEqual(filtrele(liste, { ...f, puanTuru: 'P3', minPuan: 75 }).map((i) => i.id), ['a']);
});
