package com.example.animalwiki.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val animalId: String,
    val name: String,
    val category: String,
    val favoriteTime: Long = System.currentTimeMillis(),
    val folderId: Long=0
)