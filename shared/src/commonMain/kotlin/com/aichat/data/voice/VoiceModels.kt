package com.aichat.data.voice

import com.aichat.data.api.ApiResult
import kotlinx.coroutines.flow.Flow

data class TtsConfig(
    val providerId: String,
    val apiHost: String = "",
    val apiKey: String = "",
    val model: String = "tts-1",
    val voiceId: String = "alloy",
)

data class TtsRequest(
    val text: String,
    val voiceId: String = "alloy",
    val model: String = "tts-1",
    val responseFormat: TtsAudioFormat = TtsAudioFormat.Mp3,
    val speed: Float = 1.0f,
)

enum class TtsAudioFormat(val value: String) {
    Mp3("mp3"),
    Opus("opus"),
    Aac("aac"),
    Flac("flac"),
    Pcm("pcm"),
    Wav("wav"),
}

data class TtsResult(
    val audioData: ByteArray,
    val format: TtsAudioFormat = TtsAudioFormat.Mp3,
) {
    override fun equals(other: Any?): Boolean = this === other
    override fun hashCode(): Int = System.identityHashCode(this)
}

interface TtsProvider {
    val id: String
    val displayName: String

    suspend fun synthesize(config: TtsConfig, request: TtsRequest): ApiResult<TtsResult>
    suspend fun getAvailableVoices(config: TtsConfig): ApiResult<List<String>>
}

interface AudioPlayer {
    val isPlaying: Boolean
    val currentPlayback: Flow<PlaybackState>

    suspend fun play(audioData: ByteArray, format: TtsAudioFormat = TtsAudioFormat.Mp3)
    suspend fun playFromUrl(url: String)
    suspend fun stop()
    suspend fun pause()
    suspend fun resume()
    fun release()
}

sealed interface PlaybackState {
    data object Idle : PlaybackState
    data class Playing(val progress: Float = 0f) : PlaybackState
    data object Paused : PlaybackState
    data object Completed : PlaybackState
    data class Error(val message: String) : PlaybackState
}

class TtsProviderRegistry(
    providers: List<TtsProvider>,
) {
    private val providersById = providers.associateBy(TtsProvider::id)

    fun find(providerId: String): TtsProvider? = providersById[providerId]

    fun allProviders(): List<TtsProvider> = providersById.values.toList()
}
