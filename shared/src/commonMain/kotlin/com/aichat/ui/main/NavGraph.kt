package com.aichat.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.aichat.ui.character.CharacterDetailScreen
import com.aichat.ui.character.CharacterEditScreen
import com.aichat.ui.character.CharacterListScreen
import com.aichat.ui.chat.ChatScreen
import com.aichat.ui.chat.SessionListScreen
import com.aichat.ui.settings.AboutSettingsScreen
import com.aichat.ui.settings.AppearanceSettingsScreen
import com.aichat.ui.settings.ModelSettingsScreen
import com.aichat.ui.settings.SettingsScreen
import com.aichat.ui.settings.TextModelSettingsScreen
import com.aichat.ui.settings.VoiceModelSettingsScreen
import com.aichat.ui.settings.VoiceSettingsScreen
import com.aichat.ui.story.StoryDetailScreen
import com.aichat.ui.story.StoryEditScreen
import com.aichat.ui.story.StoryListScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.ChatList,
    ) {
        composable<Route.ChatList> {
            CharacterListScreen(
                onCharacterClick = { id -> navController.navigate(Route.CharacterDetail(id)) },
                onStoriesClick = { navController.navigate(Route.StoryList) },
                onSettingsClick = { navController.navigate(Route.Settings) },
                onAddCharacterClick = { navController.navigate(Route.CharacterEdit()) },
                onSessionsClick = { navController.navigate(Route.SessionList) },
            )
        }
        composable<Route.SessionList> {
            SessionListScreen(
                onSessionClick = { sessionId, characterId ->
                    navController.navigate(Route.Chat(characterId = characterId, sessionId = sessionId))
                },
            )
        }
        composable<Route.CharacterDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.CharacterDetail>()
            CharacterDetailScreen(
                characterId = route.characterId,
                onBack = { navController.popBackStack() },
                onStartChat = { id -> navController.navigate(Route.Chat(id)) },
                onEdit = { id -> navController.navigate(Route.CharacterEdit(id)) },
            )
        }
        composable<Route.CharacterEdit> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.CharacterEdit>()
            CharacterEditScreen(
                characterId = route.characterId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Chat> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Chat>()
            ChatScreen(
                sessionId = route.sessionId,
                characterId = route.characterId,
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.Settings> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onModelSettingsClick = { navController.navigate(Route.ModelSettings) },
                onAppearanceSettingsClick = { navController.navigate(Route.AppearanceSettings) },
                onAboutClick = { navController.navigate(Route.AboutSettings) },
            )
        }
        composable<Route.ModelSettings> {
            ModelSettingsScreen(
                onBack = { navController.popBackStack() },
                onTextModelClick = { navController.navigate(Route.TextModelSettings) },
                onVoiceModelClick = { navController.navigate(Route.VoiceModelSettings) },
            )
        }
        composable<Route.TextModelSettings> {
            TextModelSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.VoiceModelSettings> {
            VoiceModelSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.AppearanceSettings> {
            AppearanceSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.VoiceSettings> {
            VoiceSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.AboutSettings> {
            AboutSettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }
        composable<Route.StoryList> {
            StoryListScreen(
                onBack = { navController.popBackStack() },
                onStoryClick = { id -> navController.navigate(Route.StoryDetail(id)) },
                onAddStoryClick = { navController.navigate(Route.StoryEdit()) },
            )
        }
        composable<Route.StoryDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.StoryDetail>()
            StoryDetailScreen(
                storyId = route.storyId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Route.StoryEdit(id)) },
            )
        }
        composable<Route.StoryEdit> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.StoryEdit>()
            StoryEditScreen(
                storyId = route.storyId,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
