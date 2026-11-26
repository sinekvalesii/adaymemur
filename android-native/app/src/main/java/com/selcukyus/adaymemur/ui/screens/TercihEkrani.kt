package com.selcukyus.adaymemur.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.Sabitler
import com.selcukyus.adaymemur.data.Sans
import com.selcukyus.adaymemur.data.Tercih
import com.selcukyus.adaymemur.data.TercihKadro
import com.selcukyus.adaymemur.ui.components.AltBaslik
import com.selcukyus.adaymemur.ui.components.Buton
import com.selcukyus.adaymemur.ui.components.ButonTur
import com.selcukyus.adaymemur.ui.components.Cip
import com.selcukyus.adaymemur.ui.components.Girdi
import com.selcukyus.adaymemur.ui.components.Kart
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.theme.T
import java.util.Locale

private const val UCRETSIZ_SONUC = 5
private const val UCRETSIZ_LISTE = 3
private const val LISTE_SINIRI = 30
private val TR = Locale.forLanguageTag("tr")
private fun katla(s: String) = s.lowercase(TR).replace('ı', 'i').replace('ş', 's').replace('ğ', 'g').replace('ü', 'u').replace('ö', 'o').replace('ç', 'c')
private fun puanYaz(d: Double?) = d?.let { String.format(TR, "%.3f", it) } ?: "—"

