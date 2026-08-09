package com.qrscangera.app.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/** Salva o QR Code gerado na galeria e monta o Intent de compartilhamento. */
object ImageSaver {

    fun saveToGallery(context: Context, bitmap: Bitmap, displayName: String = "qrscangera_${System.currentTimeMillis()}"): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/QRScanGera")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let { writeBitmap(context.contentResolver.openOutputStream(it), bitmap) }
            uri
        } else {
            @Suppress("DEPRECATION")
            val dir = File(android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_PICTURES), "QRScanGera")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "$displayName.png")
            writeBitmap(FileOutputStream(file), bitmap)
            Uri.fromFile(file)
        }
    }

    private fun writeBitmap(out: OutputStream?, bitmap: Bitmap) {
        out?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    /** Salva num arquivo temporário em cache e devolve um Intent de compartilhamento pronto. */
    fun shareBitmap(context: Context, bitmap: Bitmap): Intent {
        val cacheDir = File(context.cacheDir, "shared_qr").apply { mkdirs() }
        val file = File(cacheDir, "qr_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
