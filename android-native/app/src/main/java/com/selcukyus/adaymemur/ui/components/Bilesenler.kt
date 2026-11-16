package com.selcukyus.adaymemur.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.selcukyus.adaymemur.ui.theme.T
import com.selcukyus.adaymemur.ui.theme.yazi

@Composable
fun Yazi(
    metin: String,
    modifier: Modifier = Modifier,
    boyut: Int = 15,
    agirlik: FontWeight = FontWeight.Normal,
    renk: Color = T.p.ink,
    satir: Int = Int.MAX_VALUE,
    hiza: TextAlign? = null,
) {
    Text(metin, modifier, style = yazi(boyut, agirlik, renk), maxLines = satir, overflow = TextOverflow.Ellipsis, textAlign = hiza)
}

/** Fosforlu kalem vurgusu: metnin alt yarısının arkasında sarı şerit. */
@Composable
fun Fosforlu(metin: String, boyut: Int = 18, agirlik: FontWeight = FontWeight.ExtraBold) {
    val p = T.p
    Text(
        metin,
        style = yazi(boyut, agirlik, if (p.koyu) p.ink else p.onMarker).copy(letterSpacing = (-0.4).sp()),
        modifier = Modifier.drawBehind {
            val h = size.height * 0.48f
            drawRoundRect(
                color = p.marker,
                topLeft = Offset(-4f, size.height - h - size.height * 0.06f),
                size = Size(size.width + 8f, h),
                cornerRadius = CornerRadius(6f, 6f),
            )
        },
    )
}

private fun Double.sp() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

@Composable
fun Kart(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    zemin: Color = T.p.sheet,
    kenar: Color = T.p.line,
    dolgu: PaddingValues = PaddingValues(16.dp),
    icerik: @Composable ColumnScope.() -> Unit,
) {
    val sekil = RoundedCornerShape(16.dp)
    Column(
        modifier
            .clip(sekil)
            .background(zemin)
            .border(BorderStroke(1.dp, kenar), sekil)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(dolgu),
        content = icerik,
    )
}

@Composable
fun Cip(etiket: String, secili: Boolean = false, kucuk: Boolean = false, ikon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    val p = T.p
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(if (secili) p.ink else p.sheet)
            .border(1.dp, if (secili) p.ink else p.line, RoundedCornerShape(50))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = if (kucuk) 11.dp else 14.dp, vertical = if (kucuk) 6.dp else 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (ikon != null) Icon(ikon, null, Modifier.size(16.dp), tint = if (secili) p.paper else p.ink2)
        Yazi(etiket, boyut = if (kucuk) 13 else 14, agirlik = FontWeight.SemiBold, renk = if (secili) p.paper else p.ink)
    }
}

enum class RozetTon { FORM, OK, BAD, WARN, MARKER, MUTE }

@Composable
fun Rozet(metin: String, ton: RozetTon = RozetTon.FORM) {
    val p = T.p
    val (bg, fg) = when (ton) {
        RozetTon.FORM -> p.form2 to p.formInk
        RozetTon.OK -> p.okBg to p.ok
        RozetTon.BAD -> p.badBg to p.bad
        RozetTon.WARN -> p.warnBg to p.warn
        RozetTon.MARKER -> p.marker to p.onMarker
        RozetTon.MUTE -> p.form2 to p.ink3
    }
    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(bg).padding(horizontal = 8.dp, vertical = 3.dp)) {
        Yazi(metin, boyut = 12, agirlik = FontWeight.SemiBold, renk = fg)
    }
}

enum class ButonTur { ANA, IKINCIL, SADE, TEHLIKE }

@Composable
fun Buton(
    etiket: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tur: ButonTur = ButonTur.ANA,
    ikon: ImageVector? = null,
    pasif: Boolean = false,
    yukleniyor: Boolean = false,
) {
    val p = T.p
    val (bg, fg, br) = when (tur) {
        ButonTur.ANA -> Triple(p.marker, p.onMarker, p.marker)
        ButonTur.IKINCIL -> Triple(p.sheet, p.ink, p.line)
        ButonTur.SADE -> Triple(Color.Transparent, p.formInk, Color.Transparent)
        ButonTur.TEHLIKE -> Triple(p.badBg, p.bad, p.badBg)
    }
    val kapali = pasif || yukleniyor
    Row(
        modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (kapali) bg.copy(alpha = 0.55f) else bg)
            .border(1.dp, br, RoundedCornerShape(12.dp))
            .clickable(enabled = !kapali, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (yukleniyor) {
            CircularProgressIndicator(Modifier.size(20.dp), color = fg, strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        } else if (ikon != null) {
            Icon(ikon, null, Modifier.size(20.dp), tint = fg)
            Spacer(Modifier.width(8.dp))
        }
        Yazi(etiket, boyut = 16, agirlik = FontWeight.Bold, renk = fg)
    }
}

@Composable
fun Satir(ikon: ImageVector?, baslik: String, alt: String? = null, tehlike: Boolean = false, sag: (@Composable RowScope.() -> Unit)? = null, onClick: (() -> Unit)? = null) {
    val p = T.p
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (ikon != null) Icon(ikon, null, Modifier.size(22.dp), tint = if (tehlike) p.bad else p.ink2)
        Column(Modifier.weight(1f)) {
            Yazi(baslik, agirlik = FontWeight.Medium, renk = if (tehlike) p.bad else p.ink)
            if (alt != null) Yazi(alt, boyut = 13, renk = p.ink3)
        }
        if (sag != null) sag() else if (onClick != null) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = p.ink3)
    }
}

