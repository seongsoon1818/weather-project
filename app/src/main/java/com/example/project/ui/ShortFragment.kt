package com.example.project.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project.R
import com.example.project.data.model.CurrentWeather
import com.example.project.data.repository.WeatherRepository
import com.example.project.location.GridConverter
import kotlinx.coroutines.launch

class ShortFragment: Fragment(R.layout.fragment_short_weather) {
    private val weatherRepository = WeatherRepository()

    private lateinit var tvWeatherIcon: TextView
    private lateinit var tvTemperature: TextView
    private lateinit var tvPrecipitation: TextView
    private lateinit var tvRainAmount: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvWeatherStatus: TextView
    private lateinit var rvHourlyWeather: RecyclerView
    private lateinit var swipeShortRefresh: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvWeatherIcon = view.findViewById(R.id.tvWeatherIcon)
        tvTemperature = view.findViewById(R.id.tvTemperature)
        tvPrecipitation = view.findViewById(R.id.tvPrecipitation)
        tvRainAmount = view.findViewById(R.id.tvRainAmount)
        tvHumidity = view.findViewById(R.id.tvHumidity)
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed)
        tvWeatherStatus = view.findViewById(R.id.tvWeatherStatus)
        rvHourlyWeather = view.findViewById(R.id.rvHourlyWeather)

        rvHourlyWeather.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        val latitude = arguments?.getDouble(ARG_LATITUDE)
        val longitude = arguments?.getDouble(ARG_LONGITUDE)

        if (latitude == null || longitude == null) {
            tvWeatherStatus.text = "현재 위치 정보가 없습니다."
            return
        }
        swipeShortRefresh = view.findViewById(R.id.swipeShortRefresh)

        loadShortWeather(latitude, longitude)

        swipeShortRefresh.setOnRefreshListener {
            loadShortWeather(latitude, longitude, isRefresh = true)
        }
    }

    private fun showCurrentWeather(weather: CurrentWeather) {
        tvTemperature.text = "${weather.temperature}℃"
        tvPrecipitation.text = getPrecipitationText(weather.precipitationType)
        tvWeatherIcon.text = getWeatherIcon(weather.precipitationType)
        tvRainAmount.text = "1시간 강수량: ${weather.rainAmount}mm"
        tvHumidity.text = "습도: ${weather.humidity}%"
        tvWindSpeed.text = "풍속: ${weather.windSpeed}m/s"
        tvWeatherStatus.text = "기준 시각: ${weather.baseDate} ${weather.baseTime}"
    }

    private fun getPrecipitationText(pty: String): String{
        return when (pty) {
            "0" -> "강수 없음"
            "1" -> "비"
            "2" -> "비 또는 눈"
            "3" -> "눈"
            "5" -> "빗방울"
            "6" -> "빗방울 또는 눈날림"
            "7" -> "눈날림"
            else -> "알 수 없음"
        }
    }

    private fun getWeatherIcon(pty: String): String {
        return when (pty) {
            "1", "5" -> "🌧️"
            "2", "6" -> "🌨️"
            "3", "7" -> "❄️"
            else -> "☀️"
        }
    }

    private fun loadShortWeather(
        latitude: Double,
        longitude: Double,
        isRefresh: Boolean = false
    ) {
        val grid = GridConverter.convert(latitude, longitude)

        tvWeatherStatus.text = if (isRefresh) {
            "날씨 정보 새로고침 중..."
        } else {
            "날씨 정보 요청 중... nx = ${grid.x}, ny = ${grid.y}"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentWeather = weatherRepository.getCurrentWeather(
                    nx = grid.x,
                    ny = grid.y
                )

                showCurrentWeather(currentWeather)

                val hourlyWeatherList = weatherRepository.getHourlyWeather(
                    nx = grid.x,
                    ny = grid.y
                )

                rvHourlyWeather.adapter = HourlyAdapter(hourlyWeatherList)

                tvWeatherStatus.text = "기준 시각: ${currentWeather.baseDate} ${currentWeather.baseTime}"

            } catch (e: Exception) {
                tvWeatherStatus.text = "날씨 정보 조회 실패: ${e.message}"
            } finally {
                swipeShortRefresh.isRefreshing = false
            }
        }
    }

    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(latitude: Double, longitude: Double): ShortFragment {
            val fragment = ShortFragment()
            fragment.arguments = Bundle().apply {
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
            }
            return fragment
        }
    }
}

