package com.example.animalwiki.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animalwiki.data.database.entity.AnimalEntity
import kotlinx.coroutines.flow.Flow

// Room DAO（@Dao）
@Dao
interface AnimalDao {
    // 插入单个动物（冲突时替换）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity)

    // 插入多个动物
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnimals(animals: List<AnimalEntity>)

    // 根据ID查询
    @Query("SELECT * FROM animals WHERE id = :animalId")
    suspend fun getAnimalById(animalId: Int): AnimalEntity?

    // 查询所有
    @Query("SELECT * FROM animals")
    suspend fun getAllAnimals(): List<AnimalEntity>

    // 根据ID删除
    @Query("DELETE FROM animals WHERE id = :animalId")
    suspend fun deleteAnimalById(animalId: Int): Int

    // 删除所有
    @Query("DELETE FROM animals")
    suspend fun deleteAllAnimals()
}