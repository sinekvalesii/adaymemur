package com.selcukyus.adaymemur.billing

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Paket(
    val urunId: String,
    val tip: String, // aylik | yillik
    val fiyat: String,
    val deneme: String?,
    val aylikKarsilik: String?,
    internal val detay: ProductDetails,
    internal val teklifToken: String,
)

/**
 * Aday Memur Pro — Google Play aboneliği.
 * Play Console > Para kazanma > Abonelikler altında iki ürün oluşturulmalı: pro_aylik, pro_yillik.
 * Ürünler oluşturulup uygulama bir test kanalına yüklenene kadar paket listesi boş gelir.
 */
object Pro : PurchasesUpdatedListener {
    val URUNLER = mapOf("pro_aylik" to "aylik", "pro_yillik" to "yillik")

    private lateinit var client: BillingClient
    private lateinit var prefs: android.content.SharedPreferences

    private val _pro = MutableStateFlow(false)
    val pro: StateFlow<Boolean> = _pro.asStateFlow()
    private val _paketler = MutableStateFlow<List<Paket>?>(null)
    val paketler: StateFlow<List<Paket>?> = _paketler.asStateFlow()
    private val _mesaj = MutableStateFlow<String?>(null)
    val mesaj: StateFlow<String?> = _mesaj.asStateFlow()

    fun mesajOkundu() { _mesaj.value = null }

    private var testModu = false

    /** Yalnızca debug derlemede ekran görüntüsü testleri için. */
    fun testPro() {
        if (!com.selcukyus.adaymemur.BuildConfig.DEBUG) return
        testModu = true
        _pro.value = true
    }

    fun baslat(context: Context) {
        if (::client.isInitialized) return
        prefs = context.getSharedPreferences("aday_memur_pro", Context.MODE_PRIVATE)
        _pro.value = prefs.getBoolean("pro", false)
        client = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        baglan()
    }

    private fun baglan(sonra: (() -> Unit)? = null) {
        if (client.isReady) { sonra?.invoke(); return }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    yenile()
                    sonra?.invoke()
                } else {
                    if (_paketler.value == null) _paketler.value = emptyList()
                }
            }

            override fun onBillingServiceDisconnected() {}
        })
    }

    fun yenile() {
        if (!::client.isInitialized || !client.isReady) return
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { r, list ->
            if (r.responseCode == BillingClient.BillingResponseCode.OK) isle(list, sessiz = true)
        }
    }

    fun paketleriGetir() {
        if (!::client.isInitialized) return
        baglan {
            val urunler = URUNLER.keys.map {
                QueryProductDetailsParams.Product.newBuilder().setProductId(it).setProductType(BillingClient.ProductType.SUBS).build()
            }
            client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(urunler).build()) { r, sonuc ->
                if (r.responseCode != BillingClient.BillingResponseCode.OK) {
                    _paketler.value = emptyList(); return@queryProductDetailsAsync
                }
                _paketler.value = sonuc.productDetailsList.mapNotNull { pd ->
                    val teklifler = pd.subscriptionOfferDetails ?: return@mapNotNull null
                    val teklif = teklifler.maxByOrNull { it.pricingPhases.pricingPhaseList.size } ?: return@mapNotNull null
                    val asamalar = teklif.pricingPhases.pricingPhaseList
                    val temel = asamalar.last()
                    val ucretsiz = asamalar.firstOrNull { it.priceAmountMicros == 0L }
                    val tip = URUNLER[pd.productId] ?: "aylik"
                    val aylik = if (tip == "yillik") {
                        val tutar = temel.priceAmountMicros / 1_000_000.0 / 12
                        java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("tr-TR")).apply {
                            currency = java.util.Currency.getInstance(temel.priceCurrencyCode)
                        }.format(tutar)
                    } else null
                    Paket(pd.productId, tip, temel.formattedPrice, ucretsiz?.let { sure(it.billingPeriod) + " ücretsiz" }, aylik, pd, teklif.offerToken)
                }.sortedBy { if (it.tip == "yillik") 0 else 1 }
            }
        }
    }

    private fun sure(iso: String): String {
        val m = Regex("P(\\d+)([DWMY])").find(iso) ?: return "Deneme"
        val n = m.groupValues[1]
        return when (m.groupValues[2]) {
            "D" -> "$n gün"
            "W" -> "$n hafta"
            "M" -> "$n ay"
            else -> "$n yıl"
        }
    }

    fun satinAl(activity: Activity, p: Paket) {
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(p.detay).setOfferToken(p.teklifToken).build()),
        ).build()
        client.launchBillingFlow(activity, params)
    }

    fun geriYukle() {
        if (!::client.isInitialized) return
        baglan {
            client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { r, list ->
                if (r.responseCode == BillingClient.BillingResponseCode.OK) {
                    isle(list, sessiz = true)
                    _mesaj.value = if (_pro.value) "Pro aboneliğin geri yüklendi" else "Bu Google hesabında etkin abonelik bulunamadı"
                }
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases?.let { isle(it, sessiz = false) }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> yenile()
            else -> _mesaj.value = "Satın alma tamamlanamadı. Ödemen alınmadıysa tekrar deneyebilirsin."
        }
    }

    private fun isle(list: List<Purchase>, sessiz: Boolean) {
        val aktif = list.filter { p -> p.purchaseState == Purchase.PurchaseState.PURCHASED && p.products.any { it in URUNLER } }
        aktif.filter { !it.isAcknowledged }.forEach {
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(it.purchaseToken).build()) { }
        }
        if (testModu) return
        val onceki = _pro.value
        _pro.value = aktif.isNotEmpty()
        prefs.edit { putBoolean("pro", _pro.value) }
        if (!sessiz && !onceki && _pro.value) _mesaj.value = "Pro’ya hoş geldin — tüm özellikler açıldı"
    }
}
