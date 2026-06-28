package com.aichat.platform

import java.awt.EventQueue
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class FilePicker actual constructor() {
    actual suspend fun pickImage(): String? = suspendCancellableCoroutine { cont ->
        Thread {
            try {
                var result: String? = null
                EventQueue.invokeAndWait {
                    val fileDialog = FileDialog(null as Frame?, "Select Image", FileDialog.LOAD)
                    fileDialog.setFilenameFilter { _, name ->
                        val lower = name.lowercase()
                        lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                            lower.endsWith(".gif") || lower.endsWith(".webp") || lower.endsWith(".bmp")
                    }
                    fileDialog.isVisible = true
                    val dir = fileDialog.directory
                    val file = fileDialog.file
                    if (dir != null && file != null) {
                        result = File(dir, file).absolutePath
                    }
                }
                cont.resume(result)
            } catch (_: Exception) {
                cont.resume(null)
            }
        }.start()
    }
}

actual class FileSaver actual constructor() {
    actual suspend fun saveTextFile(defaultName: String, content: String): Boolean =
        suspendCancellableCoroutine { cont ->
            Thread {
                try {
                    var result: String? = null
                    EventQueue.invokeAndWait {
                        val fileDialog = FileDialog(null as Frame?, "Save File", FileDialog.SAVE)
                        fileDialog.file = defaultName
                        fileDialog.isVisible = true
                        val dir = fileDialog.directory
                        val fileName = fileDialog.file
                        if (dir != null && fileName != null) {
                            result = dir + fileName
                        }
                    }
                    val path = result
                    if (path != null) {
                        File(path).writeText(content)
                        cont.resume(true)
                    } else {
                        cont.resume(false)
                    }
                } catch (_: Exception) {
                    cont.resume(false)
                }
            }.start()
        }
}

actual suspend fun pickJsonFile(): String? = suspendCancellableCoroutine { cont ->
    Thread {
        try {
            var result: String? = null
            EventQueue.invokeAndWait {
                val fileDialog = FileDialog(null as Frame?, "Select JSON File", FileDialog.LOAD)
                fileDialog.setFilenameFilter { _, name ->
                    name.lowercase().endsWith(".json")
                }
                fileDialog.isVisible = true
                val dir = fileDialog.directory
                val file = fileDialog.file
                if (dir != null && file != null) {
                    result = File(dir, file).readText()
                }
            }
            cont.resume(result)
        } catch (_: Exception) {
            cont.resume(null)
        }
    }.start()
}
