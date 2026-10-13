package com.example.project.data.api

import com.example.project.data.model.ShortForecastResponse
import com.example.project.data.model.ShortResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ShortApi {
    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
    suspend fun getUltraSrcNcst(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "json",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): ShortResponse

    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtFcst")
    suspend fun getUltraSrtFcst(
        @Query("serviceKey") serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): ShortForecastResponse

    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst")
    suspend fun getVilageFcst(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 1000,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "JSON",
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): ShortForecastResponse
}