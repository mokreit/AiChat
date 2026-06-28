package com.aichat.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val DEFAULT_MODEL_CONFIG_ID = stringPreferencesKey("default_model_config_id")
        val DEFAULT_TTS_PROVIDER_ID = stringPreferencesKey("default_tts_provider_id")
        val AUTO_PLAY_VOICE = booleanPreferencesKey("auto_play_voice")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val API_HOST = stringPreferencesKey("api_host")
        val API_KEY = stringPreferencesKey("api_key")
        val VOICE_API_HOST = stringPreferencesKey("voice_api_host")
        val VOICE_API_KEY = stringPreferencesKey("voice_api_key")
        val VOICE_MODEL = stringPreferencesKey("voice_model")
        val LANGUAGE = stringPreferencesKey("language")
        val NICKNAME = stringPreferencesKey("nickname")
        val AVATAR = stringPreferencesKey("avatar")
        val CONFIG_PRESETS = stringSetPreferencesKey("config_presets")
        val AUTO_GENERATE_IMAGE = booleanPreferencesKey("auto_generate_image")
    }

    val defaultModelConfigId: Flow<String?> = dataStore.data.map { it[Keys.DEFAULT_MODEL_CONFIG_ID] }
    val defaultTtsProviderId: Flow<String?> = dataStore.data.map { it[Keys.DEFAULT_TTS_PROVIDER_ID] }
    val autoPlayVoice: Flow<Boolean> = dataStore.data.map { it[Keys.AUTO_PLAY_VOICE] ?: true }
    val darkTheme: Flow<Boolean> = dataStore.data.map { it[Keys.DARK_THEME] ?: true }
    val apiHost: Flow<String?> = dataStore.data.map { it[Keys.API_HOST] }
    val apiKey: Flow<String?> = dataStore.data.map { it[Keys.API_KEY] }
    val voiceApiHost: Flow<String?> = dataStore.data.map { it[Keys.VOICE_API_HOST] }
    val voiceApiKey: Flow<String?> = dataStore.data.map { it[Keys.VOICE_API_KEY] }
    val voiceModel: Flow<String?> = dataStore.data.map { it[Keys.VOICE_MODEL] }
    val language: Flow<String?> = dataStore.data.map { it[Keys.LANGUAGE] }
    val nickname: Flow<String?> = dataStore.data.map { it[Keys.NICKNAME] }
    val avatar: Flow<String?> = dataStore.data.map { it[Keys.AVATAR] }
    val themeMode: Flow<String> = dataStore.data.map { it[Keys.THEME_MODE] ?: "system" }
    val configPresets: Flow<Set<String>> = dataStore.data.map { it[Keys.CONFIG_PRESETS] ?: emptySet() }
    val autoGenerateImage: Flow<Boolean> = dataStore.data.map { it[Keys.AUTO_GENERATE_IMAGE] ?: false }

    suspend fun setDefaultModelConfigId(id: String) {
        dataStore.edit { it[Keys.DEFAULT_MODEL_CONFIG_ID] = id }
    }

    suspend fun setDefaultTtsProviderId(id: String) {
        dataStore.edit { it[Keys.DEFAULT_TTS_PROVIDER_ID] = id }
    }

    suspend fun setAutoPlayVoice(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_PLAY_VOICE] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setApiHost(host: String) {
        dataStore.edit { it[Keys.API_HOST] = host }
    }

    suspend fun setApiKey(key: String) {
        dataStore.edit { it[Keys.API_KEY] = key }
    }

    suspend fun setVoiceApiHost(host: String) {
        dataStore.edit { it[Keys.VOICE_API_HOST] = host }
    }

    suspend fun setVoiceApiKey(key: String) {
        dataStore.edit { it[Keys.VOICE_API_KEY] = key }
    }

    suspend fun setVoiceModel(model: String) {
        dataStore.edit { it[Keys.VOICE_MODEL] = model }
    }

    suspend fun setLanguage(lang: String) {
        dataStore.edit { it[Keys.LANGUAGE] = lang }
    }

    suspend fun setNickname(name: String) {
        dataStore.edit { it[Keys.NICKNAME] = name }
    }

    suspend fun setAvatar(uri: String) {
        dataStore.edit { it[Keys.AVATAR] = uri }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun saveConfigPreset(presetJson: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.CONFIG_PRESETS] ?: emptySet()
            prefs[Keys.CONFIG_PRESETS] = current + presetJson
        }
    }

    suspend fun deleteConfigPreset(presetJson: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.CONFIG_PRESETS] ?: emptySet()
            prefs[Keys.CONFIG_PRESETS] = current - presetJson
        }
    }

    suspend fun setAutoGenerateImage(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_GENERATE_IMAGE] = enabled }
    }
}
