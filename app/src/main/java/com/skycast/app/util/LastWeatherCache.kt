package com.skycast.app.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Tiny local cache for the Home screen's last successful weather fetch
 * (Part 1, R8: "the app will keep the most recently downloaded weather
 * information locally"). Deliberately separate from Room's FavouriteCity
 * table, since this caches whatever city/location the user last viewed —
 * favourite or not.
 */
class LastWeatherCache(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    data class CachedWeather(
        val cityName: String,
        val country: String,
        val tempC: Double,
        val feelsLikeC: Double,
        val humidity: Int,
        val windKmh: Double,
        val condition: String,
        val lat: Double,
        val lon: Double,
        val updatedAtMs: Long
    )

    fun save(weather: CachedWeather) {
        prefs.edit()
            .putString("cache_city", weather.cityName)
            .putString("cache_country", weather.country)
            .putFloat("cache_temp", weather.tempC.toFloat())
            .putFloat("cache_feels_like", weather.feelsLikeC.toFloat())
            .putInt("cache_humidity", weather.humidity)
            .putFloat("cache_wind", weather.windKmh.toFloat())
            .putString("cache_condition", weather.condition)
            .putFloat("cache_lat", weather.lat.toFloat())
            .putFloat("cache_lon", weather.lon.toFloat())
            .putLong("cache_updated_at", weather.updatedAtMs)
            .apply()
    }

    fun load(): CachedWeather? {
        val city = prefs.getString("cache_city", null) ?: return null
        return CachedWeather(
            cityName = city,
            country = prefs.getString("cache_country", "") ?: "",
            tempC = prefs.getFloat("cache_temp", 0f).toDouble(),
            feelsLikeC = prefs.getFloat("cache_feels_like", 0f).toDouble(),
            humidity = prefs.getInt("cache_humidity", 0),
            windKmh = prefs.getFloat("cache_wind", 0f).toDouble(),
            condition = prefs.getString("cache_condition", "") ?: "",
            lat = prefs.getFloat("cache_lat", 0f).toDouble(),
            lon = prefs.getFloat("cache_lon", 0f).toDouble(),
            updatedAtMs = prefs.getLong("cache_updated_at", 0L)
        )
    }
}
