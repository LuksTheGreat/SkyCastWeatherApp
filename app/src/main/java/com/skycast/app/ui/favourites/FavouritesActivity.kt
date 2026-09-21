package com.skycast.app.ui.favourites

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skycast.app.data.local.FavouriteCity
import com.skycast.app.data.repository.StreakRepository
import com.skycast.app.data.repository.WeatherRepository
import com.skycast.app.databinding.ActivityFavouritesBinding
import com.skycast.app.ui.forecast.ForecastActivity
import com.skycast.app.util.Constants
import kotlinx.coroutines.launch

/**
 * "Favourites Screen (saved cities)" from Part 1's navigation diagram (R6).
 * Backed by Room via [WeatherRepository.observeFavourites], so the list
 * updates live as cities are added/removed from Search.
 */
class FavouritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavouritesBinding
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var adapter: FavouritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavouritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Favourites"

        weatherRepository = WeatherRepository(applicationContext)
        streakRepository = StreakRepository(applicationContext)

        adapter = FavouritesAdapter(
            onCityClicked = { city -> openForecast(city) },
            onRemoveClicked = { city -> removeFavourite(city) }
        )
        binding.rvFavourites.layoutManager = LinearLayoutManager(this)
        binding.rvFavourites.adapter = adapter

        observeFavourites()
    }

    private fun observeFavourites() {
        lifecycleScope.launch {
            weatherRepository.observeFavourites().collect { list ->
                adapter.submitList(list)
                binding.tvEmpty.visibility = if (list.isEmpty()) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
                refreshTemps(list)
            }
        }
    }

    /** Best-effort background refresh of each favourite's at-a-glance temperature. */
    private fun refreshTemps(cities: List<FavouriteCity>) {
        lifecycleScope.launch {
            for (city in cities) {
                try {
                    val weather = weatherRepository.getCurrentWeatherByCity(city.cityName, city.country)
                    weatherRepository.updateFavouriteTemp(city, weather.main.temp)
                } catch (e: Exception) {
                    Log.w(Constants.LOG_TAG, "Could not refresh temp for ${city.cityName}, using cached value", e)
                }
            }
        }
    }

    private fun removeFavourite(city: FavouriteCity) {
        lifecycleScope.launch {
            weatherRepository.removeFavourite(city)
            streakRepository.refreshExplorerBadge()
        }
    }

    private fun openForecast(city: FavouriteCity) {
        val intent = Intent(this, ForecastActivity::class.java).apply {
            putExtra(Constants.EXTRA_LAT, city.lat)
            putExtra(Constants.EXTRA_LON, city.lon)
            putExtra(Constants.EXTRA_CITY_NAME, "${city.cityName}, ${city.country}")
        }
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
