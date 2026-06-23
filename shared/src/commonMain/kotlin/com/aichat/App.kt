package com.aichat

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.aichat.design.AppTheme
import com.aichat.di.appModule
import com.aichat.ui.main.AppNavHost
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        AppTheme {
            val navController = rememberNavController()
            AppNavHost(navController = navController)
        }
    }
}
