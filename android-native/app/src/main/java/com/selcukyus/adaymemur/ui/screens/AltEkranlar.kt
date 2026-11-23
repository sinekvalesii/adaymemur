package com.selcukyus.adaymemur.ui.screens

import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import com.selcukyus.adaymemur.data.Takipler
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selcukyus.adaymemur.BuildConfig
import com.selcukyus.adaymemur.billing.Paket
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.Filtre
import com.selcukyus.adaymemur.data.Liste
import com.selcukyus.adaymemur.data.Profil
import com.selcukyus.adaymemur.data.Sabitler
import com.selcukyus.adaymemur.ui.components.AltBaslik
import com.selcukyus.adaymemur.ui.components.Buton
import com.selcukyus.adaymemur.ui.components.ButonTur
import com.selcukyus.adaymemur.ui.components.Cip
import com.selcukyus.adaymemur.ui.components.Fosforlu
import com.selcukyus.adaymemur.ui.components.Girdi
import com.selcukyus.adaymemur.ui.components.Isaret
import com.selcukyus.adaymemur.ui.components.Kart
import com.selcukyus.adaymemur.ui.components.Rozet
import com.selcukyus.adaymemur.ui.components.RozetTon
import com.selcukyus.adaymemur.ui.components.Satir
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.components.Grup
import com.selcukyus.adaymemur.ui.theme.T

@Composable
private fun AltCubuk(icerik: @Composable () -> Unit) {
    Column(Modifier.background(T.p.paper)) {
        HorizontalDivider(color = T.p.line)
        Box(Modifier.navigationBarsPadding().imePadding().padding(16.dp)) { icerik() }
    }
}

@Composable
private fun Baslik(metin: String, alt: String? = null) {
    Column(Modifier.padding(top = 20.dp, bottom = 8.dp)) {
        Yazi(metin, boyut = 16, agirlik = FontWeight.Bold)
        if (alt != null) Yazi(alt, boyut = 13, renk = T.p.ink3)
    }
}

private fun <T> List<T>.degistir(v: T) = if (v in this) this - v else this + v

