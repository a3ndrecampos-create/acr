package com.qrscangera.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.qrscangera.app.R
import com.qrscangera.app.ui.components.CameraScannerView
import com.qrscangera.app.ui.components.ResultBottomSheet
import com.qrscangera.app.ui.components.ScannerFrameOverlay
import com.qrscangera.app.utils.AdsManager
import com.qrscangera.app.utils.VibrationHelper
import com.qrscangera.app.viewmodel.ScanViewModel

@Composable
fun ScanScreen(viewModel: ScanViewModel) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var showRationale by remember { mutableStateOf(!hasCameraPermission) }
    var showDeniedMessage by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
        showDeniedMessage = !granted
    }

    var torchOn by remember { mutableStateOf(false) }
    var justDetected by remember { mutableStateOf(false) }
    val result by viewModel.lastResult.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            try {
                val image = InputImage.fromFilePath(context, uri)
                BarcodeScanning.getClient().process(image).addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.let { viewModel.onQrDetected(it) }
                }
            } catch (e: Exception) { /* imagem inválida - ignora */ }
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            CameraScannerView(
                modifier = Modifier.fillMaxSize(),
                torchOn = torchOn,
                onQrDetected = { raw ->
                    justDetected = true
                    VibrationHelper.vibrateShort(context)
                    viewModel.onQrDetected(raw)
                    if (activity != null) AdsManager.onScanCompleted(activity)
                }
            )
            ScannerFrameOverlay(detected = justDetected)
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column {
                    Text(
                        if (showDeniedMessage) stringRes(R.string.camera_permission_denied) else stringRes(R.string.scan_instructions),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Instrução no topo
        Text(
            text = stringRes(R.string.scan_instructions),
            color = androidx.compose.ui.graphics.Color.White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )

        // Botão de lanterna - canto superior direito, como pedido no briefing
        if (hasCameraPermission) {
            FloatingActionButton(
                onClick = { torchOn = !torchOn },
                modifier = Modifier.align(Alignment.TopEnd).padding(20.dp).size(48.dp)
            ) {
                Icon(if (torchOn) Icons.Default.FlashOn else Icons.Default.FlashOff, contentDescription = "Lanterna")
            }
        }

        // Botão para importar QR Code de uma imagem da galeria
        FloatingActionButton(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.align(Alignment.BottomCenter).padding(28.dp)
        ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = stringRes(R.string.action_import_gallery))
        }
    }

    // Diálogo de explicação amigável ANTES do prompt do sistema, como pedido no briefing
    if (showRationale && !hasCameraPermission) {
        AlertDialog(
            onDismissRequest = { showRationale = false },
            title = { Text(stringRes(R.string.camera_permission_title)) },
            text = { Text(stringRes(R.string.camera_permission_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showRationale = false
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }) { Text(stringRes(R.string.camera_permission_allow)) }
            },
            dismissButton = { TextButton(onClick = { showRationale = false }) { Text("Agora não") } }
        )
    }

    result?.let { r ->
        ResultBottomSheet(result = r, onDismiss = {
            justDetected = false
            viewModel.dismissResult()
        })
    }
}

@Composable
private fun stringRes(id: Int): String = androidx.compose.ui.res.stringResource(id)
