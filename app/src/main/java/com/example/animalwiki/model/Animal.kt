package com.example.animalwiki.model

// 匹配 Mock API 的数据结构
data class Animal(
    val id: Int,
    val title: String,  // 动物名称
    val body: String    // 动物描述
)