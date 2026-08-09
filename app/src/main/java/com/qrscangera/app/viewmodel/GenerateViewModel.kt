package com.qrscangera.app.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qrscangera.app.QrScanGeraApp
import com.qrscangera.app.data.HistoryEntity
import com.qrscangera.app.data.HistorySource
import com.qrscangera.app.data.QrType
import com.qrscangera.app.utils.PixPayloadBuilder
import com.qrscangera.app.utils.QrCodeGenerator
import com.qrscangera.app.utils.QrStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Campos específicos preenchidos quando o tipo escolhido é Pix. */
data class PixFields(val chave: String = "", val nome: String = "", val cidade: String = "", val valor: String = "")

data class GenerateUiState(
    val selectedType: QrType = QrType.TEXT,
    val rawText: String = "",          // usado para Texto, Link, Wi-Fi (SSID), Contato (nome)
    val wifiPassword: String = "",
    val contactPhone: String = "",
    val pix: PixFields = PixFields(),
    val style: QrStyle = QrStyle(),
    val bitmap: Bitmap? = null,
    val hasContent: Boolean = false
)

class GenerateViewModel(app: Application) : AndroidViewModel(app) {
    private val historyDao = (app as QrScanGeraApp).database.historyDao()

    private val _uiState = MutableStateFlow(GenerateUiState())
    val uiState: StateFlow<GenerateUiState> = _uiState

    private var debounceJob: Job? = null

    fun selectType(type: QrType) {
        _uiState.update { it.copy(selectedType = type) }
        regenerate()
    }

    fun updateRawText(text: String) {
        _uiState.update { it.copy(rawText = text) }
        regenerateDebounced()
    }

    fun updateWifiPassword(text: String) {
        _uiState.update { it.copy(wifiPassword = text) }
        regenerateDebounced()
    }

    fun updateContactPhone(text: String) {
        _uiState.update { it.copy(contactPhone = text) }
        regenerateDebounced()
    }

    fun updatePix(pix: PixFields) {
        _uiState.update { it.copy(pix = pix) }
        regenerateDebounced()
    }

    fun updateColor(colorInt: Int) {
        _uiState.update { it.copy(style = it.style.copy(color = colorInt)) }
        regenerate()
    }

    fun updateRoundedCorners(rounded: Boolean) {
        _uiState.update { it.copy(style = it.style.copy(roundedCorners = rounded)) }
        regenerate()
    }

    fun updateLogo(logo: Bitmap?) {
        _uiState.update { it.copy(style = it.style.copy(logo = logo)) }
        regenerate()
    }

    /** Debounce de 300ms conforme pedido no briefing, para não regerar a cada tecla digitada. */
    private fun regenerateDebounced() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            regenerate()
        }
    }

    private fun buildContent(state: GenerateUiState): String? = when (state.selectedType) {
        QrType.TEXT, QrType.LINK -> state.rawText.trim().ifBlank { null }
        QrType.WIFI -> {
            val ssid = state.rawText.trim()
            if (ssid.isBlank()) null
            else "WIFI:S:${escape(ssid)};T:WPA;P:${escape(state.wifiPassword.trim())};;"
        }
        QrType.CONTACT -> {
            val nome = state.rawText.trim()
            if (nome.isBlank()) null
            else buildString {
                append("BEGIN:VCARD\nVERSION:3.0\n")
                append("FN:$nome\n")
                if (state.contactPhone.isNotBlank()) append("TEL:${state.contactPhone.trim()}\n")
                append("END:VCARD")
            }
        }
        QrType.PIX -> {
            val p = state.pix
            if (p.chave.isBlank() || p.nome.isBlank() || p.cidade.isBlank()) null
            else PixPayloadBuilder.build(
                chave = p.chave.trim(),
                nomeRecebedor = p.nome,
                cidade = p.cidade,
                valor = p.valor.replace(",", ".").toDoubleOrNull()
            )
        }
    }

    private fun escape(value: String) = value.replace(";", "\\;").replace(":", "\\:")

    private fun regenerate() {
        viewModelScope.launch {
            val state = _uiState.value
            val content = buildContent(state)
            if (content == null) {
                _uiState.update { it.copy(bitmap = null, hasContent = false) }
                return@launch
            }
            val bitmap = withContext(Dispatchers.Default) {
                QrCodeGenerator.generate(content, 800, state.style)
            }
            _uiState.update { it.copy(bitmap = bitmap, hasContent = true) }
        }
    }

    /** Salva o QR Code gerado no histórico local - chamado ao Salvar/Compartilhar. */
    fun saveToHistory() {
        val state = _uiState.value
        val content = buildContent(state) ?: return
        viewModelScope.launch {
            historyDao.insert(
                HistoryEntity(
                    content = content,
                    type = state.selectedType,
                    source = HistorySource.GENERATED,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
}
