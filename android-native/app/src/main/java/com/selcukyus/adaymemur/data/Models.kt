package com.selcukyus.adaymemur.data

import kotlinx.serialization.Serializable

@Serializable
data class KpssSart(val tur: String, val min: Int? = null)

@Serializable
data class KaynakLink(val kaynak: String, val url: String)

@Serializable
data class Ilan(
    val id: String,
    val kaynak: String,
    val baslik: String,
    val kurum: String = "",
    val tur: String = "diger",
    val iller: List<String> = emptyList(),
    val egitim: List<String> = emptyList(),
    val kpss: List<KpssSart> = emptyList(),
    val kpssGerekli: Boolean? = null,
    val bolumSarti: String = "bilinmiyor",
    val bolumler: List<String> = emptyList(),
    val kadro: Int? = null,
    val unvanlar: List<String> = emptyList(),
    val basvuruBaslangic: String? = null,
    val sonBasvuru: String? = null,
    val yayin: String? = null,
    val eklenme: String,
    val url: String,
    val logo: String? = null,
    val ozet: String = "",
    val detay: Boolean = false,
    val kaynaklar: List<KaynakLink> = emptyList(),
)

@Serializable
data class IlanlarYanit(
    val surum: Int = 1,
    val guncelleme: String,
    val adet: Int = 0,
    val ilanlar: List<Ilan>,
)

@Serializable
data class IlanMetni(val id: String, val metin: String)

@Serializable
data class TakvimOgesi(
    val id: String,
    val sinav: String,
    val baslik: String,
    val tarih: String,
    val bitis: String? = null,
    val tip: String,
    val not: String? = null,
)

@Serializable
data class Takvim(
    val guncelleme: String = "",
    val kaynak: String = "",
    val kaynakUrl: String = "",
    val uyari: String = "",
    val ogeler: List<TakvimOgesi> = emptyList(),
)

@Serializable
data class Duyuru(
    val id: String,
    val baslik: String,
    val metin: String,
    val tarih: String,
    val url: String? = null,
    val vurgulu: Boolean = false,
)

@Serializable
data class Duyurular(val duyurular: List<Duyuru> = emptyList())

@Serializable
data class Profil(
    val egitim: String? = null,
    val bolum: String = "",
    val puanlar: Map<String, Double> = emptyMap(),
    val iller: List<String> = emptyList(),
    val tamamlandi: Boolean = false,
)

@Serializable
data class Filtre(
    val arama: String = "",
    val iller: List<String> = emptyList(),
    val turler: List<String> = emptyList(),
    val egitim: List<String> = emptyList(),
    val puanTuru: String? = null,
    val puan: Double? = null,
    val suresiGecenler: Boolean = false,
    val siralama: String = "yeni", // yeni | sonGun | kadro
) {
    val aktifSayi: Int
        get() = listOf(iller.isNotEmpty(), turler.isNotEmpty(), egitim.isNotEmpty(), puanTuru != null, suresiGecenler).count { it }
}

@Serializable
data class Kayit(val ilan: Ilan, val eklenme: Long)

@Serializable
data class Ayarlar(
    val karsilamaGoruldu: Boolean = false,
    val bildirim: Boolean = true,
    val bildirimIlleri: List<String> = emptyList(),
    val goruldu: Set<String> = emptySet(),
    /** Pro: "Benzer ilanlar çıkınca haber al" takipleri */
    val takipler: List<Takip> = emptyList(),
    /** Pro: başvurusu henüz açılmamış ilanlar → ilanId to başvuru başlangıç tarihi */
    val acilisTakip: Map<String, String> = emptyMap(),
    /** Pro: e-posta bildirimi */
    val eposta: String = "",
    val epostaToken: String = "",
)

@Serializable
data class Takip(
    val id: String,
    val unvanlar: List<String> = emptyList(),
    val kurum: String = "",
    val profilIlleri: Boolean = true,
)
