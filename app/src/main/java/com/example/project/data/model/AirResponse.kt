package com.example.project.data.model

data class AirResponse (
    val response: AirKoreaResponse
)

data class AirKoreaResponse(
    val header: AirHeader,
    val body: AirBody?
)

data class AirHeader(
    val resultCode: String,
    val resultMsg: String
)

data class AirBody(
    val totalCount: Int?,
    val items: List<AirItem> = emptyList()
)

data class AirItem(
    val stationName: String?,
    val sidoName: String?,
    val dataTime: String?,

    val khaiValue: String?,
    val khaiGrade: String?,

    val pm10Value: String?,
    val pm10Grade: String?,
    val pm10Grade1h: String?,

    val pm25Value: String?,
    val pm25Grade: String?,
    val pm25Grade1h: String?,

    val so2Value: String?,
    val coValue: String?,
    val o3Value: String?,
    val no2Value: String?
)

data class AirQuality(
    val stationName: String,
    val dataTime: String,
    val khaiValue: String,
    val khaiGrade: String,
    val pm10Value: String,
    val pm10Grade: String,
    val pm25Value: String,
    val pm25Grade: String
)

data class AirRegion(
    val sidoName: String,
    val stationName: String?
)