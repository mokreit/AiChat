package com.aichat.platform

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage
import org.jetbrains.skia.EncodedImageFormat

actual fun loadImageFromFile(path: String): ImageBitmap? {
    return try {
        val file = java.io.File(path)
        if (!file.exists()) return null
        val bytes = file.readBytes()
        SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}

actual fun loadImageFromBytes(bytes: ByteArray): ImageBitmap? {
    return try {
        SkiaImage.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}

actual fun saveImageBitmap(bitmap: ImageBitmap, filename: String): String? {
    return try {
        val dir = java.io.File(System.getProperty("user.home"), ".aichat/avatars")
        dir.mkdirs()
        val file = java.io.File(dir, "$filename.png")
        val skiaImage = SkiaImage.makeFromBitmap(bitmap.asSkiaBitmap())
        val data = skiaImage.encodeToData(EncodedImageFormat.PNG, 100)
        if (data != null) {
            file.writeBytes(data.bytes)
            file.absolutePath
        } else null
    } catch (_: Exception) {
        null
    }
}

actual fun cropImageBitmap(source: ImageBitmap, x: Int, y: Int, width: Int, height: Int): ImageBitmap? {
    return try {
        val srcImage = SkiaImage.makeFromBitmap(source.asSkiaBitmap())
        val imageInfo = org.jetbrains.skia.ImageInfo.makeN32Premul(width, height)
        val surface = org.jetbrains.skia.Surface.makeRaster(imageInfo)
        val canvas = surface.canvas
        canvas.drawImageRect(
            srcImage,
            org.jetbrains.skia.Rect.makeXYWH(x.toFloat(), y.toFloat(), width.toFloat(), height.toFloat()),
            org.jetbrains.skia.Rect.makeWH(width.toFloat(), height.toFloat()),
        )
        val snapshot = surface.makeImageSnapshot()
        snapshot.toComposeImageBitmap()
    } catch (_: Exception) {
        null
    }
}
