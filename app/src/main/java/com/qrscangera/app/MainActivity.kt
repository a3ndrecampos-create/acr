package com.qrscangera.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.qrscangera.app.billing.BillingManager
import com.qrscangera.app.ui.components.ProUpsellCard
import com.qrscangera.app.ui.screens.GenerateScreen
import com.qrscangera.app.ui.screens.HistoryScreen
import com.qrscangera.app.ui.screens.ScanScreen
import com.qrscangera.app.ui.theme.AppBackgroundGradient
import com.qrscangera.app.ui.theme.QrScanGeraTheme
import com.qrscangera.app.viewmodel.GenerateViewModel
import com.qrscangera.app.viewmodel.HistoryViewModel
import com.qrscangera.app.viewmodel.ScanViewModel

/**
 * Tela principal do app (bottom nav com as 3 abas). Quem abre por último no fluxo de
 * inicialização: a SplashActivity.kt é o launcher e chama esta Activity ao terminar
 * a animação padrão da Arka Cortex.
 *
 * Versão Free: só a aba "Escanear" funciona de verdade. "Gerar" e "Histórico" mostram
 * a tela de upgrade (ProUpsellCard) até a compra do Pro ser confirmada pelo BillingManager.
 */
class MainActivity : ComponentActivity() {

    private val scanViewModel: ScanViewModel by viewModels()
    private val generateViewModel: GenerateViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BillingManager.initialize(this)

        setContent {
            QrScanGeraTheme {
                val darkTheme = isSystemInDarkTheme()
                AppBackgroundGradient(darkTheme = darkTheme) {
                    MainScreen(scanViewModel, generateViewModel, historyViewModel)
                }
            }
        }
    }
}

@Composable
private fun MainScreen(
    scanViewModel: ScanViewModel,
    generateViewModel: GenerateViewModel,
    historyViewModel: HistoryViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isPro by BillingManager.isPro.collectAsState()
    val monthlyPrice by BillingManager.monthlyPriceLabel.collectAsState()
    val lifetimePrice by BillingManager.lifetimePriceLabel.collectAsState()

    val onSubscribeMonthly: () -> Unit = { activity?.let { BillingManager.launchPurchase(it, BillingManager.PRO_MONTHLY_PRODUCT_ID) } }
    val onBuyLifetime: () -> Unit = { activity?.let { BillingManager.launchPurchase(it, BillingManager.PRO_LIFETIME_PRODUCT_ID) } }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.Transparent) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_scan)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { TabIcon(Icons.Default.QrCode2, locked = !isPro) },
                    label = { Text(stringResource(R.string.tab_generate)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { TabIcon(Icons.Default.History, locked = !isPro) },
                    label = { Text(stringResource(R.string.tab_history)) }
                )
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = selectedTab,
            modifier = androidx.compose.ui.Modifier.padding(padding),
            transitionSpec = {
                (fadeIn() + slideInHorizontally { it / 4 }) togetherWith
                    (fadeOut() + slideOutHorizontally { -it / 4 })
            },
            label = "tab_transition"
        ) { tab ->
            when (tab) {
                0 -> ScanScreen(scanViewModel)
                1 -> if (isPro) GenerateScreen(generateViewModel) else ProUpsellCard("Gerar QR Codes", monthlyPrice, lifetimePrice, onSubscribeMonthly, onBuyLifetime)
                else -> if (isPro) HistoryScreen(historyViewModel) else ProUpsellCard("O histórico", monthlyPrice, lifetimePrice, onSubscribeMonthly, onBuyLifetime)
            }
        }
    }
}

/** Ícone da aba com um pequeno cadeado no canto quando o recurso ainda é bloqueado (Free). */
@Composable
private fun TabIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, locked: Boolean) {
    if (locked) {
        BadgedBox(badge = { Badge { Icon(Icons.Default.Lock, contentDescription = null, modifier = androidx.compose.ui.Modifier.size(10.dp)) } }) {
            Icon(icon, contentDescription = null)
        }
    } else {
        Icon(icon, contentDescription = null)
    }
}