@Composable
fun Grup(baslik: String? = null, alt: String? = null, icerik: @Composable ColumnScope.() -> Unit) {
    val p = T.p
    Column(Modifier.padding(top = 20.dp)) {
        if (baslik != null) Yazi(baslik.uppercase(java.util.Locale.forLanguageTag("tr")), Modifier.padding(start = 20.dp, bottom = 8.dp), boyut = 12, agirlik = FontWeight.Bold, renk = p.ink3)
        Kart(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), dolgu = PaddingValues(0.dp), icerik = icerik)
        if (alt != null) Yazi(alt, Modifier.padding(horizontal = 20.dp, vertical = 8.dp), boyut = 12, renk = p.ink3)
    }
}

@Composable
fun Girdi(
    deger: String,
    degisti: (String) -> Unit,
    modifier: Modifier = Modifier,
    etiket: String? = null,
    ipucu: String = "",
    ikon: ImageVector? = null,
    klavye: KeyboardOptions = KeyboardOptions.Default,
    cokSatir: Boolean = false,
) {
    val p = T.p
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (etiket != null) Yazi(etiket, boyut = 13, agirlik = FontWeight.SemiBold, renk = p.ink2)
        OutlinedTextField(
            value = deger,
            onValueChange = degisti,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Yazi(ipucu, renk = p.ink3) },
            leadingIcon = ikon?.let { { Icon(it, null, tint = p.ink3) } },
            singleLine = !cokSatir,
            keyboardOptions = klavye,
            textStyle = yazi(16, renk = p.ink),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = p.sheet, unfocusedContainerColor = p.sheet,
                focusedBorderColor = p.formInk, unfocusedBorderColor = p.line, cursorColor = p.formInk,
            ),
        )
    }
}

@Composable
fun Bos(ikon: ImageVector, baslik: String, metin: String? = null, eylem: (@Composable () -> Unit)? = null) {
    val p = T.p
    Column(Modifier.fillMaxWidth().padding(horizontal = 36.dp, vertical = 48.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(64.dp).clip(CircleShape).background(p.form2), contentAlignment = Alignment.Center) {
            Icon(ikon, null, Modifier.size(28.dp), tint = p.formInk)
        }
        Yazi(baslik, boyut = 18, agirlik = FontWeight.Bold, hiza = TextAlign.Center)
        if (metin != null) Yazi(metin, renk = p.ink2, hiza = TextAlign.Center)
        if (eylem != null) Box(Modifier.padding(top = 8.dp).fillMaxWidth()) { eylem() }
    }
}

@Composable
fun SekmeBasligi(baslik: String, alt: String? = null, sag: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 8.dp), verticalAlignment = Alignment.Bottom) {
        Column(Modifier.weight(1f)) {
            Text(baslik, style = yazi(30, FontWeight.ExtraBold, T.p.ink).copy(letterSpacing = (-0.8).sp(), lineHeight = 36.sp()))
            if (alt != null) Yazi(alt, Modifier.padding(top = 2.dp), boyut = 13, renk = T.p.ink3, satir = 2)
        }
        sag?.invoke()
    }
}

private fun Int.sp() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

@Composable
fun AltBaslik(baslik: String? = null, geri: () -> Unit, kapatIkonu: Boolean = false, sag: (@Composable RowScope.() -> Unit)? = null) {
    Surface(color = T.p.paper) {
        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = geri) {
                Icon(if (kapatIkonu) Icons.Filled.Close else Icons.AutoMirrored.Filled.ArrowBack, if (kapatIkonu) "Kapat" else "Geri", tint = T.p.ink)
            }
            Box(Modifier.weight(1f)) { if (baslik != null) Yazi(baslik, boyut = 17, agirlik = FontWeight.Bold, satir = 1) }
            sag?.invoke(this)
        }
    }
}

/** Logo işareti: optik form satırları, ilk balon fosforlu sarı. */
@Composable
fun Isaret(boyut: Int = 64) {
    val s = boyut / 64f
    Column(
        Modifier.size(boyut.dp).clip(RoundedCornerShape((16 * s).dp)).background(if (T.p.koyu) T.p.sheet else Color(0xFF1B1E23)).padding((13 * s).dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(3) { i ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy((6 * s).dp)) {
                Box(
                    Modifier.size((9 * s).dp).clip(CircleShape)
                        .then(if (i == 0) Modifier.background(Color(0xFFFFE45A)) else Modifier.border((1.6f * s).dp, Color.White, CircleShape)),
                )
                Box(Modifier.height((3 * s).dp).width(((if (i == 2) 18 else 26) * s).dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = if (i == 0) 1f else 0.55f)))
            }
        }
    }
}
