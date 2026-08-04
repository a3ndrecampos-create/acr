package com.qrscangera.app.data

import androidx.room.TypeConverter

/** Conversores para o Room salvar os enums do app como texto simples. */
class Converters {
    @TypeConverter
    fun fromQrType(value: QrType): String = value.name

    @TypeConverter
    fun toQrType(value: String): QrType = QrType.valueOf(value)

    @TypeConverter
    fun fromHistorySource(value: HistorySource): String = value.name

    @TypeConverter
    fun toHistorySource(value: String): HistorySource = HistorySource.valueOf(value)
}
