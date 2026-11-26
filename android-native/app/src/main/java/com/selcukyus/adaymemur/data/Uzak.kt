package com.selcukyus.adaymemur.data

import com.selcukyus.adaymemur.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

@Serializable
data class UzakReklam(val acik: Boolean = false, val banner: String = "", val sikMaddeler: Int = 8)

@Serializable
data class UzakAyar(val reklam: UzakReklam = UzakReklam(), val degerlendirme: Boolean = true)

/**
 * Yeni sürüm yayımlamadan değiştirilebilen ayarlar: site/uygulama.json.
 * Reklamı açıp kapatmak, reklam birimi kodu vb. buradan yönetilir.
 */
object Uzak {
    private val _ayar = MutableStateFlow(UzakAyar())
    val ayar: StateFlow<UzakAyar> = _ayar.asStateFlow()

    fun yukle() {
        runCatching { json.decodeFromString(UzakAyar.serializer(), Depo.indir("${BuildConfig.DATA_URL}/uygulama.json", 10000)) }
            .onSuccess { _ayar.value = it }
    }
}
