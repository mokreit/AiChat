package com.aichat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val characterIds: String = "",
    val systemPrompt: String = "",
    val backgroundImage: String = "",
    @ColumnInfo(defaultValue = "1") val userJoined: Boolean = true,
    @ColumnInfo(defaultValue = "") val userNickname: String = "",
    @ColumnInfo(defaultValue = "") val userAvatar: String = "",
    @ColumnInfo(defaultValue = "") val userDescription: String = "",
    val createdAt: Long,
    val updatedAt: Long,
)
