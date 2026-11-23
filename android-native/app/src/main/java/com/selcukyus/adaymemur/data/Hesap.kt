package com.selcukyus.adaymemur.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.selcukyus.adaymemur.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class Oturum(
    val erisim: String,
    val yenileme: String,
    val bitis: Long,
    val id: String,
    val eposta: String,
    val ad: String = "",
    val soyad: String = "",
    val telefon: String = "",
    val epostaBildirim: Boolean = true,
)

/**
 * Aday Memur hesabı — internet sitesiyle aynı hesap (Supabase Auth).
 * Profil, takipler ve bildirim tercihi buluta yazılır; yeni ilan e-postaları sunucudan gider.
 */
object Hesap {
    private lateinit var prefs: SharedPreferences
    private val kapsam = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _oturum = MutableStateFlow<Oturum?>(null)
    val oturum: StateFlow<Oturum?> = _oturum.asStateFlow()

    private val AUTH get() = "${BuildConfig.SUPABASE_URL}/auth/v1"
    private val REST get() = "${BuildConfig.SUPABASE_URL}/rest/v1"
    const val SIFRE_SAYFASI = "https://sinekvalesii.github.io/adaymemurweb/"

    class Hata(m: String) : Exception(m)

    @OptIn(FlowPreview::class)
    fun baslat(context: Context) {
        if (::prefs.isInitialized) return
        prefs = context.applicationContext.getSharedPreferences("aday_memur_hesap", Context.MODE_PRIVATE)
        _oturum.value = prefs.getString("oturum", null)?.let { runCatching { json.decodeFromString(Oturum.serializer(), it) }.getOrNull() }
        // Profil veya takipler değişince buluta yaz
        kapsam.launch {
            combine(Depo.profil.akis, Depo.ayarlar.akis) { p, a -> Triple(p, a.takipler, a.acilisTakip.keys) }
                .drop(1).debounce(1500).collect { if (_oturum.value != null) runCatching { profilYaz() } }
        }
        kapsam.launch { if (_oturum.value != null) runCatching { profilCek() } }
    }

    private fun kaydet(o: Oturum?) {
        _oturum.value = o
        prefs.edit { if (o == null) remove("oturum") else putString("oturum", json.encodeToString(Oturum.serializer(), o)) }
    }

    // ---- HTTP ----
    private fun istek(yontem: String, url: String, govde: JsonElement? = null, token: String? = null, prefer: String? = null): Pair<Int, String> {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = yontem; c.connectTimeout = 15000; c.readTimeout = 15000
        c.setRequestProperty("apikey", BuildConfig.SUPABASE_KEY)
        c.setRequestProperty("Content-Type", "application/json")
        token?.let { c.setRequestProperty("Authorization", "Bearer $it") }
        prefer?.let { c.setRequestProperty("Prefer", it) }
        if (govde != null) { c.doOutput = true; c.outputStream.use { it.write(govde.toString().toByteArray()) } }
        val kod = c.responseCode
        val metin = runCatching { (if (kod < 400) c.inputStream else c.errorStream)?.bufferedReader()?.readText() }.getOrNull().orEmpty()
        c.disconnect()
        return kod to metin
    }

    private fun hataMetni(govde: String): String {
        val o = runCatching { json.parseToJsonElement(govde).jsonObject }.getOrNull()
        val m = listOf("msg", "error_description", "message", "error").firstNotNullOfOrNull { o?.get(it)?.jsonPrimitive?.content }.orEmpty()
        return when {
            m.contains("Invalid login", true) -> "E-posta veya şifre hatalı."
            m.contains("already registered", true) -> "Bu e-postayla zaten hesap var. Giriş yap."
            m.contains("Password should be", true) -> "Şifre en az 6 karakter olmalı."
            m.contains("rate limit", true) || m.contains("only request this after", true) -> "Çok sık deneme yapıldı. Birkaç dakika sonra tekrar dene."
            m.contains("invalid", true) && m.contains("email", true) -> "Geçerli bir e-posta adresi yaz."
            m.contains("not confirmed", true) -> "E-posta adresin doğrulanmamış."
            m.isBlank() -> "Bağlantı kurulamadı. İnternetini kontrol et."
            else -> m
        }
    }

