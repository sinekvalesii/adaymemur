package com.selcukyus.adaymemur.data

import kotlin.math.log10
import kotlin.math.min

enum class Kontrol { EVET, HAYIR, BELIRSIZ }
enum class Durum { UYGUN, KONTROL, DEGIL }

data class Gerekce(val sonuc: Kontrol, val metin: String)
data class UygunlukSonucu(val durum: Durum, val puan: Double, val gerekceler: List<Gerekce>)

/** Profil ile ilan şartlarını karşılaştırır (Pro: Bana Uygun). */
object Uygunluk {
    private val BOS = setOf("ve", "bolumu", "bolum", "programi", "lisans", "onlisans", "fakultesi", "mezun", "bilimi", "bilimleri")

    fun bolumAnahtarlari(bolum: String): List<String> {
        val k = Bicim.katla(bolum).replace(Regex("[^a-z\\s]"), " ").replace(Regex("\\s+"), " ").trim()
        if (k.isEmpty()) return emptyList()
        if (k.endsWith("muhendisligi")) return listOf(k)
        val kel = k.split(" ").filter { it.length > 3 && it !in BOS }
        return if (kel.isNotEmpty()) listOf(kel.joinToString(" "), kel.first()) else listOf(k)
    }

    private fun egitimAdlari(l: List<String>) = l.joinToString(", ") { Sabitler.EGITIM_ADI[it] ?: it }

    fun hesapla(ilan: Ilan, p: Profil, detay: String? = null): UygunlukSonucu {
        val g = mutableListOf<Gerekce>()
        val kalan = Bicim.kalanGun(ilan.sonBasvuru)
        if (kalan != null && kalan < 0) g += Gerekce(Kontrol.HAYIR, "Başvuru süresi doldu")

        p.egitim?.let { benimEgitim ->
            if (ilan.egitim.isEmpty()) {
                g += Gerekce(Kontrol.BELIRSIZ, "Öğrenim şartı ilan metninde")
            } else if (benimEgitim in ilan.egitim) {
                g += Gerekce(Kontrol.EVET, "${Sabitler.EGITIM_ADI[benimEgitim]} mezunları başvurabilir")
            } else {
                val ben = Sabitler.EGITIM_SIRA.indexOf(benimEgitim)
                val sira = ilan.egitim.map { Sabitler.EGITIM_SIRA.indexOf(it) }
                if (ben > sira.max()) {
                    g += Gerekce(Kontrol.BELIRSIZ, "Kadrolar ${egitimAdlari(ilan.egitim)} düzeyinde; o düzeyin diplomasıyla başvurulur")
                } else {
                    g += Gerekce(Kontrol.HAYIR, "İstenen: ${egitimAdlari(ilan.egitim)}")
                }
            }
        }

        when {
            ilan.kpssGerekli == false -> g += Gerekce(Kontrol.EVET, "KPSS şartı yok")
            ilan.kpss.isNotEmpty() -> {
                val eslesen = ilan.kpss.filter { p.puanlar.containsKey(it.tur) }
                if (eslesen.isEmpty()) {
                    g += Gerekce(Kontrol.HAYIR, "İstenen puan türü: ${ilan.kpss.joinToString(", ") { it.tur }}")
                } else {
                    val yeten = eslesen.firstOrNull { it.min == null || p.puanlar.getValue(it.tur) >= it.min }
                    if (yeten != null) {
                        val puan = Bicim.puan(p.puanlar.getValue(yeten.tur))
                        g += Gerekce(Kontrol.EVET, if (yeten.min != null) "KPSS ${yeten.tur}: $puan ≥ ${yeten.min} taban" else "KPSS ${yeten.tur} puanınla başvurabilirsin")
                    } else {
                        val k = eslesen.first()
                        g += Gerekce(Kontrol.HAYIR, "KPSS ${k.tur}: ${Bicim.puan(p.puanlar.getValue(k.tur))} < ${k.min} taban")
                    }
                }
            }
            ilan.kpssGerekli == true -> g += Gerekce(Kontrol.BELIRSIZ, "KPSS istenir; puan türü ilan metninde")
            else -> g += Gerekce(Kontrol.BELIRSIZ, "KPSS şartını ilan metninden kontrol et")
        }

        if (ilan.bolumSarti == "yok") {
            g += Gerekce(Kontrol.EVET, "Bölüm şartı yok")
        } else if (p.bolum.isNotBlank()) {
            val metin = Bicim.katla(listOf(detay ?: "", ilan.ozet, ilan.bolumler.joinToString(" "), ilan.unvanlar.joinToString(" ")).joinToString(" "))
            when {
                bolumAnahtarlari(p.bolum).any { it in metin } -> g += Gerekce(Kontrol.EVET, "${p.bolum} ilanda geçiyor")
                detay != null && ilan.bolumSarti == "var" && !ilan.egitim.all { it == "ortaogretim" } ->
                    g += Gerekce(Kontrol.HAYIR, "${p.bolum} istenen bölümler arasında görünmüyor")
                else -> g += Gerekce(Kontrol.BELIRSIZ, "Bölüm şartını ilan metninden kontrol et")
            }
        }

        if (p.iller.isNotEmpty()) {
            g += when {
                ilan.iller.isEmpty() -> Gerekce(Kontrol.EVET, "Türkiye geneli / çok ilde kadro")
                ilan.iller.any { it in p.iller } -> Gerekce(Kontrol.EVET, ilan.iller.joinToString(", ") { Sabitler.ILLER[it] ?: it })
                else -> Gerekce(Kontrol.HAYIR, "Kadro yeri: ${ilan.iller.joinToString(", ") { Sabitler.ILLER[it] ?: it }}")
            }
        }

        val hayir = g.count { it.sonuc == Kontrol.HAYIR }
        val belirsiz = g.count { it.sonuc == Kontrol.BELIRSIZ }
        val evet = g.count { it.sonuc == Kontrol.EVET }
        val durum = when {
            hayir > 0 -> Durum.DEGIL
            belirsiz > 0 -> Durum.KONTROL
            else -> Durum.UYGUN
        }
        val puan = evet * 10.0 - belirsiz * 3 - hayir * 50 + (ilan.kadro?.let { min(10.0, log10(it + 1.0) * 3) } ?: 0.0)
        return UygunlukSonucu(durum, puan, g)
    }
}

