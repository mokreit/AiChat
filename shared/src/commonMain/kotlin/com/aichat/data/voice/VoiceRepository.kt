package com.aichat.data.voice

import com.aichat.data.api.ApiResult

class VoiceRepository(
    private val ttsProviderRegistry: TtsProviderRegistry,
    private val audioPlayer: AudioPlayer,
) {

    suspend fun synthesize(
        config: TtsConfig,
        text: String,
        voiceId: String = config.voiceId,
        model: String = config.model,
    ): ApiResult<TtsResult> {
        val provider = ttsProviderRegistry.find(config.providerId)
            ?: return ApiResult.UnexpectedError("Unknown TTS provider: ${config.providerId}")
        val request = TtsRequest(
            text = text,
            voiceId = voiceId,
            model = model,
        )
        return provider.synthesize(config, request)
    }

    suspend fun synthesizeAndPlay(
        config: TtsConfig,
        text: String,
        voiceId: String = config.voiceId,
        model: String = config.model,
    ): ApiResult<Unit> {
        val result = synthesize(config, text, voiceId, model)
        return when (result) {
            is ApiResult.Success -> {
                audioPlayer.play(result.value.audioData, result.value.format)
                ApiResult.Success(Unit)
            }
            is ApiResult.HttpError -> result
            is ApiResult.NetworkError -> result
            is ApiResult.UnexpectedError -> result
        }
    }

    suspend fun getAvailableVoices(config: TtsConfig): ApiResult<List<String>> {
        val provider = ttsProviderRegistry.find(config.providerId)
            ?: return ApiResult.UnexpectedError("Unknown TTS provider: ${config.providerId}")
        return provider.getAvailableVoices(config)
    }

    fun getPlayer(): AudioPlayer = audioPlayer
}
