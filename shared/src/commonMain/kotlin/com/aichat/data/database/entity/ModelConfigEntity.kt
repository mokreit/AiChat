package com.aichat.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "model_configs")
data class ModelConfigEntity(
    @PrimaryKey val id: String,
    val provider: String,
    val name: String,
    val baseUrl: String,
    val apiKey: String,
    val modelName: String,
    val contextWindow: Int = 128_000,
    val maxOutputTokens: Int = 4_096,
    val supportVision: Boolean = false,
    val supportToolCall: Boolean = false,
    val supportReasoning: Boolean = false,
    val temperature: Double = 0.7,
    val topP: Double = 1.0,
    val enabled: Boolean = true,
)
