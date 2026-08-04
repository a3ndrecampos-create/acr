package com.qrscangera.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qrscangera.app.QrScanGeraApp
import com.qrscangera.app.data.HistoryEntity
import com.qrscangera.app.data.HistorySource
import com.qrscangera.app.data.QrType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Resultado de uma leitura, já com o tipo detectado, pronto para o bottom sheet exibir. */
data class ScanResult(val content: String, val type: QrType)

class ScanViewModel(app: Application) : AndroidViewModel(app) {
    private val historyDao = (app as QrScanGeraApp).database.historyDao()

    private val _lastResult = MutableStateFlow<ScanResult?>(null)
    val lastResult: StateFlow<ScanResult?> = _lastResult

    // Incrementado a cada leitura confirmada - usado pela tela para decidir quando
    // chamar AdsManager.onScanCompleted(activity).
    private val _scanCompletedTick = MutableStateFlow(0)
    val scanCompletedTick: StateFlow<Int> = _scanCompletedTick

    /** Chamado assim que a câmera detecta e decodifica um QR Code. */
    fun onQrDetected(raw: String) {
        if (_lastResult.value != null) return // já tem um resultado aberto, ignora novas leituras
        val type = QrType.detect(raw)
        _lastResult.value = ScanResult(raw, type)
        viewModelScope.launch {
            historyDao.insert(
                HistoryEntity(
                    content = raw,
                    type = type,
                    source = HistorySource.SCANNED,
                    timestamp = System.currentTimeMillis()
                )
            )
            _scanCompletedTick.value += 1
        }
    }

    fun dismissResult() {
        _lastResult.value = null
    }
}
