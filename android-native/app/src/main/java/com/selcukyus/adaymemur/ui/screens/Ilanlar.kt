package com.selcukyus.adaymemur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcukyus.adaymemur.BuildConfig
import com.selcukyus.adaymemur.ads.ReklamAlani
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.Filtre
import com.selcukyus.adaymemur.data.Liste
import com.selcukyus.adaymemur.data.VeriDurumu
import com.selcukyus.adaymemur.ui.components.Bos
import com.selcukyus.adaymemur.ui.components.Buton
import com.selcukyus.adaymemur.ui.components.ButonTur
import com.selcukyus.adaymemur.ui.components.Cip
import com.selcukyus.adaymemur.ui.components.Girdi
import com.selcukyus.adaymemur.ui.components.IlanKarti
import com.selcukyus.adaymemur.ui.components.SekmeBasligi
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.theme.T
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val HIZLI = listOf("memur" to "Memur", "sozlesmeli" to "Sözleşmeli", "isci" to "İşçi", "akademik" to "Akademik", "kolluk" to "Askerî / Kolluk")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IlanlarEkrani(git: Gezgin, bildir: (String) -> Unit) {
    val p = T.p
    val ilanlar by Depo.ilanlar.collectAsState()
    val durum by Depo.durum.collectAsState()
    val guncelleme by Depo.guncelleme.collectAsState()
    val yenileniyor by Depo.yenileniyor.collectAsState()
    val filtre by Depo.filtre.akis.collectAsState()
    val kayitlar by Depo.kayitlar.akis.collectAsState()
    val ayar by Depo.ayarlar.akis.collectAsState()
    val pro by Pro.pro.collectAsState()
    var arama by remember { mutableStateOf(filtre.arama) }
    var simdi by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val kapsam = rememberCoroutineScope()

    LaunchedEffect(Unit) { while (true) { delay(60_000); simdi = System.currentTimeMillis() } }
    LaunchedEffect(arama) { delay(200); Depo.filtre.guncelle { it.copy(arama = arama) } }

    val sonuc = remember(ilanlar, filtre) { Liste.filtrele(ilanlar, filtre) }
    val gunOnce = simdi - 24 * 3600_000L

    PullToRefreshBox(isRefreshing = yenileniyor && durum == VeriDurumu.HAZIR, onRefresh = { kapsam.launch { Depo.yenile() } }, modifier = Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                SekmeBasligi(
                    "Kamu İlanları",
                    guncelleme?.let { "${Bicim.sayi(sonuc.size)} ilan · ${Bicim.goreli(it)} güncellendi" } ?: "Resmî kaynaklardan güncel ilanlar",
                )
                Row(Modifier.padding(horizontal = 16.dp).padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                    Girdi(arama, { arama = it }, Modifier.weight(1f), ipucu = "Kurum, unvan veya il ara", ikon = Icons.Default.Search)
                    Box {
                        Box(
                            Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (filtre.aktifSayi > 0) p.ink else p.sheet)
                                .border(1.dp, if (filtre.aktifSayi > 0) p.ink else p.line, RoundedCornerShape(12.dp))
                                .clickable { git.filtre() },
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.Tune, "Filtreler", tint = if (filtre.aktifSayi > 0) p.paper else p.ink) }
                        if (filtre.aktifSayi > 0) {
                            Box(Modifier.align(Alignment.TopEnd).offset(6.dp, (-6).dp).size(20.dp).clip(CircleShape).background(p.marker), contentAlignment = Alignment.Center) {
                                Yazi("${filtre.aktifSayi}", boyut = 11, agirlik = FontWeight.Bold, renk = p.onMarker)
                            }
                        }
                    }
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Cip("Tümü", filtre.turler.isEmpty()) { Depo.filtre.guncelle { it.copy(turler = emptyList()) } } }
                    items(HIZLI) { (tur, ad) ->
                        Cip(ad, tur in filtre.turler) {
                            Depo.filtre.guncelle { f -> f.copy(turler = if (tur in f.turler) f.turler - tur else f.turler + tur) }
                        }
                    }
                }
                GundemKarti(git)
            }

            when {
                durum == VeriDurumu.YUKLENIYOR && ilanlar.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = p.formInk) }
                }
                durum == VeriDurumu.HATA -> item {
                    Bos(Icons.Default.WarningAmber, "Bağlantı kurulamadı", "İlanlar şu an yüklenemedi. İnternet bağlantını kontrol edip tekrar dene.") {
                        Buton("Tekrar dene", { kapsam.launch { Depo.yenile() } }, Modifier.fillMaxWidth(), ikon = Icons.Default.Refresh)
                    }
                }
                sonuc.isEmpty() -> item {
                    Bos(Icons.Default.Search, "Bu filtrelere uyan ilan yok", "Filtreleri gevşetmeyi ya da aramayı değiştirmeyi dene.") {
                        Buton("Filtreleri temizle", { arama = ""; Depo.filtre.guncelle { Filtre(siralama = it.siralama) } }, Modifier.fillMaxWidth(), tur = ButonTur.IKINCIL)
                    }
                }
                else -> {
                    sonuc.forEachIndexed { i, ilan ->
                        item(key = ilan.id) {
                            val kilit = Liste.kilitDakika(ilan, pro, BuildConfig.ERKEN_ERISIM_DK, simdi)
                            IlanKarti(
                                ilan = ilan,
                                kayitli = kayitlar.containsKey(ilan.id),
                                onKaydet = { kaydet(ilan, pro, bildir, git) },
                                onClick = { if (kilit != null) git.pro() else git.ilan(ilan.id) },
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp),
                                yeni = ilan.id !in ayar.goruldu && Bicim.zaman(ilan.eklenme) > gunOnce,
                                kilitDk = kilit,
                            )
                        }
                        if (!pro && i % 8 == 4) item(key = "reklam$i") { ReklamAlani(Modifier.padding(bottom = 12.dp)) }
                    }
                }
            }
        }
    }
}

