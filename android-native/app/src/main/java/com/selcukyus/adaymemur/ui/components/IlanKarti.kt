package com.selcukyus.adaymemur.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Durum
import com.selcukyus.adaymemur.data.Ilan
import com.selcukyus.adaymemur.data.Sabitler
import com.selcukyus.adaymemur.data.UygunlukSonucu
import com.selcukyus.adaymemur.ui.theme.T

fun kurumKisaltma(kurum: String): String {
    val tr = java.util.Locale.forLanguageTag("tr")
    val k = kurum.replace(Regex("T\\.C\\.\\s*", RegexOption.IGNORE_CASE), "").split(Regex("\\s+"))
        .filter { it.length > 2 && Bicim.katla(it) !in setOf("ve", "ile", "bakanligi", "genel", "mudurlugu", "baskanligi") }
    return (k.getOrNull(0)?.take(1) ?: "K").uppercase(tr) + (k.getOrNull(1)?.take(1) ?: "").uppercase(tr)
}

fun ilKonum(i: Ilan): String = when {
    i.iller.isEmpty() -> "Türkiye geneli"
    i.iller.size > 2 -> "${Sabitler.ILLER[i.iller[0]]} +${i.iller.size - 1}"
    else -> i.iller.joinToString(", ") { Sabitler.ILLER[it] ?: it }
}

fun kpssEtiketi(i: Ilan): String? {
    if (i.kpssGerekli == false) return "KPSS şartsız"
    if (i.kpss.isEmpty()) return null
    val k = i.kpss.take(2).joinToString(" · ") { it.tur }
    val min = i.kpss.firstOrNull { it.min != null }?.min
    return if (min != null) "$k ≥ $min" else k + if (i.kpss.size > 2) " +" else ""
}

@Composable
fun KurumLogo(ilan: Ilan, boyut: Int = 34) {
    val p = T.p
    Box(Modifier.size(boyut.dp).clip(RoundedCornerShape(9.dp)).background(p.form2), contentAlignment = Alignment.Center) {
        if (ilan.logo != null) {
            AsyncImage(ilan.logo, null, Modifier.size((boyut - 6).dp), contentScale = ContentScale.Fit)
        } else {
            Yazi(kurumKisaltma(ilan.kurum.ifEmpty { ilan.baslik }), boyut = (boyut * 0.36).toInt(), agirlik = FontWeight.Bold, renk = p.formInk)
        }
    }
}

@Composable
fun UygunlukRozeti(u: UygunlukSonucu) = when (u.durum) {
    Durum.UYGUN -> Rozet("Uygunsun", RozetTon.OK)
    Durum.KONTROL -> Rozet("Şartları kontrol et", RozetTon.WARN)
    Durum.DEGIL -> Rozet("Uygun değil", RozetTon.BAD)
}

@Composable
fun tonRengi(t: Bicim.Ton): Color = when (t) {
    Bicim.Ton.KOTU -> T.p.bad
    Bicim.Ton.UYARI -> T.p.warn
    Bicim.Ton.IYI -> T.p.ok
    Bicim.Ton.SOLUK -> T.p.ink3
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IlanKarti(
    ilan: Ilan,
    kayitli: Boolean,
    onKaydet: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    yeni: Boolean = false,
    kilitDk: Int? = null,
    uygunluk: UygunlukSonucu? = null,
) {
    val p = T.p
    val (kalanYazi, ton) = Bicim.kalanMetin(ilan.sonBasvuru)
    val tonRenk = tonRengi(ton)
    val egitim = ilan.egitim.filter { it != "ilkogretim" }.take(3).joinToString(" · ") { Sabitler.EGITIM_ADI[it] ?: it }
    val sekil = RoundedCornerShape(16.dp)
    Box(modifier.fillMaxWidth().clip(sekil).background(p.sheet).border(1.dp, p.line, sekil).clickable(onClick = onClick)) {
        Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KurumLogo(ilan)
                Column(Modifier.weight(1f)) {
                    Yazi(ilan.kurum.ifEmpty { "Kamu kurumu" }, boyut = 13, agirlik = FontWeight.SemiBold, renk = p.ink2, satir = 1)
                    Yazi(ilKonum(ilan), boyut = 12, renk = p.ink3, satir = 1)
                }
                if (yeni) Rozet("Yeni", RozetTon.MARKER)
                IconButton(onClick = onKaydet) {
                    Icon(
                        if (kayitli) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        if (kayitli) "Kaydedilenlerden çıkar" else "Kaydet",
                        tint = if (kayitli) p.formInk else p.ink3,
                    )
                }
            }
            Yazi(ilan.baslik, Modifier.padding(top = 8.dp, end = 8.dp), boyut = 16, agirlik = FontWeight.Bold, satir = 2)
            FlowRow(Modifier.padding(top = 10.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Rozet(Sabitler.TUR_ADI[ilan.tur] ?: "Diğer")
                ilan.kadro?.let { Rozet("${Bicim.sayi(it)} kadro", RozetTon.MUTE) }
                kpssEtiketi(ilan)?.let { Rozet(it, RozetTon.MUTE) }
                if (egitim.isNotEmpty()) Rozet(egitim, RozetTon.MUTE)
            }
            HorizontalDivider(Modifier.padding(top = 14.dp, end = 8.dp), color = p.line)
            Row(Modifier.padding(vertical = 12.dp).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.Schedule, null, Modifier.size(16.dp), tint = tonRenk)
                Yazi(kalanYazi, Modifier.weight(1f), boyut = 13, agirlik = FontWeight.SemiBold, renk = tonRenk)
                uygunluk?.let { UygunlukRozeti(it) }
            }
        }
        if (kilitDk != null) {
            Column(
                Modifier.matchParentSize().background(p.sheet.copy(alpha = 0.95f)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(p.marker), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Lock, null, Modifier.size(20.dp), tint = p.onMarker)
                }
                Spacer(Modifier.height(6.dp))
                Yazi("Yeni ilan — Pro üyelere açıldı", agirlik = FontWeight.Bold, hiza = TextAlign.Center)
                Yazi("$kilitDk dk sonra herkese açılacak", boyut = 13, renk = p.ink2, hiza = TextAlign.Center)
            }
        }
    }
}
