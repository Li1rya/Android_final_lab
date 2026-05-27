package com.example.animalwiki.data.network

import com.example.animalwiki.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface AnimalApiService {

    // ========== 原有接口 ==========

    @GET("https://api.inaturalist.org/v1/taxa")
    suspend fun searchTaxa(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 5
    ): INatResponse

    // ========== 新增：图片识别 ==========

    /**
     * iNaturalist 图片识别（计算机视觉）
     * POST multipart/form-data，上传图片文件
     */
    @Multipart
    @POST("https://api.inaturalist.org/v1/computervision/score_image")
    suspend fun identifyImage(
        @Part image: MultipartBody.Part
    ): INatIdentifyResponse

    /**
     * 获取指定 taxon 的详细信息
     */
    @GET("https://api.inaturalist.org/v1/taxa/{id}")
    suspend fun getTaxonById(
        @Path("id") id: Int
    ): INatResponse
}