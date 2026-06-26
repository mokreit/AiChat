package com.aichat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aichat.data.database.entity.ModelConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelConfigDao {
    @Query("SELECT * FROM model_configs WHERE enabled = 1")
    fun getEnabled(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM model_configs ORDER BY name ASC")
    fun getAll(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM model_configs WHERE id = :id")
    suspend fun getById(id: String): ModelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ModelConfigEntity)

    @Update
    suspend fun update(config: ModelConfigEntity)

    @Query("DELETE FROM model_configs WHERE id = :id")
    suspend fun deleteById(id: String)
}
