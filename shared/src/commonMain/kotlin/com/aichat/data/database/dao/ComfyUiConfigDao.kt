package com.aichat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aichat.data.database.entity.ComfyUiConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComfyUiConfigDao {
    @Query("SELECT * FROM comfyui_config WHERE id = :id")
    fun getConfig(id: String = "default"): Flow<ComfyUiConfigEntity?>

    @Query("SELECT * FROM comfyui_config WHERE id = :id")
    suspend fun getConfigOnce(id: String = "default"): ComfyUiConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: ComfyUiConfigEntity)
}
