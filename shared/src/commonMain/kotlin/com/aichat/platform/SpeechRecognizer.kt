package com.aichat.platform

import androidx.compose.runtime.Composable

data class RecordResult(
    val filePath: String,
    val durationMs: Long,
)

interface AudioRecorderManager {
    fun startRecording(onError: (String) -> Unit)
    fun stopRecording(cancel: Boolean): RecordResult?
    fun isRecording(): Boolean
    fun release()
}

@Composable
expect fun rememberAudioRecorder(): AudioRecorderManager
