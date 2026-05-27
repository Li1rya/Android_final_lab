package com.example.animalwiki.data.model

import com.google.gson.annotations.SerializedName

// ==================== 原有：iNaturalist 搜索 API ====================

data class INatResponse(
    @SerializedName("total_results") val totalResults: Int?,
    @SerializedName("page") val page: Int?,
    @SerializedName("per_page") val perPage: Int?,
    @SerializedName("results") val results: List<INatTaxon>?
)

data class INatTaxon(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name: String?,                          // 拉丁学名
    @SerializedName("preferred_common_name") val preferredCommonName: String?,
    @SerializedName("wikipedia_summary") val wikipediaSummary: String?,
    @SerializedName("default_photo") val defaultPhoto: INatPhoto?,
    @SerializedName("taxon_photos") val taxonPhotos: List<INatTaxonPhoto>?,
    @SerializedName("rank") val rank: String? = null,                   // 新增：species, genus 等
    @SerializedName("ancestor_ids") val ancestorIds: List<Int>? = null  // 新增
)

data class INatTaxonPhoto(
    @SerializedName("photo") val photo: INatPhoto?
)

data class INatPhoto(
    @SerializedName("id") val id: Int?,
    @SerializedName("url") val url: String?,
    @SerializedName("medium_url") val mediumUrl: String?,
    @SerializedName("large_url") val largeUrl: String?,
    @SerializedName("original_dimensions") val originalDimensions: INatDimensions?
)

data class INatDimensions(
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?
)

// ==================== 新增：iNaturalist 识别 API ====================

data class INatIdentifyResponse(
    @SerializedName("results") val results: List<INatIdentification>?
)

data class INatIdentification(
    @SerializedName("taxon") val taxon: INatTaxon?,              // 直接用已有的 INatTaxon
    @SerializedName("vision_score") val visionScore: Double?,
    @SerializedName("frequency_score") val frequencyScore: Double?
)