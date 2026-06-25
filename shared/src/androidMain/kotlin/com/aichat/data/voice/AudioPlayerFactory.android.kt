package com.aichat.data.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream

actual fun createPlatformAudioPlayer(): AudioPlayer = AndroidAudioPlayer()

private class AndroidAudioPlayer : AudioPlayer {
    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.Idle)
    private var mediaPlayer: MediaPlayer? = null
    private var tempFile: File? = null

    override val isPlaying: Boolean
        get() = mediaPlayer?.isPlaying == true

    override val currentPlayback: Flow<PlaybackState> = _playbackState.asStateFlow()

    override suspend fun play(audioData: ByteArray, format: TtsAudioFormat) {
        stop()
        try {
            val temp = File.createTempFile("tts_", ".${format.value}")
            temp.deleteOnExit()
            FileOutputStream(temp).use { it.write(audioData) }
            tempFile = temp
            playFromUrl(temp.toURI().toString())
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Error(e.message ?: "Failed to play audio")
        }
    }

    override suspend fun playFromUrl(url: String) {
        stop()
        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build(),
                )
                setDataSource(url)
                setOnPreparedListener {
                    it.start()
                    _playbackState.value = PlaybackState.Playing()
                }
                setOnCompletionListener {
                    _playbackState.value = PlaybackState.Completed
                    it.release()
                }
                setOnErrorListener { _, what, extra ->
                    _playbackState.value = PlaybackState.Error("MediaPlayer error: $what/$extra")
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            _playbackState.value = PlaybackState.Error(e.message ?: "Failed to play audio")
        }
    }

    override suspend fun stop() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        tempFile?.delete()
        tempFile = null
        _playbackState.value = PlaybackState.Idle
    }

    override suspend fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _playbackState.value = PlaybackState.Paused
            }
        }
    }

    override suspend fun resume() {
        mediaPlayer?.let {
            if (!it.isPlaying && _playbackState.value is PlaybackState.Paused) {
                it.start()
                _playbackState.value = PlaybackState.Playing()
            }
        }
    }

    override fun release() {
        mediaPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        mediaPlayer = null
        tempFile?.delete()
        tempFile = null
        _playbackState.value = PlaybackState.Idle
    }
}
