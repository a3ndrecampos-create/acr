package com.qrscangera.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Fonte do app: "Inter"/"Poppins", como pedido no briefing.
 *
 * Para usar a fonte real, baixe os arquivos .ttf em https://fonts.google.com/specimen/Poppins,
 * coloque-os em app/src/main/res/font/ (ex: poppins_regular.ttf, poppins_medium.ttf,
 * poppins_semibold.ttf) e troque o FontFamily.Default abaixo por:
 *
 * val Poppins = FontFamily(
 *     Font(R.font.poppins_regular, FontWeight.Normal),
 *     Font(R.font.poppins_medium, FontWeight.Medium),
 *     Font(R.font.poppins_semibold, FontWeight.SemiBold),
 *     Font(R.font.poppins_bold, FontWeight.Bold),
 * )
 *
 * Não incluí os binários da fonte aqui para não depender de download em tempo de build;
 * enquanto isso o app usa a fonte padrão do sistema para não quebrar a compilação.
 */
val AppFontFamily = FontFamily.Default

val AppTypography = Typography(
    headlineSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp),
)
