package com.skycast.app.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skycast.app.data.remote.model.GeoResult
import com.skycast.app.databinding.ItemSearchResultBinding

class SearchResultsAdapter(
    private val onCityClicked: (GeoResult) -> Unit,
    private val onStarClicked: (GeoResult) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    private val items = mutableListOf<GeoResult>()

    fun submitList(newItems: List<GeoResult>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val stateLabel = item.state?.let { ", $it" } ?: ""
        holder.binding.tvCityCountry.text = "${item.name}$stateLabel, ${item.country}"
        holder.binding.root.setOnClickListener { onCityClicked(item) }
        holder.binding.btnStar.setOnClickListener { onStarClicked(item) }
    }

    override fun getItemCount(): Int = items.size
}