    private fun oturumOku(govde: String, ek: Oturum? = null): Oturum {
        val o = json.parseToJsonElement(govde).jsonObject
        val k = o["user"]?.jsonObject ?: throw Hata("Hesap oluşturuldu. Giriş yap.")
        val meta = k["user_metadata"] as? JsonObject
        fun m(a: String) = meta?.get(a)?.jsonPrimitive?.content.orEmpty()
        return Oturum(
            erisim = o["access_token"]?.jsonPrimitive?.content ?: throw Hata("Giriş yapılamadı."),
            yenileme = o["refresh_token"]!!.jsonPrimitive.content,
            bitis = System.currentTimeMillis() / 1000 + (o["expires_in"]?.jsonPrimitive?.longOrNull ?: 3600),
            id = k["id"]!!.jsonPrimitive.content,
            eposta = k["email"]?.jsonPrimitive?.content.orEmpty(),
            ad = ek?.ad ?: m("ad"), soyad = ek?.soyad ?: m("soyad"), telefon = ek?.telefon ?: m("telefon"),
            epostaBildirim = ek?.epostaBildirim ?: true,
        )
    }

    /** Geçerli erişim anahtarı; süresi dolmak üzereyse yeniler. */
    private fun token(): String {
        val o = _oturum.value ?: throw Hata("Oturum yok")
        if (o.bitis - 60 > System.currentTimeMillis() / 1000) return o.erisim
        val (kod, g) = istek("POST", "$AUTH/token?grant_type=refresh_token", buildJsonObject { put("refresh_token", o.yenileme) })
        if (kod !in 200..299) { if (kod in 400..499) kaydet(null); throw Hata("Oturum süresi doldu. Tekrar giriş yap.") }
        val y = oturumOku(g, o)
        kaydet(y)
        return y.erisim
    }

    // ---- Eylemler ----
    suspend fun kayitOl(ad: String, soyad: String, telefon: String, eposta: String, sifre: String) = withContext(Dispatchers.IO) {
        val (kod, g) = istek("POST", "$AUTH/signup", buildJsonObject {
            put("email", eposta.trim()); put("password", sifre)
            put("data", buildJsonObject { put("ad", ad.trim()); put("soyad", soyad.trim()); put("telefon", telefon) })
        })
        if (kod !in 200..299) throw Hata(hataMetni(g))
        val o = runCatching { json.parseToJsonElement(g).jsonObject }.getOrNull()
        if (o?.get("access_token") == null) throw Hata("Hesabın oluşturuldu. E-postana gelen bağlantıyı onayla, sonra giriş yap.")
        kaydet(oturumOku(g))
        profilOlustur()
    }

    suspend fun girisYap(eposta: String, sifre: String) = withContext(Dispatchers.IO) {
        val (kod, g) = istek("POST", "$AUTH/token?grant_type=password", buildJsonObject { put("email", eposta.trim()); put("password", sifre) })
        if (kod !in 200..299) throw Hata(hataMetni(g))
        kaydet(oturumOku(g))
        if (!profilCek()) profilOlustur()
    }

    suspend fun sifreSifirla(eposta: String) = withContext(Dispatchers.IO) {
        val hedef = java.net.URLEncoder.encode(SIFRE_SAYFASI, "UTF-8")
        val (kod, g) = istek("POST", "$AUTH/recover?redirect_to=$hedef", buildJsonObject { put("email", eposta.trim()) })
        if (kod !in 200..299) throw Hata(hataMetni(g))
    }

    suspend fun cikisYap() = withContext(Dispatchers.IO) {
        runCatching { istek("POST", "$AUTH/logout", token = token()) }
        kaydet(null)
    }

    /** Hesabı ve tüm verilerini kalıcı olarak siler (sunucuda hesap_sil fonksiyonu). */
    suspend fun hesabiSil() = withContext(Dispatchers.IO) {
        val (kod, g) = istek("POST", "$REST/rpc/hesap_sil", buildJsonObject { }, token = token())
        if (kod !in 200..299) throw Hata(hataMetni(g))
        kaydet(null)
    }

    suspend fun bilgileriGuncelle(ad: String, soyad: String, telefon: String) = withContext(Dispatchers.IO) {
        val o = _oturum.value ?: return@withContext
        satirGuncelle(buildJsonObject { put("ad", ad); put("soyad", soyad); put("telefon", telefon) })
        kaydet(o.copy(ad = ad, soyad = soyad, telefon = telefon))
    }

    suspend fun epostaBildirimi(acik: Boolean) = withContext(Dispatchers.IO) {
        val o = _oturum.value ?: return@withContext
        satirGuncelle(buildJsonObject { put("bildirim", acik) })
        kaydet(o.copy(epostaBildirim = acik))
    }

    private fun satirGuncelle(alan: JsonObject) {
        val o = _oturum.value ?: return
        val g = JsonObject(alan + ("guncelleme" to JsonPrimitive(java.time.Instant.now().toString())))
        val (kod, m) = istek("PATCH", "$REST/profiller?id=eq.${o.id}", g, token(), "return=minimal")
        if (kod !in 200..299) throw Hata(hataMetni(m))
    }

