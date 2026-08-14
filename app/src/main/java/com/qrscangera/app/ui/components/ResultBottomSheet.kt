package com.qrscangera.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.qrscangera.app.data.QrContentParser
import com.qrscangera.app.data.QrType
import com.qrscangera.app.viewmodel.ScanResult

/**
 * Bottom sheet que aparece após detectar um QR Code, com as ações certas para cada tipo
 * de conteúdo (link, texto, wi-fi, contato), como pedido no briefing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultBottomSheet(result: ScanResult, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text(typeLabel(result.type), fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(result.content, maxLines = 4)
            Spacer(Modifier.height(16.dp))

            when (result.type) {
                QrType.LINK -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(result.content)))
                    }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Abrir link")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { clipboard.setText(AnnotatedString(result.content)) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Copiar")
                        }
                        OutlinedButton(onClick = { shareText(context, result.content) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Share, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Compartilhar")
                        }
                    }
                }

                QrType.WIFI -> {
                    val wifi = QrContentParser.parseWifi(result.content)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (wifi != null) Text("Rede: ${wifi.ssid}", fontWeight = FontWeight.SemiBold)
                        Button(onClick = {
                            // A partir do Android 10, redes não podem mais ser adicionadas
                            // programaticamente sem confirmação do usuário; abrimos as
                            // configurações de Wi-Fi para ele conectar com a senha copiada.
                            wifi?.let { clipboard.setText(AnnotatedString(it.password)) }
                            context.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Wifi, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Conectar (senha copiada)")
                        }
                    }
                }

                QrType.CONTACT -> {
                    val contact = QrContentParser.parseVCard(result.content)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(contact.name, fontWeight = FontWeight.SemiBold)
                        contact.phone?.let { Text(it) }
                        Button(onClick = {
                            val intent = Intent(Intent.ACTION_INSERT).apply {
                                type = android.provider.ContactsContract.Contacts.CONTENT_TYPE
                                putExtra(android.provider.ContactsContract.Intents.Insert.NAME, contact.name)
                                contact.phone?.let { putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, it) }
                                contact.email?.let { putExtra(android.provider.ContactsContract.Intents.Insert.EMAIL, it) }
                            }
                            context.startActivity(intent)
                        }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Salvar contato")
                        }
                    }
                }

                else -> Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { clipboard.setText(AnnotatedString(result.content)) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Copiar")
                    }
                    OutlinedButton(onClick = { shareText(context, result.content) }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Share, contentDescription = null); Spacer(Modifier.width(6.dp)); Text("Compartilhar")
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun shareText(context: android.content.Context, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text) }
    context.startActivity(Intent.createChooser(intent, null))
}

private fun typeLabel(type: QrType): String = when (type) {
    QrType.LINK -> "Link detectado"
    QrType.WIFI -> "Rede Wi-Fi detectada"
    QrType.WHATSAPP -> "Link do WhatsApp detectado"
    QrType.CONTACT -> "Contato detectado"
    QrType.PIX -> "Pix detectado"
    QrType.TEXT -> "Texto detectado"
}
