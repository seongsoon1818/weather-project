package com.example.project.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.model.HourlyWeather

class HourlyAdapter (
    private val items: List<HourlyWeather>
): RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder>(){
    class HourlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHourlyTime: TextView = itemView.findViewById(R.id.tvHourlyTime)
        val tvHourlyIcon: TextView = itemView.findViewById(R.id.tvHourlyIcon)
        val tvHourlyTemp: TextView = itemView.findViewById(R.id.tvHourlyTemp)
        val tvHourlyRain: TextView = itemView.findViewById(R.id.tvHourlyRain)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hourly_weather, parent, false)

            return HourlyViewHolder(view)
    }


    override fun onBindViewHolder(holder: HourlyViewHolder, position: Int) {
        val item = items[position]

        holder.tvHourlyTime.text = item.time
        holder.tvHourlyIcon.text = item.icon
        holder.tvHourlyTemp.text = item.temperature
        holder.tvHourlyRain.text = item.rain
    }

    override fun getItemCount(): Int = items.size
}