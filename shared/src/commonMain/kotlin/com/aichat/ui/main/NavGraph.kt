package com.aichat.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.ChatList,
    ) {
        composable<Route.ChatList> {
            ChatListScreen()
        }
        composable<Route.Chat> { backStackEntry ->
            val route = backStackEntry.toRoute<Route.Chat>()
            ChatScreen(characterId = route.characterId)
        }
        composable<Route.Settings> {
            SettingsScreen()
        }
        composable<Route.CharacterSelect> {
            CharacterSelectScreen()
        }
    }
}
