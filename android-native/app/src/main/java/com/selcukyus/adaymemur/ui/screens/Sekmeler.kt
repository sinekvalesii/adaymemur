package com.selcukyus.adaymemur.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.selcukyus.adaymemur.BuildConfig
import com.selcukyus.adaymemur.ads.Reklam
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.Durum
import com.selcukyus.adaymemur.data.Ilan
import com.selcukyus.adaymemur.data.Sabitler
import com.selcukyus.adaymemur.data.Uygunluk
import com.selcukyus.adaymemur.data.UygunlukSonucu
import com.selcukyus.adaymemur.ui.components.Bos
import com.selcukyus.adaymemur.ui.components.Buton
import com.selcukyus.adaymemur.ui.components.ButonTur
import com.selcukyus.adaymemur.ui.components.Cip
import com.selcukyus.adaymemur.ui.components.Fosforlu
import com.selcukyus.adaymemur.ui.components.Grup
import com.selcukyus.adaymemur.ui.components.IlanKarti
import com.selcukyus.adaymemur.ui.components.Kart
import com.selcukyus.adaymemur.ui.components.Satir
import com.selcukyus.adaymemur.ui.components.SekmeBasligi
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.theme.T

fun tarayicidaAc(ctx: Context, url: String) {
    runCatching { CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(ctx, url.toUri()) }
        .onFailure { runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) } }
}

// --- Bana Uygun -------------------------------------------------------------------
@Composable
fun UygunEkrani(git: Gezgin, bildir: (String) -> Unit) {
    val p = T.p
    val ilanlar by Depo.ilanlar.collectAsState()
    val profil by Depo.profil.akis.collectAsState()
    val kayitlar by Depo.kayitlar.akis.collectAsState()
    val pro by Pro.pro.collectAsState()
    var sekme by remember { mutableStateOf(Durum.UYGUN) }

    if (!profil.tamamlandi) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            SekmeBasligi("Bana Uygun", "Kişisel ilan eşleştirme")
            Kart(Modifier.padding(16.dp).fillMaxWidth()) {
                Fosforlu("Sana uygun ilanları tek listede gör", 20)
                Yazi("Öğrenim düzeyin, bölümün, KPSS puanın ve il tercihin her ilanın şartlarıyla karşılaştırılır. Başvurabileceğin ilanlar öne çıkar.", Modifier.padding(top = 12.dp), renk = p.ink2)
                listOf("Öğrenim ve bölüm uyumu", "KPSS puan türü ve taban puan", "Kadro yeri ve son başvuru").forEach {
                    Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, null, Modifier.size(18.dp), tint = p.ok)
                        Yazi(it)
                    }
                }
                Buton("Profilimi oluştur", git::profil, Modifier.padding(top = 16.dp).fillMaxWidth())
            }
        }
        return
    }

    val sonuc = remember(ilanlar, profil) {
        ilanlar.filter { (Bicim.kalanGun(it.sonBasvuru) ?: 0) >= 0 }
            .map { it to Uygunluk.hesapla(it, profil) }
            .sortedWith(compareByDescending<Pair<Ilan, UygunlukSonucu>> { it.second.puan }.thenBy { it.first.sonBasvuru ?: "9" })
    }
    val uygun = sonuc.filter { it.second.durum == Durum.UYGUN }
    val kontrol = sonuc.filter { it.second.durum == Durum.KONTROL }

    // Pro: Kariyer Kapısı ilanlarının metnini arka planda çekip şartları netleştir
    LaunchedEffect(pro, kontrol.size) {
        if (pro) kontrol.map { it.first }.filter { it.kaynak == "kariyerkapisi" && it.egitim.isEmpty() }.take(20).forEach { Depo.metin(it) }
    }

    val ozet = listOfNotNull(
        profil.egitim?.let { Sabitler.EGITIM_ADI[it] }, profil.bolum.ifBlank { null },
        *profil.puanlar.map { "${it.key} ${Bicim.puan(it.value)}" }.toTypedArray(),
    ).joinToString(" · ")
    val liste = if (sekme == Durum.UYGUN) uygun else kontrol

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            SekmeBasligi("Bana Uygun", ozet) { Box(Modifier.padding(bottom = 4.dp)) { Buton("Düzenle", git::profil, tur = ButonTur.SADE) } }
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Cip("Uygun · ${uygun.size}", sekme == Durum.UYGUN) { sekme = Durum.UYGUN }
                Cip("Kontrol et · ${kontrol.size}", sekme == Durum.KONTROL) { sekme = Durum.KONTROL }
            }
        }
        if (!pro) {
            item {
                Kart(Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(p.marker), contentAlignment = Alignment.Center) { Icon(Icons.Default.Lock, null, tint = p.onMarker) }
                    Yazi(if (uygun.isNotEmpty()) "${uygun.size} ilan profilinle eşleşiyor" else "Eşleşmeleri Pro ile gör", Modifier.padding(top = 12.dp), boyut = 20, agirlik = FontWeight.ExtraBold)
                    Yazi(
                        (if (kontrol.isNotEmpty()) "${kontrol.size} ilan için de şartları birlikte kontrol edelim. " else "") +
                            "Kişisel listeni, her ilanın neden uygun olduğunu ve yeni eşleşmelerde anında bildirimi Pro ile aç.",
                        Modifier.padding(top = 8.dp), renk = p.ink2,
                    )
                    Buton("Pro’yu incele", git::pro, Modifier.padding(top = 14.dp).fillMaxWidth())
                }
            }
        } else if (liste.isEmpty()) {
            item {
                Bos(
                    Icons.Outlined.TrackChanges,
                    if (sekme == Durum.UYGUN) "Şu an tam eşleşen ilan yok" else "Kontrol edilecek ilan yok",
                    "Yeni ilanlar geldikçe bu liste güncellenir. Bildirimler açıksa eşleşmeleri kaçırmazsın.",
                )
            }
        } else {
            items(liste, key = { it.first.id }) { (ilan, u) ->
                IlanKarti(
                    ilan, kayitlar.containsKey(ilan.id), { kaydet(ilan, pro, bildir, git) }, { git.ilan(ilan.id) },
                    Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp), uygunluk = u,
                )
            }
        }
    }
}

