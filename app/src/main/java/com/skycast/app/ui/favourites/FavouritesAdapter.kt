package com.skycast.app.ui.favourites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skycast.app.data.local.FavouriteCity
import com.skycast.app.databinding.ItemFavouriteCityBinding

class FavouritesAdapter(
    private val onCityClicked: (FavouriteCity) -> Unit,
    private val onRemoveClicked: (FavouriteCity) -> Unit
) : RecyclerView.Adapter<FavouritesAdapter.ViewHolder>() {

    private val items = mutableListOf<FavouriteCity>()

    fun submitList(newItems: List<FavouriteCity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemFavouriteCityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFavouriteCityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvCityName.text = item.cityName
        holder.binding.tvCountry.text = item.country
        holder.binding.tvTemp.text = item.lastKnownTempC?.let { "${it.toInt()}°C" } ?: "—"
        holder.binding.root.setOnClickListener { onCityClicked(item) }
        holder.binding.btnRemove.setOnClickListener { onRemoveClicked(item) }
    }

    override fun getItemCount(): Int = items.size
}