    // ---- Profil eşitleme ----
    private fun profilJson(): JsonObject {
        val p = Depo.profil.deger; val a = Depo.ayarlar.deger
        return buildJsonObject {
            put("kaynak", "android")
            p.egitim?.let { put("egitim", it) } ?: put("egitim", "")
            put("bolum", p.bolum)
            put("puanlar", buildJsonObject { p.puanlar.forEach { (k, v) -> put(k, v) } })
            put("iller", buildJsonArray { p.iller.forEach { add(JsonPrimitive(it)) } })
            put("takip", buildJsonArray {
                a.takipler.forEach { t ->
                    add(buildJsonObject {
                        put("id", t.id)
                        put("unvanlar", buildJsonArray { t.unvanlar.forEach { add(JsonPrimitive(it)) } })
                        put("kurum", t.kurum); put("profilIlleri", t.profilIlleri)
                    })
                }
            })
            put("acilis", buildJsonArray { a.acilisTakip.keys.forEach { add(JsonPrimitive(it)) } })
        }
    }

    private fun profilOlustur() {
        val o = _oturum.value ?: return
        val g = buildJsonObject {
            put("id", o.id); put("ad", o.ad); put("soyad", o.soyad); put("telefon", o.telefon); put("eposta", o.eposta)
            put("profil", profilJson())
        }
        istek("POST", "$REST/profiller", g, token(), "resolution=merge-duplicates,return=minimal")
    }

    fun profilYaz() { if (_oturum.value != null) satirGuncelle(buildJsonObject { put("profil", profilJson()) }) }

    /** Buluttaki profili cihaza alır. Satır yoksa false. */
    fun profilCek(): Boolean {
        val o = _oturum.value ?: return false
        val (kod, g) = istek("GET", "$REST/profiller?id=eq.${o.id}&select=ad,soyad,telefon,bildirim,profil", token = token())
        if (kod !in 200..299) return false
        val satir = runCatching { json.parseToJsonElement(g).jsonArray.firstOrNull()?.jsonObject }.getOrNull() ?: return false
        fun s(a: String) = satir[a]?.takeIf { it !is JsonNull }?.jsonPrimitive?.content.orEmpty()
        kaydet(o.copy(ad = s("ad"), soyad = s("soyad"), telefon = s("telefon"), epostaBildirim = satir["bildirim"]?.jsonPrimitive?.booleanOrNull ?: true))
        val p = satir["profil"] as? JsonObject ?: return true
        val egitim = p["egitim"]?.jsonPrimitive?.content?.ifBlank { null }
        val puanlar = (p["puanlar"] as? JsonObject)?.mapNotNull { (k, v) -> v.jsonPrimitive.doubleOrNull?.let { k to it } }?.toMap().orEmpty()
        val iller = (p["iller"] as? JsonArray)?.map { it.jsonPrimitive.content }.orEmpty()
        val bolum = p["bolum"]?.jsonPrimitive?.content.orEmpty()
        if (egitim != null || puanlar.isNotEmpty() || iller.isNotEmpty()) {
            Depo.profil.guncelle { it.copy(egitim = egitim ?: it.egitim, bolum = bolum.ifBlank { it.bolum }, puanlar = puanlar.ifEmpty { it.puanlar }, iller = iller, tamamlandi = true) }
        }
        val takip = (p["takip"] as? JsonArray)?.mapNotNull { e ->
            val t = e as? JsonObject ?: return@mapNotNull null
            Takip(
                id = t["id"]?.jsonPrimitive?.content ?: java.util.UUID.randomUUID().toString(),
                unvanlar = (t["unvanlar"] as? JsonArray)?.map { it.jsonPrimitive.content }.orEmpty(),
                kurum = t["kurum"]?.jsonPrimitive?.content.orEmpty(),
                profilIlleri = t["profilIlleri"]?.jsonPrimitive?.booleanOrNull ?: true,
            )
        }.orEmpty()
        val acilis = (p["acilis"] as? JsonArray)?.map { it.jsonPrimitive.content }.orEmpty()
        Depo.ayarlar.guncelle { a ->
            a.copy(
                takipler = (a.takipler + takip).distinctBy { it.id },
                acilisTakip = a.acilisTakip + acilis.filter { it !in a.acilisTakip }.associateWith { id -> Depo.ilanlar.value.find { it.id == id }?.basvuruBaslangic.orEmpty() },
            )
        }
        return true
    }
}
