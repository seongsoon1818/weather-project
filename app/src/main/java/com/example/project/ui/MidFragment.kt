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
import com.example.project.data.repository.WeatherRepository
import com.example.project.location.GridConverter
import kotlinx.coroutines.launch

class MidFragment: Fragment(R.layout.fragment_mid_weather) {
    private val weatherRepository = WeatherRepository()

    private lateinit var tvMidStatus: TextView
    private lateinit var rvDailyWeather: RecyclerView
    private lateinit var swipeMidRefresh: SwipeRefreshLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvMidStatus = view.findViewById(R.id.tvMidStatus)
        rvDailyWeather = view.findViewById(R.id.rvDailyWeather)

        rvDailyWeather.layoutManager = LinearLayoutManager(requireContext())
        swipeMidRefresh = view.findViewById(R.id.swipeMidRefresh)

        val latitude = arguments?.getDouble(ARG_LATITUDE)
        val longitude = arguments?.getDouble(ARG_LONGITUDE)

        if (latitude == null || longitude == null) {
            tvMidStatus.text = "현재 위치 정보가 없습니다."
            return
        }

        loadDailyWeather(latitude, longitude)

        swipeMidRefresh.setOnRefreshListener {
            loadDailyWeather(latitude, longitude, isRefresh = true)
        }
    }

    private fun loadDailyWeather(latitude: Double, longitude: Double, isRefresh: Boolean = false) {
        val grid = GridConverter.convert(latitude, longitude)

        tvMidStatus.text = if (isRefresh) {
            "일별 예보 새로고침 중..."
        } else {
            "일별 예보 조회 중... nx=${grid.x}, ny=${grid.y}"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val dailyWeatherList = weatherRepository.getDailyWeather(
                    nx = grid.x,
                    ny = grid.y
                )

                rvDailyWeather.adapter = DailyAdapter(dailyWeatherList)
                tvMidStatus.text = "일별 예보 조회 완료"

            } catch (e: Exception) {
                tvMidStatus.text = "일별 예보 조회 실패: ${e.message}"
            } finally {
                swipeMidRefresh.isRefreshing = false
            }
        }
    }

    companion object {
        private const val ARG_LATITUDE = "latitude"
        private const val ARG_LONGITUDE = "longitude"

        fun newInstance(latitude: Double, longitude: Double): MidFragment {
            val fragment = MidFragment()
            fragment.arguments = Bundle().apply {
                putDouble(ARG_LATITUDE, latitude)
                putDouble(ARG_LONGITUDE, longitude)
            }
            return fragment
        }
    }
}