fun kaydet(ilan: com.selcukyus.adaymemur.data.Ilan, pro: Boolean, bildir: (String) -> Unit, git: Gezgin) {
    when (Depo.kaydetDegistir(ilan, pro)) {
        true -> bildir("İlan kaydedildi")
        false -> bildir("Kaydedilenlerden çıkarıldı")
        null -> { bildir("Ücretsiz planda ${BuildConfig.UCRETSIZ_KAYIT_LIMITI} ilan kaydedebilirsin"); git.pro() }
    }
}

@Composable
fun GundemKarti(git: Gezgin) {
    val p = T.p
    val takvim by Depo.takvim.collectAsState()
    val duyurular by Depo.duyurular.collectAsState()
    val siradaki = takvim.ogeler.filter { (Bicim.kalanGun(it.bitis ?: it.tarih) ?: -1) >= 0 }.minByOrNull { it.tarih }
    val duyuru = duyurular.firstOrNull { it.vurgulu }
    if (siradaki == null && duyuru == null) return
    val k = siradaki?.let { Bicim.kalanGun(it.tarih) }
    val zemin = if (p.koyu) p.sheet else p.ink
    val yazi = if (p.koyu) p.ink else p.paper
    Row(
        Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(zemin)
            .then(if (p.koyu) Modifier.border(1.dp, p.line, RoundedCornerShape(16.dp)) else Modifier)
            .clickable { git.gundem() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (siradaki != null) {
            Column(Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)).background(p.marker), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Yazi(if (k != null && k > 0) "$k" else "!", boyut = 22, agirlik = FontWeight.ExtraBold, renk = p.onMarker)
                Yazi(if (k != null && k > 0) "gün" else "bugün", boyut = 11, agirlik = FontWeight.SemiBold, renk = p.onMarker)
            }
        }
        Column(Modifier.weight(1f)) {
            Yazi("KPSS GÜNDEMİ", boyut = 11, agirlik = FontWeight.Bold, renk = p.marker)
            Yazi(siradaki?.baslik ?: duyuru!!.baslik, boyut = 15, agirlik = FontWeight.Bold, renk = yazi, satir = 2)
            Yazi(
                siradaki?.let { Bicim.uzun(it.tarih) + (it.bitis?.let { b -> " – " + Bicim.uzun(b) } ?: "") } ?: "Duyuruyu oku",
                boyut = 12, renk = yazi.copy(alpha = 0.75f), satir = 1,
            )
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = yazi)
    }
}
