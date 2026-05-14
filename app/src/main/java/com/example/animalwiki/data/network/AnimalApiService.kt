package com.example.animalwiki.data.network

import com.example.animalwiki.data.model.ApiAnimal
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimalApiService {
    @GET("posts/{id}")
    suspend fun getAnimalById(@Path("id") id: Int): ApiAnimal

    @GET("posts")
    suspend fun getAnimalsByCategory(@Query("userId") categoryId: Int): List<ApiAnimal>

    @POST("posts")
    suspend fun addAnimal(@retrofit2.http.Body apiAnimal: ApiAnimal): ApiAnimal
}