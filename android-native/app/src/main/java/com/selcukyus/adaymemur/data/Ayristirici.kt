package com.selcukyus.adaymemur.data

import java.time.LocalDate

/**
 * Kural tabanlı ilan ayrıştırıcı — pipeline/adaymemur/parse.py ile aynı mantık.
 * Kariyer Kapısı ilan metni yalnızca Türkiye IP'lerine açık olduğundan bu ilanların
 * tam metnini cihaz çeker ve burada ayrıştırır.
 */
object Ayristirici {
    private val AYLAR = mapOf(
        "ocak" to 1, "subat" to 2, "mart" to 3, "nisan" to 4, "mayis" to 5, "haziran" to 6,
        "temmuz" to 7, "agustos" to 8, "eylul" to 9, "ekim" to 10, "kasim" to 11, "aralik" to 12,
    )
    private val AY = AYLAR.keys.joinToString("|")
    private const val D_NUM = """(\d{1,2})\s?[./-]\s?(\d{1,2})\s?[./-]\s?(\d{4})"""
    private val D_TXT = """(\d{1,2})\s+($AY)\s+(\d{4})"""

    private val ARALIK = listOf(
        Regex("""$D_NUM\s*(?:-|–|ile|ila|ve)\s*$D_NUM"""),
        Regex("""$D_TXT\s*(?:-|–|ile|ila|ve)\s*$D_TXT"""),
        Regex("""(\d{1,2})\s*(?:-|–)\s*(\d{1,2})\s+($AY)\s+(\d{4})"""),
        Regex("""(\d{1,2})\s+($AY)\s*(?:-|–)\s*(\d{1,2})\s+($AY)\s+(\d{4})"""),
        Regex("""(\d{1,2})[./](\d{1,2})\s*(?:-|–)\s*(\d{1,2})[./](\d{1,2})[./](\d{4})"""),
    )
    private val TEK = listOf(Regex(D_NUM), Regex(D_TXT))

    private fun mk(d: Any?, m: Any?, y: Any?): LocalDate? = runCatching {
        val yy = y.toString().toInt()
        if (yy !in 2020..2100) return null
        LocalDate.of(yy, m.toString().toInt(), d.toString().toInt())
    }.getOrNull()

    private fun aralik(g: List<String>, i: Int): Pair<LocalDate?, LocalDate?> = when (i) {
        0 -> mk(g[1], g[2], g[3]) to mk(g[4], g[5], g[6])
        1 -> mk(g[1], AYLAR[g[2]], g[3]) to mk(g[4], AYLAR[g[5]], g[6])
        2 -> mk(g[1], AYLAR[g[3]], g[4]) to mk(g[2], AYLAR[g[3]], g[4])
        3 -> mk(g[1], AYLAR[g[2]], g[5]) to mk(g[3], AYLAR[g[4]], g[5])
        4 -> mk(g[1], g[2], g[5]) to mk(g[3], g[4], g[5])
        else -> null to null
    }

    fun tarihler(text: String): Pair<String?, String?> {
        val t = Bicim.katla(text)
        data class Aday(val s: Int, val b: LocalDate?, val e: LocalDate)
        val adaylar = mutableListOf<Aday>()
        fun skor(pos: Int, end: Int): Int {
            val once = t.substring(maxOf(0, pos - 160), pos)
            val sonra = t.substring(end, minOf(t.length, end + 80))
            var s = 0
            if (Regex("basvuru|basvurular|son gun|tarihleri arasinda|saat 23").containsMatchIn(once)) s += 3
            if (Regex("tarihleri arasinda|arasinda|kadar|son gun").containsMatchIn(sonra)) s += 2
            if (Regex("sinav|mulakat|sozlu|yazili|sonuc|ilan edil|yayim").containsMatchIn(once.takeLast(60))) s -= 2
            return s
        }
        ARALIK.forEachIndexed { i, rx ->
            rx.findAll(t).forEach { m ->
                val (b, e) = aralik(m.groupValues, i)
                if (e != null && (b == null || !b.isAfter(e))) adaylar += Aday(skor(m.range.first, m.range.last + 1) + 1, b, e)
            }
        }
        if (adaylar.isEmpty()) {
            TEK.forEach { rx ->
                rx.findAll(t).forEach { m ->
                    val g = m.groupValues
                    val ay = g[2].toIntOrNull() ?: AYLAR[g[2]]
                    val d = mk(g[1], ay, g[3])
                    val s = skor(m.range.first, m.range.last + 1)
                    if (d != null && s >= 3) adaylar += Aday(s, null, d)
                }
            }
        }
        val en = adaylar.sortedWith(compareBy<Aday>({ -it.s }, { it.e })).firstOrNull() ?: return null to null
        return en.b?.toString() to en.e.toString()
    }

