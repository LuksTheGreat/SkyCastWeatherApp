package com.skycast.app.ui.forecast

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skycast.app.data.remote.model.ForecastDay
import com.skycast.app.databinding.ItemForecastDayBinding

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    private val items = mutableListOf<ForecastDay>()

    fun submitList(newItems: List<ForecastDay>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemForecastDayBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemForecastDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvDayName.text = item.dayLabel
        holder.binding.tvCondition.text = item.condition
        holder.binding.tvMinMax.text = "${item.maxTemp.toInt()}°/${item.minTemp.toInt()}°"
    }

    override fun getItemCount(): Int = items.size
}
