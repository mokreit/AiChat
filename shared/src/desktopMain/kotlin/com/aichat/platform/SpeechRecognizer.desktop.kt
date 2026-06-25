package com.aichat.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class DesktopAudioRecorderManager : AudioRecorderManager {
    override fun startRecording(onError: (String) -> Unit) {
        onError("Desktop not supported")
    }
    override fun stopRecording(cancel: Boolean): RecordResult? = null
    override fun isRecording(): Boolean = false
    override fun release() {}
}

@Composable
actual fun rememberAudioRecorder(): AudioRecorderManager {
    return remember { DesktopAudioRecorderManager() }
}