@Composable
fun TercihEkrani(git: Gezgin, bildir: (String) -> Unit) {
    val p = T.p
    val ctx = LocalContext.current
    val veri by Tercih.veri.collectAsState()
    val pro by Pro.pro.collectAsState()
    val profil by Depo.profil.akis.collectAsState()
    val ayar by Depo.ayarlar.akis.collectAsState()
    LaunchedEffect(Unit) { Tercih.yukle() }

    var duzey by remember { mutableStateOf(Tercih.duzey(profil.egitim)) }
    var puanMetin by remember(duzey) { mutableStateOf(profil.puanlar[Tercih.PUAN[duzey]]?.let { String.format(TR, "%.3f", it) } ?: "") }
    var arama by remember { mutableStateOf("") }
    var sadeceIllerim by remember { mutableStateOf(profil.iller.isNotEmpty()) }
    var donem by remember { mutableStateOf<String?>(null) }
    var listeGoster by remember { mutableStateOf(false) }
    val puan = puanMetin.replace(',', '.').toDoubleOrNull()
    val illerim = profil.iller.mapNotNull { Sabitler.ILLER[it] }.map { katla(it) }.toSet()

    val v = veri
    val sonuc: List<Pair<TercihKadro, Sans>> = remember(v, duzey, puan, arama, sadeceIllerim, donem, illerim) {
        if (v == null || puan == null) emptyList() else {
            val a = katla(arama.trim())
            v.kadrolar.asSequence()
                .filter { it.d == duzey && (donem == null || it.donem == donem) }
                .filter { !sadeceIllerim || illerim.isEmpty() || katla(it.il) in illerim }
                .filter { a.isEmpty() || katla(it.unvan).contains(a) || katla(it.kurum).contains(a) || katla(it.il).contains(a) }
                .map { it to Tercih.sans(it, puan) }
                .filter { it.second != Sans.DUSUK }
                .sortedWith(compareBy<Pair<TercihKadro, Sans>> { it.second.ordinal }.thenByDescending { it.first.min ?: 0.0 })
                .toList()
        }
    }
    val liste = ayar.tercihListesi
    val listeKadrolari = remember(v, liste) { liste.mapNotNull { a -> v?.kadrolar?.firstOrNull { it.anahtar == a } } }

    fun listeDegistir(k: TercihKadro) {
        val var_ = k.anahtar in liste
        if (!var_) {
            val sinir = if (pro) LISTE_SINIRI else UCRETSIZ_LISTE
            if (liste.size >= sinir) { bildir(if (pro) "Tercih listesi en fazla $LISTE_SINIRI kadro olabilir" else "Ücretsiz planda $UCRETSIZ_LISTE tercih; Pro ile $LISTE_SINIRI"); if (!pro) git.pro(); return }
        }
        Depo.ayarlar.guncelle { it.copy(tercihListesi = if (var_) it.tercihListesi - k.anahtar else it.tercihListesi + k.anahtar) }
        bildir(if (var_) "Listeden çıkarıldı" else "Tercih listene eklendi")
    }

    fun paylas() {
        val m = buildString {
            appendLine("KPSS tercih listem (${Tercih.DUZEY_AD[duzey]}, ${Tercih.PUAN[duzey]}: ${puanMetin})")
            listeKadrolari.forEachIndexed { i, k -> appendLine("${i + 1}. ${k.unvan} — ${k.kurum} / ${k.il} (kod ${k.k}, ${k.donem} taban ${puanYaz(k.min)})") }
            appendLine(); append("Aday Memur ile hazırlandı. Tercih kodlarını yeni ÖSYM kılavuzundan kontrol et.")
        }
        ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, m), "Tercih listemi paylaş"))
    }

    Column(Modifier.fillMaxSize().background(p.paper)) {
        AltBaslik("KPSS tercih asistanı", git::geri, sag = {
            if (liste.isNotEmpty()) IconButton(onClick = ::paylas) { Icon(Icons.Outlined.Share, "Listeyi paylaş", tint = p.ink) }
        })
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Yazi("Puanınla önceki merkezi yerleştirmelerde hangi kadrolara yerleşilebildiğini gör, tercih listeni hazırla.", renk = p.ink2)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ort", "onl", "lis").forEach { d -> Cip(Tercih.DUZEY_AD[d]!!, secili = duzey == d) { duzey = d } }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                        Girdi(puanMetin, { puanMetin = it.filter { c -> c.isDigit() || c == ',' || c == '.' }.take(9) }, Modifier.weight(1f),
                            etiket = "KPSS ${Tercih.PUAN[duzey]} puanın", ipucu = "Örn. 71,494", klavye = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    }
                    Girdi(arama, { arama = it }, etiket = null, ipucu = "Unvan, kurum veya il ara (ör. memur, Bolu)", ikon = Icons.Outlined.Search)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (profil.iller.isNotEmpty()) Cip("Profilimdeki iller", secili = sadeceIllerim) { sadeceIllerim = !sadeceIllerim }
                        Cip("Tüm dönemler", secili = donem == null) { donem = null }
                        v?.donemler?.filter { d -> v.kadrolar.any { it.donem == d } }?.forEach { d -> Cip("KPSS-$d", secili = donem == d) { donem = d } }
                    }
                    if (liste.isNotEmpty()) Kart(Modifier.fillMaxWidth().clickable { listeGoster = !listeGoster }, zemin = p.form2, kenar = p.form2) {
                        Yazi("Tercih listem · ${liste.size}${if (pro) "/$LISTE_SINIRI" else "/$UCRETSIZ_LISTE"}", agirlik = FontWeight.Bold)
                        if (listeGoster) listeKadrolari.forEachIndexed { i, k ->
                            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Yazi("${i + 1}. ${k.unvan} · ${k.il}", Modifier.weight(1f), boyut = 14)
                                Yazi("Çıkar", Modifier.clickable { listeDegistir(k) }, boyut = 13, agirlik = FontWeight.SemiBold, renk = p.bad)
                            }
                        } else Yazi("Görmek için dokun · Paylaş simgesiyle gönder", boyut = 13, renk = p.ink2)
                    }
                    when {
                        v == null -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = p.ink2) }
                        puan == null -> Yazi("Sonuçları görmek için puanını yaz.", Modifier.padding(top = 8.dp), renk = p.ink3)
                        else -> Yazi("${sonuc.size} kadro · ${sonuc.count { it.second == Sans.GUVENLI }} güvenli, ${sonuc.count { it.second == Sans.OLASI }} olası, ${sonuc.count { it.second == Sans.ZOR }} zor",
                            Modifier.padding(top = 4.dp), boyut = 14, agirlik = FontWeight.SemiBold)
                    }
                }
            }
            val gorunen = if (pro) sonuc else sonuc.take(UCRETSIZ_SONUC)
            items(gorunen, key = { it.first.anahtar }) { (k, s) -> KadroKarti(k, s, k.anahtar in liste) { listeDegistir(k) } }
            if (!pro && sonuc.size > UCRETSIZ_SONUC) item {
                Kart(Modifier.fillMaxWidth(), zemin = p.ink, kenar = p.ink) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Lock, null, tint = p.marker)
                        Yazi("${sonuc.size - UCRETSIZ_SONUC} kadro daha seni bekliyor", agirlik = FontWeight.Bold, renk = p.paper)
                    }
                    Yazi("Tüm sonuçlar ve $LISTE_SINIRI tercihlik liste Aday Memur Pro’da.", Modifier.padding(vertical = 6.dp), boyut = 14, renk = p.paper)
                    Buton("Pro’yu dene", git::pro, Modifier.fillMaxWidth())
                }
            }
            item {
                Yazi("Puanlar ÖSYM’nin önceki merkezi yerleştirme sonuçlarındaki en küçük ve en büyük puanlardır; yeni dönemde kontenjan ve taban puanlar değişir. Tercih yapmadan önce ÖSYM tercih kılavuzundaki kadro kodu ve nitelik şartlarını mutlaka kontrol et.",
                    Modifier.padding(top = 8.dp), boyut = 12, renk = p.ink3)
            }
        }
    }
}

@Composable
private fun KadroKarti(k: TercihKadro, s: Sans, listede: Boolean, degistir: () -> Unit) {
    val p = T.p
    val (bg, fg) = when (s) {
        Sans.GUVENLI -> p.okBg to p.ok
        Sans.OLASI -> p.warnBg to p.warn
        else -> p.badBg to p.bad
    }
    Kart(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Yazi(k.unvan, agirlik = FontWeight.Bold)
                Yazi("${k.kurum}${if (k.il.isNotBlank()) " · " + k.il else ""}", boyut = 13, renk = p.ink2)
                Yazi("Kod ${k.k} · KPSS-${k.donem} · ${k.kont} kontenjan", boyut = 12, renk = p.ink3)
            }
            Box(Modifier.clip(RoundedCornerShape(999.dp)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)) {
                Yazi(s.etiket, boyut = 12, agirlik = FontWeight.Bold, renk = fg)
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Yazi("Taban ${puanYaz(k.min)}  ·  Tavan ${puanYaz(k.max)}", Modifier.weight(1f), boyut = 13, agirlik = FontWeight.SemiBold)
            Buton(if (listede) "✓ Listede" else "+ Listeme ekle", degistir, tur = if (listede) ButonTur.IKINCIL else ButonTur.ANA)
        }
    }
}
