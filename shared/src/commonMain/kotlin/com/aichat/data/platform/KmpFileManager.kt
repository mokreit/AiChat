package com.aichat.data.platform

import okio.Path

expect class KmpFileManager() {
    fun getCacheDir(): Path
    fun getFilesDir(): Path
    fun writeFile(path: Path, data: ByteArray)
    fun readFile(path: Path): ByteArray?
    fun deleteFile(path: Path): Boolean
    fun exists(path: Path): Boolean
    fun createTempFile(prefix: String, suffix: String): Path
}
