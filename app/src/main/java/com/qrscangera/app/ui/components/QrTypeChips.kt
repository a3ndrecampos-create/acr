package com.qrscangera.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qrscangera.app.data.QrType

private val ORDER = listOf(QrType.TEXT, QrType.LINK, QrType.WIFI, QrType.CONTACT, QrType.PIX)

private fun label(type: QrType) = when (type) {
    QrType.TEXT -> "Texto"
    QrType.LINK -> "Link"
    QrType.WIFI -> "Wi-Fi"
    QrType.CONTACT -> "Contato"
    QrType.PIX -> "Pix"
}

/** Seletor rápido de tipo de conteúdo a gerar, com chips horizontais (Texto | Link | Wi-Fi | Contato | Pix). */
@Composable
fun QrTypeChips(selected: QrType, onSelect: (QrType) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(ORDER) { type ->
            FilterChip(
                selected = type == selected,
                onClick = { onSelect(type) },
                label = { Text(label(type)) }
            )
        }
    }
}
