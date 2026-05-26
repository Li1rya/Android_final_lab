package com.example.animalwiki.data.model

data class Favorite(
    val id: Int = 0,
    val animalId: String,
    val name: String,
    val category: String,
    val favoriteTime: Long = System.currentTimeMillis()
)