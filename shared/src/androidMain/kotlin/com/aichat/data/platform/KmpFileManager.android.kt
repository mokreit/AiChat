package com.aichat.data.platform

import android.content.Context
import okio.Path
import okio.Path.Companion.toPath
import java.io.File

actual class KmpFileManager actual constructor() {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
    }

    actual fun getCacheDir(): Path {
        val dir = context?.cacheDir ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp")
        return dir.absolutePath.toPath()
    }

    actual fun getFilesDir(): Path {
        val dir = context?.filesDir ?: File(System.getProperty("user.home") ?: "/tmp", ".aichat")
        return dir.absolutePath.toPath()
    }

    actual fun writeFile(path: Path, data: ByteArray) {
        val file = File(path.toString())
        file.parentFile?.mkdirs()
        file.writeBytes(data)
    }

    actual fun readFile(path: Path): ByteArray? {
        val file = File(path.toString())
        return if (file.exists()) file.readBytes() else null
    }

    actual fun deleteFile(path: Path): Boolean {
        return File(path.toString()).delete()
    }

    actual fun exists(path: Path): Boolean {
        return File(path.toString()).exists()
    }

    actual fun createTempFile(prefix: String, suffix: String): Path {
        val dir = context?.cacheDir ?: File(System.getProperty("java.io.tmpdir") ?: "/tmp")
        val file = File.createTempFile(prefix, suffix, dir)
        return file.absolutePath.toPath()
    }
}
