package com.qrscangera.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Um item do histórico: tanto QR Codes escaneados quanto gerados. */
@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val type: QrType,
    val source: HistorySource,
    val timestamp: Long
)
