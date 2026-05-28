package com.example.animalwiki.data.network

import com.example.animalwiki.data.model.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface AnimalApiService {

    // ========== 新增：百度认证 & 动物识别 ==========

    /** 获取 access_token（有效期约 30 天） */
    @POST("https://aip.baidubce.com/oauth/2.0/token")
    @FormUrlEncoded
    suspend fun getBaiduAccessToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): BaiduTokenResponse

    /** 百度动物识别 */
    @POST("https://aip.baidubce.com/rest/2.0/image-classify/v1/animal")
    @FormUrlEncoded
    suspend fun baiduAnimalDetect(
        @Query("access_token") accessToken: String,
        @Field("image") imageBase64: String,      // Retrofit 会自动 URL encode
        @Field("top_num") topNum: Int = 6,
        @Field("baike_num") baikeNum: Int = 1
    ): BaiduAnimalResponse




// ==================== 以下接口已经废弃，保留是为了防止编译出错 ========================

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