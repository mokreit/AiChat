package com.aichat.data.database.dao

import androidx.room.*
import com.aichat.data.database.entity.ImageModelConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageModelConfigDao {
    @Query("SELECT * FROM image_model_configs WHERE enabled = 1")
    fun getEnabled(): Flow<List<ImageModelConfigEntity>>

    @Query("SELECT * FROM image_model_configs ORDER BY name ASC")
    fun getAll(): Flow<List<ImageModelConfigEntity>>

    @Query("SELECT * FROM image_model_configs WHERE id = :id")
    suspend fun getById(id: String): ImageModelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ImageModelConfigEntity)

    @Update
    suspend fun update(config: ImageModelConfigEntity)

    @Query("DELETE FROM image_model_configs WHERE id = :id")
    suspend fun deleteById(id: String)
}
