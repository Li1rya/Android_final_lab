package com.example.animalwiki.data.model

/**
 * 搜索筛选条件
 */
data class SearchFilter(
    // 收藏夹筛选
    val favoriteFolderId: Long? = null,  // null表示不筛选，0=全部收藏，其他=指定收藏夹
    val onlyFavorites: Boolean = false,
    
    // 分类筛选（界门纲目科属种）
    val kingdom: String? = null,
    val phylum: String? = null,
    val className: String? = null,
    val order: String? = null,
    val family: String? = null,
    val genus: String? = null,
    val species: String? = null
) {
    /**
     * 判断是否有任何筛选条件激活
     */
    fun hasAnyFilter(): Boolean {
        return favoriteFolderId != null ||
                kingdom != null ||
                phylum != null ||
                className != null ||
                order != null ||
                family != null ||
                genus != null ||
                species != null
    }
    
    /**
     * 清除所有筛选条件
     */
    fun clearAll(): SearchFilter {
        return SearchFilter()
    }
}

/**
 * 分类层级选项
 */
data class ClassificationOption(
    val level: ClassificationLevel,
    val value: String,
    val count: Int = 0
)

enum class ClassificationLevel(val label: String) {
    KINGDOM("界"),
    PHYLUM("门"),
    CLASS("纲"),
    ORDER("目"),
    FAMILY("科"),
    GENUS("属"),
    SPECIES("种")
}
