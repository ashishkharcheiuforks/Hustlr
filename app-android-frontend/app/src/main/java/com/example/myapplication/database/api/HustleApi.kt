package com.example.myapplication.database.api

import com.example.myapplication.BASE_URL
import com.example.myapplication.database.model.Hustle
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface HustleApi {

    @GET("hustle/users/{userId}")
    fun getHustlesByUser(@Path("userId") hustlrId: String) : Call<List<Hustle>>

    @GET("hustle/users/{userId}/matched")
//    fun getHustlesByUserMatched(@Path("userId") hustlrId: String) : Single<HustleModel.HustleResponse>
    fun getHustlesByUserMatched(@Path("userId") hustlrId: String) : Call<HustleModel.HustleResponse>

    @POST("hustle/users/{userId}")
    fun postHustle(@Path("userId") providerId: String, @Body hustle: HustleModel.HustleRequest) : Call<Hustle>

    @PATCH("hustle/users/{userId}/{hustleId}")
    fun updateHustle(@Path("userId") hustlrId: String, @Path("hustleId") hustleId: String, @Body hustle: Hustle) : Call<Hustle>

    companion object {
        fun create(): HustleApi {
            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build()

            return retrofit.create(HustleApi::class.java)
        }
    }
}