package com.qrscangera.app.ui.components

import android.util.Size
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.qrscangera.app.utils.ZxingDecoder

/**
 * Preview de câmera em tela cheia (CameraX) já ligado à leitura de QR/barcode via ZXing.
 * Chama [onQrDetected] com o conteúdo bruto assim que um QR Code é lido.
 *
 * A instância de [Camera] retornada pelo bind fica guardada em [cameraRef]: é ela que
 * permite ligar/desligar a lanterna depois que a câmera já está rodando - sem isso, o
 * toggle da lanterna só valeria no instante exato do bind (quase sempre "desligado"),
 * que era o bug de "lanterna não liga".
 */
@Composable
fun CameraScannerView(
    modifier: Modifier = Modifier,
    torchOn: Boolean,
    onQrDetected: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraRef = remember { mutableStateOf<Camera?>(null) }
    // Decodificação roda numa thread própria: o ZXing decodifica de forma síncrona
    // (diferente do ML Kit, que era assíncrono por padrão), então rodar na thread
    // principal travaria o preview da câmera.
    val analysisExecutor = remember { java.util.concurrent.Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose { analysisExecutor.shutdown() }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                var handled = false
                analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                    if (!handled) {
                        val value = ZxingDecoder.decode(imageProxy)
                        if (value != null && !handled) {
                            handled = true
                            ContextCompat.getMainExecutor(ctx).execute { onQrDetected(value) }
                        }
                    }
                    imageProxy.close()
                }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                    )
                    cameraRef.value = camera
                    // Aplica o estado da lanterna já vigente no momento em que a câmera fica pronta
                    camera.cameraControl.enableTorch(torchOn)
                } catch (e: Exception) {
                    // Câmera indisponível (ex: emulador sem câmera) - preview fica em branco
                }
            }, ContextCompat.getMainExecutor(ctx))

            previewView
        },
        // Reexecutado sempre que torchOn mudar (recomposição) - é isso que faz o botão
        // de lanterna realmente ligar/desligar depois que a câmera já está rodando.
        update = {
            cameraRef.value?.cameraControl?.enableTorch(torchOn)
        }
    )
}
