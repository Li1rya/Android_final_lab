package com.example.animalwiki.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val animalId: String,
    val name: String,
    val category: String,
    val viewTime: Long = System.currentTimeMillis()
)