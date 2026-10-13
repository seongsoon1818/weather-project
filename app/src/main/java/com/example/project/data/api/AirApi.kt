package com.example.project.data.api

import com.example.project.data.model.AirResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AirApi {
    @GET("B552584/ArpltnInforInqireSvc/getCtprvnRltmMesureDnsty")
    suspend fun getSidoAirQuality(
        @Query(value = "serviceKey", encoded = true) serviceKey: String,
        @Query("returnType") returnType: String = "json",
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1,
        @Query("sidoName") sidoName: String,
        @Query("ver") ver: String = "1.3"
    ): AirResponse
}