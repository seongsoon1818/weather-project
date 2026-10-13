package com.example.project.data.repository

import android.icu.util.Calendar
import com.example.project.data.api.AirApi
import com.example.project.data.api.MidApi
import com.example.project.data.api.ShortApi
import com.example.project.data.model.AirItem
import com.example.project.data.model.AirQuality
import com.example.project.data.model.CurrentWeather
import com.example.project.data.model.DailyWeather
import com.example.project.data.model.HourlyWeather
import com.example.project.data.model.MidLandItem
import com.example.project.data.model.MidTaItem
import com.example.project.data.model.ShortForecastItem
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.max

class WeatherRepository {
    private val shortApi: ShortApi
    private val midApi: MidApi
    private val airApi: AirApi
    private val serviceKey = "3f0a3d7b76dca879563887af54dc437fe45a89c756b36b8d5e06168753afa26c"
    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://apis.data.go.kr/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        shortApi = retrofit.create(ShortApi::class.java)
        midApi = retrofit.create(MidApi::class.java)
        airApi = retrofit.create(AirApi::class.java)
    }

    suspend fun getCurrentWeather(nx: Int, ny: Int): CurrentWeather {
        val baseDateTime = getUltraSrtNcstBaseDateTime()
        val baseDate = baseDateTime.first
        val baseTime = baseDateTime.second

        val response = shortApi.getUltraSrcNcst(
            serviceKey = serviceKey,
            baseDate = baseDate,
            baseTime = baseTime,
            nx = nx,
            ny = ny
        )

        val header = response.response.header

        if(header.resultCode != "00" && header.resultCode != "0") {
            throw Exception("API 오류: ${header.resultCode} / ${header.resultMsg}")
        }

        val items = response.response.body
            ?.items
            ?.item
            ?: emptyList()

        val weatherMap = items.associate {
            it.category to it.obsrValue
        }

        return CurrentWeather(
            temperature = weatherMap["T1H"] ?: "-",
            rainAmount = weatherMap["RN1"] ?: "0",
            humidity = weatherMap["REH"] ?: "-",
            precipitationType = weatherMap["PTY"] ?: "0",
            windSpeed = weatherMap["WSD"] ?: "-",
            baseDate = baseDate,
            baseTime = baseTime
        )
    }

    private fun getUltraSrtNcstBaseDateTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()

        if (calendar.get(Calendar.MINUTE) < 10) {
            calendar.add(Calendar.HOUR_OF_DAY, -1)
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val hourFormat = SimpleDateFormat("HH", Locale.KOREA)

        val baseDate = dateFormat.format(calendar.time)
        val baseTime = "${hourFormat.format(calendar.time)}00"

        return baseDate to baseTime
    }

    suspend fun getHourlyWeather(nx: Int, ny: Int): List<HourlyWeather> {
        val baseDateTime = getUltraSrtNcstBaseDateTime()
        val baseDate = baseDateTime.first
        val baseTime = baseDateTime.second

        val response = shortApi.getUltraSrtFcst(
            serviceKey = serviceKey,
            baseDate = baseDate,
            baseTime = baseTime,
            nx = nx,
            ny = ny
        )

        val header = response.response.header

        if (header.resultCode != "00" && header.resultCode != "0") {
            throw Exception("초단기예보 API 오류: ${header.resultCode} / ${header.resultMsg}")
        }

        val items = response.response.body
            ?.items
            ?.item
            ?: emptyList()

        val groupedItems = items
            .groupBy { "${it.fcstDate}${it.fcstTime}" }
            .toSortedMap()

        return groupedItems.values.mapNotNull { forecastItems ->
            val first = forecastItems.firstOrNull() ?: return@mapNotNull null

            val timeText = "${first.fcstTime.substring(0, 2).toInt()}시"

            val temperature = forecastItems
                .firstOrNull { it.category == "T1H" }
                ?.fcstValue
                ?: return@mapNotNull null

            val sky = forecastItems
                .firstOrNull { it.category == "SKY" }
                ?.fcstValue
                ?: "1"

            val pty = forecastItems
                .firstOrNull { it.category == "PTY" }
                ?.fcstValue
                ?: "0"

            val rain = forecastItems
                .firstOrNull { it.category == "RN1" }
                ?.fcstValue
                ?: "0"

            HourlyWeather(
                time = timeText,
                icon = getHourlyWeatherIcon(pty, sky),
                temperature = "${temperature}℃",
                rain = "강수 ${formatRainAmount(rain)}"
            )
        }.take(6)
    }

    private fun getUltraSrtFcstBaseDateTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()

        if (calendar.get(Calendar.MINUTE) < 45) {
            calendar.add(Calendar.HOUR_OF_DAY, -1)
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val hourFormat = SimpleDateFormat("HH", Locale.KOREA)

        val baseDate = dateFormat.format(calendar.time)
        val baseTime = "${hourFormat.format(calendar.time)}30"

        return baseDate to baseTime
    }

    private fun getHourlyWeatherIcon(pty: String, sky: String): String {
        return when (pty) {
            "1", "5" -> "🌧️"
            "2", "6" -> "🌨️"
            "3", "7" -> "❄️"
            else -> {
                when (sky) {
                    "1" -> "☀️"
                    "3" -> "⛅"
                    "4" -> "☁️"
                    else -> "☀️"
                }
            }
        }
    }

    private fun formatRainAmount(rain: String): String {
        return when {
            rain == "0" || rain == "0.0" || rain == "강수없음" -> "0mm"
            rain.contains("mm") -> rain
            else -> "${rain}mm"
        }
    }

    suspend fun getDailyWeather(nx: Int, ny: Int): List<DailyWeather> {
        val shortDaily = getShortDailyWeather(nx, ny)
        val midDaily = getMidDailyWeather()

        return shortDaily + midDaily
    }

    private suspend fun getShortDailyWeather(nx: Int, ny: Int): List<DailyWeather> {
        val baseDateTime = getVilageFcstBaseDateTime()
        val baseDate = baseDateTime.first
        val baseTime = baseDateTime.second

        val response = shortApi.getVilageFcst(
            serviceKey = serviceKey,
            baseDate = baseDate,
            baseTime = baseTime,
            nx = nx,
            ny = ny
        )

        val header = response.response.header

        if (header.resultCode != "00" && header.resultCode != "0") {
            throw Exception("단기예보 API 오류: ${header.resultCode} / ${header.resultMsg}")
        }

        val items = response.response.body
            ?.items
            ?.item
            ?: emptyList()

        return (1..3).map { day ->
            val targetDate = getDateAfter(day, "yyyyMMdd")
            createShortDailyWeather(day, targetDate, items)
        }
    }

    private fun createShortDailyWeather(
        day: Int,
        targetDate: String,
        items: List<ShortForecastItem>
    ): DailyWeather {
        val dayItems = items.filter { it.fcstDate == targetDate }

        val minTemp = dayItems
            .firstOrNull { it.category == "TMN" }
            ?.fcstValue
            ?: "-"

        val maxTemp = dayItems
            .firstOrNull { it.category == "TMX" }
            ?.fcstValue
            ?: "-"

        val rainProbability = dayItems
            .filter { it.category == "POP" }
            .mapNotNull { it.fcstValue.toIntOrNull() }
            .maxOrNull()
            ?.toString()
            ?: "-"

        val weatherText = getShortWeatherText(dayItems)

        return DailyWeather(
            date = getDateAfter(day, "M / d (E)"),
            icon = getWeatherIconByText(weatherText),
            weather = weatherText,
            rainProbability = "$rainProbability%",
            minTemp = minTemp,
            maxTemp = maxTemp
        )
    }

    private fun getShortWeatherText(items: List<ShortForecastItem>): String {
        val ptyItems = items.filter { it.category == "PTY" }

        val rainOrSnow = ptyItems.firstOrNull {
            it.fcstValue != "0"
        }?.fcstValue

        if (rainOrSnow != null) {
            return when (rainOrSnow) {
                "1" -> "비"
                "2" -> "비/눈"
                "3" -> "눈"
                "4" -> "소나기"
                else -> "강수"
            }
        }

        val skyValue = items
            .filter { it.category == "SKY" }
            .firstOrNull { it.fcstTime == "1200" }
            ?.fcstValue
            ?: items.firstOrNull { it.category == "SKY" }?.fcstValue

        return when (skyValue) {
            "1" -> "맑음"
            "3" -> "구름많음"
            "4" -> "흐림"
            else -> "-"
        }
    }

    private suspend fun getMidDailyWeather(): List<DailyWeather> {
        val tmFc = getMidTmFcForFourDayForecast()

        val landRegId = "11B00000"
        val tempRegId = "11B10101"

        val landResponse = midApi.getMidLandFcst(
            serviceKey = serviceKey,
            regId = landRegId,
            tmFc = tmFc
        )

        val taResponse = midApi.getMidTa(
            serviceKey = serviceKey,
            regId = tempRegId,
            tmFc = tmFc
        )

        val landHeader = landResponse.response.header
        val taHeader = taResponse.response.header

        if (landHeader.resultCode != "00" && landHeader.resultCode != "0") {
            throw Exception("중기육상예보 API 오류: ${landHeader.resultCode} / ${landHeader.resultMsg}")
        }

        if (taHeader.resultCode != "00" && taHeader.resultCode != "0") {
            throw Exception("중기기온 API 오류: ${taHeader.resultCode} / ${taHeader.resultMsg}")
        }

        val landItem = landResponse.response.body
            ?.items
            ?.item
            ?.firstOrNull()
            ?: throw Exception("중기육상예보 데이터가 없습니다.")

        val taItem = taResponse.response.body
            ?.items
            ?.item
            ?.firstOrNull()
            ?: throw Exception("중기기온 데이터가 없습니다.")

        return (4..6).map { day ->
            createMidDailyWeather(day, landItem, taItem)
        }
    }

    private fun createMidDailyWeather(
        day: Int,
        land: MidLandItem,
        temp: MidTaItem
    ): DailyWeather {
        val weatherText = getMidWeatherText(day, land)
        val rainProbability = getMidRainProbability(day, land)
        val minTemp = getMidMinTemp(day, temp)
        val maxTemp = getMidMaxTemp(day, temp)

        return DailyWeather(
            date = getDateAfter(day, "M / d (E)"),
            icon = getWeatherIconByText(weatherText),
            weather = weatherText,
            rainProbability = "$rainProbability%",
            minTemp = minTemp,
            maxTemp = maxTemp
        )
    }

    private fun getMidWeatherText(day: Int, item: MidLandItem): String {
        return when (day) {
            4 -> selectWeather(item.wf4Am, item.wf4Pm)
            5 -> selectWeather(item.wf5Am, item.wf5Pm)
            6 -> selectWeather(item.wf6Am, item.wf6Pm)
            else -> "-"
        }
    }

    private fun getMidRainProbability(day: Int, item: MidLandItem): String {
        return when (day) {
            4 -> maxRain(item.rnSt4Am, item.rnSt4Pm)
            5 -> maxRain(item.rnSt5Am, item.rnSt5Pm)
            6 -> maxRain(item.rnSt6Am, item.rnSt6Pm)
            else -> "-"
        }
    }

    private fun getMidMinTemp(day: Int, item: MidTaItem): String {
        return when (day) {
            4 -> item.taMin4
            5 -> item.taMin5
            6 -> item.taMin6
            else -> null
        } ?: "-"
    }

    private fun getMidMaxTemp(day: Int, item: MidTaItem): String {
        return when (day) {
            4 -> item.taMax4
            5 -> item.taMax5
            6 -> item.taMax6
            else -> null
        } ?: "-"
    }

    private fun selectWeather(am: String?, pm: String?): String {
        val amText = am ?: "-"
        val pmText = pm ?: "-"

        return when {
            pmText.contains("비") || pmText.contains("눈") -> pmText
            amText.contains("비") || amText.contains("눈") -> amText
            pmText != "-" -> pmText
            else -> amText
        }
    }

    private fun maxRain(am: String?, pm: String?): String {
        val amValue = am?.toIntOrNull()
        val pmValue = pm?.toIntOrNull()

        return when {
            amValue != null && pmValue != null -> max(amValue, pmValue).toString()
            amValue != null -> amValue.toString()
            pmValue != null -> pmValue.toString()
            else -> "-"
        }
    }

    private fun getWeatherIconByText(weather: String): String {
        return when {
            weather.contains("눈") -> "❄️"
            weather.contains("비") -> "🌧️"
            weather.contains("흐림") -> "☁️"
            weather.contains("구름") -> "⛅"
            weather.contains("맑음") -> "☀️"
            else -> "⛅"
        }
    }

    private fun getDateAfter(day: Int, pattern: String): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, day)

        val dateFormat = SimpleDateFormat(pattern, Locale.KOREA)
        return dateFormat.format(calendar.time)
    }

    private fun getVilageFcstBaseDateTime(): Pair<String, String> {
        val calendar = Calendar.getInstance()

        val baseTimes = listOf(2, 5, 8, 11, 14, 17, 20, 23)
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        var selectedHour = baseTimes.lastOrNull { hour ->
            hour < currentHour || hour == currentHour && currentMinute >= 10
        }

        if (selectedHour == null) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            selectedHour = 23
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val baseDate = dateFormat.format(calendar.time)
        val baseTime = String.format("%02d00", selectedHour)

        return baseDate to baseTime
    }

    private fun getMidTmFcForFourDayForecast(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        if (hour < 6) {
            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        return dateFormat.format(calendar.time) + "0600"
    }

    suspend fun getAirQuality(
        sidoName: String,
        stationName: String?
    ): AirQuality {
        val response = airApi.getSidoAirQuality(
            serviceKey = serviceKey,
            sidoName = sidoName
        )

        val header = response.response.header

        if (header.resultCode != "00" && header.resultCode != "0") {
            throw Exception("대기질 API 오류: ${header.resultCode} / ${header.resultMsg}")
        }

        val items = response.response.body
            ?.items
            ?: emptyList()

        if (items.isEmpty()) {
            throw Exception("대기질 데이터가 없습니다.")
        }

        val selectedItem = selectAirItem(items, stationName)

        return AirQuality(
            stationName = selectedItem.stationName ?: "-",
            dataTime = selectedItem.dataTime ?: "-",
            khaiValue = cleanAirValue(selectedItem.khaiValue),
            khaiGrade = getAirGradeText(selectedItem.khaiGrade),
            pm10Value = cleanAirValue(selectedItem.pm10Value),
            pm10Grade = getAirGradeText(selectedItem.pm10Grade1h ?: selectedItem.pm10Grade),
            pm25Value = cleanAirValue(selectedItem.pm25Value),
            pm25Grade = getAirGradeText(selectedItem.pm25Grade1h ?: selectedItem.pm25Grade)
        )
    }

    private fun selectAirItem(
        items: List<AirItem>,
        stationName: String?
    ): AirItem {
        val targetStation = stationName?.trim()

        if (!targetStation.isNullOrBlank()) {
            val exactItem = items.firstOrNull {
                it.stationName == targetStation
            }

            if (exactItem != null) {
                return exactItem
            }

            val containsItem = items.firstOrNull {
                it.stationName?.contains(targetStation) == true ||
                        targetStation.contains(it.stationName ?: "")
            }

            if (containsItem != null) {
                return containsItem
            }
        }

        return items.firstOrNull {
            isValidAirValue(it.khaiValue) ||
                    isValidAirValue(it.pm10Value) ||
                    isValidAirValue(it.pm25Value)
        } ?: items.first()
    }

    private fun isValidAirValue(value: String?): Boolean {
        return !value.isNullOrBlank() && value != "-"
    }

    private fun cleanAirValue(value: String?): String {
        return if (value.isNullOrBlank() || value == "-") {
            "-"
        } else {
            value
        }
    }

    private fun getAirGradeText(grade: String?): String {
        return when (grade) {
            "1" -> "좋음"
            "2" -> "보통"
            "3" -> "나쁨"
            "4" -> "매우나쁨"
            else -> "-"
        }
    }
}