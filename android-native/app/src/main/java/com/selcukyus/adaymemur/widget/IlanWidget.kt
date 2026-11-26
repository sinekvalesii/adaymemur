package com.selcukyus.adaymemur.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.selcukyus.adaymemur.MainActivity
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.Durum
import com.selcukyus.adaymemur.data.Ilan
import com.selcukyus.adaymemur.data.Uygunluk

private data class WidgetVeri(val baslik: String, val alt: String, val ilanlar: List<Ilan>)

private fun hesapla(): WidgetVeri {
    val p = Depo.profil.deger
    val aktif = Depo.ilanlar.value.filter { (Bicim.kalanGun(it.sonBasvuru) ?: 0) >= 0 }
    val liste = if (p.tamamlandi) aktif.filter { Uygunluk.hesapla(it, p).durum == Durum.UYGUN } else aktif
    val sirali = liste.sortedBy { Bicim.kalanGun(it.sonBasvuru) ?: Long.MAX_VALUE }
    return if (p.tamamlandi) WidgetVeri("Sana uygun ${liste.size} ilan", "Son günü yaklaşanlar", sirali.take(3))
    else WidgetVeri("${aktif.size} güncel ilan", "Profilini doldur, sana uygunları gösterelim", sirali.take(3))
}

class IlanWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Depo.baslat(context)
        val v = hesapla()
        provideContent { Icerik(v) }
    }

    companion object {
        suspend fun guncelle(context: Context) = runCatching { IlanWidget().updateAll(context) }
    }
}

private val KAGIT = ColorProvider(Color(0xFF171A1F))
private val MUREKKEP = ColorProvider(Color(0xFFF1F3F6))
private val SOLUK = ColorProvider(Color(0xFF9AA1AC))
private val FOSFOR = ColorProvider(Color(0xFFFFE45A))

@Composable
private fun Icerik(v: WidgetVeri) {
    Column(
        GlanceModifier.fillMaxSize().background(KAGIT).cornerRadius(20.dp).padding(14.dp)
            .clickable(actionStartActivity<MainActivity>()),
    ) {
        Text("Aday Memur", style = TextStyle(color = FOSFOR, fontSize = 12.sp, fontWeight = FontWeight.Bold))
        Text(v.baslik, style = TextStyle(color = MUREKKEP, fontSize = 17.sp, fontWeight = FontWeight.Bold))
        Text(v.alt, style = TextStyle(color = SOLUK, fontSize = 11.sp))
        Spacer(GlanceModifier.height(8.dp))
        if (v.ilanlar.isEmpty()) Text("Şu an listelenecek ilan yok.", style = TextStyle(color = SOLUK, fontSize = 12.sp))
        v.ilanlar.forEach { i ->
            Row(GlanceModifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                val k = Bicim.kalanGun(i.sonBasvuru)
                Text(
                    if (k == null) "—" else if (k == 0L) "Bugün" else "${k}g",
                    modifier = GlanceModifier.width(44.dp),
                    style = TextStyle(color = if (k != null && k <= 2) FOSFOR else SOLUK, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                )
                Text(i.baslik, maxLines = 1, style = TextStyle(color = MUREKKEP, fontSize = 13.sp))
            }
        }
    }
}

class IlanWidgetAlici : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = IlanWidget()
}