// --- Filtre -----------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltreEkrani(git: Gezgin) {
    val p = T.p
    val mevcut by Depo.filtre.akis.collectAsState()
    val profil by Depo.profil.akis.collectAsState()
    val ilanlar by Depo.ilanlar.collectAsState()
    var f by remember { mutableStateOf(mevcut) }
    var ilAra by remember { mutableStateOf("") }
    var puanYazi by remember { mutableStateOf(f.puan?.let { Bicim.puan(it) } ?: "") }
    val sonuc = remember(ilanlar, f) { Liste.filtrele(ilanlar, f).size }

    Column(Modifier.fillMaxSize().background(p.paper)) {
        AltBaslik("Filtrele", git::geri, kapatIkonu = true, sag = { Buton("Sıfırla", { f = Filtre(arama = f.arama); puanYazi = "" }, tur = ButonTur.SADE) })
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Baslik("Sıralama")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("yeni" to "En yeni", "sonGun" to "Son başvurusu yakın", "kadro" to "En çok kadro").forEach { (k, ad) ->
                    Cip(ad, f.siralama == k) { f = f.copy(siralama = k) }
                }
            }
            Baslik("KPSS puanı", "Puan türünü seç; taban puanı senin puanından yüksek olanlar gizlenir.")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Cip("Hepsi", f.puanTuru == null) { f = f.copy(puanTuru = null, puan = null); puanYazi = "" }
                listOf("P3", "P93", "P94").forEach { k ->
                    Cip("KPSS $k", f.puanTuru == k) {
                        val v = f.puan ?: profil.puanlar[k]
                        f = f.copy(puanTuru = k, puan = v); puanYazi = v?.let { Bicim.puan(it) } ?: ""
                    }
                }
            }
            if (f.puanTuru != null) {
                Girdi(puanYazi, { t ->
                    puanYazi = t
                    f = f.copy(puan = t.replace(',', '.').toDoubleOrNull()?.coerceAtMost(100.0))
                }, Modifier.padding(top = 12.dp), etiket = "${f.puanTuru} puanın", ipucu = "Ör. 72,45", klavye = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            }
            Baslik("Kadro türü")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Sabitler.TUR_ADI.forEach { (k, ad) -> Cip(ad, k in f.turler) { f = f.copy(turler = f.turler.degistir(k)) } }
            }
            Baslik("Öğrenim düzeyi")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ortaogretim", "onlisans", "lisans", "yukseklisans", "doktora").forEach { k ->
                    Cip(Sabitler.EGITIM_ADI.getValue(k), k in f.egitim) { f = f.copy(egitim = f.egitim.degistir(k)) }
                }
            }
            Baslik("İl", "Türkiye geneli kadro açan ilanlar her zaman görünür.")
            IlSecici(f.iller, ilAra, { ilAra = it }) { f = f.copy(iller = f.iller.degistir(it)) }
            Row(Modifier.padding(vertical = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Yazi("Süresi dolan ilanları göster", agirlik = FontWeight.SemiBold)
                    Yazi("Son 30 güne ait arşiv", boyut = 13, renk = p.ink3)
                }
                Switch(f.suresiGecenler, { f = f.copy(suresiGecenler = it) }, colors = SwitchDefaults.colors(checkedTrackColor = p.formInk, checkedThumbColor = p.sheet))
            }
        }
        AltCubuk { Buton("$sonuc ilanı göster", { Depo.filtre.guncelle { f }; git.geri() }, Modifier.fillMaxWidth()) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IlSecici(secili: List<String>, ara: String, araDegisti: (String) -> Unit, limit: Int = 81, sec: (String) -> Unit) {
    if (secili.isNotEmpty()) {
        FlowRow(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            secili.forEach { k -> Cip(Sabitler.ILLER[k] ?: k, true, kucuk = true, ikon = Icons.Default.Close) { sec(k) } }
        }
    }
    Girdi(ara, araDegisti, ipucu = "İl ara", ikon = Icons.Default.Search)
    val tr = java.util.Locale.forLanguageTag("tr")
    val liste = Sabitler.IL_LISTESI.filter { ara.isBlank() || it.second.lowercase(tr).contains(ara.lowercase(tr)) }.take(if (ara.isBlank()) 24 else 81)
    FlowRow(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        liste.forEach { (k, ad) -> Cip(ad, k in secili, kucuk = true) { if (k in secili || secili.size < limit) sec(k) } }
    }
}

// --- Profil -----------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfilFormu(p0: Profil, degisti: (Profil) -> Unit) {
    var ilAra by remember { mutableStateOf("") }
    var ekTur by remember { mutableStateOf("") }
    var ekDeger by remember { mutableStateOf("") }
    val yazilar = remember { mutableStateOf(p0.puanlar.mapValues { Bicim.puan(it.value) }) }
    val sira = Sabitler.EGITIM_SIRA

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Baslik("En son mezuniyetin")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ortaogretim", "onlisans", "lisans", "yukseklisans", "doktora").forEach { k ->
                Cip(Sabitler.EGITIM_ADI.getValue(k), p0.egitim == k) { degisti(p0.copy(egitim = k)) }
            }
        }
        if (p0.egitim != null && p0.egitim != "ortaogretim") {
            Girdi(p0.bolum, { degisti(p0.copy(bolum = it.take(60))) }, Modifier.padding(top = 12.dp), etiket = "Bölümün", ipucu = "Ör. Lojistik, İşletme, Bilgisayar Mühendisliği")
        }
        Baslik("KPSS puanların", "Geçerli sınav puanlarını gir. Bilmiyorsan boş bırakabilirsin.")
        Sabitler.PUAN_TURLERI.filter { p0.egitim == null || sira.indexOf(it.third) <= sira.indexOf(p0.egitim) }.forEach { (kod, ad, _) ->
            Girdi(yazilar.value[kod] ?: "", { t ->
                yazilar.value = yazilar.value + (kod to t)
                val n = t.replace(',', '.').toDoubleOrNull()
                degisti(p0.copy(puanlar = if (n != null && n > 0) p0.puanlar + (kod to n.coerceAtMost(100.0)) else p0.puanlar - kod))
            }, Modifier.padding(bottom = 8.dp), etiket = ad, ipucu = "Ör. 74,512", klavye = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        }
        p0.puanlar.filterKeys { k -> Sabitler.PUAN_TURLERI.none { it.first == k } }.forEach { (k, v) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Yazi("KPSS $k: ${Bicim.puan(v)}", Modifier.weight(1f), agirlik = FontWeight.SemiBold)
                IconButton(onClick = { degisti(p0.copy(puanlar = p0.puanlar - k)) }) { Icon(Icons.Default.Close, "Kaldır", tint = T.p.ink3) }
            }
        }
        if (p0.egitim in listOf("lisans", "yukseklisans", "doktora")) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                Girdi(ekTur, { ekTur = it.uppercase() }, Modifier.weight(1f), etiket = "A grubu alan puanı", ipucu = "Tür, ör. P48")
                Girdi(ekDeger, { ekDeger = it }, Modifier.weight(1f), ipucu = "Puan", klavye = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                Box(Modifier.padding(bottom = 10.dp)) {
                    Cip("Ekle", ikon = Icons.Default.Add) {
                        val m = Regex("^P?(\\d{1,3})$").find(ekTur.replace(Regex("\\s|KPSS|-"), ""))
                        val n = ekDeger.replace(',', '.').toDoubleOrNull()
                        if (m != null && n != null && n > 0) {
                            degisti(p0.copy(puanlar = p0.puanlar + ("P${m.groupValues[1].toInt()}" to n.coerceAtMost(100.0))))
                            ekTur = ""; ekDeger = ""
                        }
                    }
                }
            }
        }
        Baslik("Çalışmak istediğin iller", "Boş bırakırsan tüm Türkiye’deki ilanlar değerlendirilir.")
        IlSecici(p0.iller, ilAra, { ilAra = it }) { k -> degisti(p0.copy(iller = p0.iller.degistir(k))) }
    }
}

