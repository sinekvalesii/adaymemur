package com.selcukyus.adaymemur.data

import com.selcukyus.adaymemur.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID

/** Pro takip ve açılış bildirimlerinin ortak mantığı. */
object Takipler {

    /** İlandan takip önerisi: unvanlar (yoksa başlık), kurum "Tümü", iller profilden. */
    fun oner(i: Ilan) = Takip(
        id = UUID.randomUUID().toString(),
        unvanlar = i.unvanlar.ifEmpty { listOf(i.baslik.substringBefore(" alım").take(60)) }.take(5),
    )

    fun ayni(a: Takip, i: Ilan) = a.unvanlar.map { Bicim.katla(it) }.toSet() == oner(i).unvanlar.map { Bicim.katla(it) }.toSet()

    fun eslesir(t: Takip, i: Ilan, iller: List<String>): Boolean {
        val metin = Bicim.katla((i.unvanlar + i.baslik).joinToString(" "))
        if (t.unvanlar.none { u -> Bicim.katla(u).let { it.isNotEmpty() && metin.contains(it) } }) return false
        if (t.kurum.isNotEmpty() && !Bicim.katla(i.kurum).contains(Bicim.katla(t.kurum))) return false
        if (t.profilIlleri && iller.isNotEmpty() && i.iller.isNotEmpty() && i.iller.none { it in iller }) return false
        return true
    }

    /** Pro kullanıcıya bu ilan bildirilmeli mi? Profil uygunluğu veya takip eşleşmesi. */
    fun ilgili(i: Ilan, p: Profil, a: Ayarlar): Boolean {
        val iller = p.iller.ifEmpty { a.bildirimIlleri }
        if (a.takipler.any { eslesir(it, i, iller) }) return true
        if (p.tamamlandi) return Uygunluk.hesapla(i, p).durum != Durum.DEGIL
        return a.takipler.isEmpty() && (a.bildirimIlleri.isEmpty() || i.iller.isEmpty() || i.iller.any { it in a.bildirimIlleri })
    }

    fun basvuruAcilmadi(i: Ilan) = i.basvuruBaslangic?.let { it > LocalDate.now().toString() } == true

    /**
     * E-posta bildirimi için abonelik bilgisini sunucuya yazar (eski kaydı siler, yenisini ekler).
     * Sunucu aynı mantıkla eşleştirip e-posta gönderir. Hata olursa false.
     */
    suspend fun bulutaYaz(eposta: String, p: Profil, a: Ayarlar): String? = withContext(Dispatchers.IO) {
        runCatching {
            if (a.epostaToken.isNotEmpty()) istek("rpc/abonelik_iptal", """{"t":"${a.epostaToken}"}""")
            if (eposta.isBlank()) return@runCatching ""
            val token = UUID.randomUUID().toString()
            val govde = buildJsonObject {
                put("eposta", eposta.trim())
                put("token", token)
                put("profil", buildJsonObject {
                    put("kaynak", "android-pro")
                    p.egitim?.let { put("egitim", it) }
                    put("bolum", p.bolum)
                    put("puanlar", buildJsonObject { p.puanlar.forEach { (k, v) -> put(k, v) } })
                    put("iller", buildJsonArray { p.iller.ifEmpty { a.bildirimIlleri }.forEach { add(JsonPrimitive(it)) } })
                    put("takip", buildJsonArray {
                        a.takipler.forEach { t ->
                            add(buildJsonObject {
                                put("unvanlar", buildJsonArray { t.unvanlar.forEach { add(JsonPrimitive(it)) } })
                                put("kurum", t.kurum); put("profilIlleri", t.profilIlleri)
                            })
                        }
                    })
                    put("acilis", buildJsonArray { a.acilisTakip.keys.forEach { add(JsonPrimitive(it)) } })
                })
            }
            if (istek("aboneler", govde.toString()) !in 200..299) error("kayit")
            token
        }.getOrNull()
    }

    private fun istek(yol: String, govde: String): Int {
        val c = URL("${BuildConfig.SUPABASE_URL}/rest/v1/$yol").openConnection() as HttpURLConnection
        c.requestMethod = "POST"; c.doOutput = true; c.connectTimeout = 15000; c.readTimeout = 15000
        c.setRequestProperty("apikey", BuildConfig.SUPABASE_KEY)
        c.setRequestProperty("Content-Type", "application/json")
        c.setRequestProperty("Prefer", "return=minimal")
        c.outputStream.use { it.write(govde.toByteArray()) }
        return c.responseCode.also { c.disconnect() }
    }
}
