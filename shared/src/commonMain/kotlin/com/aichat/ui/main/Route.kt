package com.aichat.ui.main

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object ChatList : Route

    @Serializable
    data class Chat(val characterId: String) : Route

    @Serializable
    data object Settings : Route

    @Serializable
    data object CharacterSelect : Route
}