@Composable
fun ProfilEkrani(git: Gezgin, bildir: (String) -> Unit) {
    val mevcut by Depo.profil.akis.collectAsState()
    var p by remember { mutableStateOf(mevcut) }
    Column(Modifier.fillMaxSize().background(T.p.paper)) {
        AltBaslik("Öğrenim ve KPSS bilgilerim", git::geri)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Yazi("Bu bilgiler yalnızca cihazında saklanır ve ilanlarla eşleştirme için kullanılır.", renk = T.p.ink2)
            ProfilFormu(p) { p = it }
            Spacer(Modifier.height(24.dp))
        }
        AltCubuk {
            Buton("Kaydet", {
                Depo.profil.guncelle { p.copy(tamamlandi = true) }
                bildir("Profilin kaydedildi")
                git.geri()
            }, Modifier.fillMaxWidth(), pasif = p.egitim == null)
        }
    }
}

// --- Karşılama --------------------------------------------------------------------
@Composable
private fun Madde(ikon: ImageVector, baslik: String, metin: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(T.p.form2), contentAlignment = Alignment.Center) { Icon(ikon, null, tint = T.p.formInk) }
        Column {
            Yazi(baslik, agirlik = FontWeight.Bold)
            Yazi(metin, boyut = 14, renk = T.p.ink2)
        }
    }
}

