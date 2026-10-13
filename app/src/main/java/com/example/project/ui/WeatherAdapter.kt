package com.example.project.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class WeatherAdapter(
    activity: FragmentActivity,
    private val latitude: Double,
    private val longitude: Double
): FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ShortFragment.newInstance(latitude, longitude)
            1 -> MidFragment.newInstance(latitude, longitude)
            2 -> AirFragment.newInstance(latitude, longitude)
            else -> ShortFragment.newInstance(latitude, longitude)
        }
    }
}