// --- Kaydedilenler ----------------------------------------------------------------
@Composable
fun KayitliEkrani(git: Gezgin, bildir: (String) -> Unit) {
    val p = T.p
    val kayitlar by Depo.kayitlar.akis.collectAsState()
    val ilanlar by Depo.ilanlar.collectAsState()
    val pro by Pro.pro.collectAsState()
    val hepsi = remember(kayitlar, ilanlar) {
        val canli = ilanlar.associateBy { it.id }
        kayitlar.values.map { canli[it.ilan.id] ?: it.ilan }
    }
    val aktif = hepsi.filter { (Bicim.kalanGun(it.sonBasvuru) ?: 0) >= 0 }.sortedBy { it.sonBasvuru ?: "9" }
    val biten = hepsi.filter { (Bicim.kalanGun(it.sonBasvuru) ?: 0) < 0 }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            SekmeBasligi(
                "Kaydedilenler",
                when {
                    hepsi.isEmpty() -> "Takip ettiğin ilanlar"
                    pro -> "${hepsi.size} ilan · son başvuruya göre sıralı"
                    else -> "${hepsi.size}/${BuildConfig.UCRETSIZ_KAYIT_LIMITI} ilan kaydedildi"
                },
            )
        }
        if (hepsi.isEmpty()) {
            item {
                Bos(Icons.Outlined.BookmarkBorder, "Henüz ilan kaydetmedin", "İlan kartındaki işarete dokun; son başvuru tarihleri burada sıralansın.") {
                    Buton("İlanlara göz at", git::ilanlarSekmesi, Modifier.fillMaxWidth(), tur = ButonTur.IKINCIL)
                }
            }
        }
        listOf("BAŞVURUSU AÇIK" to aktif, "SÜRESİ DOLANLAR" to biten).forEach { (baslik, liste) ->
            if (liste.isNotEmpty()) {
                item { Yazi(baslik, Modifier.padding(start = 20.dp, top = 12.dp, bottom = 8.dp), boyut = 12, agirlik = FontWeight.Bold, renk = p.ink3) }
                items(liste, key = { it.id }) { ilan ->
                    IlanKarti(ilan, true, { kaydet(ilan, pro, bildir, git) }, { git.ilan(ilan.id) }, Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp))
                }
            }
        }
    }
}

