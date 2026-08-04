package com.qrscangera.app.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Opções de customização visual do QR Code gerado, escolhidas na aba "Gerar". */
data class QrStyle(
    val color: Int = Color.parseColor("#2D5FFF"),
    val roundedCorners: Boolean = true,
    val logo: Bitmap? = null
)

/**
 * Gera o Bitmap do QR Code a partir do conteúdo e do [QrStyle] escolhido pelo usuário.
 *
 * Usamos o ZXing só para calcular a matriz de módulos (BitMatrix) - a pintura de cada módulo
 * é feita manualmente aqui, o que permite desenhar cantos arredondados e colorir o código,
 * algo que o encoder padrão do ZXing não faz sozinho.
 */
object QrCodeGenerator {

    fun generate(content: String, sizePx: Int, style: QrStyle): Bitmap {
        // Nível de correção de erro alto (H) - necessário para o QR continuar legível
        // mesmo com um logo cobrindo parte central dele.
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1
        )
        val matrix: BitMatrix = QRCodeWriter().encode(content, com.google.zxing.BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = style.color }

        val moduleWidth = sizePx.toFloat() / matrix.width
        val moduleHeight = sizePx.toFloat() / matrix.height
        val cornerRadius = if (style.roundedCorners) moduleWidth * 0.35f else 0f

        for (x in 0 until matrix.width) {
            for (y in 0 until matrix.height) {
                if (matrix.get(x, y)) {
                    val left = x * moduleWidth
                    val top = y * moduleHeight
                    val rect = RectF(left, top, left + moduleWidth, top + moduleHeight)
                    if (cornerRadius > 0f) {
                        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
                    } else {
                        canvas.drawRect(rect, paint)
                    }
                }
            }
        }

        style.logo?.let { logo ->
            drawLogo(canvas, logo, sizePx)
        }

        return bitmap
    }

    private fun drawLogo(canvas: Canvas, logo: Bitmap, sizePx: Int) {
        val logoSize = (sizePx * 0.22f).toInt()
        val cx = sizePx / 2f
        val cy = sizePx / 2f

        // Fundo branco circular atrás do logo para não atrapalhar a leitura do QR
        val bgRadius = logoSize * 0.62f
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawCircle(cx, cy, bgRadius, bgPaint)

        val scaledLogo = Bitmap.createScaledBitmap(logo, logoSize, logoSize, true)
        canvas.drawBitmap(scaledLogo, cx - logoSize / 2f, cy - logoSize / 2f, Paint(Paint.ANTI_ALIAS_FLAG))
    }
}
