package com.example.animalwiki.data.model

data class History (
    val id: Int = 0,
    val animalId: String,
    val name: String,
    val category: String,
    val viewTime: Long = System.currentTimeMillis()
)