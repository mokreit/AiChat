package com.aichat.platform

import android.net.Uri
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume

actual class FilePicker actual constructor() {
    actual suspend fun pickImage(): String? {
        val launchPicker = AndroidActivityHelper.launchImagePicker
            ?: error("Image picker not initialized. Activity must set AndroidActivityHelper.launchImagePicker")

        return suspendCancellableCoroutine { continuation ->
            AndroidActivityHelper.pendingCallback = { uriStr ->
                if (uriStr != null) {
                    try {
                        val context = AndroidAppContext.context
                        val inputStream = context.contentResolver.openInputStream(Uri.parse(uriStr))
                        if (inputStream != null) {
                            val dir = File(context.filesDir, "avatars")
                            dir.mkdirs()
                            val file = File(dir, "avatar_${System.currentTimeMillis()}.png")
                            FileOutputStream(file).use { fos ->
                                inputStream.copyTo(fos)
                            }
                            inputStream.close()
                            continuation.resume(file.absolutePath)
                        } else {
                            continuation.resume(null)
                        }
                    } catch (_: Exception) {
                        continuation.resume(null)
                    }
                } else {
                    continuation.resume(null)
                }
            }
            launchPicker()
        }
    }
}

actual class FileSaver actual constructor() {
    actual suspend fun saveTextFile(defaultName: String, content: String): Boolean {
        return try {
            val context = AndroidAppContext.context
            val dir = File(context.filesDir, "exports")
            dir.mkdirs()
            val file = File(dir, defaultName)
            file.writeText(content)
            true
        } catch (_: Exception) {
            false
        }
    }
}
