package com.qrscangera.app.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.qrscangera.app.ui.theme.QrColorSwatches

/** Paleta com as 6 cores rápidas + opção de cor personalizada (RGB), para colorir o QR gerado. */
@Composable
fun QrColorPicker(selectedColorInt: Int, onColorSelected: (Int) -> Unit) {
    var showCustomDialog by remember { mutableStateOf(false) }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        QrColorSwatches.forEach { swatch ->
            val colorInt = swatch.toArgbInt()
            SwatchDot(color = swatch, selected = colorInt == selectedColorInt) { onColorSelected(colorInt) }
        }
        // Botão "+" para cor personalizada
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable { showCustomDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Cor personalizada", modifier = Modifier.size(16.dp))
        }
    }

    if (showCustomDialog) {
        CustomColorDialog(
            initial = selectedColorInt,
            onConfirm = { onColorSelected(it); showCustomDialog = false },
            onDismiss = { showCustomDialog = false }
        )
    }
}

private fun Color.toArgbInt(): Int = AndroidColor.rgb(
    (red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt()
)

@Composable
private fun SwatchDot(color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(if (selected) 2.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun CustomColorDialog(initial: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var r by remember { mutableFloatStateOf(AndroidColor.red(initial).toFloat()) }
    var g by remember { mutableFloatStateOf(AndroidColor.green(initial).toFloat()) }
    var b by remember { mutableFloatStateOf(AndroidColor.blue(initial).toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cor personalizada") },
        text = {
            Column {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .background(Color(AndroidColor.rgb(r.toInt(), g.toInt(), b.toInt())))
                )
                Spacer(Modifier.height(12.dp))
                Text("Vermelho"); Slider(value = r, onValueChange = { r = it }, valueRange = 0f..255f)
                Text("Verde"); Slider(value = g, onValueChange = { g = it }, valueRange = 0f..255f)
                Text("Azul"); Slider(value = b, onValueChange = { b = it }, valueRange = 0f..255f)
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(AndroidColor.rgb(r.toInt(), g.toInt(), b.toInt())) }) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
