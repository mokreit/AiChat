package com.aichat.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.aichat.data.database.initDatabase
import com.aichat.data.settings.initSettings
import com.aichat.platform.AndroidAppContext
import com.aichat.platform.AndroidActivityHelper

class MainActivity : ComponentActivity() {
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize centralized Android context for all platform implementations
        AndroidAppContext.init(applicationContext)
        initDatabase(applicationContext)
        initSettings(applicationContext)

        // Register image picker launcher
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            if (uri != null) {
                try {
                    contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) {}
                AndroidActivityHelper.pendingCallback?.invoke(uri.toString())
            } else {
                AndroidActivityHelper.pendingCallback?.invoke(null)
            }
            AndroidActivityHelper.pendingCallback = null
        }

        // Set the launcher lambda for shared module to use
        AndroidActivityHelper.launchImagePicker = { imagePickerLauncher.launch("image/*") }

        enableEdgeToEdge()
        setContent {
            com.aichat.App()
        }
    }
}
