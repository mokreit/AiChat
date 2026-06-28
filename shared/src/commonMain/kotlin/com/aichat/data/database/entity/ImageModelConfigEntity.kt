package com.aichat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_model_configs")
data class ImageModelConfigEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "comfyui", "siliconflow", "dall-e", "stability", "custom_api"
    @ColumnInfo(defaultValue = "") val baseUrl: String = "",
    @ColumnInfo(defaultValue = "") val apiKey: String = "",
    @ColumnInfo(defaultValue = "") val modelName: String = "",
    // ComfyUI specific
    @ColumnInfo(defaultValue = "") val serverUrl: String = "",
    @ColumnInfo(defaultValue = "") val workflowJson: String = "",
    @ColumnInfo(defaultValue = "") val positivePromptPrefix: String = "",
    @ColumnInfo(defaultValue = "") val negativePrompt: String = "",
    @ColumnInfo(defaultValue = "1") val enabled: Boolean = true,
)