@Composable
fun KarsilamaEkrani(bitti: () -> Unit, bildirimIzniIste: () -> Unit) {
    val p = T.p
    var adim by remember { mutableStateOf(0) }
    var profil by remember { mutableStateOf(Depo.profil.deger) }
    fun bitir(kaydet: Boolean) {
        if (kaydet && profil.egitim != null) Depo.profil.guncelle { profil.copy(tamamlandi = true) }
        Depo.ayarlar.guncelle { it.copy(karsilamaGoruldu = true) }
        bildirimIzniIste()
        bitti()
    }
    if (adim == 0) {
        Column(Modifier.fillMaxSize().background(p.paper).statusBarsPadding().navigationBarsPadding().padding(24.dp)) {
            Spacer(Modifier.height(16.dp))
            Isaret()
            Yazi("Kamu ilanları,", Modifier.padding(top = 28.dp), boyut = 34, agirlik = FontWeight.ExtraBold)
            Fosforlu("tek yerde.", 34)
            Yazi("Kariyer Kapısı, ilan.gov.tr ve İŞKUR’daki kamu personel alımları; KPSS şartlarıyla özetlenmiş halde.", Modifier.padding(top = 12.dp), boyut = 16, renk = p.ink2)
            Column(Modifier.padding(top = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Madde(Icons.Outlined.NotificationsNone, "Yeni ilanı ilk sen gör", "İl ve kadro tercihine göre bildirim.")
                Madde(Icons.Outlined.BarChart, "KPSS puanına göre süz", "Puan türü ve taban puanla filtrele.")
                Madde(Icons.Outlined.Schedule, "Son başvuruyu kaçırma", "Kaydet, takvimine ekle, geri sayımı izle.")
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.padding(bottom = 16.dp).clip(RoundedCornerShape(12.dp)).background(p.form2).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Shield, null, Modifier.size(18.dp), tint = p.formInk)
                Yazi("Bağımsız bir uygulamadır; hiçbir kamu kurumuyla bağlantısı yoktur. Başvurular resmî kanallardan yapılır.", boyut = 12, renk = p.ink2)
            }
            Buton("Başla", { adim = 1 }, Modifier.fillMaxWidth())
        }
        return
    }
    Column(Modifier.fillMaxSize().background(p.paper)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).statusBarsPadding().padding(16.dp)) {
            Yazi("PROFİLİN", boyut = 13, agirlik = FontWeight.Bold, renk = p.ink3)
            Yazi("Sana uygun ilanları bulalım", Modifier.padding(top = 4.dp), boyut = 26, agirlik = FontWeight.ExtraBold)
            Yazi("Bilgiler yalnızca cihazında kalır. Sonra Hesap sekmesinden değiştirebilirsin.", Modifier.padding(top = 6.dp), renk = p.ink2)
            ProfilFormu(profil) { profil = it }
        }
        AltCubuk {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Buton("Devam", { bitir(true) }, Modifier.fillMaxWidth(), pasif = profil.egitim == null)
                Buton("Şimdilik geç", { bitir(false) }, Modifier.fillMaxWidth(), tur = ButonTur.SADE)
            }
        }
    }
}

