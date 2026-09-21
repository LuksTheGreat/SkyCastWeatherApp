package com.skycast.app.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skycast.app.data.remote.model.GeoResult
import com.skycast.app.databinding.ItemSearchResultBinding

/**
 * onStarClicked receives the item AND whether it's currently a favourite,
 * so the Activity knows whether to add or remove it.
 */
class SearchResultsAdapter(
    private val onCityClicked: (GeoResult) -> Unit,
    private val onStarClicked: (GeoResult, Boolean) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private val items = mutableListOf<GeoResult>()
    private val favouriteKeys = mutableSetOf<String>()

    fun submitList(newItems: List<GeoResult>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    /** Called whenever the Favourites list changes, so stars stay in sync. */
    fun setFavouriteKeys(keys: Set<String>) {
        favouriteKeys.clear()
        favouriteKeys.addAll(keys)
        notifyDataSetChanged()
    }

    private fun keyOf(item: GeoResult) = "${item.name}|${item.country}"

    inner class ViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val stateLabel = item.state?.let { ", $it" } ?: ""
        holder.binding.tvCityCountry.text = "${item.name}$stateLabel, ${item.country}"

        val isFavourite = favouriteKeys.contains(keyOf(item))
        holder.binding.btnStar.setImageResource(
            if (isFavourite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
        )
        holder.binding.btnStar.setColorFilter(
            if (isFavourite) 0xFFFFC107.toInt() else 0xFF9E9E9E.toInt()
        )

        holder.binding.root.setOnClickListener { onCityClicked(item) }
        holder.binding.btnStar.setOnClickListener { onStarClicked(item, isFavourite) }
    }

    override fun getItemCount(): Int = items.size
}
