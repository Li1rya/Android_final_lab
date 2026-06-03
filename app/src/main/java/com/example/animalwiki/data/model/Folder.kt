package com.example.animalwiki.data.model

data class Folder(
    val id: Long = 0,
    val name: String,
    val createTime: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false // ✅ 同步添加isDefault字段
)