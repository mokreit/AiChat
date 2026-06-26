package com.aichat.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
        containerColor = Color.White,
        topBar = {
            Surface(color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E7EB))) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()).padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color(0xFF374151)) }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(s.about, style = AiChatTypography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = Color(0xFF111827))
                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.size(40.dp))
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                    color = Color(0xFF000000),
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = s.appName,
                style = AiChatTypography.titleLarge,
                color = Color(0xFF111827),
            )
            Text(
                text = "${s.version} 1.0.0",
                style = AiChatTypography.bodySmall,
                color = Color(0xFF9CA3AF),
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info card
            Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp, border = BorderStroke(1.dp, Color(0xFFE5E7EB)), modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingItem(title = s.appName, subtitle = "AiChat", onClick = {})
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    SettingItem(title = s.version, subtitle = "1.0.0", onClick = {})
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    SettingItem(title = s.techStack, subtitle = s.builtWith, onClick = {})
                }
            }
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
