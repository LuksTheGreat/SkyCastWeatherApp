package com.skycast.app.ui.forecast

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.skycast.app.data.repository.WeatherRepository
import com.skycast.app.databinding.ActivityForecastBinding
import com.skycast.app.util.Constants
import kotlinx.coroutines.launch

/**
 * "5-Day Forecast Screen" from Part 1's navigation diagram (R5).
 * The heavy lifting (grouping 3-hour readings into daily min/max) happens in
 * [com.skycast.app.util.ForecastAggregator], called from [WeatherRepository.getForecast].
 */
class ForecastActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForecastBinding
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var adapter: ForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForecastBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weatherRepository = WeatherRepository(applicationContext)
        adapter = ForecastAdapter()
        binding.rvForecast.layoutManager = LinearLayoutManager(this)
        binding.rvForecast.adapter = adapter

        val cityLabel = intent.getStringExtra(Constants.EXTRA_CITY_NAME) ?: "Forecast"
        binding.tvCityName.text = "5-Day Forecast — $cityLabel"

        val lat = intent.getDoubleExtra(Constants.EXTRA_LAT, Double.NaN)
        val lon = intent.getDoubleExtra(Constants.EXTRA_LON, Double.NaN)

        if (lat.isNaN() || lon.isNaN()) {
            binding.tvCityName.text = "Forecast unavailable"
            return
        }

        loadForecast(lat, lon)
    }

    private fun loadForecast(lat: Double, lon: Double) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        lifecycleScope.launch {
            try {
                val days = weatherRepository.getForecast(lat, lon)
                adapter.submitList(days)
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, "Failed to load forecast", e)
                binding.tvCityName.text = "Could not load forecast — check your connection"
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
}