object Liste {
    fun kilitDakika(ilan: Ilan, pro: Boolean, erkenDk: Int, simdi: Long = System.currentTimeMillis()): Int? {
        if (pro || erkenDk <= 0) return null
        val gecen = (simdi - Bicim.zaman(ilan.eklenme)) / 60000.0
        if (gecen < 0 || gecen >= erkenDk) return null
        return maxOf(1, kotlin.math.ceil(erkenDk - gecen).toInt())
    }

    fun filtrele(ilanlar: List<Ilan>, f: Filtre): List<Ilan> {
        val terimler = Bicim.katla(f.arama).split(" ").filter { it.isNotBlank() }
        val out = ilanlar.filter { i ->
            if (!f.suresiGecenler) {
                val k = Bicim.kalanGun(i.sonBasvuru)
                if (k != null && k < 0) return@filter false
            }
            if (f.iller.isNotEmpty() && i.iller.isNotEmpty() && i.iller.none { it in f.iller }) return@filter false
            if (f.turler.isNotEmpty() && i.tur !in f.turler) return@filter false
            if (f.egitim.isNotEmpty() && i.egitim.isNotEmpty() && i.egitim.none { it in f.egitim }) return@filter false
            if (f.puanTuru != null && i.kpssGerekli != false) {
                val k = i.kpss.firstOrNull { it.tur == f.puanTuru } ?: return@filter false
                if (f.puan != null && k.min != null && k.min > f.puan) return@filter false
            }
            if (terimler.isNotEmpty()) {
                val hay = Bicim.katla(
                    listOf(
                        i.baslik, i.kurum, i.unvanlar.joinToString(" "), i.iller.joinToString(" ") { Sabitler.ILLER[it] ?: "" },
                        Sabitler.TUR_ADI[i.tur] ?: "", i.ozet, i.kpss.joinToString(" ") { it.tur },
                    ).joinToString(" "),
                )
                if (!terimler.all { it in hay }) return@filter false
            }
            true
        }
        return when (f.siralama) {
            "sonGun" -> out.sortedBy { it.sonBasvuru ?: "9999" }
            "kadro" -> out.sortedByDescending { it.kadro ?: 0 }
            else -> out
        }
    }
}
