package com.selcukyus.adaymemur.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.selcukyus.adaymemur.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    coerceInputValues = true
}

/** SharedPreferences üzerinde JSON olarak saklanan, gözlenebilir durum. */
class Kalici<T>(private val prefs: SharedPreferences, private val anahtar: String, private val ser: KSerializer<T>, varsayilan: T) {
    private val _akis = MutableStateFlow(
        prefs.getString(anahtar, null)?.let { runCatching { json.decodeFromString(ser, it) }.getOrNull() } ?: varsayilan,
    )
    val akis: StateFlow<T> = _akis.asStateFlow()
    val deger: T get() = _akis.value

    fun guncelle(f: (T) -> T) {
        _akis.update(f)
        prefs.edit { putString(anahtar, json.encodeToString(ser, _akis.value)) }
    }
}

enum class VeriDurumu { YUKLENIYOR, HAZIR, HATA }

object Depo {
    lateinit var app: Context
        private set
    private lateinit var prefs: SharedPreferences

    lateinit var profil: Kalici<Profil>
    lateinit var filtre: Kalici<Filtre>
    lateinit var kayitlar: Kalici<Map<String, Kayit>>
    lateinit var ayarlar: Kalici<Ayarlar>
    private lateinit var zengin: Kalici<Map<String, Ilan>>

    private val _ilanlar = MutableStateFlow<List<Ilan>>(emptyList())
    val ilanlar: StateFlow<List<Ilan>> = _ilanlar.asStateFlow()
    private val _durum = MutableStateFlow(VeriDurumu.YUKLENIYOR)
    val durum: StateFlow<VeriDurumu> = _durum.asStateFlow()
    private val _guncelleme = MutableStateFlow<String?>(null)
    val guncelleme: StateFlow<String?> = _guncelleme.asStateFlow()
    private val _yenileniyor = MutableStateFlow(false)
    val yenileniyor: StateFlow<Boolean> = _yenileniyor.asStateFlow()
    private val _takvim = MutableStateFlow(Takvim())
    val takvim: StateFlow<Takvim> = _takvim.asStateFlow()
    private val _duyurular = MutableStateFlow<List<Duyuru>>(emptyList())
    val duyurular: StateFlow<List<Duyuru>> = _duyurular.asStateFlow()

    private val kilit = Mutex()
    private val metinler = HashMap<String, String>()
    private val DATA = "${BuildConfig.DATA_URL}/data"

    fun baslat(context: Context) {
        if (::app.isInitialized) return
        app = context.applicationContext
        prefs = app.getSharedPreferences("aday_memur", Context.MODE_PRIVATE)
        profil = Kalici(prefs, "profil", Profil.serializer(), Profil())
        filtre = Kalici(prefs, "filtre", Filtre.serializer(), Filtre())
        kayitlar = Kalici(prefs, "kayitlar", MapSerializer(String.serializer(), Kayit.serializer()), emptyMap())
        ayarlar = Kalici(prefs, "ayarlar", Ayarlar.serializer(), Ayarlar())
        zengin = Kalici(prefs, "zengin", MapSerializer(String.serializer(), Ilan.serializer()), emptyMap())
        onbellektenYukle()
    }

    private fun onbellek(ad: String) = File(app.filesDir, ad)

    private fun onbellektenYukle() {
        runCatching {
            val y = json.decodeFromString(IlanlarYanit.serializer(), onbellek("ilanlar.json").readText())
            yayinla(y)
        }
        runCatching { _takvim.value = json.decodeFromString(Takvim.serializer(), onbellek("takvim.json").readText()) }
        runCatching { _duyurular.value = json.decodeFromString(Duyurular.serializer(), onbellek("duyurular.json").readText()).duyurular }
    }

    private fun yayinla(y: IlanlarYanit) {
        val z = zengin.deger
        _ilanlar.value = if (z.isEmpty()) y.ilanlar else y.ilanlar.map { z[it.id]?.let { e -> birlestir(it, e) } ?: it }
        _guncelleme.value = y.guncelleme
        _durum.value = VeriDurumu.HAZIR
    }

    private fun birlestir(a: Ilan, z: Ilan) = a.copy(
        kpss = a.kpss.ifEmpty { z.kpss },
        kpssGerekli = a.kpssGerekli ?: z.kpssGerekli,
        egitim = a.egitim.ifEmpty { z.egitim },
        bolumSarti = if (a.bolumSarti == "bilinmiyor") z.bolumSarti else a.bolumSarti,
        kadro = a.kadro ?: z.kadro,
        basvuruBaslangic = a.basvuruBaslangic ?: z.basvuruBaslangic,
        sonBasvuru = a.sonBasvuru ?: z.sonBasvuru,
        ozet = a.ozet.ifEmpty { z.ozet },
    )

    fun indir(url: String, zamanAsimi: Int = 20000): String {
        val c = URL("$url?t=${System.currentTimeMillis() / 60000}").openConnection() as HttpURLConnection
        c.connectTimeout = zamanAsimi
        c.readTimeout = zamanAsimi
        c.setRequestProperty("Accept", "application/json")
        try {
            if (c.responseCode !in 200..299) error("HTTP ${c.responseCode}")
            return c.inputStream.bufferedReader().use { it.readText() }
        } finally {
            c.disconnect()
        }
    }

