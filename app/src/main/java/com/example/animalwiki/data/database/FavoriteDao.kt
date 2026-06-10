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
    @Query("SELECT * FROM favorites WHERE folderId = :folderId ORDER BY favoriteTime DESC")
    fun getFavoritesByFolder(folderId: Long): Flow<List<FavoriteEntity>>
    @Query("SELECT * FROM favorites WHERE folderId = :folderId ORDER BY favoriteTime DESC")
    suspend fun getFavoritesByFolderSync(folderId: Long): List<FavoriteEntity>
    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()
    @Query("SELECT * FROM favorites WHERE animalId = :animalId LIMIT 1")
    suspend fun getFavoriteByAnimalId(animalId: String): FavoriteEntity?

    // 获取所有收藏的动物ID
    @Query("SELECT animalId FROM favorites")
    suspend fun getAllFavoriteAnimalIds(): List<String>

    // 获取指定收藏夹中的动物ID
    @Query("SELECT animalId FROM favorites WHERE folderId = :folderId")
    suspend fun getFavoriteAnimalIdsByFolder(folderId: Long): List<String>
}
