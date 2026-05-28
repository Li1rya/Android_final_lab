package com.example.animalwiki.data.model

enum class SearchMode(val label: String) {
    ALL("全文"),
    CN_NAME("中文名"),
    LATIN_NAME("拉丁名"),
    CLASSIFICATION("分类")
}