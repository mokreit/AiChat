package com.aichat.platform

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

actual fun loadImageFromFile(path: String): ImageBitmap? {
    return try {
        val file = File(path)
        if (!file.exists()) return null
        val bitmap = BitmapFactory.decodeFile(path) ?: return null
        bitmap.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

actual fun loadImageFromBytes(bytes: ByteArray): ImageBitmap? {
    return try {
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
        bitmap.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

actual fun saveImageBitmap(bitmap: ImageBitmap, filename: String): String? {
    return try {
        val context = AndroidAppContext.context
        val dir = File(context.filesDir, "avatars")
        dir.mkdirs()
        val file = File(dir, "$filename.png")
        FileOutputStream(file).use { fos ->
            bitmap.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, fos)
        }
        file.absolutePath
    } catch (_: Exception) {
        null
    }
}

actual fun cropImageBitmap(source: ImageBitmap, x: Int, y: Int, width: Int, height: Int): ImageBitmap? {
    return try {
        val androidBitmap = source.asAndroidBitmap()
        Bitmap.createBitmap(androidBitmap, x, y, width, height).asImageBitmap()
    } catch (_: Exception) {
        null
    }
}
