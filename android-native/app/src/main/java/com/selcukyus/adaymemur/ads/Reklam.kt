package com.selcukyus.adaymemur.ads

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.selcukyus.adaymemur.BuildConfig
import com.selcukyus.adaymemur.billing.Pro
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * AdMob reklam altyapısı. BuildConfig.ADMOB_BANNER_ID boşken SDK başlatılmaz,
 * onay formu gösterilmez ve reklam alanı hiç çizilmez. Pro aboneler reklam görmez.
 */
object Reklam {
    private val basladi = AtomicBoolean(false)
    val hazir = MutableStateFlow(false)
    val gizlilikSecenegi = MutableStateFlow(false)

    fun aktif() = BuildConfig.ADMOB_BANNER_ID.isNotBlank()

    fun baslat(activity: Activity) {
        if (!aktif() || !basladi.compareAndSet(false, true)) return
        val onay = UserMessagingPlatform.getConsentInformation(activity)
        onay.requestConsentInfoUpdate(activity, ConsentRequestParameters.Builder().build(), {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                gizlilikSecenegi.value = onay.privacyOptionsRequirementStatus ==
                    com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
                if (onay.canRequestAds()) {
                    MobileAds.initialize(activity) { hazir.value = true }
                }
            }
        }, { _ -> })
    }

    fun gizlilikAyarlari(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
    }
}

@Composable
fun ReklamAlani(modifier: Modifier = Modifier) {
    val pro by Pro.pro.collectAsState()
    val hazir by Reklam.hazir.collectAsState()
    if (pro || !hazir || !Reklam.aktif()) return
    val genislik = LocalConfiguration.current.screenWidthDp
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, genislik))
                adUnitId = BuildConfig.ADMOB_BANNER_ID
                loadAd(AdRequest.Builder().build())
            }
        },
    )
}
