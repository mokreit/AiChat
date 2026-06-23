package com.aichat

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AiChat",
        state = rememberWindowState(),
    ) {
        window.minimumSize = Dimension(400, 600)
        App()
    }
}
