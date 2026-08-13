package com.qrscangera.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qrscangera.app.data.QrType
import com.qrscangera.app.ui.components.QrColorPicker
import com.qrscangera.app.ui.components.QrTypeChips
import com.qrscangera.app.utils.ImageSaver
import com.qrscangera.app.viewmodel.GenerateViewModel

@Composable
fun GenerateScreen(viewModel: GenerateViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    val logoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bitmap: Bitmap? = try {
                android.graphics.BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
            } catch (e: Exception) { null }
            viewModel.updateLogo(bitmap)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        QrTypeChips(selected = state.selectedType, onSelect = viewModel::selectType)
        Spacer(Modifier.height(16.dp))

        // ─── Campos de entrada, de acordo com o tipo selecionado ───────────────
        when (state.selectedType) {
            QrType.TEXT, QrType.LINK -> OutlinedTextField(
                value = state.rawText,
                onValueChange = viewModel::updateRawText,
                label = { Text("Conteúdo") },
                placeholder = { Text(stringResource()) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            QrType.WIFI -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.rawText, onValueChange = viewModel::updateRawText,
                    label = { Text("Nome da rede (SSID)") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.wifiPassword, onValueChange = viewModel::updateWifiPassword,
                    label = { Text("Senha") }, modifier = Modifier.fillMaxWidth()
                )
            }

            QrType.CONTACT -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = state.rawText, onValueChange = viewModel::updateRawText,
                    label = { Text("Nome") }, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.contactPhone, onValueChange = viewModel::updateContactPhone,
                    label = { Text("Telefone") }, modifier = Modifier.fillMaxWidth()
                )
            }

            // Geração de Pix foi removida (estava com erro); QrType.PIX ainda existe só
            // para a leitura/scanner reconhecer o tipo, então esse branch nunca é
            // alcançado aqui (Pix não aparece mais nos chips de seleção acima).
            QrType.PIX -> {}
        }

        Spacer(Modifier.height(20.dp))

        // ─── Preview do QR Code em tempo real ───────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (state.bitmap != null) {
                    Image(
                        bitmap = state.bitmap!!.asImageBitmap(),
                        contentDescription = "QR Code gerado",
                        modifier = Modifier.size(220.dp)
                    )
                } else {
                    Column(
                        Modifier.size(220.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Preencha os campos acima\npara ver o QR Code", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Customização visual ────────────────────────────────────────────────
        Text("Cor do QR Code", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        QrColorPicker(selectedColorInt = state.style.color, onColorSelected = viewModel::updateColor)

        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cantos arredondados")
            Switch(checked = state.style.roundedCorners, onCheckedChange = viewModel::updateRoundedCorners)
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = { logoPickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            androidx.compose.material3.Icon(Icons.Default.ImageIcon, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(if (state.style.logo == null) "Adicionar logo" else "Trocar logo")
        }
        if (state.style.logo != null) {
            Spacer(Modifier.height(4.dp))
            OutlinedButton(onClick = { viewModel.updateLogo(null) }, modifier = Modifier.fillMaxWidth()) {
                Text("Remover logo")
            }
        }

        Spacer(Modifier.height(20.dp))

        // ─── Ações ───────────────────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    state.bitmap?.let {
                        ImageSaver.saveToGallery(context, it)
                        viewModel.saveToHistory()
                    }
                },
                enabled = state.hasContent,
                modifier = Modifier.weight(1f)
            ) {
                androidx.compose.material3.Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Salvar")
            }
            OutlinedButton(
                onClick = {
                    state.bitmap?.let {
                        val intent = ImageSaver.shareBitmap(context, it)
                        context.startActivity(android.content.Intent.createChooser(intent, null))
                        viewModel.saveToHistory()
                    }
                },
                enabled = state.hasContent,
                modifier = Modifier.weight(1f)
            ) {
                androidx.compose.material3.Icon(Icons.Default.Share, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Compartilhar")
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun stringResource(): String = com.qrscangera.app.R.string.generate_hint.let {
    androidx.compose.ui.res.stringResource(it)
}
