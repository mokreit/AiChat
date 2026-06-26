package com.aichat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.aichat.data.character.CharacterRepository
import com.aichat.data.settings.SettingsRepository
import com.aichat.data.story.StoryRepository
import com.aichat.design.LocalStrings
import com.aichat.design.ProvideStrings
import com.aichat.design.ZhStrings
import com.aichat.design.EnStrings
import com.aichat.di.appModule
import com.aichat.ui.main.MainScreen
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        val characterRepository: CharacterRepository = koinInject()
        val storyRepository: StoryRepository = koinInject()
        val settingsRepository: SettingsRepository = koinInject()

        LaunchedEffect(Unit) {
            characterRepository.ensureBuiltinCharacters()
            storyRepository.ensureBuiltinStories()
        }

        val lang by settingsRepository.language.collectAsState(initial = "zh")
        val strings = when {
            lang?.startsWith("zh") == false -> EnStrings
            else -> ZhStrings
        }

        ProvideStrings(strings) {
            MainScreen()
        }
    }
}
