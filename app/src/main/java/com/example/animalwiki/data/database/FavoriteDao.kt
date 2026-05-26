package com.example.animalwiki.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.OnConflictStrategy
import kotlinx.coroutines.flow.Flow
import com.example.animalwiki.data.database.entity.FavoriteEntity

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites ORDER BY favoriteTime DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()

    @Query("SELECT * FROM favorites WHERE animalId = :animalId LIMIT 1")
    suspend fun getFavoriteByAnimalId(animalId: String): FavoriteEntity?
}