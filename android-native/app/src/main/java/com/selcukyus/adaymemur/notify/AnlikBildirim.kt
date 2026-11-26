package com.selcukyus.adaymemur.notify

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.selcukyus.adaymemur.BuildConfig
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Depo
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

/**
 * Anında bildirim (Firebase Cloud Messaging). Sunucu yeni ilanı yayımladığı anda Pro kullanıcılara gönderir.
 * google-services.json yoksa Firebase başlatılmaz ve bu sınıf hiçbir şey yapmaz; 15 dk'lık kontrol çalışmaya devam eder.
 */
class AnlikBildirim : FirebaseMessagingService() {
    override fun onNewToken(token: String) { Thread { Cihaz.kaydet(applicationContext, token) }.start() }

    override fun onMessageReceived(m: RemoteMessage) {
        val d = m.data
        val baslik = d["baslik"] ?: m.notification?.title ?: return
        if (!IlanIscisi.izinVar(applicationContext) || !Depo.run { baslat(applicationContext); ayarlar.deger.bildirim }) return
        IlanIscisi.goster(applicationContext, (d["ilanId"] ?: baslik).hashCode(), baslik, d["metin"] ?: m.notification?.body.orEmpty(), d["ilanId"]?.ifBlank { null })
    }
}

object Cihaz {
    fun etkin(ctx: Context) = FirebaseApp.getApps(ctx).isNotEmpty()

    /** Güncel jetonu ve eşleştirme profilini sunucuya yazar. */
    fun guncelle(ctx: Context) {
        if (!etkin(ctx)) return
        runCatching { FirebaseMessaging.getInstance().token.addOnSuccessListener { t -> Thread { kaydet(ctx, t) }.start() } }
    }

    fun kaydet(ctx: Context, token: String) = runCatching {
        Depo.baslat(ctx)
        val p = Depo.profil.deger; val a = Depo.ayarlar.deger
        val profil = buildJsonObject {
            p.egitim?.let { put("egitim", it) }
            put("bolum", p.bolum)
            put("puanlar", buildJsonObject { p.puanlar.forEach { (k, v) -> put(k, v) } })
            put("iller", buildJsonArray { (a.bildirimIlleri.ifEmpty { p.iller }).forEach { add(JsonPrimitive(it)) } })
            put("takip", buildJsonArray {
                a.takipler.forEach { t -> add(buildJsonObject {
                    put("id", t.id); put("kurum", t.kurum); put("profilIlleri", t.profilIlleri)
                    put("unvanlar", buildJsonArray { t.unvanlar.forEach { add(JsonPrimitive(it)) } })
                }) }
            })
            put("acilis", buildJsonArray { a.acilisTakip.keys.forEach { add(JsonPrimitive(it)) } })
            put("kaynak", "android")
        }
        val govde = buildJsonObject {
            put("p_token", token); put("p_profil", profil); put("p_pro", Pro.pro.value && a.bildirim)
        }
        val c = URL("${BuildConfig.SUPABASE_URL}/rest/v1/rpc/cihaz_kaydet").openConnection() as HttpURLConnection
        c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 15000; c.readTimeout = 15000
        c.setRequestProperty("apikey", BuildConfig.SUPABASE_KEY)
        c.setRequestProperty("Content-Type", "application/json")
        c.outputStream.use { it.write(govde.toString().toByteArray()) }
        c.responseCode; c.disconnect()
    }
}