    fun kpss(text: String): Pair<List<KpssSart>, Boolean?> {
        val t = Bicim.katla(text)
        val turler = mutableListOf<String>()
        Regex("""\bkpss\s*[-_]?\s*p\s*[-_]?\s*(\d{1,3})\b|\b(?:puan turu|puan turleri)\s*:?\s*(?:kpss)?\s*p\s?(\d{1,3})\b""").findAll(t).forEach {
            val n = (it.groupValues[1].ifEmpty { it.groupValues[2] }).toIntOrNull() ?: return@forEach
            if (n in 1..125 && "P$n" !in turler) turler += "P$n"
        }
        if (turler.isEmpty() && "kpss" in t) {
            Regex("""\bp\s?(3|93|94|\d{1,2})\b""").findAll(t).forEach {
                val ctx = t.substring(maxOf(0, it.range.first - 60), minOf(t.length, it.range.last + 31))
                val k = "P${it.groupValues[1].toInt()}"
                if ("puan" in ctx && k !in turler) turler += k
            }
        }
        val minRx = listOf(
            Regex("""(\d{2,3})(?:[.,]\d+)?\s*(?:\(\w+\)\s*)?(?:ve|veya)\s*(?:daha\s*)?(?:uzeri|ustu|yuksek)"""),
            Regex("""en\s*az\s*(\d{2,3})(?:[.,]\d+)?\s*(?:\(\w+\)\s*)?puan"""),
            Regex("""(\d{2,3})(?:[.,]\d+)?\s*puan(?:in| ve)?\s*(?:ve\s*)?uzeri"""),
            Regex("""(?:taban|asgari|minimum)\s*(?:kpss\s*)?puan[a-z]*\s*:?\s*(\d{2,3})"""),
        )
        val minler = mutableListOf<Int>()
        minRx.forEach { rx ->
            rx.findAll(t).forEach {
                val ctx = t.substring(maxOf(0, it.range.first - 120), minOf(t.length, it.range.last + 41))
                val v = it.groupValues[1].toInt()
                if (("kpss" in ctx || "puan" in ctx) && v in 40..100) minler += v
            }
        }
        val taban = minler.minOrNull()
        val gerekli = when {
            Regex("""kpss\s*(?:puan[a-z]*\s*)?(?:sarti\s*)?(?:aranmaz|aranmayacak|sarti yoktur|sarti bulunmamaktadir)""").containsMatchIn(t) -> false
            turler.isNotEmpty() || Regex("""kpss\s*\(?b\)?\s*grubu|kpss puan""").containsMatchIn(t) -> true
            else -> null
        }
        return turler.map { KpssSart(it, taban) } to gerekli
    }

