package com.example.animalwiki.data.model

import com.example.animalwiki.data.database.entity.AnimalEntity

data class Animal(
    val id: Int,
    val name: String,
    val description: String,
    val category: String
)

fun ApiAnimal.toAnimalEntity(): AnimalEntity {
    return AnimalEntity(
        id = this.id,
        name = this.title,
        description = this.body,
        category = "哺乳类"
    )
}

fun AnimalEntity.toAnimal(): Animal {
    return Animal(
        id = this.id,
        name = this.name,
        description = this.description,
        category = this.category
    )
}