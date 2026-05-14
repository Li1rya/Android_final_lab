package com.example.animalwiki.data.network

import com.example.animalwiki.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val animalApiService: AnimalApiService by lazy {
        retrofit.create(AnimalApiService::class.java)
    }
}