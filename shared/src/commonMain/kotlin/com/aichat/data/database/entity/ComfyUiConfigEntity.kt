package com.aichat.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comfyui_config")
data class ComfyUiConfigEntity(
    @PrimaryKey val id: String = "default",
    val serverUrl: String = "",
    val workflowJson: String = "",
    val negativePrompt: String = "",
    val positivePromptPrefix: String = "",
)
