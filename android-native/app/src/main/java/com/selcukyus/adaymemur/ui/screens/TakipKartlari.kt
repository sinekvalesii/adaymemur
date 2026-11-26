package com.selcukyus.adaymemur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationAdd
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.Ilan
import com.selcukyus.adaymemur.data.Takipler
import com.selcukyus.adaymemur.ui.components.Buton
import com.selcukyus.adaymemur.ui.components.ButonTur
import com.selcukyus.adaymemur.ui.components.Kart
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.theme.T
import kotlinx.coroutines.launch

/** Ayarlar değişince Pro e-posta aboneliğini sunucuda tazeler. */
suspend fun epostaTazele() {
    val a = Depo.ayarlar.deger
    if (a.eposta.isBlank()) return
    Takipler.bulutaYaz(a.eposta, Depo.profil.deger, a)?.let { t -> Depo.ayarlar.guncelle { it.copy(epostaToken = t) } }
}

@Composable
private fun Satir(etiket: String, deger: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Yazi(etiket, Modifier.weight(0.35f), boyut = 14, renk = T.p.ink2)
        Yazi(deger, Modifier.weight(0.65f), boyut = 14, agirlik = FontWeight.SemiBold)
    }
}

/** İlan detayında: "Benzer ilanlar çıkınca haber al" ve "Başvuru açılınca haber ver" (Pro). */
@Composable
fun TakipKartlari(ilan: Ilan, pro: Boolean, proAc: () -> Unit, bildir: (String) -> Unit) {
    val p = T.p
    val ayar by Depo.ayarlar.akis.collectAsState()
    val profil by Depo.profil.akis.collectAsState()
    val kapsam = rememberCoroutineScope()
    val oneri = Takipler.oner(ilan)
    val mevcut = ayar.takipler.firstOrNull { Takipler.ayni(it, ilan) }
    val iller = profil.iller.ifEmpty { ayar.bildirimIlleri }

    if (Takipler.basvuruAcilmadi(ilan)) {
        val takipte = ilan.id in ayar.acilisTakip
        Kart(Modifier.padding(top = 12.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Outlined.Schedule, null, tint = p.formInk)
                Column(Modifier.weight(1f)) {
                    Yazi("Başvurular ${Bicim.uzun(ilan.basvuruBaslangic!!)} tarihinde açılıyor", agirlik = FontWeight.Bold)
                    Yazi("Başvuru açıldığı anda bildirim ve e-posta gönderelim.", boyut = 14, renk = p.ink2)
                }
            }
            Buton(if (takipte) "Açılınca haber verilecek ✓" else "Açılınca haber ver", {
                if (!pro) return@Buton proAc()
                Depo.ayarlar.guncelle { it.copy(acilisTakip = if (takipte) it.acilisTakip - ilan.id else it.acilisTakip + (ilan.id to ilan.basvuruBaslangic!!)) }
                bildir(if (takipte) "Açılış bildirimi kaldırıldı" else "Başvuru açılınca haber vereceğiz").also { if (!takipte) com.selcukyus.adaymemur.review.Degerlendirme.olumlu() }
                kapsam.launch { epostaTazele() }
            }, Modifier.padding(top = 12.dp).fillMaxWidth(), tur = if (takipte) ButonTur.IKINCIL else ButonTur.ANA)
            if (!pro) Yazi("Aday Memur Pro özelliği", Modifier.padding(top = 6.dp).align(Alignment.CenterHorizontally), boyut = 12, renk = p.ink3)
        }
    }

    Kart(Modifier.padding(top = 12.dp).fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.NotificationAdd, null, tint = p.formInk)
            Column(Modifier.weight(1f)) {
                Yazi("Benzer ilanlar çıkınca haber al", agirlik = FontWeight.Bold)
                Yazi("Yeni ${oneri.unvanlar.take(3).joinToString(", ").lowercase(java.util.Locale.forLanguageTag("tr"))} ilanları yayına girer girmez bildirim ve e-posta gelsin.", boyut = 14, renk = p.ink2)
            }
        }
        Column(Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.paper).padding(horizontal = 14.dp)) {
            Satir("Unvan", (mevcut ?: oneri).unvanlar.joinToString(", "))
            HorizontalDivider(color = p.line)
            Satir("Kurum", (mevcut ?: oneri).kurum.ifEmpty { "Tümü" })
            HorizontalDivider(color = p.line)
            Satir("İl", if (iller.isEmpty()) "Tüm Türkiye" else "Profilindeki iller")
        }
        Buton(if (mevcut != null) "Bildirim açık ✓ (kapat)" else "Bildirim al", {
            if (!pro) return@Buton proAc()
            Depo.ayarlar.guncelle { a -> a.copy(takipler = if (mevcut != null) a.takipler - mevcut else a.takipler + oneri) }
            bildir(if (mevcut != null) "Takip kapatıldı" else "Benzer ilanlar çıkınca haber vereceğiz").also { if (mevcut == null) com.selcukyus.adaymemur.review.Degerlendirme.olumlu() }
            kapsam.launch { epostaTazele() }
        }, Modifier.padding(top = 12.dp).fillMaxWidth(), tur = if (mevcut != null) ButonTur.IKINCIL else ButonTur.ANA)
        if (!pro) Yazi("Aday Memur Pro özelliği", Modifier.padding(top = 6.dp).align(Alignment.CenterHorizontally), boyut = 12, renk = p.ink3)
    }
}
