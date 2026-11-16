package com.selcukyus.adaymemur.data

import java.text.NumberFormat
import java.time.Duration
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.Locale

/** Tarih, sayı ve metin biçimleri (Türkçe). */
object Bicim {
    private val TR = Locale.forLanguageTag("tr")
    private val AYLAR = listOf("Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran", "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık")
    private val AYLAR_KISA = listOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")
    private val GUNLER = listOf("Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma", "Cumartesi", "Pazar")

    fun gun(iso: String): LocalDate? = runCatching { LocalDate.parse(iso.take(10)) }.getOrNull()

    fun kalanGun(iso: String?): Long? = iso?.let { gun(it) }?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }

    fun uzun(iso: String): String = gun(iso)?.let { "${it.dayOfMonth} ${AYLAR[it.monthValue - 1]} ${it.year}" } ?: iso
    fun kisaGun(iso: String): String = gun(iso)?.dayOfMonth?.toString() ?: ""
    fun kisaAy(iso: String): String = gun(iso)?.let { AYLAR_KISA[it.monthValue - 1] } ?: ""
    fun gunAdi(iso: String): String = gun(iso)?.let { GUNLER[it.dayOfWeek.value - 1] } ?: ""

    enum class Ton { KOTU, UYARI, IYI, SOLUK }

    fun kalanMetin(iso: String?): Pair<String, Ton> {
        val k = kalanGun(iso) ?: return "Tarih ilanda" to Ton.SOLUK
        return when {
            k < 0 -> "Süresi doldu" to Ton.SOLUK
            k == 0L -> "Son gün bugün" to Ton.KOTU
            k == 1L -> "Son gün yarın" to Ton.KOTU
            k <= 3 -> "Son $k gün" to Ton.KOTU
            k <= 7 -> "$k gün kaldı" to Ton.UYARI
            else -> "$k gün kaldı" to Ton.IYI
        }
    }

    fun zaman(iso: String): Long = runCatching { OffsetDateTime.parse(iso).toInstant().toEpochMilli() }.getOrDefault(0L)

    fun goreli(iso: String): String {
        val fark = Duration.ofMillis((System.currentTimeMillis() - zaman(iso)).coerceAtLeast(0))
        val dk = fark.toMinutes()
        return when {
            dk < 1 -> "şimdi"
            dk < 60 -> "$dk dk önce"
            dk < 60 * 24 -> "${dk / 60} sa önce"
            dk < 60 * 24 * 7 -> "${dk / 60 / 24} gün önce"
            else -> uzun(iso.take(10))
        }
    }

    fun sayi(n: Number): String = NumberFormat.getIntegerInstance(TR).format(n)

    fun puan(d: Double): String = NumberFormat.getNumberInstance(TR).apply { maximumFractionDigits = 3 }.format(d)

    /** Karşılaştırma için Türkçe katlama: küçük harf, aksansız. */
    fun katla(s: String?): String {
        if (s.isNullOrEmpty()) return ""
        val b = StringBuilder(s.length)
        for (c in s) {
            b.append(
                when (c) {
                    'İ', 'I', 'ı', 'î', 'Î' -> 'i'
                    'Ş', 'ş' -> 's'
                    'Ğ', 'ğ' -> 'g'
                    'Ü', 'ü', 'û', 'Û' -> 'u'
                    'Ö', 'ö' -> 'o'
                    'Ç', 'ç' -> 'c'
                    'Â', 'â' -> 'a'
                    else -> c.lowercaseChar()
                },
            )
        }
        return b.toString().replace(Regex("\\s+"), " ").trim()
    }

    /** "ADALET BAKANLIĞI" → "Adalet Bakanlığı" */
    fun duzAd(s: String): String {
        if (s.isEmpty() || s != s.uppercase(TR)) return s
        val kucuk = setOf("ve", "ile", "de", "da", "veya")
        return s.lowercase(TR).split(" ").mapIndexed { i, w ->
            if (i > 0 && w in kucuk) w else w.replaceFirstChar { it.titlecase(TR) }
        }.joinToString(" ")
    }
}