    suspend fun yenile() = withContext(Dispatchers.IO) {
        if (!kilit.tryLock()) return@withContext
        try {
            _yenileniyor.value = true
            if (_ilanlar.value.isEmpty()) _durum.value = VeriDurumu.YUKLENIYOR
            val ham = indir("$DATA/ilanlar.json")
            val y = json.decodeFromString(IlanlarYanit.serializer(), ham)
            onbellek("ilanlar.json").writeText(ham)
            yayinla(y)
            runCatching {
                val t = indir("$DATA/takvim.json")
                _takvim.value = json.decodeFromString(Takvim.serializer(), t)
                onbellek("takvim.json").writeText(t)
            }
            runCatching {
                val d = indir("$DATA/duyurular.json")
                _duyurular.value = json.decodeFromString(Duyurular.serializer(), d).duyurular
                onbellek("duyurular.json").writeText(d)
            }
        } catch (e: Exception) {
            if (_ilanlar.value.isEmpty()) _durum.value = VeriDurumu.HATA
        } finally {
            _yenileniyor.value = false
            kilit.unlock()
        }
    }

    fun ilan(id: String): Ilan? = _ilanlar.value.firstOrNull { it.id == id } ?: kayitlar.deger[id]?.ilan

    /** İlanın tam metni: yayınlanmış metin dosyası; Kariyer Kapısı'nda yoksa cihazdan resmî servis. */
    suspend fun metin(ilan: Ilan): String? = withContext(Dispatchers.IO) {
        metinler[ilan.id]?.let { return@withContext it }
        var m: String? = null
        if (ilan.detay) {
            m = runCatching { json.decodeFromString(IlanMetni.serializer(), indir("$DATA/ilan/${ilan.id}.json")).metin }.getOrNull()
        }
        if (m.isNullOrBlank() && ilan.kaynak == "kariyerkapisi") {
            m = kariyerKapisiMetni(ilan.id.removePrefix("kk-"))
            if (m != null) {
                val z = Ayristirici.zenginlestir(ilan, m)
                zengin.guncelle { (it + (ilan.id to z)).entries.toList().takeLast(300).associate { e -> e.key to e.value } }
                _ilanlar.update { liste -> liste.map { if (it.id == ilan.id) birlestir(it, z) else it } }
            }
        }
        m?.also { metinler[ilan.id] = it }
    }

    private fun kariyerKapisiMetni(guid: String): String? = runCatching {
        fun post(yol: String): String {
            val c = URL("https://api.kariyerkapisi.gov.tr/api/$yol").openConnection() as HttpURLConnection
            c.requestMethod = "POST"
            c.connectTimeout = 12000
            c.readTimeout = 15000
            c.doOutput = true
            c.setRequestProperty("Content-Type", "application/json")
            c.setRequestProperty("Accept", "application/json")
            c.outputStream.use { it.write("{\"ilanGuid\":\"$guid\"}".toByteArray()) }
            try {
                if (c.responseCode !in 200..299) return ""
                return c.inputStream.bufferedReader().use { it.readText() }
            } finally {
                c.disconnect()
            }
        }
        val parcalar = mutableListOf<String>()
        val ana = post("ilan/GetIlanPreviewPublic")
        if (ana.startsWith("{")) json.parseToJsonElement(ana).jsonObject["ilanMetni"]?.jsonPrimitive?.content?.let { parcalar += it }
        val alt = runCatching { post("altilan/GetAltIlanInfoByIlanIdPublic") }.getOrDefault("")
        if (alt.startsWith("[")) {
            json.parseToJsonElement(alt).let { it as? kotlinx.serialization.json.JsonArray }?.forEach { e ->
                val o = e as? JsonObject ?: return@forEach
                parcalar += "${o["ilanBaslik"]?.jsonPrimitive?.content.orEmpty()}\n${o["ilanMetni"]?.jsonPrimitive?.content.orEmpty()}"
            }
        }
        temizle(parcalar.joinToString("\n\n")).takeIf { it.length > 120 }
    }.getOrNull()

    private fun temizle(html: String) = html
        .replace(Regex("<\\s*br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("</(p|div|li|tr|h\\d)>", RegexOption.IGNORE_CASE), "\n")
        .replace(Regex("<[^>]+>"), " ")
        .replace(Regex("\\[/?[a-z0-9=#]+]", RegexOption.IGNORE_CASE), " ")
        .replace("&nbsp;", " ").replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'")
        .replace(Regex("[ \\t]+"), " ")
        .replace(Regex("\\n\\s*\\n+"), "\n\n")
        .trim()

    // --- kayıtlar ---
    fun kayitliMi(id: String) = kayitlar.deger.containsKey(id)

    /** true: eklendi, false: çıkarıldı, null: ücretsiz limit doldu */
    fun kaydetDegistir(ilan: Ilan, pro: Boolean): Boolean? {
        if (kayitliMi(ilan.id)) {
            kayitlar.guncelle { it - ilan.id }
            return false
        }
        if (!pro && kayitlar.deger.size >= BuildConfig.UCRETSIZ_KAYIT_LIMITI) return null
        kayitlar.guncelle { it + (ilan.id to Kayit(ilan, System.currentTimeMillis())) }
        return true
    }

    fun goruldu(id: String) = ayarlar.guncelle { it.copy(goruldu = (it.goruldu + id).toList().takeLast(2000).toSet()) }

    // Bildirim işçisi için
    fun bildirilenler(): Set<String> = prefs.getStringSet("bildirilen", emptySet()) ?: emptySet()
    fun bildirilenKaydet(ids: Set<String>) = prefs.edit { putStringSet("bildirilen", ids.toList().takeLast(3000).toSet()) }
    fun sonOzet(): String? = prefs.getString("son_ozet", null)
    fun sonOzetKaydet(gun: String) = prefs.edit { putString("son_ozet", gun) }
}
