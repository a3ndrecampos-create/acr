package com.qrscangera.app.data

/** Dados extraídos de um QR Code do tipo Wi-Fi (formato `WIFI:S:<ssid>;T:<WPA|WEP|nopass>;P:<senha>;;`). */
data class WifiInfo(val ssid: String, val password: String, val security: String)

/** Dados extraídos de um QR Code do tipo contato (vCard). */
data class ContactInfo(val name: String, val phone: String?, val email: String?)

/**
 * Extrai os campos relevantes do conteúdo bruto lido pela câmera, de acordo com o [QrType]
 * já detectado por [QrType.detect].
 */
object QrContentParser {

    fun parseWifi(raw: String): WifiInfo? {
        // Ex: WIFI:S:MinhaRede;T:WPA;P:minhasenha123;;
        val ssid = extractField(raw, "S") ?: return null
        val password = extractField(raw, "P").orEmpty()
        val security = extractField(raw, "T") ?: "WPA"
        return WifiInfo(unescape(ssid), unescape(password), security)
    }

    private fun extractField(raw: String, key: String): String? {
        val marker = "$key:"
        val start = raw.indexOf(marker).takeIf { it >= 0 } ?: return null
        val from = start + marker.length
        val sb = StringBuilder()
        var i = from
        while (i < raw.length) {
            val c = raw[i]
            if (c == '\\' && i + 1 < raw.length) {
                sb.append(raw[i + 1])
                i += 2
                continue
            }
            if (c == ';') break
            sb.append(c)
            i++
        }
        return sb.toString()
    }

    private fun unescape(value: String) = value.replace("\\;", ";").replace("\\:", ":").replace("\\,", ",")

    fun parseVCard(raw: String): ContactInfo {
        val lines = raw.lines()
        val name = lines.firstOrNull { it.startsWith("FN:", ignoreCase = true) }
            ?.substringAfter(":")?.trim()
            ?: lines.firstOrNull { it.startsWith("N:", ignoreCase = true) }
                ?.substringAfter(":")?.replace(";", " ")?.trim()
            ?: "Contato"
        val phone = lines.firstOrNull { it.startsWith("TEL", ignoreCase = true) }
            ?.substringAfter(":")?.trim()
        val email = lines.firstOrNull { it.startsWith("EMAIL", ignoreCase = true) }
            ?.substringAfter(":")?.trim()
        return ContactInfo(name, phone, email)
    }
}
