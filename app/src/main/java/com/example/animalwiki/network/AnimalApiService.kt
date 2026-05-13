package com.example.animalwiki.network

import com.example.animalwiki.model.Animal
import retrofit2.http.GET
import retrofit2.http.Path

interface AnimalApiService {
    // GET 1：获取动物列表
    @GET("posts")
    suspend fun getAnimalList(): List<Animal>

    // GET 2：根据ID获取详情
    @GET("posts/{id}")
    suspend fun getAnimalDetail(@Path("id") id: Int): Animal
}