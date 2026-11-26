package com.selcukyus.adaymemur.data

import com.selcukyus.adaymemur.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class TercihKadro(
    val k: String,
    val d: String, // ort | onl | lis
    val kurum: String,
    val il: String = "",
    val yer: String = "",
    val unvan: String,
    val kont: Int = 0,
    val yerlesen: Int = 0,
    val min: Double? = null,
    val max: Double? = null,
    val donem: String,
) { val anahtar get() = "$donem|$k" }

@Serializable
data class TercihVeri(val donemler: List<String> = emptyList(), val kadrolar: List<TercihKadro> = emptyList())

enum class Sans(val etiket: String) { GUVENLI("Güvenli"), OLASI("Olası"), ZOR("Zor"), DUSUK("Düşük") }

/**
 * KPSS tercih asistanı: ÖSYM'nin önceki merkezi yerleştirmelerindeki en küçük/en büyük puanlarına göre
 * adayın puanıyla hangi kadrolara yerleşebileceğini tahmin eder. Kaynak: site/tercih.json (günlük güncellenir).
 */
object Tercih {
    val PUAN = mapOf("ort" to "P94", "onl" to "P93", "lis" to "P3")
    val DUZEY_AD = mapOf("ort" to "Ortaöğretim", "onl" to "Önlisans", "lis" to "Lisans")
    fun duzey(egitim: String?) = when (egitim) { "onlisans" -> "onl"; "lisans", "yukseklisans", "doktora" -> "lis"; else -> "ort" }

    private val _veri = MutableStateFlow<TercihVeri?>(null)
    val veri: StateFlow<TercihVeri?> = _veri.asStateFlow()

    private fun dosya() = File(Depo.app.filesDir, "tercih.json")

    suspend fun yukle() = withContext(Dispatchers.IO) {
        if (_veri.value == null) runCatching { _veri.value = json.decodeFromString(TercihVeri.serializer(), dosya().readText()) }
        runCatching {
            val m = Depo.indir("${BuildConfig.DATA_URL}/tercih.json", 30000)
            val v = json.decodeFromString(TercihVeri.serializer(), m)
            dosya().writeText(m); _veri.value = v
        }.onFailure { if (_veri.value == null) _veri.value = TercihVeri() }
    }

    fun sans(k: TercihKadro, puan: Double): Sans {
        val min = k.min ?: return Sans.DUSUK
        val max = k.max ?: min
        return when {
            puan >= max -> Sans.GUVENLI
            puan >= min -> Sans.OLASI
            puan >= min - 2.0 -> Sans.ZOR
            else -> Sans.DUSUK
        }
    }
}
