package com.selcukyus.adaymemur.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.selcukyus.adaymemur.R

/** sinalish ailesinin paleti: kâğıt zemin, fosforlu sarı vurgu, optik form mavisi. */
@Immutable
data class Palet(
    val paper: Color, val sheet: Color, val ink: Color, val ink2: Color, val ink3: Color, val line: Color,
    val form: Color, val form2: Color, val formInk: Color, val marker: Color, val onMarker: Color,
    val ok: Color, val okBg: Color, val bad: Color, val badBg: Color, val warn: Color, val warnBg: Color,
    val koyu: Boolean,
)

val Acik = Palet(
    paper = Color(0xFFF4F6F9), sheet = Color(0xFFFFFFFF), ink = Color(0xFF1B1E23), ink2 = Color(0xFF4A505B),
    ink3 = Color(0xFF767D89), line = Color(0xFFE3E7ED), form = Color(0xFFC3D0E0), form2 = Color(0xFFE4EAF2),
    formInk = Color(0xFF335C8C), marker = Color(0xFFFFE45A), onMarker = Color(0xFF1B1E23),
    ok = Color(0xFF15784A), okBg = Color(0xFFE0F1E7), bad = Color(0xFFBF3A30), badBg = Color(0xFFF9E3E0),
    warn = Color(0xFF8F6400), warnBg = Color(0xFFFFF2CC), koyu = false,
)

val Koyu = Palet(
    paper = Color(0xFF0F1115), sheet = Color(0xFF171A1F), ink = Color(0xFFECEEF1), ink2 = Color(0xFFB5BBC5),
    ink3 = Color(0xFF8A919D), line = Color(0xFF262B33), form = Color(0xFF323944), form2 = Color(0xFF1F242C),
    formInk = Color(0xFF96B8DE), marker = Color(0xFFF1D23A), onMarker = Color(0xFF15171B),
    ok = Color(0xFF52C68C), okBg = Color(0xFF15301F), bad = Color(0xFFF3786C), badBg = Color(0xFF3A1C19),
    warn = Color(0xFFE8B84E), warnBg = Color(0xFF33290F), koyu = true,
)

val Archivo = FontFamily(
    Font(R.font.archivo_regular, FontWeight.Normal),
    Font(R.font.archivo_medium, FontWeight.Medium),
    Font(R.font.archivo_semibold, FontWeight.SemiBold),
    Font(R.font.archivo_bold, FontWeight.Bold),
    Font(R.font.archivo_extrabold, FontWeight.ExtraBold),
)

val LocalPalet = staticCompositionLocalOf { Acik }

object T {
    val p: Palet @Composable get() = LocalPalet.current
}

@Composable
fun AdayMemurTema(icerik: @Composable () -> Unit) {
    val p = if (isSystemInDarkTheme()) Koyu else Acik
    val renk = if (p.koyu) {
        darkColorScheme(primary = p.marker, onPrimary = p.onMarker, background = p.paper, surface = p.sheet, onSurface = p.ink, onBackground = p.ink, outline = p.line, secondary = p.formInk)
    } else {
        lightColorScheme(primary = p.ink, onPrimary = p.paper, background = p.paper, surface = p.sheet, onSurface = p.ink, onBackground = p.ink, outline = p.line, secondary = p.formInk)
    }
    val d = Typography()
    val tip = Typography(
        displayLarge = d.displayLarge.copy(fontFamily = Archivo),
        headlineLarge = d.headlineLarge.copy(fontFamily = Archivo),
        headlineMedium = d.headlineMedium.copy(fontFamily = Archivo),
        titleLarge = d.titleLarge.copy(fontFamily = Archivo),
        titleMedium = d.titleMedium.copy(fontFamily = Archivo),
        titleSmall = d.titleSmall.copy(fontFamily = Archivo),
        bodyLarge = d.bodyLarge.copy(fontFamily = Archivo),
        bodyMedium = d.bodyMedium.copy(fontFamily = Archivo),
        bodySmall = d.bodySmall.copy(fontFamily = Archivo),
        labelLarge = d.labelLarge.copy(fontFamily = Archivo),
        labelMedium = d.labelMedium.copy(fontFamily = Archivo),
        labelSmall = d.labelSmall.copy(fontFamily = Archivo),
    )
    androidx.compose.runtime.CompositionLocalProvider(LocalPalet provides p) {
        MaterialTheme(colorScheme = renk, typography = tip, content = icerik)
    }
}

fun yazi(boyut: Int, agirlik: FontWeight = FontWeight.Normal, renk: Color = Color.Unspecified) =
    TextStyle(fontFamily = Archivo, fontSize = boyut.sp, fontWeight = agirlik, color = renk, lineHeight = (boyut * 1.38).sp)
