package com.qrscangera.app.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta principal do app - azul elétrico com acento roxo
val BrandBlue = Color(0xFF2D5FFF)
val BrandPurple = Color(0xFF7B61FF)

// Tons usados no gradiente de fundo (dark mode: azul/roxo escuro | light mode: branco/azul claro)
val DarkGradientTop = Color(0xFF0E1030)
val DarkGradientBottom = Color(0xFF1B1440)
val LightGradientTop = Color(0xFFF5F7FF)
val LightGradientBottom = Color(0xFFE9EEFF)

val DarkSurface = Color(0xFF14162B)
val DarkSurfaceVariant = Color(0xFF1E2140)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF0F3FF)

val SuccessGreen = Color(0xFF2ECC71)
val ErrorRed = Color(0xFFFF5C5C)

// Paleta de 6 cores rápidas para customização do QR Code gerado
val QrColorSwatches = listOf(
    BrandBlue,
    BrandPurple,
    Color(0xFF111111), // preto clássico
    Color(0xFF0FA37F), // verde
    Color(0xFFFF7A45), // laranja
    Color(0xFFE6396C)  // rosa
)
