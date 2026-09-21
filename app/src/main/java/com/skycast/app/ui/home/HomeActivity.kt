package com.skycast.app.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.skycast.app.R
import com.skycast.app.data.remote.model.CurrentWeatherResponse
import com.skycast.app.data.repository.StreakRepository
import com.skycast.app.data.repository.WeatherRepository
import com.skycast.app.databinding.ActivityHomeBinding
import com.skycast.app.ui.auth.LoginActivity
import com.skycast.app.ui.favourites.FavouritesActivity
import com.skycast.app.ui.forecast.ForecastActivity
import com.skycast.app.ui.search.SearchActivity
import com.skycast.app.ui.settings.SettingsActivity
import com.skycast.app.ui.streak.StreakActivity
import com.skycast.app.util.Constants
import com.skycast.app.util.LastWeatherCache
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * "Home Screen (current location weather)" from Part 1's navigation diagram.
 * Shows current conditions for the device's GPS location (R3/R4), falls back
 * to the last cached reading when offline (R8), and records the day's
 * gamification check-in (R7) once weather has loaded successfully.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var streakRepository: StreakRepository
    private lateinit var cache: LastWeatherCache

    private var lastLat: Double? = null
    private var lastLon: Double? = null

    private val locationPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadWeatherFromGps() else loadFallbackOrCache()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseAuth.getInstance().currentUser == null) {
            // Defensive guard: never show Home without a signed-in user.
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        weatherRepository = WeatherRepository(applicationContext)
        streakRepository = StreakRepository(applicationContext)
        cache = LastWeatherCache(applicationContext)

        setupBottomNav()
        binding.swipeRefresh.setOnRefreshListener { requestWeatherRefresh() }
        binding.btnViewForecast.setOnClickListener { openForecast() }
        binding.btnStreak.setOnClickListener {
            startActivity(Intent(this, StreakActivity::class.java))
        }

        observeStreak()
        requestWeatherRefresh()
    }

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.nav_home
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_search -> {
                    startActivity(Intent(this, SearchActivity::class.java)); true
                }
                R.id.nav_favourites -> {
                    startActivity(Intent(this, FavouritesActivity::class.java)); true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java)); true
                }
                else -> false
            }
        }
    }

    private fun requestWeatherRefresh() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            loadWeatherFromGps()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun loadWeatherFromGps() {
        binding.swipeRefresh.isRefreshing = true
        lifecycleScope.launch {
            try {
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY, null
                ).await()

                if (location == null) {
                    Log.w(Constants.LOG_TAG, "Fused location returned null, falling back to cache")
                    loadFallbackOrCache()
                    return@launch
                }

                lastLat = location.latitude
                lastLon = location.longitude
                val weather = weatherRepository.getCurrentWeatherByCoords(location.latitude, location.longitude)
                onWeatherLoaded(weather, location.latitude, location.longitude)
            } catch (e: Exception) {
                Log.e(Constants.LOG_TAG, "Failed to load GPS weather, falling back to cache", e)
                loadFallbackOrCache()
            } finally {
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    /** Called when there's no location permission, or the GPS/network fetch failed. */
    private fun loadFallbackOrCache() {
        binding.swipeRefresh.isRefreshing = false
        val cached = cache.load()
        if (cached != null) {
            binding.tvOffline.visibility = android.view.View.VISIBLE
            binding.tvCityName.text = "${cached.cityName}, ${cached.country}"
            binding.tvTemperature.text = "${cached.tempC.toInt()}°C"
            binding.tvConditions.text = cached.condition
            binding.tvHumidity.text = getString(R.string.humidity_label, cached.humidity)
            binding.tvWind.text = getString(R.string.wind_label, cached.windKmh)
            binding.tvFeelsLike.text = getString(R.string.feels_like_label, cached.feelsLikeC)
            binding.tvLastUpdated.text = getString(
                R.string.last_updated_label,
                formatTimestamp(cached.updatedAtMs)
            )
            lastLat = cached.lat
            lastLon = cached.lon
        } else {
            binding.tvCityName.text = "No data available"
            binding.tvConditions.text = "Grant location access or search for a city"
        }
    }

    private fun onWeatherLoaded(weather: CurrentWeatherResponse, lat: Double, lon: Double) {
        binding.tvOffline.visibility = android.view.View.GONE
        binding.tvCityName.text = "${weather.cityName}, ${weather.sys.country.orEmpty()}"
        binding.tvTemperature.text = "${weather.main.temp.toInt()}°C"
        binding.tvConditions.text = weather.weather.firstOrNull()?.description
            ?.replaceFirstChar { it.uppercase() } ?: ""
        binding.tvHumidity.text = getString(R.string.humidity_label, weather.main.humidity)
        binding.tvWind.text = getString(R.string.wind_label, weather.wind.speedKmh)
        binding.tvFeelsLike.text = getString(R.string.feels_like_label, weather.main.feelsLike)
        val now = System.currentTimeMillis()
        binding.tvLastUpdated.text = getString(R.string.last_updated_label, formatTimestamp(now))

        cache.save(
            LastWeatherCache.CachedWeather(
                cityName = weather.cityName,
                country = weather.sys.country.orEmpty(),
                tempC = weather.main.temp,
                feelsLikeC = weather.main.feelsLike,
                humidity = weather.main.humidity,
                windKmh = weather.wind.speedKmh,
                condition = weather.weather.firstOrNull()?.description.orEmpty(),
                lat = lat,
                lon = lon,
                updatedAtMs = now
            )
        )

        // Gamification: a successful weather check counts as today's check-in (R7).
        lifecycleScope.launch { streakRepository.checkInToday() }
    }

    private fun observeStreak() {
        lifecycleScope.launch {
            streakRepository.observeStreak().collect { record ->
                binding.btnStreak.text = getString(R.string.streak_banner, record.currentStreak)
            }
        }
    }

    private fun openForecast() {
        val lat = lastLat
        val lon = lastLon
        if (lat == null || lon == null) return
        val intent = Intent(this, ForecastActivity::class.java).apply {
            putExtra(Constants.EXTRA_LAT, lat)
            putExtra(Constants.EXTRA_LON, lon)
            putExtra(Constants.EXTRA_CITY_NAME, binding.tvCityName.text.toString())
        }
        startActivity(intent)
    }

    private fun formatTimestamp(epochMs: Long): String =
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(epochMs))

    override fun onResume() {
        super.onResume()
        // Refresh streak badges in case a favourite was added/removed elsewhere.
        lifecycleScope.launch { streakRepository.refreshExplorerBadge() }
    }
}
