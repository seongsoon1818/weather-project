package com.example.project.data.model

data class ShortResponse (
    val response: KmaResponse
)

data class KmaResponse(
    val header: KmaHeader,
    val body: KmaBody?
)

data class  KmaHeader(
    val resultCode: String,
    val resultMsg: String
)

data class KmaBody(
    val dataType: String?,
    val items: KmaItems?
)

data class KmaItems(
    val item: List<KmaWeatherItem> = emptyList()
)

data class KmaWeatherItem(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val nx: Int,
    val ny: Int,
    val obsrValue: String
)

data class CurrentWeather(
    val temperature: String,
    val rainAmount: String,
    val humidity: String,
    val precipitationType: String,
    val windSpeed: String,
    val baseDate: String,
    val baseTime: String
)

data class HourlyWeather(
    val time: String,
    val icon: String,
    val temperature: String,
    val rain: String
)

data class DailyWeather(
    val date: String,
    val icon: String,
    val weather: String,
    val rainProbability: String,
    val minTemp: String,
    val maxTemp: String
)

data class ShortForecastResponse(
    val response: ShortForecastKmaResponse
)

data class ShortForecastKmaResponse(
    val header: KmaHeader,
    val body: ShortForecastBody?
)

data class ShortForecastBody(
    val dataType: String?,
    val items: ShortForecastItems?
)

data class ShortForecastItems(
    val item: List<ShortForecastItem> = emptyList()
)

data class ShortForecastItem(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String,
    val nx: Int,
    val ny: Int
)