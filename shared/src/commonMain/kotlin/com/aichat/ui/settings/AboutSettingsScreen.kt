package com.aichat.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aichat.design.AiChatTypography
import com.aichat.design.SettingItem
import com.aichat.design.strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutSettingsScreen(
    onBack: () -> Unit,
) {
    val s = strings()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(s.about) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back)
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Logo section
            Spacer(modifier = Modifier.height(32.dp))
            Box(
                modifier = Modifier
                    .size(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "AI",
                    style = AiChatTypography.headlineLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = s.appName,
                style = AiChatTypography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "${s.version} 1.0.0",
                style = AiChatTypography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info section
            SectionHeader(s.about)
            SettingItem(
                title = s.appName,
                subtitle = "AiChat",
                onClick = {},
            )
            SettingItem(
                title = s.version,
                subtitle = "1.0.0",
                onClick = {},
            )
            SettingItem(
                title = s.techStack,
                subtitle = s.builtWith,
                onClick = {},
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = AiChatTypography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}
