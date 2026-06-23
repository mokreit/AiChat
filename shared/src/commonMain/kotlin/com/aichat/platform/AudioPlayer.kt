package com.aichat.platform

import androidx.compose.runtime.Composable

interface AudioPlayer {
    fun play(uri: String, onComplete: () -> Unit)
    fun stop()
    fun isPlaying(): Boolean
    fun release()
    suspend fun getDuration(uri: String): Int?
}

@Composable
expect fun rememberAudioPlayer(): AudioPlayer
