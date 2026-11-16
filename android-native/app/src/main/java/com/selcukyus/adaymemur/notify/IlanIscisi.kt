package com.selcukyus.adaymemur.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.selcukyus.adaymemur.BuildConfig
import com.selcukyus.adaymemur.MainActivity
import com.selcukyus.adaymemur.R
import com.selcukyus.adaymemur.data.Bicim
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.data.IlanlarYanit
import com.selcukyus.adaymemur.data.json
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

/**
 * Arka planda ilan listesini kontrol eder:
 *  - Pro: yeni ilan çıktığında (il tercihine göre) bildirim
 *  - Ücretsiz: her akşam 19:00'dan sonra günün yeni ilanlarının özeti
 */
class IlanIscisi(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        Depo.baslat(applicationContext)
        val ayar = Depo.ayarlar.deger
        if (!ayar.bildirim || !izinVar(applicationContext)) return Result.success()
        val pro = applicationContext.getSharedPreferences("aday_memur_pro", Context.MODE_PRIVATE).getBoolean("pro", false)
        val yanit = runCatching {
            json.decodeFromString(IlanlarYanit.serializer(), Depo.indir("${BuildConfig.SITE_URL}/data/ilanlar.json"))
        }.getOrElse { return Result.retry() }

        val bilinen = Depo.bildirilenler()
        val ids = yanit.ilanlar.map { it.id }.toSet()
        if (bilinen.isEmpty()) { // ilk çalıştırma: mevcut ilanları "görülmüş" say
            Depo.bildirilenKaydet(ids)
            return Result.success()
        }
        val yeniler = yanit.ilanlar.filter { it.id !in bilinen && (Bicim.kalanGun(it.sonBasvuru) ?: 0) >= 0 }

        if (pro) {
            val iller = ayar.bildirimIlleri
            val uygun = yeniler.filter { iller.isEmpty() || it.iller.isEmpty() || it.iller.any { i -> i in iller } }
            uygun.take(5).forEach { i ->
                val kadro = i.kadro?.let { "${Bicim.sayi(it)} kadro · " } ?: ""
                val govde = kadro + (i.sonBasvuru?.let { "Son başvuru ${Bicim.uzun(it)}" } ?: i.kurum)
                goster(applicationContext, i.id.hashCode(), "${i.kurum.ifEmpty { "Yeni ilan" }}: ${i.baslik}", govde, i.id)
            }
            if (uygun.size > 5) goster(applicationContext, 7001, "${uygun.size} yeni kamu ilanı", "Tümünü görmek için dokun", null)
            Depo.bildirilenKaydet(bilinen + yeniler.map { it.id })
        } else {
            val bugun = LocalDate.now().toString()
            if (LocalTime.now().hour >= 19 && Depo.sonOzet() != bugun) {
                if (yeniler.isNotEmpty()) {
                    val kadro = yeniler.sumOf { it.kadro ?: 0 }
                    goster(
                        applicationContext, 7000, "Bugünün kamu ilanları",
                        "${yeniler.size} yeni ilan" + (if (kadro > 0) ", toplam ${Bicim.sayi(kadro)} kadro" else "") + ". Hemen incele.", null,
                    )
                }
                Depo.sonOzetKaydet(bugun)
                Depo.bildirilenKaydet(bilinen + yeniler.map { it.id })
            }
        }
        return Result.success()
    }

    companion object {
        const val KANAL = "ilanlar"

        fun kanalOlustur(ctx: Context) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val k = NotificationChannel(KANAL, ctx.getString(R.string.kanal_ilan), NotificationManager.IMPORTANCE_DEFAULT)
            k.description = ctx.getString(R.string.kanal_ilan_aciklama)
            ctx.getSystemService(NotificationManager::class.java).createNotificationChannel(k)
        }

        fun planla(ctx: Context) {
            val is_ = PeriodicWorkRequestBuilder<IlanIscisi>(1, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx).enqueueUniquePeriodicWork("ilan-kontrol", ExistingPeriodicWorkPolicy.KEEP, is_)
        }

        fun izinVar(ctx: Context) = Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

        fun goster(ctx: Context, nid: Int, baslik: String, govde: String, ilanId: String?) {
            if (!izinVar(ctx)) return
            val intent = Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                ilanId?.let { putExtra("ilanId", it) }
            }
            val pi = PendingIntent.getActivity(ctx, nid, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val n = NotificationCompat.Builder(ctx, KANAL)
                .setSmallIcon(R.drawable.ic_bildirim)
                .setColor(0xFFF1D23A.toInt())
                .setContentTitle(baslik.take(110))
                .setContentText(govde)
                .setStyle(NotificationCompat.BigTextStyle().bigText(govde))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build()
            runCatching { NotificationManagerCompat.from(ctx).notify(nid, n) }
        }
    }
}
