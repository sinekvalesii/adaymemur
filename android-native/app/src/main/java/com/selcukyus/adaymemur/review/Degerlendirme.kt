package com.selcukyus.adaymemur.review

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.google.android.play.core.review.ReviewManagerFactory
import com.selcukyus.adaymemur.data.Uzak

/**
 * Google Play uygulama içi değerlendirme. Kullanıcı en az 3 olumlu işlem yaptıktan
 * (ilan kaydetme, takip, bildirim alma) ve uygulamayı 2 farklı günde açtıktan sonra,
 * 60 günde en fazla bir kez gösterilir. Gösterim sıklığını ayrıca Google sınırlar.
 */
object Degerlendirme {
    private const val GUN = 24 * 60 * 60 * 1000L

    private fun prefs(c: Context) = c.getSharedPreferences("aday_memur_degerlendirme", Context.MODE_PRIVATE)

    fun acildi(c: Context) {
        val p = prefs(c); val bugun = System.currentTimeMillis() / GUN
        if (p.getLong("sonGun", -1) != bugun) p.edit { putLong("sonGun", bugun); putInt("gunSayisi", p.getInt("gunSayisi", 0) + 1) }
    }

    var aktivite: java.lang.ref.WeakReference<Activity>? = null

    /** Olumlu bir işlem oldu (kaydetme, takip). Koşullar uygunsa değerlendirme penceresi açılır. */
    fun olumlu() { aktivite?.get()?.let { olumlu(it) } }

    fun olumlu(activity: Activity) {
        val p = prefs(activity)
        val n = p.getInt("olumlu", 0) + 1
        p.edit { putInt("olumlu", n) }
        val son = p.getLong("sonSoru", 0)
        if (!Uzak.ayar.value.degerlendirme || n < 3 || p.getInt("gunSayisi", 0) < 2 || System.currentTimeMillis() - son < 60 * GUN) return
        val yonetici = ReviewManagerFactory.create(activity)
        yonetici.requestReviewFlow().addOnCompleteListener { t ->
            if (t.isSuccessful) {
                p.edit { putLong("sonSoru", System.currentTimeMillis()) }
                yonetici.launchReviewFlow(activity, t.result)
            }
        }
    }
}
