package com.example.animalwiki.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AnimalDao {
    @Query("SELECT * FROM animal_table")
    suspend fun getAllAnimals(): List<AnimalEntity>

    @Query("SELECT * FROM animal_table WHERE id = :id")
    suspend fun getAnimalById(id: Int): AnimalEntity?

    @Insert
    suspend fun insertAnimals(animals: List<AnimalEntity>)

    @Query("DELETE FROM animal_table")
    suspend fun clearAll()
}