// --- Pro --------------------------------------------------------------------------
@Composable
fun ProEkrani(git: Gezgin) {
    val p = T.p
    val ctx = LocalContext.current
    val pro by Pro.pro.collectAsState()
    val paketler by Pro.paketler.collectAsState()
    var secili by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { Pro.paketleriGetir() }
    LaunchedEffect(paketler) { if (secili == null) secili = paketler?.firstOrNull()?.urunId }
    LaunchedEffect(pro) { if (pro) git.geri() }
    val sec: Paket? = paketler?.firstOrNull { it.urunId == secili }
    val avantajlar = listOf(
        Triple(Icons.Outlined.TrackChanges, "Sana özel ilan listesi", "Öğrenim, bölüm, KPSS puanı ve il tercihine göre başvurabileceğin ilanlar; her biri için neden uygun olduğu."),
        Triple(Icons.Outlined.NotificationsNone, "Anında bildirim ve e-posta", "Sana uygun ilan yayına girer girmez bildirim ve e-posta. Ücretsiz planda günlük özet gelir."),
        Triple(Icons.Outlined.Schedule, "Başvuru açılınca haber", "Başvurusu henüz açılmamış ilan, başvurular açıldığı anda bildirilir."),
        Triple(Icons.Outlined.TrackChanges, "Benzer ilan takibi", "Bir ilandaki unvanları takip et; benzer ilanlar çıkınca haber al."),
        Triple(Icons.Outlined.Schedule, "Erken erişim", "Yeni ilanları herkesten ${BuildConfig.ERKEN_ERISIM_DK} dakika önce gör."),
        Triple(Icons.Outlined.BookmarkBorder, "Sınırsız kayıt", "Ücretsiz planda ${BuildConfig.UCRETSIZ_KAYIT_LIMITI} ilan; Pro’da sınırsız."),
        Triple(Icons.Outlined.Shield, "Reklamsız", "Uygulamanın her yerinde reklamsız kullanım."),
    )
    Column(Modifier.fillMaxSize().background(p.paper)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).statusBarsPadding().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = git::geri) { Icon(Icons.Default.Close, "Kapat", tint = p.ink) }
            }
            Rozet("ADAY MEMUR PRO", RozetTon.MARKER)
            Yazi("Başvurabileceğin ilanı", Modifier.padding(top = 12.dp), boyut = 30, agirlik = FontWeight.ExtraBold)
            Fosforlu("ilk sen gör.", 30)
            Column(Modifier.padding(top = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                avantajlar.forEach { (ik, b, m) -> Madde(ik, b, m) }
            }
            Spacer(Modifier.height(20.dp))
            when {
                paketler == null -> Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = p.ink2) }
                paketler!!.isEmpty() -> Kart(Modifier.fillMaxWidth(), zemin = p.form2, kenar = p.form2) {
                    Yazi("Abonelik seçenekleri yüklenemedi", agirlik = FontWeight.Bold)
                    Yazi("Google Play bağlantısını kontrol edip birazdan tekrar dene.", boyut = 14, renk = p.ink2)
                }
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    paketler!!.forEach { pk ->
                        val aktif = pk.urunId == secili
                        Row(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(p.sheet)
                                .border(2.dp, if (aktif) p.ink else p.line, RoundedCornerShape(16.dp))
                                .clickable { secili = pk.urunId }.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(Modifier.size(22.dp).clip(CircleShape).border(2.dp, if (aktif) p.ink else p.ink3, CircleShape), contentAlignment = Alignment.Center) {
                                if (aktif) Box(Modifier.size(10.dp).clip(CircleShape).background(p.ink))
                            }
                            Column(Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Yazi(if (pk.tip == "yillik") "Yıllık" else "Aylık", boyut = 16, agirlik = FontWeight.Bold)
                                    if (pk.tip == "yillik") Rozet("En avantajlı", RozetTon.MARKER)
                                }
                                Yazi("${pk.deneme?.let { "$it, sonra " } ?: ""}${pk.fiyat}${if (pk.tip == "yillik") "/yıl" else "/ay"}", boyut = 13, renk = p.ink2)
                            }
                            pk.aylikKarsilik?.let { Yazi("$it/ay", boyut = 13, agirlik = FontWeight.SemiBold, renk = p.ink3) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
        AltCubuk {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Buton(if (sec?.deneme != null) "Ücretsiz denemeyi başlat" else "Pro’ya geç", { sec?.let { s -> (ctx as? Activity)?.let { Pro.satinAl(it, s) } } }, Modifier.fillMaxWidth(), pasif = sec == null)
                Yazi(
                    (sec?.let { "${it.deneme?.let { d -> "$d deneme bitince " } ?: ""}${it.fiyat} ${if (it.tip == "yillik") "yıllık" else "aylık"} olarak Google Play hesabından tahsil edilir. " } ?: "") +
                        "Abonelik, dönem bitmeden en az 24 saat önce iptal edilmezse otomatik yenilenir; Google Play › Abonelikler’den istediğin zaman iptal edebilirsin.",
                    boyut = 11, renk = p.ink3,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally)) {
                    Yazi("Geri yükle", Modifier.clickable { Pro.geriYukle() }, boyut = 12, agirlik = FontWeight.SemiBold, renk = p.formInk)
                    Yazi("Koşullar", Modifier.clickable { tarayicidaAc(ctx, "${BuildConfig.SITE_URL}/kosullar.html") }, boyut = 12, agirlik = FontWeight.SemiBold, renk = p.formInk)
                    Yazi("Gizlilik", Modifier.clickable { tarayicidaAc(ctx, "${BuildConfig.SITE_URL}/gizlilik.html") }, boyut = 12, agirlik = FontWeight.SemiBold, renk = p.formInk)
                }
            }
        }
    }
}

