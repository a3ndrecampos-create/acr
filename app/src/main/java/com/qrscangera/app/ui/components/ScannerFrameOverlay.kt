package com.qrscangera.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.qrscangera.app.ui.theme.BrandBlue
import com.qrscangera.app.ui.theme.SuccessGreen

/**
 * Moldura de foco estilo "mira", com os 4 cantos destacados e uma animação de pulso sutil.
 * Fica verde por um instante quando [detected] vira true, como pedido no briefing.
 */
@Composable
fun ScannerFrameOverlay(
    modifier: Modifier = Modifier,
    detected: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "scanner_pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )

    val color = if (detected) SuccessGreen else BrandBlue

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val frameSize = size.minDimension * 0.68f * pulse
            val left = (size.width - frameSize) / 2f
            val top = (size.height - frameSize) / 2f
            val cornerLen = frameSize * 0.16f
            val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)

            fun corner(x: Float, y: Float, dx: Float, dy: Float) {
                drawLine(color, Offset(x, y), Offset(x + dx, y), strokeWidth = stroke.width, cap = stroke.cap)
                drawLine(color, Offset(x, y), Offset(x, y + dy), strokeWidth = stroke.width, cap = stroke.cap)
            }

            // Canto superior-esquerdo
            corner(left, top, cornerLen, cornerLen)
            // Canto superior-direito
            corner(left + frameSize, top, -cornerLen, cornerLen)
            // Canto inferior-esquerdo
            corner(left, top + frameSize, cornerLen, -cornerLen)
            // Canto inferior-direito
            corner(left + frameSize, top + frameSize, -cornerLen, -cornerLen)
        }
    }
}