    fun egitim(text: String, k: List<KpssSart>): List<String> {
        val t = Bicim.katla(text)
        val f = mutableSetOf<String>()
        if (Regex("""\b(ortaogretim|lise|meslek lisesi|lise ve dengi)\b""").containsMatchIn(t)) f += "ortaogretim"
        if (Regex("""\bon\s?lisans\b|\bonlisans\b|meslek yuksekokulu""").containsMatchIn(t)) f += "onlisans"
        for (m in Regex("""\blisans\b""").findAll(t)) {
            if (!Regex("""(on\s?|yuksek\s?)$""").containsMatchIn(t.substring(maxOf(0, m.range.first - 8), m.range.first))) {
                f += "lisans"; break
            }
        }
        if (Regex("""yuksek\s?lisans|master""").containsMatchIn(t)) f += "yukseklisans"
        if (Regex("""\bdoktora\b|doktor ogretim uyesi""").containsMatchIn(t)) f += "doktora"
        if (Regex("""\bilkogretim\b|\bilkokul\b|\bortaokul\b""").containsMatchIn(t)) f += "ilkogretim"
        k.forEach {
            val n = it.tur.drop(1).toIntOrNull() ?: return@forEach
            when {
                n == 3 -> f += "lisans"
                n == 93 -> f += "onlisans"
                n == 94 -> f += "ortaogretim"
                n in 1..48 || n in 100..125 -> f += "lisans"
            }
        }
        return Sabitler.EGITIM_SIRA.filter { it in f }
    }

    fun bolumSarti(text: String): String {
        val t = Bicim.katla(text)
        if (Regex("""herhangi bir (?:lisans|on\s?lisans|onlisans|yuksekogretim|ortaogretim|lise)?\s*(?:programindan|bolumunden|bolumden)|tum bolumler|tum lisans|her bolum|bolum (?:sarti|sinirlamasi) (?:aranmaz|yoktur|bulunmamaktadir)|bolum ayrimi (?:yapilmaksizin|yoktur)""").containsMatchIn(t)) return "yok"
        if (Regex("""bolum(?:u|unden|lerinden|lerin|leri)\b|muhendisligi|fakultesi|programlarindan""").containsMatchIn(t)) return "var"
        return "bilinmiyor"
    }

    fun kadro(text: String): Int? {
        val t = Bicim.katla(text)
        val rxs = listOf(
            Regex("""toplam\s*(\d{1,3}(?:\.\d{3})*|\d+)\s*\(?[a-z ]{0,20}\)?\s*(?:adet\s*)?(?:sozlesmeli |surekli |kadrolu |gecici |daimi )?(?:personel|isci|memur|kadro|pozisyon|ogretim|uzman|aday)"""),
            Regex("""(\d{1,3}(?:\.\d{3})*|\d+)\s*(?:adet\s*|kisi\s*|\(.{1,12}\)\s*)?(?:sozlesmeli |surekli |kadrolu |gecici |daimi )?[a-z ]{0,40}?(?:personel|isci|memur)\s*(?:alacak|alinacak|alimi|istihdam)"""),
            Regex("""toplam\s*(\d{1,3}(?:\.\d{3})*|\d+)\s*(?:adet\s*|kisi\s*)?[a-z ]{0,40}?(?:alinacak|alimi yapilacak|istihdam edilecek)"""),
        )
        for (rx in rxs) {
            val n = rx.find(t)?.groupValues?.get(1)?.replace(".", "")?.toIntOrNull()
            if (n != null && n in 1..99999) return n
        }
        return null
    }

    /** Boş alanları metinden doldurur; dolu alanlara dokunmaz. */
    fun zenginlestir(i: Ilan, metin: String): Ilan {
        val tam = "${i.baslik}\n$metin"
        val (k, gerekli) = kpss(tam)
        val (bas, son) = tarihler(metin)
        return i.copy(
            kpss = i.kpss.ifEmpty { k },
            kpssGerekli = i.kpssGerekli ?: gerekli,
            egitim = i.egitim.ifEmpty { egitim(tam, k) },
            bolumSarti = if (i.bolumSarti == "bilinmiyor") bolumSarti(metin) else i.bolumSarti,
            kadro = i.kadro ?: kadro(tam),
            basvuruBaslangic = i.basvuruBaslangic ?: bas,
            sonBasvuru = i.sonBasvuru ?: son,
            ozet = i.ozet.ifEmpty { metin.take(320) },
        )
    }
}
