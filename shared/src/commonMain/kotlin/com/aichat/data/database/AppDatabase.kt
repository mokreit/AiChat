package com.aichat.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.aichat.data.database.dao.CharacterDao
import com.aichat.data.database.dao.ChatSessionDao
import com.aichat.data.database.dao.MessageDao
import com.aichat.data.database.dao.ModelConfigDao
import com.aichat.data.database.dao.StoryDao
import com.aichat.data.database.dao.VoiceConfigDao
import com.aichat.data.database.entity.CharacterEntity
import com.aichat.data.database.entity.ChatSessionEntity
import com.aichat.data.database.entity.MessageEntity
import com.aichat.data.database.entity.ModelConfigEntity
import com.aichat.data.database.entity.StoryEntity
import com.aichat.data.database.entity.VoiceConfigEntity

@Database(
    entities = [
        CharacterEntity::class,
        ChatSessionEntity::class,
        MessageEntity::class,
        StoryEntity::class,
        ModelConfigEntity::class,
        VoiceConfigEntity::class,
    ],
    version = 6,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
    abstract fun chatSessionDao(): ChatSessionDao
    abstract fun messageDao(): MessageDao
    abstract fun storyDao(): StoryDao
    abstract fun modelConfigDao(): ModelConfigDao
    abstract fun voiceConfigDao(): VoiceConfigDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
