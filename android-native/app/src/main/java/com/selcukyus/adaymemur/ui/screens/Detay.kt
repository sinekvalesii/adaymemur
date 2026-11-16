package com.selcukyus.adaymemur.ui.screens

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcukyus.adaymemur.ads.ReklamAlani
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.Durum
import com.selcukyus.adaymemur.data.Kontrol
import com.selcukyus.adaymemur.data.Sabitler
import com.selcukyus.adaymemur.data.Uygunluk
import com.selcukyus.adaymemur.ui.components.AltBaslik
import com.selcukyus.adaymemur.ui.components.Buton
import com.selcukyus.adaymemur.ui.components.ButonTur
import com.selcukyus.adaymemur.ui.components.Cip
import com.selcukyus.adaymemur.ui.components.Fosforlu
import com.selcukyus.adaymemur.ui.components.Kart
import com.selcukyus.adaymemur.ui.components.KurumLogo
import com.selcukyus.adaymemur.ui.components.Rozet
import com.selcukyus.adaymemur.ui.components.RozetTon
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.components.ilKonum
import com.selcukyus.adaymemur.ui.components.kpssEtiketi
import com.selcukyus.adaymemur.ui.components.tonRengi
import com.selcukyus.adaymemur.ui.theme.T
import java.time.ZoneId

