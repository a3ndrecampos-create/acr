package com.qrscangera.app.utils

/**
 * Monta o payload do "Pix Copia e Cola" (BR Code), no formato EMV definido pelo Banco Central
 * (cada campo é ID de 2 dígitos + tamanho de 2 dígitos + valor), terminando com o CRC16 do
 * payload inteiro. É esse texto que vira o conteúdo do QR Code do tipo Pix.
 */
object PixPayloadBuilder {

    fun build(
        chave: String,
        nomeRecebedor: String,
        cidade: String,
        valor: Double? = null,
        txId: String = "***"
    ): String {
        val nome = sanitize(nomeRecebedor).take(25).ifBlank { "RECEBEDOR" }
        val cidadeSan = sanitize(cidade).take(15).ifBlank { "BRASIL" }

        val merchantAccountInfo = field("00", "br.gov.bcb.pix") + field("01", chave.trim())
        val additionalData = field("05", txId.ifBlank { "***" })

        val payloadSemCrc = buildString {
            append(field("00", "01"))                       // Payload Format Indicator
            append(field("26", merchantAccountInfo))         // Merchant Account Information - Pix
            append(field("52", "0000"))                      // Merchant Category Code
            append(field("53", "986"))                       // Moeda (BRL)
            if (valor != null && valor > 0) {
                append(field("54", "%.2f".format(valor)))     // Valor da transação (opcional)
            }
            append(field("58", "BR"))                         // País
            append(field("59", nome))                         // Nome do recebedor
            append(field("60", cidadeSan))                    // Cidade do recebedor
            append(field("62", additionalData))               // Dados adicionais (txid)
        }

        // O campo do CRC entra com tamanho fixo "04" mas sem o valor ainda, pois o CRC
        // é calculado sobre o payload já incluindo "6304" no final.
        val payloadParaCrc = payloadSemCrc + "6304"
        val crc = crc16Ccitt(payloadParaCrc)
        return payloadParaCrc + crc
    }

    private fun field(id: String, value: String): String {
        val length = value.toByteArray(Charsets.UTF_8).size.toString().padStart(2, '0')
        return "$id$length$value"
    }

    private fun sanitize(value: String): String =
        java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "") // remove acentos
            .uppercase()
            .replace(Regex("[^A-Z0-9 ]"), "")
            .trim()

    /** CRC16-CCITT (falso), polinômio 0x1021, valor inicial 0xFFFF - padrão exigido pelo Pix. */
    private fun crc16Ccitt(data: String): String {
        var crc = 0xFFFF
        val bytes = data.toByteArray(Charsets.UTF_8)
        for (b in bytes) {
            crc = crc xor (b.toInt() shl 8 and 0xFF00)
            repeat(8) {
                crc = if (crc and 0x8000 != 0) (crc shl 1) xor 0x1021 else crc shl 1
                crc = crc and 0xFFFF
            }
        }
        return crc.toString(16).uppercase().padStart(4, '0')
    }
}
