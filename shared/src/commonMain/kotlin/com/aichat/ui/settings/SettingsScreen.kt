package com.aichat.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aichat.data.settings.SettingsRepository
import com.aichat.design.AiChatTypography
import com.aichat.design.BottomNavBar
import com.aichat.design.SettingItem
import com.aichat.design.strings
import com.aichat.platform.FilePicker
import com.aichat.platform.loadImageFromFile
import com.aichat.platform.saveImageBitmap
import com.aichat.ui.common.ImageCropDialog
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onModelSettingsClick: () -> Unit,
    onAppearanceSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onMessagesClick: () -> Unit = {},
    onStoriesClick: () -> Unit = {},
    settingsRepository: SettingsRepository = koinInject(),
) {
    val s = strings()
    val scope = rememberCoroutineScope()
    val nickname by settingsRepository.nickname.collectAsState(initial = "")
    val avatarUri by settingsRepository.avatar.collectAsState(initial = "")
    val themeMode by settingsRepository.themeMode.collectAsState(initial = "system")

    var showNicknameDialog by remember { mutableStateOf(false) }
    var cropSource by remember { mutableStateOf<ImageBitmap?>(null) }

    // Crop dialog for avatar
    cropSource?.let { src ->
        ImageCropDialog(
            bitmap = src,
            aspectRatio = 1f,
            circularPreview = true,
            onConfirm = { cropped ->
                val saved = saveImageBitmap(cropped, "user_avatar_${System.currentTimeMillis()}")
                if (saved != null) {
                    scope.launch { settingsRepository.setAvatar(saved) }
                }
                cropSource = null
            },
            onDismiss = { cropSource = null },
        )
    }

    if (showNicknameDialog) {
        NicknameDialog(
            currentNickname = nickname ?: "",
            onDismiss = { showNicknameDialog = false },
            onSave = { name ->
                scope.launch { settingsRepository.setNickname(name) }
                showNicknameDialog = false
            },
        )
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            // Exact from 1.html: px-4 py-3 bg-white border-b border-violet-50
            Surface(
                color = Color.White,
            ) {
                Text(
                    text = s.settings,
                    style = AiChatTypography.titleLarge.copy(fontSize = 20.sp),
                    color = Color(0xFF111827),
                    modifier = Modifier
                        .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        },
        bottomBar = {
            BottomNavBar(
                currentTab = "settings",
                onTabClick = { tab ->
                    when (tab) {
                        "messages" -> onMessagesClick()
                        "stories" -> onStoriesClick()
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
        ) {
            // User profile card - same style as settings items
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                ProfileCard(
                    nickname = nickname ?: "",
                    avatarUri = avatarUri ?: "",
                    onNicknameClick = { showNicknameDialog = true },
                    onAvatarClick = {
                        scope.launch {
                            try {
                                val picker = FilePicker()
                                val uri = picker.pickImage()
                                if (uri != null) {
                                    val bitmap = loadImageFromFile(uri)
                                    if (bitmap != null) {
                                        cropSource = bitmap
                                    }
                                }
                            } catch (_: Exception) { }
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Settings items card
            Surface(
                shape = RoundedCornerShape(20.dp), // rounded-2xl
                color = Color.White,
                shadowElevation = 2.dp, // shadow-sm
                border = BorderStroke(1.dp, Color(0xFFE5E7EB)), // border-violet-50
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Column {
                    SettingItem(
                        title = s.modelConfig,
                        subtitle = s.configureModel,
                        onClick = onModelSettingsClick,
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp) // border-violet-50
                    SettingItem(
                        title = s.appearance,
                        subtitle = when (themeMode) {
                            "light" -> s.lightMode
                            "dark" -> s.darkMode
                            else -> s.followSystem
                        },
                        onClick = onAppearanceSettingsClick,
                    )
                    HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    SettingItem(
                        title = s.about,
                        subtitle = "v1.0.0",
                        onClick = onAboutClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    nickname: String,
    avatarUri: String,
    onNicknameClick: () -> Unit,
    onAvatarClick: () -> Unit,
) {
    val s = strings()
    var avatarBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    // Load avatar image whenever avatarUri changes
    androidx.compose.runtime.LaunchedEffect(avatarUri) {
        if (avatarUri.isNullOrBlank()) {
            avatarBitmap = null
        } else {
            try {
                avatarBitmap = com.aichat.platform.loadImageFromFile(avatarUri)
            } catch (_: Exception) {
                avatarBitmap = null
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNicknameClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF000000))
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center,
        ) {
            if (avatarBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = avatarBitmap!!,
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Text(
                    text = nickname.take(1).uppercase().ifBlank { "U" },
                    style = AiChatTypography.titleLarge,
                    color = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nickname.ifBlank { "User" },
                style = AiChatTypography.bodyLarge,
                color = Color(0xFF111827), // gray-900
            )
            Text(
                text = s.tapToEdit,
                style = AiChatTypography.bodySmall,
                color = Color(0xFF9CA3AF), // gray-400
            )
        }
        Text(
            text = "\u203A",
            style = AiChatTypography.titleLarge,
            color = Color(0xFFD1D5DB), // gray-300
        )
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

@Composable
private fun NicknameDialog(
    currentNickname: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val s = strings()
    var name by remember { mutableStateOf(currentNickname) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(s.nickname) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(s.nickname) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(name) }) { Text(s.save) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(s.cancel) }
        },
    )
}
