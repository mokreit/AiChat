package com.aichat.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class WasmJsAudioPlayer : AudioPlayer {
    private var isCurrentlyPlaying = false

    override fun play(uri: String, onComplete: () -> Unit) {
        // Phase 2 会使用 Web Audio API 实现
        isCurrentlyPlaying = true
        onComplete()
        isCurrentlyPlaying = false
    }

    override fun stop() {
        isCurrentlyPlaying = false
    }

    override fun isPlaying(): Boolean = isCurrentlyPlaying

    override fun release() {
        stop()
    }

    override suspend fun getDuration(uri: String): Int? = null
}

@Composable
actual fun rememberAudioPlayer(): AudioPlayer {
    return remember { WasmJsAudioPlayer() }
}
