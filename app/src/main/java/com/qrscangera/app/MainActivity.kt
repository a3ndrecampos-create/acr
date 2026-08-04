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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.qrscangera.app.ui.screens.GenerateScreen
import com.qrscangera.app.ui.screens.HistoryScreen
import com.qrscangera.app.ui.screens.ScanScreen
import com.qrscangera.app.ui.theme.AppBackgroundGradient
import com.qrscangera.app.ui.theme.QrScanGeraTheme
import com.qrscangera.app.viewmodel.GenerateViewModel
import com.qrscangera.app.viewmodel.HistoryViewModel
import com.qrscangera.app.viewmodel.ScanViewModel

class MainActivity : ComponentActivity() {

    private val scanViewModel: ScanViewModel by viewModels()
    private val generateViewModel: GenerateViewModel by viewModels()
    private val historyViewModel: HistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen() // API 31+ mostra a splash nativa do sistema; abaixo disso, a tela abre direto
        super.onCreate(savedInstanceState)

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
                    icon = { Icon(Icons.Default.QrCode2, contentDescription = null) },
                    label = { Text(stringResource(R.string.tab_generate)) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
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
                1 -> GenerateScreen(generateViewModel)
                else -> HistoryScreen(historyViewModel)
            }
        }
    }
}
