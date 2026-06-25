package com.aichat.data.platform

import okio.Path
import okio.Path.Companion.toPath
import java.io.File

actual class KmpFileManager actual constructor() {
    private val cacheDir = System.getProperty("java.io.tmpdir")?.toPath() ?: "/tmp".toPath()
    private val filesDir = System.getProperty("user.home")?.toPath()?.resolve(".aichat") ?: cacheDir

    actual fun getCacheDir(): Path = cacheDir
    actual fun getFilesDir(): Path = filesDir

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
        val file = File.createTempFile(prefix, suffix)
        file.deleteOnExit()
        return file.absolutePath.toPath()
    }
}
