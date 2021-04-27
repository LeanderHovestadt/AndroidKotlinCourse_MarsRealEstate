package com.udacity.asteroidradar.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.Constants
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap


interface NasaApiService {
    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(@QueryMap(encoded = true) options: Map<String, String>): String

    @GET("planetary/apod")
    suspend fun getPictureOfTheDay(@Query("api_key") api_key: String): PictureOfTheDay
}

object Network {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val retrofitService: NasaApiService by lazy { retrofit.create(NasaApiService::class.java) }
}