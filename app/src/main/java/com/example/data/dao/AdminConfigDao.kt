package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AdminConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminConfigDao {
    @Query("SELECT * FROM admin_configs WHERE keyName = :key LIMIT 1")
    suspend fun getConfigByKey(key: String): AdminConfigEntity?

    @Query("SELECT * FROM admin_configs WHERE keyName = :key LIMIT 1")
    fun getConfigByKeyFlow(key: String): Flow<AdminConfigEntity?>

    @Query("SELECT * FROM admin_configs")
    fun getAllConfigsFlow(): Flow<List<AdminConfigEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateConfig(config: AdminConfigEntity)
}
