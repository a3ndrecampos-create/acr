package com.qrscangera.app.data

/** Tipo de conteúdo representado por um QR Code, tanto lido quanto gerado. */
enum class QrType {
    TEXT,
    LINK,
    WIFI,
    WHATSAPP,
    CONTACT,
    PIX;

    companion object {
        /** Detecta o tipo a partir do conteúdo bruto lido da câmera. */
        fun detect(raw: String): QrType = when {
            raw.startsWith("WIFI:") -> WIFI
            raw.startsWith("BEGIN:VCARD", ignoreCase = true) -> CONTACT
            raw.startsWith("http://") || raw.startsWith("https://") -> LINK
            raw.startsWith("00020101") -> PIX // payload EMV do Pix sempre começa com o Payload Format Indicator
            else -> TEXT
        }
    }
}

/** Origem de um item de histórico: se veio de um escaneamento ou de uma geração. */
enum class HistorySource { SCANNED, GENERATED }
