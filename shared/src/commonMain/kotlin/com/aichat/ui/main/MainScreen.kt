package com.aichat.ui.main

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.aichat.design.AiChatTheme

@Composable
fun MainScreen() {
    AiChatTheme {
        val navController = rememberNavController()
        AppNavHost(navController = navController)
    }
}
