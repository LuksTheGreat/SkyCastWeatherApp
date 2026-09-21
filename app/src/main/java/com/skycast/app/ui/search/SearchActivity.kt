package com.skycast.app.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skycast.app.data.local.FavouriteCity
import com.skycast.app.data.remote.model.GeoResult
import com.skycast.app.data.repository.StreakRepository
import com.skycast.app.data.repository.WeatherRepository
import com.skycast.app.databinding.ActivitySearchBinding
import com.skycast.app.ui.forecast.ForecastActivity
import com.skycast.app.util.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * "Search Screen (find a city)" from Part 1's navigation diagram (R2).
 * A short debounce delay is used between keystrokes so the app doesn't fire
 * a new geocoding request for every single character typed.
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var adapter: SearchResultsAdapter
    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weatherRepository = WeatherRepository(applicationContext)
        streakRepository = StreakRepository(applicationContext)

        adapter = SearchResultsAdapter(
            onCityClicked = { result -> openForecastFor(result) },
            onStarClicked = { result -> saveFavourite(result) }
        )
        binding.rvResults.layoutManager = LinearLayoutManager(this)
        binding.rvResults.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onQueryChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            adapter.submitList(emptyList())
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.tvEmpty.text = "Type a city name to search"
            return
        }
        searchJob = lifecycleScope.launch {
            delay(400) // debounce: avoid firing a request per keystroke
            runSearch(query)
        }
    }

    private suspend fun runSearch(query: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvEmpty.visibility = android.view.View.GONE
        try {
            val results = weatherRepository.searchCities(query)
            adapter.submitList(results)
            if (results.isEmpty()) {
                binding.tvEmpty.visibility = android.view.View.VISIBLE
                binding.tvEmpty.text = "No cities found for \"$query\""
            }
        } catch (e: Exception) {
            Log.e(Constants.LOG_TAG, "City search failed for '$query'", e)
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.tvEmpty.text = "Search failed. Check your connection and try again."
        } finally {
            binding.progressBar.visibility = android.view.View.GONE
        }
    }

    private fun saveFavourite(result: GeoResult) {
        lifecycleScope.launch {
            val alreadySaved = weatherRepository.isFavourite(result.name, result.country)
            if (alreadySaved) return@launch
            weatherRepository.addFavourite(
                FavouriteCity(
                    cityName = result.name,
                    country = result.country,
                    lat = result.lat,
                    lon = result.lon
                )
            )
            streakRepository.refreshExplorerBadge() // Explorer badge unlocks at 5 favourites (R7)
        }
    }

    private fun openForecastFor(result: GeoResult) {
        val intent = Intent(this, ForecastActivity::class.java).apply {
            putExtra(Constants.EXTRA_LAT, result.lat)
            putExtra(Constants.EXTRA_LON, result.lon)
            putExtra(Constants.EXTRA_CITY_NAME, "${result.name}, ${result.country}")
        }
        startActivity(intent)
    }
}
