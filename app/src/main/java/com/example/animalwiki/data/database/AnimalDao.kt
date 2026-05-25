package com.example.animalwiki.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.animalwiki.data.database.entity.AnimalEntity

@Dao
interface AnimalDao {

    @Query("SELECT * FROM animals WHERE cnname LIKE '%' || :keyword || '%' LIMIT 1")
    suspend fun getAnimalByName(keyword: String): AnimalEntity?

    @Query("SELECT * FROM animals WHERE id = :id")
    suspend fun getAnimalById(id: String): AnimalEntity?

    @Query("SELECT * FROM animals ORDER BY cnname ASC")  // 按中文名排序
    suspend fun getAllAnimals(): List<AnimalEntity>

    @Query("""
        SELECT * FROM animals 
        WHERE cnname LIKE '%' || :keyword || '%' 
        OR latinName LIKE '%' || :keyword || '%'
        OR appearance LIKE '%' || :keyword || '%'
        OR habitat LIKE '%' || :keyword || '%'
        OR diet LIKE '%' || :keyword || '%'
    """)
    suspend fun searchAnimals(keyword: String): List<AnimalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animals: List<AnimalEntity>)

    @Query("DELETE FROM animals WHERE id = :id")
    suspend fun deleteAnimal(id: String)

    @Query("DELETE FROM animals")
    suspend fun deleteAllAnimals()

    @Query("SELECT COUNT(*) FROM animals")
    suspend fun getCount(): Int
}