// --- Hesap ------------------------------------------------------------------------
@Composable
fun HesapEkrani(git: Gezgin, bildirimIzniIste: () -> Unit, bildir: (String) -> Unit = {}) {
    val p = T.p
    val ctx = LocalContext.current
    val pro by Pro.pro.collectAsState()
    val profil by Depo.profil.akis.collectAsState()
    val ayar by Depo.ayarlar.akis.collectAsState()
    val gizlilik by Reklam.gizlilikSecenegi.collectAsState()
    val site = BuildConfig.SITE_URL
    val profilOzet = if (profil.tamamlandi) listOfNotNull(
        profil.egitim?.let { Sabitler.EGITIM_ADI[it] }, profil.bolum.ifBlank { null },
        *profil.puanlar.map { "${it.key} ${Bicim.puan(it.value)}" }.toTypedArray(),
    ).joinToString(" · ") else "Bana Uygun için öğrenim ve puan bilgilerini gir"

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
        SekmeBasligi("Hesap")
        val zemin = if (p.koyu) p.sheet else p.ink
        val yazi = if (p.koyu) p.ink else p.paper
        Kart(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp).fillMaxWidth(), zemin = zemin, kenar = if (p.koyu) p.line else p.ink, onClick = if (pro) null else git::pro) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, null, tint = p.marker)
                Yazi(if (pro) "Aday Memur Pro" else "Pro’ya geç", Modifier.weight(1f), boyut = 17, agirlik = FontWeight.ExtraBold, renk = yazi)
                if (!pro) Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = yazi)
            }
            Yazi(
                if (pro) "Aboneliğin etkin. Teşekkürler!" else "Sana uygun ilan listesi, anında bildirim, erken erişim ve reklamsız kullanım.",
                Modifier.padding(top = 6.dp), boyut = 13, renk = yazi.copy(alpha = 0.8f),
            )
            if (pro) Buton("Aboneliği yönet", {
                tarayicidaAc(ctx, "https://play.google.com/store/account/subscriptions?package=${ctx.packageName}")
            }, Modifier.padding(top = 10.dp).fillMaxWidth(), tur = ButonTur.IKINCIL)
        }

        HesapBolumu(git, bildir)

        Grup("Profil") { Satir(Icons.Outlined.School, "Öğrenim ve KPSS bilgilerim", profilOzet, onClick = git::profil) }

        Grup(
            "Bildirimler",
            if (pro) "Pro: yeni ilan yayımlandığında bildirim alırsın." else "Ücretsiz planda her akşam günün yeni ilanlarının özeti gelir. Anında bildirim Pro’da.",
        ) {
            Satir(Icons.Outlined.Notifications, "Yeni ilan bildirimleri", sag = {
                Switch(
                    checked = ayar.bildirim,
                    onCheckedChange = { v -> if (v) bildirimIzniIste(); Depo.ayarlar.guncelle { it.copy(bildirim = v) } },
                    colors = SwitchDefaults.colors(checkedTrackColor = p.formInk, checkedThumbColor = p.sheet),
                )
            })
            if (pro && ayar.bildirim) {
                Satir(
                    Icons.Outlined.Place, "Bildirim alınacak iller",
                    ayar.bildirimIlleri.ifEmpty { null }?.joinToString(", ") { Sabitler.ILLER[it] ?: it } ?: "Tüm Türkiye",
                    onClick = git::bildirimIlleri,
                )
            }
        }

        Grup("Uygulama") {
            Satir(Icons.Outlined.School, "KPSS tercih asistanı", "Puanınla yerleşebileceğin kadrolar, tercih listen", onClick = git::tercih)
            Satir(Icons.Outlined.CalendarMonth, "KPSS takvimi ve duyurular", onClick = git::gundem)
            Satir(Icons.Outlined.StarOutline, "Uygulamayı değerlendir", onClick = { tarayicidaAc(ctx, "https://play.google.com/store/apps/details?id=${ctx.packageName}") })
            Satir(Icons.Outlined.Share, "Arkadaşına öner", onClick = {
                val m = "Kamu ilanlarını ve KPSS gündemini tek yerden takip ediyorum: Aday Memur\nhttps://play.google.com/store/apps/details?id=${ctx.packageName}"
                ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, m), "Paylaş"))
            })
            if (!pro) Satir(Icons.Outlined.Refresh, "Satın alımları geri yükle", onClick = { Pro.geriYukle() })
            if (gizlilik) Satir(Icons.Outlined.Shield, "Reklam gizlilik ayarları", onClick = { (ctx as? Activity)?.let { Reklam.gizlilikAyarlari(it) } })
            if (BuildConfig.DESTEK_EPOSTA.isNotBlank()) {
                Satir(Icons.Outlined.MailOutline, "Destek", BuildConfig.DESTEK_EPOSTA, onClick = {
                    runCatching { ctx.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:${BuildConfig.DESTEK_EPOSTA}?subject=Aday%20Memur".toUri())) }
                })
            }
        }

        Grup("Yasal") {
            Satir(Icons.Outlined.Info, "Veri kaynakları ve sorumluluk", onClick = git::hakkinda)
            Satir(Icons.Outlined.Shield, "Gizlilik politikası", onClick = { tarayicidaAc(ctx, "$site/gizlilik.html") })
            Satir(Icons.Outlined.Description, "Kullanım koşulları", onClick = { tarayicidaAc(ctx, "$site/kosullar.html") })
        }

        Column(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Yazi("Aday Memur ${BuildConfig.VERSION_NAME}", boyut = 12, renk = p.ink3)
            Yazi("Yapım: ${Sabitler.MAKER}", boyut = 12, renk = p.ink3)
        }
        Spacer(Modifier.height(16.dp))
    }
}
