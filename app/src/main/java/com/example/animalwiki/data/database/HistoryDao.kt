package com.example.animalwiki.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import com.example.animalwiki.data.database.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(historyEntity: HistoryEntity)

    @Query("SELECT * FROM history ORDER BY viewTime DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Delete
    suspend fun deleteHistory(historyEntity: HistoryEntity)

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()

    @Query("SELECT * FROM history WHERE animalId = :animalId LIMIT 1")
    suspend fun getHistoryByAnimalId(animalId: String): HistoryEntity?

    @Query("SELECT COUNT(*) FROM history")
    suspend fun getHistoryCount(): Int

    @Query("SELECT * FROM history ORDER BY viewTime ASC LIMIT 1")
    suspend fun getOldestHistory(): HistoryEntity?
}