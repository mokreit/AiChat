package com.aichat

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.aichat.design.AppTheme
import com.aichat.di.appModule
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = {
        modules(appModule)
    }) {
        AppTheme {
            Text("AiChat KMP")
        }
    }
}