// --- KPSS gündemi -----------------------------------------------------------------
@Composable
fun GundemEkrani(git: Gezgin) {
    val p = T.p
    val ctx = LocalContext.current
    val takvim by Depo.takvim.collectAsState()
    val duyurular by Depo.duyurular.collectAsState()
    val sirali = takvim.ogeler.sortedBy { it.tarih }
    val yaklasan = sirali.filter { (Bicim.kalanGun(it.bitis ?: it.tarih) ?: -1) >= 0 }
    val gecmis = sirali.filter { (Bicim.kalanGun(it.bitis ?: it.tarih) ?: -1) < 0 }.reversed()
    val tip = mapOf("basvuru" to ("Başvuru" to RozetTon.FORM), "sinav" to ("Sınav" to RozetTon.MARKER), "sonuc" to ("Sonuç" to RozetTon.OK), "tercih" to ("Tercih" to RozetTon.WARN))
    Column(Modifier.fillMaxSize().background(p.paper)) {
        AltBaslik("KPSS gündemi", git::geri)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (duyurular.isNotEmpty()) {
                Yazi("DUYURULAR", boyut = 12, agirlik = FontWeight.Bold, renk = p.ink3)
                duyurular.forEach { d ->
                    Kart(Modifier.fillMaxWidth(), kenar = if (d.vurgulu) p.marker else p.line, onClick = d.url?.let { u -> { tarayicidaAc(ctx, u) } }) {
                        Yazi(Bicim.uzun(d.tarih), boyut = 12, renk = p.ink3)
                        Yazi(d.baslik, boyut = 16, agirlik = FontWeight.Bold)
                        Yazi(d.metin, Modifier.padding(top = 4.dp), boyut = 14, renk = p.ink2)
                        if (d.url != null) Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(15.dp), tint = p.formInk)
                            Yazi("Kaynağı aç", boyut = 13, agirlik = FontWeight.SemiBold, renk = p.formInk)
                        }
                    }
                }
            }
            Yazi("YAKLAŞAN TARİHLER", Modifier.padding(top = 8.dp), boyut = 12, agirlik = FontWeight.Bold, renk = p.ink3)
            if (yaklasan.isEmpty()) Yazi("Takvim yükleniyor ya da yaklaşan tarih yok.", renk = p.ink3)
            else Kart(Modifier.fillMaxWidth(), dolgu = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
                yaklasan.forEachIndexed { i, o ->
                    if (i > 0) HorizontalDivider(color = p.line)
                    val k = Bicim.kalanGun(o.tarih) ?: 0
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(Modifier.width(48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Yazi(Bicim.kisaGun(o.tarih), boyut = 20, agirlik = FontWeight.ExtraBold)
                            Yazi(Bicim.kisaAy(o.tarih), boyut = 12, agirlik = FontWeight.SemiBold, renk = p.ink3)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                val (ad, ton) = tip[o.tip] ?: ("Tarih" to RozetTon.MUTE)
                                Rozet(ad, ton)
                                Yazi(o.sinav, boyut = 12, renk = p.ink3)
                            }
                            Yazi(o.baslik, agirlik = FontWeight.Bold)
                            Yazi(
                                (o.bitis?.let { "${Bicim.uzun(o.tarih)} – ${Bicim.uzun(it)}" } ?: Bicim.gunAdi(o.tarih)) +
                                    when { k > 0 -> " · $k gün kaldı"; k == 0L -> " · bugün"; else -> " · devam ediyor" },
                                boyut = 13, renk = p.ink2,
                            )
                        }
                    }
                }
            }
            if (gecmis.isNotEmpty()) {
                Yazi("GEÇMİŞ", Modifier.padding(top = 8.dp), boyut = 12, agirlik = FontWeight.Bold, renk = p.ink3)
                gecmis.take(6).forEach { Yazi("${Bicim.uzun(it.tarih)} · ${it.baslik}", boyut = 13, renk = p.ink3) }
            }
            if (takvim.uyari.isNotBlank()) {
                Column(Modifier.padding(top = 12.dp).clickable { tarayicidaAc(ctx, takvim.kaynakUrl) }) {
                    Yazi(takvim.uyari, boyut = 12, renk = p.ink3)
                    Yazi("Kaynak: ${takvim.kaynak}", boyut = 13, agirlik = FontWeight.SemiBold, renk = p.formInk)
                }
            }
        }
    }
}

