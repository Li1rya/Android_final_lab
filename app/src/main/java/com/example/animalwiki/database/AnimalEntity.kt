package com.example.animalwiki.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animal_table")
data class AnimalEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String
)