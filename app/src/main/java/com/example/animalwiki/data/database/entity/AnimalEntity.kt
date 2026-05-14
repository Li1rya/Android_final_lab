package com.example.animalwiki.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "animals")
data class AnimalEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val description: String,
    val category: String
)