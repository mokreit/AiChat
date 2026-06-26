package com.aichat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aichat.data.database.entity.VoiceConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceConfigDao {
    @Query("SELECT * FROM voice_configs WHERE enabled = 1")
    fun getEnabled(): Flow<List<VoiceConfigEntity>>

    @Query("SELECT * FROM voice_configs ORDER BY name ASC")
    fun getAll(): Flow<List<VoiceConfigEntity>>

    @Query("SELECT * FROM voice_configs WHERE id = :id")
    suspend fun getById(id: String): VoiceConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: VoiceConfigEntity)

    @Update
    suspend fun update(config: VoiceConfigEntity)

    @Query("DELETE FROM voice_configs WHERE id = :id")
    suspend fun deleteById(id: String)
}
