package com.selcukyus.adaymemur

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.selcukyus.adaymemur.ads.ReklamAlani
import com.selcukyus.adaymemur.ads.Reklam
import com.selcukyus.adaymemur.billing.Pro
import com.selcukyus.adaymemur.data.Depo
import com.selcukyus.adaymemur.ui.components.Yazi
import com.selcukyus.adaymemur.ui.screens.BildirimIlleriEkrani
import com.selcukyus.adaymemur.ui.screens.DetayEkrani
import com.selcukyus.adaymemur.ui.screens.FiltreEkrani
import com.selcukyus.adaymemur.ui.screens.Gezgin
import com.selcukyus.adaymemur.ui.screens.GundemEkrani
import com.selcukyus.adaymemur.ui.screens.HakkindaEkrani
import com.selcukyus.adaymemur.ui.screens.HesapEkrani
import com.selcukyus.adaymemur.ui.screens.IlanlarEkrani
import com.selcukyus.adaymemur.ui.screens.KarsilamaEkrani
import com.selcukyus.adaymemur.ui.screens.KayitliEkrani
import com.selcukyus.adaymemur.ui.screens.ProEkrani
import com.selcukyus.adaymemur.ui.screens.ProfilEkrani
import com.selcukyus.adaymemur.ui.screens.UygunEkrani
import com.selcukyus.adaymemur.ui.theme.AdayMemurTema
import com.selcukyus.adaymemur.ui.theme.T
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val bekleyenIlan = MutableStateFlow<String?>(null)
    private val bekleyenSekme = MutableStateFlow<Int?>(null)

    /** Yalnızca debug derlemede: otomatik ekran görüntüsü testleri için kısayollar. */
    private fun testKisayollari(i: Intent?) {
        if (!BuildConfig.DEBUG || i == null) return
        if (i.getBooleanExtra("atla", false)) Depo.ayarlar.guncelle { it.copy(karsilamaGoruldu = true) }
        if (i.hasExtra("sekme")) bekleyenSekme.value = i.getIntExtra("sekme", 0)
        if (i.getBooleanExtra("demoPro", false)) Pro.testPro()
        if (i.getBooleanExtra("demoProfil", false)) Depo.profil.guncelle {
            com.selcukyus.adaymemur.data.Profil("lisans", "", mapOf("P3" to 76.4), emptyList(), true)
        }
        i.getStringExtra("rota")?.let { bekleyenRota.value = it }
        i.getStringExtra("siralama")?.let { s -> Depo.filtre.guncelle { it.copy(siralama = s) } }
    }
    private val bekleyenRota = MutableStateFlow<String?>(null)
    private val izin = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private fun bildirimIzniIste() {
        if (Build.VERSION.SDK_INT >= 33) izin.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        testKisayollari(intent)
        bekleyenIlan.value = intent?.getStringExtra("ilanId")
        lifecycleScope.launch { Depo.yenile() }
        Reklam.baslat(this)
        setContent {
            AdayMemurTema {
                Uygulama(bekleyenIlan, bekleyenSekme, bekleyenRota, ::bildirimIzniIste)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        testKisayollari(intent)
        intent.getStringExtra("ilanId")?.let { bekleyenIlan.value = it }
    }

    override fun onResume() {
        super.onResume()
        Pro.yenile()
    }
}

private class NavGezgin(private val nav: NavHostController, private val sekmeSec: (Int) -> Unit) : Gezgin {
    override fun ilan(id: String) = nav.navigate("ilan/$id")
    override fun filtre() = nav.navigate("filtre")
    override fun pro() = nav.navigate("pro") { launchSingleTop = true }
    override fun profil() = nav.navigate("profil")
    override fun gundem() = nav.navigate("gundem")
    override fun hakkinda() = nav.navigate("hakkinda")
    override fun bildirimIlleri() = nav.navigate("bildirim-illeri")
    override fun giris() = nav.navigate("giris") { launchSingleTop = true }
    override fun kayit() = nav.navigate("kayit") { launchSingleTop = true }
    override fun ilanlarSekmesi() = sekmeSec(0)
    override fun geri() { if (!nav.popBackStack()) Unit }
}

private data class Sekme(val ad: String, val ikon: ImageVector)

private val SEKMELER = listOf(
    Sekme("İlanlar", Icons.AutoMirrored.Outlined.List),
    Sekme("Bana Uygun", Icons.Outlined.TrackChanges),
    Sekme("Kaydedilen", Icons.Outlined.BookmarkBorder),
    Sekme("Hesap", Icons.Outlined.PersonOutline),
)

@Composable
private fun Uygulama(bekleyenIlan: MutableStateFlow<String?>, bekleyenSekme: MutableStateFlow<Int?>, bekleyenRota: MutableStateFlow<String?>, bildirimIzniIste: () -> Unit) {
    val p = T.p
    val nav = rememberNavController()
    var sekme by remember { mutableIntStateOf(0) }
    val git = remember(nav) { NavGezgin(nav) { sekme = it } }
    val snack = remember { SnackbarHostState() }
    val kapsam = rememberCoroutineScope()
    val bildir: (String) -> Unit = { m -> kapsam.launch { snack.currentSnackbarData?.dismiss(); snack.showSnackbar(m) } }
    val ayar by Depo.ayarlar.akis.collectAsState()
    val proMesaj by Pro.mesaj.collectAsState()
    val ilanId by bekleyenIlan.collectAsState()
    var baslangic by remember { mutableStateOf(if (ayar.karsilamaGoruldu) "sekmeler" else "karsilama") }

    LaunchedEffect(proMesaj) { proMesaj?.let { bildir(it); Pro.mesajOkundu() } }
    LaunchedEffect(ilanId) { ilanId?.let { git.ilan(it); bekleyenIlan.value = null } }
    val rota by bekleyenRota.collectAsState()
    LaunchedEffect(rota) { rota?.let { nav.navigate(it); bekleyenRota.value = null } }
    val sekmeIstegi by bekleyenSekme.collectAsState()
    LaunchedEffect(sekmeIstegi) {
        sekmeIstegi?.let {
            sekme = it
            if (nav.currentDestination?.route != "sekmeler") nav.navigate("sekmeler") { popUpTo(0) }
            bekleyenSekme.value = null
        }
    }

    Scaffold(
        containerColor = p.paper,
        snackbarHost = {
            SnackbarHost(snack) { Snackbar(it, containerColor = p.ink, contentColor = p.paper, shape = RoundedCornerShape(14.dp)) }
        },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
    ) { ic ->
        NavHost(nav, startDestination = baslangic, modifier = Modifier.padding(ic).background(p.paper)) {
            composable("karsilama") {
                KarsilamaEkrani(bitti = {
                    baslangic = "sekmeler"
                    nav.navigate("sekmeler") { popUpTo("karsilama") { inclusive = true } }
                }, bildirimIzniIste = bildirimIzniIste)
            }
            composable("sekmeler") {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        when (sekme) {
                            0 -> IlanlarEkrani(git, bildir)
                            1 -> UygunEkrani(git, bildir)
                            2 -> KayitliEkrani(git, bildir)
                            else -> HesapEkrani(git, bildirimIzniIste, bildir)
                        }
                    }
                    ReklamAlani()
                    NavigationBar(containerColor = p.sheet, tonalElevation = 0.dp) {
                        SEKMELER.forEachIndexed { i, s ->
                            NavigationBarItem(
                                selected = sekme == i,
                                onClick = { sekme = i },
                                icon = { Icon(s.ikon, s.ad, Modifier.size(24.dp)) },
                                label = { Yazi(s.ad, boyut = 11, agirlik = FontWeight.SemiBold, renk = if (sekme == i) p.ink else p.ink3) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = p.onMarker, indicatorColor = p.marker,
                                    unselectedIconColor = p.ink3,
                                ),
                            )
                        }
                    }
                }
            }
            composable("ilan/{id}") { DetayEkrani(it.arguments?.getString("id").orEmpty(), git, bildir) }
            composable("filtre") { FiltreEkrani(git) }
            composable("pro") { ProEkrani(git) }
            composable("profil") { ProfilEkrani(git, bildir) }
            composable("gundem") { GundemEkrani(git) }
            composable("hakkinda") { HakkindaEkrani(git) }
            composable("bildirim-illeri") { BildirimIlleriEkrani(git) }
            composable("giris") { com.selcukyus.adaymemur.ui.screens.GirisEkrani(git, bildir) }
            composable("kayit") { com.selcukyus.adaymemur.ui.screens.GirisEkrani(git, bildir, kayitModu = true) }
        }
    }
}
