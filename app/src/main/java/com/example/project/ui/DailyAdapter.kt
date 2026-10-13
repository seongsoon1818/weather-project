package com.example.project.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.model.DailyWeather

class DailyAdapter(
    private val items: List<DailyWeather>
): RecyclerView.Adapter<DailyAdapter.DailyViewHolder>() {
    class DailyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val tvDailyDate: TextView = itemView.findViewById(R.id.tvDailyDate)
        val tvDailyIcon: TextView = itemView.findViewById(R.id.tvDailyIcon)
        val tvDailyWeather: TextView = itemView.findViewById(R.id.tvDailyWeather)
        val tvDailyRain: TextView = itemView.findViewById(R.id.tvDailyRain)
        val tvDailyMaxTemp: TextView = itemView.findViewById(R.id.tvDailyMaxTemp)
        val tvDailyMinTemp: TextView = itemView.findViewById(R.id.tvDailyMinTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_daily_weather, parent, false)

        return DailyViewHolder(view)
    }

    override fun onBindViewHolder(holder: DailyViewHolder, position: Int) {
        val item = items[position]

        holder.tvDailyDate.text = item.date
        holder.tvDailyIcon.text = item.icon
        holder.tvDailyWeather.text = item.weather
        holder.tvDailyRain.text = "💧${item.rainProbability}"
        holder.tvDailyMaxTemp.text = formatTemp(item.maxTemp)
        holder.tvDailyMinTemp.text = formatTemp(item.minTemp)
    }

    private fun formatTemp(value: String): String {
        return if (value == "-" || value.isBlank()) {
            "-"
        }else {
            "${value}°C"
        }
    }

    override fun getItemCount(): Int = items.size
}