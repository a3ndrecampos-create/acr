package com.qrscangera.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.qrscangera.app.QrScanGeraApp
import com.qrscangera.app.data.HistoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(app: Application) : AndroidViewModel(app) {
    private val historyDao = (app as QrScanGeraApp).database.historyDao()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val filteredHistory: StateFlow<List<HistoryEntity>> =
        combine(historyDao.observeAll(), _query) { items, q ->
            if (q.isBlank()) items
            else items.filter { it.content.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateQuery(q: String) {
        _query.value = q
    }

    fun delete(entity: HistoryEntity) {
        viewModelScope.launch { historyDao.delete(entity) }
    }
}
