package com.qrscangera.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Splash screen padrão da Arka Cortex (ver prompt-logo-arkacortex.md / memória
 * "arka-cortex-splash-padrao"). É a Activity de launcher do app - abre a MainActivity
 * sozinha ao final da animação e se fecha.
 *
 * Timings: nome do app fade-in (0-500ms) -> aguarda até 900ms -> "from" + logo fade-in
 * (900-1200ms, dura 300ms) -> aguarda até 1800ms no total -> abre a MainActivity.
 */
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplashContent(onFinished = {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            })
        }
    }
}

@Composable
private fun SplashContent(onFinished: () -> Unit) {
    var nameVisible by remember { mutableFloatStateOf(0f) }
    var footerVisible by remember { mutableFloatStateOf(0f) }

    val nameAlpha by animateFloatAsState(targetValue = nameVisible, animationSpec = tween(500), label = "name_alpha")
    val footerAlpha by animateFloatAsState(targetValue = footerVisible, animationSpec = tween(300), label = "footer_alpha")

    LaunchedEffect(Unit) {
        nameVisible = 1f   // 0ms: começa o fade-in do nome do app (dura 500ms)
        delay(900)         // aguarda até 900ms no total
        footerVisible = 1f // 900ms: começa o fade-in de "from" + logo (dura 300ms, termina 1200ms)
        delay(900)         // 900ms + 900ms = 1800ms no total
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A2E)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            color = Color(0xFFFF6B00),
            fontWeight = FontWeight.Bold,
            fontSize = 64.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(nameAlpha)
                .padding(horizontal = 24.dp)
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
                .alpha(footerAlpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "from",
                color = Color(0xFF8B93A7),
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )
            Spacer(Modifier.height(8.dp))
            Image(
                painter = painterResource(R.drawable.logo_arkacortex),
                contentDescription = "Arka Cortex",
                modifier = Modifier.width(200.dp)
            )
        }
    }
}