@Composable
private fun Bilgi(ikon: ImageVector, etiket: String, deger: String, modifier: Modifier) {
    Row(modifier.padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(ikon, null, Modifier.size(18.dp), tint = T.p.ink3)
        Column {
            Yazi(etiket, boyut = 12, renk = T.p.ink3)
            Yazi(deger, boyut = 14, agirlik = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetayEkrani(id: String, git: Gezgin, bildir: (String) -> Unit) {
    val p = T.p
    val ctx = LocalContext.current
    val ilanlar by Depo.ilanlar.collectAsState()
    val kayitlar by Depo.kayitlar.akis.collectAsState()
    val profil by Depo.profil.akis.collectAsState()
    val pro by Pro.pro.collectAsState()
    val ilan = remember(ilanlar, kayitlar, id) { ilanlar.firstOrNull { it.id == id } ?: kayitlar[id]?.ilan }
    var metin by remember(id) { mutableStateOf<String?>(null) }
    var metinYuklendi by remember(id) { mutableStateOf(false) }
    var acik by remember { mutableStateOf(false) }

    LaunchedEffect(id) { Depo.goruldu(id) }
    LaunchedEffect(ilan?.id) {
        if (ilan != null) {
            metin = Depo.metin(ilan)
            metinYuklendi = true
        }
    }

    Column(Modifier.fillMaxSize().background(p.paper)) {
        AltBaslik(geri = git::geri, sag = {
            if (ilan != null) {
                IconButton(onClick = {
                    val m = "${if (ilan.kurum.isNotEmpty()) ilan.kurum + " — " else ""}${ilan.baslik}\n${ilan.sonBasvuru?.let { "Son başvuru: ${Bicim.uzun(it)}\n" } ?: ""}${ilan.url}"
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, m), "Paylaş"))
                }) { Icon(Icons.Default.Share, "Paylaş", tint = p.ink) }
                IconButton(onClick = { kaydet(ilan, pro, bildir, git) }) {
                    Icon(if (kayitlar.containsKey(id)) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder, "Kaydet", tint = if (kayitlar.containsKey(id)) p.formInk else p.ink)
                }
            }
        })
        if (ilan == null) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Yazi("İlan yayından kalktı", boyut = 18, agirlik = FontWeight.Bold)
                Yazi("Bu ilanın başvuru süresi dolmuş ya da kaynak tarafından kaldırılmış olabilir.", renk = p.ink2)
                Buton("İlanlara dön", git::geri, Modifier.fillMaxWidth(), tur = ButonTur.IKINCIL)
            }
            return
        }
        val u = remember(ilan, profil, metin) { if (profil.tamamlandi) Uygunluk.hesapla(ilan, profil, metin) else null }
        val (kalanYazi, ton) = Bicim.kalanMetin(ilan.sonBasvuru)
        val k = Bicim.kalanGun(ilan.sonBasvuru)

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KurumLogo(ilan, 44)
                Column {
                    Yazi(ilan.kurum.ifEmpty { "Kamu kurumu" }, boyut = 14, agirlik = FontWeight.SemiBold, renk = p.ink2)
                    Yazi("${Sabitler.KAYNAK_ADI[ilan.kaynak]}${ilan.yayin?.let { " · " + Bicim.uzun(it) } ?: ""}", boyut = 12, renk = p.ink3)
                }
            }
            SelectionContainer { Yazi(ilan.baslik, Modifier.padding(top = 16.dp), boyut = 24, agirlik = FontWeight.ExtraBold) }
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Rozet(Sabitler.TUR_ADI[ilan.tur] ?: "Diğer")
                ilan.kadro?.let { Rozet("${Bicim.sayi(it)} kadro", RozetTon.MUTE) }
            }

            Kart(Modifier.padding(top = 20.dp).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(
                        Modifier.size(62.dp).clip(RoundedCornerShape(14.dp)).background(if (k != null && k >= 0) p.marker else p.form2),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                    ) {
                        if (k != null && k >= 0) {
                            Yazi("$k", boyut = 24, agirlik = FontWeight.ExtraBold, renk = p.onMarker)
                            Yazi("gün", boyut = 11, agirlik = FontWeight.SemiBold, renk = p.onMarker)
                        } else {
                            Icon(Icons.Outlined.CalendarMonth, null, tint = p.ink3)
                        }
                    }
                    Column {
                        Yazi("Son başvuru", boyut = 12, renk = p.ink3)
                        Yazi(ilan.sonBasvuru?.let { Bicim.uzun(it) } ?: "İlan metninde belirtilmiş", boyut = 16, agirlik = FontWeight.Bold)
                        if (ilan.sonBasvuru != null) {
                            Yazi((ilan.basvuruBaslangic?.let { "${Bicim.uzun(it)} başladı · " } ?: "") + kalanYazi, boyut = 13, agirlik = FontWeight.SemiBold, renk = tonRengi(ton))
                        }
                    }
                }
            }
            if (ilan.sonBasvuru != null && k != null && k >= 0) {
                Buton("Takvime ekle", {
                    val g = Bicim.gun(ilan.sonBasvuru) ?: return@Buton
                    val bas = g.atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val bit = g.atTime(23, 59).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val i = Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                        .putExtra(CalendarContract.Events.TITLE, "Son başvuru: ${ilan.kurum.ifEmpty { ilan.baslik }}")
                        .putExtra(CalendarContract.Events.DESCRIPTION, "${ilan.baslik}\n\nResmî ilan: ${ilan.url}\n\nAday Memur")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, bas)
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, bit)
                    runCatching { ctx.startActivity(i) }.onFailure { bildir("Takvim uygulaması bulunamadı") }
                }, Modifier.padding(top = 8.dp).fillMaxWidth(), tur = ButonTur.IKINCIL, ikon = Icons.Outlined.CalendarMonth)
            }

            Kart(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                Row {
                    Bilgi(Icons.Outlined.School, "Öğrenim", ilan.egitim.joinToString(", ") { Sabitler.EGITIM_ADI[it] ?: it }.ifEmpty { "İlan metninde" }, Modifier.weight(1f))
                    Bilgi(Icons.Outlined.BarChart, "KPSS", kpssEtiketi(ilan) ?: if (ilan.kpssGerekli == true) "İstenir" else "İlan metninde", Modifier.weight(1f))
                }
                Row {
                    Bilgi(Icons.Outlined.Description, "Bölüm", when (ilan.bolumSarti) { "yok" -> "Bölüm şartı yok"; "var" -> "Belirli bölümler"; else -> "İlan metninde" }, Modifier.weight(1f))
                    Bilgi(Icons.Outlined.Place, "Kadro yeri", ilKonum(ilan), Modifier.weight(1f))
                }
            }

            Box(Modifier.padding(top = 24.dp)) { Fosforlu("Bu ilan sana uygun mu?", 18) }
            when {
                !pro -> Kart(Modifier.padding(top = 12.dp).fillMaxWidth(), onClick = git::pro) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Default.Lock, null, tint = p.formInk)
                        Yazi("Pro ile anında öğren", Modifier.weight(1f), agirlik = FontWeight.Bold)
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.ink3)
                    }
                    Yazi("Öğrenim düzeyin, bölümün, KPSS puanın ve il tercihin bu ilanın şartlarıyla tek tek karşılaştırılır.", Modifier.padding(top = 8.dp), boyut = 14, renk = p.ink2)
                }
                !profil.tamamlandi -> Kart(Modifier.padding(top = 12.dp).fillMaxWidth(), onClick = git::profil) {
                    Yazi("Önce profilini tamamla", agirlik = FontWeight.Bold)
                    Yazi("Öğrenim, bölüm ve KPSS puanını gir; uygunluk hesaplansın.", boyut = 14, renk = p.ink2)
                }
                u != null -> Kart(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                    when (u.durum) {
                        Durum.UYGUN -> Rozet("Başvurabilirsin", RozetTon.OK)
                        Durum.KONTROL -> Rozet("Şartları kontrol et", RozetTon.WARN)
                        Durum.DEGIL -> Rozet("Uygun görünmüyor", RozetTon.BAD)
                    }
                    u.gerekceler.forEach { g ->
                        val (bg, fg, ik) = when (g.sonuc) {
                            Kontrol.EVET -> Triple(p.okBg, p.ok, Icons.Default.Check)
                            Kontrol.HAYIR -> Triple(p.badBg, p.bad, Icons.Default.Close)
                            Kontrol.BELIRSIZ -> Triple(p.warnBg, p.warn, Icons.Default.PriorityHigh)
                        }
                        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(Modifier.size(22.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) { Icon(ik, null, Modifier.size(14.dp), tint = fg) }
                            Yazi(g.metin, boyut = 14)
                        }
                    }
                    Yazi("Otomatik karşılaştırmadır; kesin şartlar için resmî ilan metnini esas al.", Modifier.padding(top = 12.dp), boyut = 12, renk = p.ink3)
                }
            }

            if (ilan.unvanlar.isNotEmpty()) {
                Yazi("Kadrolar", Modifier.padding(top = 24.dp, bottom = 8.dp), boyut = 16, agirlik = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ilan.unvanlar.forEach { Cip(it, kucuk = true) }
                }
            }

            Yazi("İlan metni", Modifier.padding(top = 24.dp, bottom = 8.dp), boyut = 16, agirlik = FontWeight.Bold)
            Kart(Modifier.fillMaxWidth()) {
                val m = metin
                when {
                    !metinYuklendi -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = p.ink3, strokeWidth = 2.dp)
                        Yazi("Resmî metin yükleniyor…", renk = p.ink3)
                    }
                    m != null -> {
                        SelectionContainer { Yazi(if (!acik && m.length > 1400) m.take(1400).substringBeforeLast(" ") + "…" else m, boyut = 14, renk = p.ink2) }
                        if (m.length > 1400) Yazi(if (acik) "Daha az göster" else "Devamını oku", Modifier.padding(top = 10.dp).clickable { acik = !acik }, agirlik = FontWeight.Bold, renk = p.formInk)
                    }
                    else -> Yazi(ilan.ozet.ifEmpty { "Bu ilanın tam metni kurumun resmî sayfasında yayımlanıyor." }, renk = p.ink2)
                }
            }

            if (ilan.kaynaklar.size > 1) {
                Yazi("Bu ilan şu kaynaklarda da yayımlandı:", Modifier.padding(top = 12.dp), boyut = 13, renk = p.ink3)
                ilan.kaynaklar.drop(1).forEach { kl ->
                    Row(Modifier.padding(top = 6.dp).clickable { tarayicidaAc(ctx, kl.url) }, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, null, Modifier.size(16.dp), tint = p.formInk)
                        Yazi(Sabitler.KAYNAK_ADI[kl.kaynak] ?: kl.kaynak, boyut = 14, agirlik = FontWeight.SemiBold, renk = p.formInk)
                    }
                }
            }

            Row(Modifier.padding(top = 24.dp).clip(RoundedCornerShape(12.dp)).background(p.form2).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Shield, null, Modifier.size(18.dp), tint = p.formInk)
                Yazi("Aday Memur bağımsız bir uygulamadır, hiçbir kamu kurumuyla bağlantısı yoktur. Başvurular yalnızca kurumun resmî kanallarından yapılır; şartlar için resmî ilan metnini esas al.", boyut = 12, renk = p.ink2)
            }
            ReklamAlani(Modifier.padding(top = 12.dp))
            Spacer(Modifier.height(24.dp))
        }
        HorizontalDivider(color = p.line)
        Box(Modifier.background(p.paper).navigationBarsPadding().padding(16.dp)) {
            Buton("Resmî ilana git", { tarayicidaAc(ctx, ilan.url) }, Modifier.fillMaxWidth(), ikon = Icons.AutoMirrored.Filled.OpenInNew)
        }
    }
}
