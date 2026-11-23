package com.selcukyus.adaymemur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.selcukyus.adaymemur.data.Hesap
import com.selcukyus.adaymemur.ui.components.AltBaslik
import com.selcukyus.adaymemur.ui.components.Buton
import com.selcukyus.adaymemur.ui.components.ButonTur
import com.selcukyus.adaymemur.ui.components.Girdi
import com.selcukyus.adaymemur.ui.components.Grup
import com.selcukyus.adaymemur.ui.components.Satir
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.theme.T
import kotlinx.coroutines.launch

private val TEL = Regex("^(\\+90|0)?5\\d{9}$")
private fun telDuz(t: String) = "0" + t.replace(Regex("[\\s()-]"), "").replace(Regex("^(\\+90|0)"), "")
private fun telOk(t: String) = TEL.matches(t.replace(Regex("[\\s()-]"), ""))
private fun epostaOk(e: String) = Regex("^\\S+@\\S+\\.\\S+$").matches(e.trim())

@Composable
fun GirisEkrani(git: Gezgin, bildir: (String) -> Unit, kayitModu: Boolean = false) {
    val p = T.p
    val kapsam = rememberCoroutineScope()
    var kayit by remember { mutableStateOf(kayitModu) }
    var ad by remember { mutableStateOf("") }
    var soyad by remember { mutableStateOf("") }
    var tel by remember { mutableStateOf("") }
    var eposta by remember { mutableStateOf("") }
    var sifre by remember { mutableStateOf("") }
    var hata by remember { mutableStateOf<String?>(null) }
    var bilgi by remember { mutableStateOf<String?>(null) }
    var yukleniyor by remember { mutableStateOf(false) }

    fun gonder() {
        hata = null; bilgi = null
        if (kayit) {
            if (ad.isBlank() || soyad.isBlank()) { hata = "Ad ve soyad zorunlu."; return }
            if (!telOk(tel)) { hata = "Telefonu 05xx xxx xx xx biçiminde yaz."; return }
        }
        if (!epostaOk(eposta)) { hata = "Geçerli bir e-posta adresi yaz."; return }
        if (sifre.length < 6) { hata = "Şifre en az 6 karakter olmalı."; return }
        yukleniyor = true
        kapsam.launch {
            runCatching {
                if (kayit) Hesap.kayitOl(ad, soyad, telDuz(tel), eposta, sifre) else Hesap.girisYap(eposta, sifre)
            }.onSuccess {
                bildir(if (kayit) "Hesabın oluşturuldu. Hoş geldin!" else "Giriş yapıldı")
                git.geri()
            }.onFailure { hata = it.message ?: "Bir sorun oluştu." }
            yukleniyor = false
        }
    }

    fun unuttum() {
        hata = null; bilgi = null
        if (!epostaOk(eposta)) { hata = "Önce hesabına ait e-posta adresini yaz, sonra “Şifremi unuttum”a dokun."; return }
        yukleniyor = true
        kapsam.launch {
            runCatching { Hesap.sifreSifirla(eposta) }
                .onSuccess { bilgi = "Şifre yenileme bağlantısı ${eposta.trim()} adresine gönderildi. Gelen kutunu ve spam klasörünü kontrol et." }
                .onFailure { hata = it.message }
            yukleniyor = false
        }
    }

    Column(Modifier.fillMaxSize().background(p.paper).imePadding()) {
        AltBaslik(null, git::geri, kapatIkonu = true)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Yazi(if (kayit) "Hesap oluştur" else "Giriş yap", boyut = 28, agirlik = FontWeight.ExtraBold)
            Yazi(
                "Profilin, takiplerin ve bildirim tercihlerin hesabında saklanır. Sana uygun yeni ilan çıktığında e-postayla da haber veririz. Aynı hesapla internet sitesine de girebilirsin.",
                Modifier.padding(top = 6.dp, bottom = 16.dp), renk = p.ink2,
            )
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(p.sheet).border(1.dp, p.line, RoundedCornerShape(12.dp)).padding(4.dp),
            ) {
                listOf(false to "Giriş", true to "Kayıt ol").forEach { (k, e) ->
                    Box(
                        Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (kayit == k) p.ink else p.sheet)
                            .clickable { kayit = k; hata = null; bilgi = null }.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) { Yazi(e, agirlik = FontWeight.Bold, renk = if (kayit == k) p.paper else p.ink2) }
                }
            }
            Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (kayit) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Girdi(ad, { ad = it }, Modifier.weight(1f), etiket = "Ad *", ikon = Icons.Outlined.Person)
                        Girdi(soyad, { soyad = it }, Modifier.weight(1f), etiket = "Soyad *")
                    }
                    Girdi(tel, { tel = it }, etiket = "Telefon *", ipucu = "05xx xxx xx xx", ikon = Icons.Outlined.Phone, klavye = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
                Girdi(eposta, { eposta = it }, etiket = "E-posta *", ikon = Icons.Outlined.MailOutline, klavye = KeyboardOptions(keyboardType = KeyboardType.Email))
                Girdi(sifre, { sifre = it }, etiket = "Şifre *", ipucu = "En az 6 karakter", ikon = Icons.Outlined.Lock, klavye = KeyboardOptions(keyboardType = KeyboardType.Password), sifre = true)
            }
            hata?.let { Yazi(it, Modifier.padding(top = 12.dp), agirlik = FontWeight.SemiBold, renk = p.bad) }
            bilgi?.let { Yazi(it, Modifier.padding(top = 12.dp), agirlik = FontWeight.SemiBold, renk = p.ok) }
            Buton(if (kayit) "Hesap oluştur" else "Giriş yap", ::gonder, Modifier.padding(top = 18.dp).fillMaxWidth(), yukleniyor = yukleniyor)
            if (!kayit) Buton("Şifremi unuttum", ::unuttum, Modifier.padding(top = 6.dp).fillMaxWidth(), tur = ButonTur.SADE, pasif = yukleniyor)
            else Yazi("Kayıt olarak gizlilik politikasını ve kullanım koşullarını kabul etmiş olursun.", Modifier.padding(top = 10.dp), boyut = 12, renk = p.ink3)
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Hesap sekmesindeki hesap bölümü. */
@Composable
fun HesapBolumu(git: Gezgin, bildir: (String) -> Unit) {
    val p = T.p
    val kapsam = rememberCoroutineScope()
    val o by Hesap.oturum.collectAsState()
    var silOnay by remember { mutableStateOf(false) }
    var duzenle by remember { mutableStateOf(false) }
    val oturum = o
    if (oturum == null) {
        Grup("Hesabım", "Ücretsiz hesapla profilin ve takiplerin saklanır, sana uygun ilanlar e-postana da gelir.") {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Buton("Hesap oluştur", git::kayit, Modifier.fillMaxWidth())
                Buton("Giriş yap", git::giris, Modifier.fillMaxWidth(), tur = ButonTur.IKINCIL)
            }
        }
        return
    }
    Grup("Hesabım") {
        Satir(Icons.Outlined.Person, listOf(oturum.ad, oturum.soyad).joinToString(" ").ifBlank { "Hesabım" }, "${oturum.eposta}${if (oturum.telefon.isNotBlank()) " · " + oturum.telefon else ""}", onClick = { duzenle = true })
        Satir(Icons.Outlined.MailOutline, "E-posta bildirimi", "Sana uygun yeni ilan çıkınca e-posta gelir", sag = {
            Switch(
                checked = oturum.epostaBildirim,
                onCheckedChange = { v -> kapsam.launch { runCatching { Hesap.epostaBildirimi(v) }.onFailure { bildir(it.message ?: "Kaydedilemedi") } } },
                colors = SwitchDefaults.colors(checkedTrackColor = p.formInk, checkedThumbColor = p.sheet),
            )
        })
        Satir(null, "Çıkış yap", onClick = { kapsam.launch { Hesap.cikisYap(); bildir("Çıkış yapıldı") } })
        Satir(null, "Hesabımı sil", tehlike = true, onClick = { silOnay = true })
    }
    if (silOnay) AlertDialog(
        onDismissRequest = { silOnay = false },
        title = { Yazi("Hesabın silinsin mi?", boyut = 18, agirlik = FontWeight.Bold) },
        text = { Yazi("Hesabın, profilin ve takiplerin kalıcı olarak silinir. Bu işlem geri alınamaz. Pro aboneliğin varsa Google Play’den ayrıca iptal etmelisin.", renk = p.ink2) },
        confirmButton = {
            TextButton({
                silOnay = false
                kapsam.launch { runCatching { Hesap.hesabiSil() }.onSuccess { bildir("Hesabın silindi") }.onFailure { bildir(it.message ?: "Silinemedi") } }
            }) { Yazi("Kalıcı olarak sil", agirlik = FontWeight.Bold, renk = p.bad) }
        },
        dismissButton = { TextButton({ silOnay = false }) { Yazi("Vazgeç", agirlik = FontWeight.SemiBold) } },
        containerColor = p.sheet,
    )
    if (duzenle) {
        var ad by remember { mutableStateOf(oturum.ad) }
        var soyad by remember { mutableStateOf(oturum.soyad) }
        var tel by remember { mutableStateOf(oturum.telefon) }
        var hata by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { duzenle = false },
            title = { Yazi("Hesap bilgileri", boyut = 18, agirlik = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Girdi(ad, { ad = it }, etiket = "Ad")
                    Girdi(soyad, { soyad = it }, etiket = "Soyad")
                    Girdi(tel, { tel = it }, etiket = "Telefon", klavye = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    hata?.let { Yazi(it, renk = p.bad) }
                }
            },
            confirmButton = {
                TextButton({
                    if (ad.isBlank() || soyad.isBlank()) { hata = "Ad ve soyad zorunlu."; return@TextButton }
                    if (!telOk(tel)) { hata = "Telefonu 05xx xxx xx xx biçiminde yaz."; return@TextButton }
                    kapsam.launch {
                        runCatching { Hesap.bilgileriGuncelle(ad.trim(), soyad.trim(), telDuz(tel)) }
                            .onSuccess { duzenle = false; bildir("Bilgilerin güncellendi") }
                            .onFailure { hata = it.message }
                    }
                }) { Yazi("Kaydet", agirlik = FontWeight.Bold, renk = p.formInk) }
            },
            dismissButton = { TextButton({ duzenle = false }) { Yazi("Vazgeç") } },
            containerColor = p.sheet,
        )
    }
}
