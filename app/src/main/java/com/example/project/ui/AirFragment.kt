package com.example.project.ui

import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.project.R
import com.example.project.data.model.AirQuality
import com.example.project.data.model.AirRegion
import com.example.project.data.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class AirFragment : Fragment(R.layout.fragment_air) {
    private val weatherRepository = WeatherRepository()

    private lateinit var tvAirStatus: TextView
    private lateinit var tvAirStation: TextView
    private lateinit var tvAirGrade: TextView
    private lateinit var tvAirKhaiValue: TextView
    private lateinit var tvPm10Value: TextView
    private lateinit var tvPm10Grade: TextView
    private lateinit var tvPm25Value: TextView
    private lateinit var tvPm25Grade: TextView
    private lateinit var tvAirDataTime: TextView
    private lateinit var swipeAirRefresh: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvAirStatus = view.findViewById(R.id.tvAirStatus)
        tvAirStation = view.findViewById(R.id.tvAirStation)
        tvAirGrade = view.findViewById(R.id.tvAirGrade)
        tvAirKhaiValue = view.findViewById(R.id.tvAirKhaiValue)
        tvPm10Value = view.findViewById(R.id.tvPm10Value)
        tvPm10Grade = view.findViewById(R.id.tvPm10Grade)
        tvPm25Value = view.findViewById(R.id.tvPm25Value)
        tvPm25Grade = view.findViewById(R.id.tvPm25Grade)
        tvAirDataTime = view.findViewById(R.id.tvAirDataTime)

        swipeAirRefresh = view.findViewById(R.id.swipeAirRefresh)

        val latitude = arguments?.getDouble(ARG_LATITUDE)
        val longitude = arguments?.getDouble(ARG_LONGITUDE)

        if (latitude == null || longitude == null) {
            tvAirStatus.text = "현재 위치 정보가 없습니다."
            return
        }

        loadAirQuality(latitude, longitude)
        swipeAirRefresh.setOnRefreshListener {
            loadAirQuality(latitude, longitude, isRefresh = true)
        }
    }

    private fun loadAirQuality(latitude: Double, longitude: Double, isRefresh: Boolean = false) {
        tvAirStatus.text = if(isRefresh){
            "대기질 정보 새로고침 중..."
        } else {
            "대기질 정보 조회 중..."
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val region = getRegionFromLocation(latitude, longitude)

                tvAirStatus.text = "대기질 정보 조회 중... ${region.sidoName} ${region.stationName ?: ""}"

                val airQuality = weatherRepository.getAirQuality(
                    sidoName = region.sidoName,
                    stationName = region.stationName
                )

                showAirQuality(airQuality)
            } catch (e: Exception) {
                tvAirStatus.text = "대기질 정보 조회 실패: ${e.message}"
            } finally {
                swipeAirRefresh.isRefreshing = false
            }
        }
    }

    private suspend fun getRegionFromLocation(
        latitude: Double,
        longitude: Double
    ): AirRegion {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(requireContext(), Locale.KOREA)

                @Suppress("DEPRECATION")
                val address = geocoder.getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull()

                val adminArea = address?.adminArea ?: ""
                val subLocality = address?.subLocality
                val locality = address?.locality

                val sidoName = convertSidoName(adminArea)

                val stationName = when {
                    !subLocality.isNullOrBlank() -> subLocality
                    !locality.isNullOrBlank() && locality != adminArea -> locality
                    else -> null
                }

                AirRegion(
                    sidoName = sidoName,
                    stationName = stationName
                )
            } catch (e: Exception) {
                AirRegion(
                    sidoName = "서울",
                    stationName = null
                )
            }
        }
    }

    private fun convertSidoName(adminArea: String): String {
        return when {
            adminArea.contains("서울") -> "서울"
            adminArea.contains("부산") -> "부산"
            adminArea.contains("대구") -> "대구"
            adminArea.contains("인천") -> "인천"
            adminArea.contains("광주") -> "광주"
            adminArea.contains("대전") -> "대전"
            adminArea.contains("울산") -> "울산"
            adminArea.contains("경기") -> "경기"
            adminArea.contains("강원") -> "강원"
            adminArea.contains("충청북") || adminArea.contains("충북") -> "충북"
            adminArea.contains("충청남") || adminArea.contains("충남") -> "충남"
            adminArea.contains("전라북") || adminArea.contains("전북") -> "전북"
            adminArea.contains("전라남") || adminArea.contains("전남") -> "전남"
            adminArea.contains("경상북") || adminArea.contains("경북") -> "경북"
            adminArea.contains("경상남") || adminArea.contains("경남") -> "경남"
            adminArea.contains("제주") -> "제주"
            adminArea.contains("세종") -> "세종"
            else -> "서울"
        }
    }

    private fun showAirQuality(airQuality: AirQuality) {
        tvAirStatus.text = "대기질 정보 조회 완료"
        tvAirStation.text = "측정소: ${airQuality.stationName}"

        tvAirGrade.text = airQuality.khaiGrade
        tvAirKhaiValue.text = "AQI ${airQuality.khaiValue}"

        tvPm10Value.text = "${airQuality.pm10Value}㎍/㎥"
        tvPm10Grade.text = airQuality.pm10Grade

        tvPm25Value.text = "${airQuality.pm25Value}㎍/㎥"
        tvPm25Grade.text = airQuality.pm25Grade

        tvAirDataTime.text = "측정 시각: ${airQuality.dataTime}"
        applyAirQuality(airQuality)
    }

    private fun applyAirQuality(airQuality: AirQuality) {
        val khaiColor = getAirGradeColor(airQuality.khaiGrade)
        tvAirGrade.setTextColor(khaiColor)
        tvAirKhaiValue.setTextColor(khaiColor)

        val pm10Color = getAirGradeColor(airQuality.pm10Grade)
        tvPm10Value.setTextColor(pm10Color)
        tvPm10Grade.setTextColor(pm10Color)

        val pm25Color = getAirGradeColor(airQuality.pm25Grade)
        tvPm25Value.setTextColor(pm25Color)
        tvPm25Grade.setTextColor(pm25Color)
    }

    private fun getAirGradeColor(grade: String): Int {
        val colorRes = when (grade) {
            "좋음" -> R.color.air_good
            "보통" -> R.color.air_normal
            "나쁨" -> R.color.air_bad
            "매우나쁨" -> R.color.air_very_bad
            else -> R.color.air_unknown
        }

        return ContextCompat.getColor(requireContext(), colorRes)
    }

    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(latitude: Double, longitude: Double): AirFragment {
            val fragment = AirFragment()
            fragment.arguments = Bundle().apply {
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
            }
            return fragment
        }
    }
}

