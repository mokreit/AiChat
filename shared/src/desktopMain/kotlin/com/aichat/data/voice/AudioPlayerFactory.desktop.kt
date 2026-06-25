package com.aichat.data.voice

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.DataLine

actual fun createPlatformAudioPlayer(): AudioPlayer = DesktopAudioPlayer()

private class DesktopAudioPlayer : AudioPlayer {
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    private var clip: Clip? = null
    private var tempFile: File? = null

    override val isPlaying: Boolean
        get() = clip?.isRunning == true

    override val currentPlayback: Flow<PlaybackState> = _playbackState.asStateFlow()

    override suspend fun play(audioData: ByteArray, format: TtsAudioFormat) {
        stop()
        try {
            val temp = File.createTempFile("tts_", ".${format.value}")
            temp.deleteOnExit()
            temp.writeBytes(audioData)
            tempFile = temp
            playFromUrl(temp.toURI().toString())
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Error(e.message ?: "Failed to play audio")
        }
    }

    override suspend fun playFromUrl(url: String) {
        stop()
        try {
            val file = File(java.net.URI(url))
            val audioInputStream = AudioSystem.getAudioInputStream(file)
            val format = audioInputStream.format
            val info = DataLine.Info(Clip::class.java, format)
            val newClip = AudioSystem.getClip()
            newClip.open(audioInputStream)
            newClip.addLineListener { event ->
                when (event.type) {
                    javax.sound.sampled.LineEvent.Type.START -> {
                        _playbackState.value = PlaybackState.Playing()
                    }
                    javax.sound.sampled.LineEvent.Type.STOP -> {
                        if (_playbackState.value is PlaybackState.Playing) {
                            _playbackState.value = PlaybackState.Completed
                        }
                    }
                    else -> {}
                }
            }
            clip = newClip
            _playbackState.value = PlaybackState.Playing()
            newClip.start()
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Error(e.message ?: "Failed to play audio")
        }
    }

    override suspend fun stop() {
        clip?.let {
            it.stop()
            it.close()
        }
        clip = null
        tempFile?.delete()
        tempFile = null
        _playbackState.value = PlaybackState.Idle
    }

    override suspend fun pause() {
        clip?.let {
            if (it.isRunning) {
                it.stop()
                _playbackState.value = PlaybackState.Paused
            }
        }
    }

    override suspend fun resume() {
        clip?.let {
            if (!it.isRunning && _playbackState.value is PlaybackState.Paused) {
                it.start()
                _playbackState.value = PlaybackState.Playing()
            }
        }
    }

    override fun release() {
        clip?.let {
            it.stop()
            it.close()
        }
        clip = null
        tempFile?.delete()
        tempFile = null
        _playbackState.value = PlaybackState.Idle
    }
}
