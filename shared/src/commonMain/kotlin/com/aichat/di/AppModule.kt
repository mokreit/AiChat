package com.aichat.di

import com.aichat.data.ai.AiProviderRegistry
import com.aichat.data.ai.AiRepository
import com.aichat.data.ai.AliCompatibleProvider
import com.aichat.data.ai.ClaudeProvider
import com.aichat.data.ai.OpenAiCompatibleProvider
import com.aichat.data.ai.XiaomiMimoProvider
import com.aichat.data.ai.tool.AiToolRegistry
import com.aichat.data.ai.tool.ToolCallCoordinator
import com.aichat.data.ai.tool.VoiceSynthesisTool
import com.aichat.data.api.createPlatformHttpClient
import com.aichat.data.character.CharacterRepository
import com.aichat.data.chat.ChatSessionRepository
import com.aichat.data.database.AppDatabase
import com.aichat.data.database.getRoomDatabase
import com.aichat.data.model.ModelConfigRepository
import com.aichat.data.platform.KmpFileManager
import com.aichat.data.settings.SettingsRepository
import com.aichat.data.settings.createDataStore
import com.aichat.data.story.StoryRepository
import com.aichat.data.voice.AliyunTtsProvider
import com.aichat.data.voice.AudioPlayer
import com.aichat.data.voice.EdgeTtsProvider
import com.aichat.data.voice.OpenAiCompatTtsProvider
import com.aichat.data.voice.TtsProviderRegistry
import com.aichat.data.voice.VoiceRepository
import com.aichat.data.voice.createPlatformAudioPlayer
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    // Database
    single {
        getRoomDatabase().build()
    }
    single { get<AppDatabase>().characterDao() }
    single { get<AppDatabase>().chatSessionDao() }
    single { get<AppDatabase>().messageDao() }
    single { get<AppDatabase>().storyDao() }
    single { get<AppDatabase>().modelConfigDao() }

    // Repositories
    single { CharacterRepository(get()) }
    single { ChatSessionRepository(get(), get()) }
    single { StoryRepository(get()) }
    single { ModelConfigRepository(get()) }
    single { SettingsRepository(createDataStore()) }

    // AI Engine
    single { createPlatformHttpClient() }
    single { OpenAiCompatibleProvider(get()) }
    single { AliCompatibleProvider(get()) }
    single { XiaomiMimoProvider(get()) }
    single { ClaudeProvider(get()) }
    single {
        AiProviderRegistry(
            providers = listOf(
                get<OpenAiCompatibleProvider>(),
                get<AliCompatibleProvider>(),
                get<XiaomiMimoProvider>(),
                get<ClaudeProvider>(),
            ),
            aliases = mapOf(
                "custom" to OpenAiCompatibleProvider.Id,
                "openai" to OpenAiCompatibleProvider.Id,
                "gemini" to OpenAiCompatibleProvider.Id,
                "deepseek" to OpenAiCompatibleProvider.Id,
                "doubao" to OpenAiCompatibleProvider.Id,
                "moonshot" to OpenAiCompatibleProvider.Id,
                "glm" to OpenAiCompatibleProvider.Id,
                "custom-text" to OpenAiCompatibleProvider.Id,
                "qwen" to AliCompatibleProvider.Id,
            ),
        )
    }
    single { AiRepository(get(), get()) }

    // AI Tool System
    single { VoiceSynthesisTool(get(), get()) }
    single {
        AiToolRegistry(
            tools = listOf(get<VoiceSynthesisTool>()),
        )
    }
    single { ToolCallCoordinator(get(), get()) }

    // Platform
    single { KmpFileManager() }

    // Voice / TTS
    single<AudioPlayer> { createPlatformAudioPlayer() }
    single { OpenAiCompatTtsProvider(get()) }
    single { EdgeTtsProvider(get()) }
    single { AliyunTtsProvider(get()) }
    single {
        TtsProviderRegistry(
            providers = listOf(
                get<OpenAiCompatTtsProvider>(),
                get<EdgeTtsProvider>(),
                get<AliyunTtsProvider>(),
            ),
        )
    }
    single { VoiceRepository(get(), get()) }
}
