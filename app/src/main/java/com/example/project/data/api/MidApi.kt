package com.example.project.data.api

import com.example.project.data.model.MidLandResponse
import com.example.project.data.model.MidTaResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MidApi {
    @GET("1360000/MidFcstInfoService/getMidLandFcst")
    suspend fun getMidLandFcst(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "JSON",
        @Query("regId") regId: String,
        @Query("tmFc") tmFc: String
    ): MidLandResponse

    @GET("1360000/MidFcstInfoService/getMidTa")
    suspend fun getMidTa(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("pageNo") pageNo: Int = 1,
        @Query("dataType") dataType: String = "JSON",
        @Query("regId") regId: String,
        @Query("tmFc") tmFc: String
    ): MidTaResponse
}