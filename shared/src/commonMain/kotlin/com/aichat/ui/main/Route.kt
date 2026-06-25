package com.aichat.ui.main

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object ChatList : Route

    @Serializable
    data object SessionList : Route

    @Serializable
    data class Chat(val characterId: String, val sessionId: String = "") : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object ModelSettings : Route

    @Serializable
    data object TextModelSettings : Route

    @Serializable
    data object VoiceModelSettings : Route

    @Serializable
    data object AppearanceSettings : Route

    @Serializable
    data object VoiceSettings : Route

    @Serializable
    data object AboutSettings : Route

    @Serializable
    data object CharacterSelect : Route

    @Serializable
    data class CharacterEdit(val characterId: String? = null) : Route

    @Serializable
    data class CharacterDetail(val characterId: String) : Route

    @Serializable
    data class StoryEdit(val storyId: String? = null) : Route

    @Serializable
    data object StoryList : Route

    @Serializable
    data class StoryDetail(val storyId: String) : Route
}
