package com.aichat.platform

/** Platform-specific file picker for selecting images */
expect class FilePicker() {
    suspend fun pickImage(): String?
}

/** Platform-specific file saver for exporting content */
expect class FileSaver() {
    suspend fun saveTextFile(defaultName: String, content: String): Boolean
}

/** Platform-specific JSON file picker, returns file content or null */
expect suspend fun pickJsonFile(): String?
