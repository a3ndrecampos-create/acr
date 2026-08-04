package com.qrscangera.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Cantos arredondados padrão do app (16dp) e elevação suave (2-4dp), como pedido no briefing
val AppShapes = androidx.compose.material3.Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

private val DarkColors = darkColorScheme(
    primary = BrandBlue,
    secondary = BrandPurple,
    tertiary = BrandPurple,
    background = DarkGradientTop,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFEAEBFF),
    onSurface = Color(0xFFEAEBFF),
    error = ErrorRed
)

private val LightColors = lightColorScheme(
    primary = BrandBlue,
    secondary = BrandPurple,
    tertiary = BrandPurple,
    background = LightGradientTop,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF15172E),
    onSurface = Color(0xFF15172E),
    error = ErrorRed
)

/** Gradiente de fundo sutil, usado atrás das telas principais (dark: azul/roxo escuro | light: branco/azul claro). */
@Composable
fun AppBackgroundGradient(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) listOf(DarkGradientTop, DarkGradientBottom) else listOf(LightGradientTop, LightGradientBottom)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        content()
    }
}

@Composable
fun QrScanGeraTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