// --- Hakkında ---------------------------------------------------------------------
@Composable
fun HakkindaEkrani(git: Gezgin) {
    val p = T.p
    val ctx = LocalContext.current
    val guncelleme by Depo.guncelleme.collectAsState()
    val kaynaklar = listOf(
        Triple("Cumhurbaşkanlığı Kariyer Kapısı", "kariyerkapisi.gov.tr — kamu işe alım ilanları", "https://kariyerkapisi.gov.tr/isealim"),
        Triple("Basın İlan Kurumu", "ilan.gov.tr — personel alımı ilanları", "https://www.ilan.gov.tr"),
        Triple("Türkiye İş Kurumu", "esube.iskur.gov.tr — kamu işyeri iş ilanları", "https://esube.iskur.gov.tr"),
        Triple("ÖSYM", "osym.gov.tr — sınav takvimi", "https://www.osym.gov.tr"),
    )
    Column(Modifier.fillMaxSize().background(p.paper)) {
        AltBaslik("Veri kaynakları ve sorumluluk", git::geri)
        Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            Column(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Isaret(56)
                Yazi("Aday Memur", boyut = 20, agirlik = FontWeight.ExtraBold)
                Yazi(guncelleme?.let { "Veriler ${Bicim.goreli(it)} güncellendi" } ?: "Kamu ilanları takip uygulaması", boyut = 13, renk = p.ink3)
            }
            Grup("Bilgilerin kaynağı", "Sosyal medya, haber siteleri ve forumlar kaynak olarak kullanılmaz. İlanlar yaklaşık 30 dakikada bir resmî kaynaklardan otomatik derlenir.") {
                kaynaklar.forEach { (ad, alt, url) -> Satir(Icons.AutoMirrored.Filled.OpenInNew, ad, alt, onClick = { tarayicidaAc(ctx, url) }) }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Yazi("Sorumluluk reddi", boyut = 16, agirlik = FontWeight.Bold)
                Yazi("Aday Memur bağımsız bir uygulamadır; T.C. Cumhurbaşkanlığı, ÖSYM, İŞKUR, Basın İlan Kurumu veya başka bir kamu kurumuyla bağlantısı, ortaklığı ya da yetkilendirmesi yoktur. Uygulama başvuru almaz ve hiçbir kurum adına işlem yapmaz.", boyut = 14, renk = p.ink2)
                Yazi("Kadro, puan türü, taban puan ve tarih gibi özet bilgiler ilan metinlerinden otomatik olarak çıkarılır ve hata içerebilir. Başvuru öncesinde şartları daima kurumun resmî ilan metninden doğrula.", boyut = 14, renk = p.ink2)
                Yazi("Yapım: ${Sabitler.MAKER}", Modifier.padding(top = 12.dp), boyut = 13, renk = p.ink3)
            }
        }
    }
}

// --- Bildirim illeri --------------------------------------------------------------
@Composable
fun BildirimIlleriEkrani(git: Gezgin) {
    val mevcut by Depo.ayarlar.akis.collectAsState()
    var secim by remember { mutableStateOf(mevcut.bildirimIlleri) }
    var ara by remember { mutableStateOf("") }
    var eposta by remember { mutableStateOf(mevcut.eposta) }
    val kapsam = rememberCoroutineScope()
    Column(Modifier.fillMaxSize().background(T.p.paper)) {
        AltBaslik("Bildirim ayarları", git::geri)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Yazi("En fazla 5 il seçebilirsin. Türkiye geneli kadro açan ilanlar her durumda bildirilir. Hiç seçmezsen tüm iller için bildirim alırsın.", renk = T.p.ink2)
            IlSecici(secim, ara, { ara = it }, limit = 5) { k -> secim = secim.degistir(k) }
            Yazi("E-posta bildirimi (Pro)", Modifier.padding(top = 8.dp), agirlik = FontWeight.Bold)
            Yazi("Sana uygun yeni ilanlar ve takip ettiğin ilanlar e-postana da gelsin. Boş bırakırsan e-posta gönderilmez.", boyut = 14, renk = T.p.ink2)
            OutlinedTextField(eposta, { eposta = it.trim() }, Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("ornek@gmail.com") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email))
        }
        AltCubuk { Buton("Kaydet", {
            if (eposta.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(eposta).matches()) return@Buton
            val proMu = Pro.pro.value
            Depo.ayarlar.guncelle { it.copy(bildirimIlleri = secim, eposta = if (proMu) eposta else it.eposta) }
            kapsam.launch {
                val a = Depo.ayarlar.deger
                Takipler.bulutaYaz(if (proMu) eposta else "", Depo.profil.deger, a)?.let { t -> Depo.ayarlar.guncelle { it.copy(epostaToken = t) } }
            }
            if (!proMu && eposta.isNotEmpty()) git.pro() else git.geri()
        }, Modifier.fillMaxWidth()) }
    }
}

@Suppress("unused") private val bos: Color = Color.Unspecified
