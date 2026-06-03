package com.example.animalwiki.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.animalwiki.data.database.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Query("SELECT * FROM folders ORDER BY isDefault DESC, createTime ASC")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("UPDATE favorites SET folderId = (SELECT id FROM folders WHERE isDefault = 1 LIMIT 1) WHERE folderId = :folderId")
    suspend fun moveFavoritesToDefault(folderId: Long)

    // ✅ 新增：查询是否存在默认收藏夹
    @Query("SELECT COUNT(*) FROM folders WHERE isDefault = 1")
    suspend fun countDefaultFolders(): Int

    // ✅ 新增：删除所有非默认收藏夹
    @Query("DELETE FROM folders WHERE isDefault = 0")
    suspend fun deleteAllNonDefaultFolders()

    // ✅ 新增：删除所有重复的默认收藏夹，只保留第一个
    @Query("DELETE FROM folders WHERE isDefault = 1 AND id NOT IN (SELECT MIN(id) FROM folders WHERE isDefault = 1)")
    suspend fun deleteDuplicateDefaultFolders()
}