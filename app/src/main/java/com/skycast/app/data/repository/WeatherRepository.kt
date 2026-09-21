package com.skycast.app.data.repository

import android.content.Context
import android.util.Log
import com.skycast.app.data.local.AppDatabase
import com.skycast.app.data.local.FavouriteCity
import com.skycast.app.data.remote.RetrofitClient
import com.skycast.app.data.remote.model.CurrentWeatherResponse
import com.skycast.app.data.remote.model.ForecastDay
import com.skycast.app.data.remote.model.GeoResult
import com.skycast.app.util.Constants
import com.skycast.app.util.ForecastAggregator
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for weather + favourites data. Mirrors the
 * WeatherRepository shown in Part 1's system architecture diagram
 * (getCurrentWeather, getForecast, saveFavourite, getFavourites, updateStreak).
 *
 * UI code never talks to Retrofit or Room directly — everything goes through here,
 * which keeps the Activities small and makes the underlying logic (aggregation,
 * caching) independently testable.
 */
class WeatherRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val api = RetrofitClient.service

    // ---- Remote (OpenWeatherMap) ----

    suspend fun getCurrentWeatherByCoords(lat: Double, lon: Double): CurrentWeatherResponse {
        Log.d(Constants.LOG_TAG, "Fetching current weather for lat=$lat, lon=$lon")
        return api.getCurrentWeather(lat = lat, lon = lon, apiKey = RetrofitClient.apiKey)
    }

    suspend fun getCurrentWeatherByCity(cityName: String, country: String): CurrentWeatherResponse {
        Log.d(Constants.LOG_TAG, "Fetching current weather for city=$cityName,$country")
        return api.getCurrentWeather(cityQuery = "$cityName,$country", apiKey = RetrofitClient.apiKey)
    }

    suspend fun getForecast(lat: Double, lon: Double): List<ForecastDay> {
        val response = api.getForecast(lat = lat, lon = lon, apiKey = RetrofitClient.apiKey)
        return ForecastAggregator.aggregate(response.entries)
    }

    suspend fun searchCities(query: String): List<GeoResult> {
        if (query.isBlank()) return emptyList()
        return api.searchCity(cityName = query, apiKey = RetrofitClient.apiKey)
    }

    // ---- Local (Room) — favourites ----

    fun observeFavourites(): Flow<List<FavouriteCity>> = db.favouriteCityDao().observeAll()

    suspend fun favouritesCount(): Int = db.favouriteCityDao().count()

    suspend fun isFavourite(cityName: String, country: String): Boolean =
        db.favouriteCityDao().isFavourite(cityName, country)

    suspend fun addFavourite(city: FavouriteCity): Long {
        Log.d(Constants.LOG_TAG, "Saving favourite: ${city.cityName}, ${city.country}")
        return db.favouriteCityDao().insert(city)
    }

    suspend fun updateFavouriteTemp(city: FavouriteCity, tempC: Double) {
        db.favouriteCityDao().update(
            city.copy(lastKnownTempC = tempC, lastUpdatedEpochMs = System.currentTimeMillis())
        )
    }

    suspend fun removeFavourite(city: FavouriteCity) {
        Log.d(Constants.LOG_TAG, "Removing favourite: ${city.cityName}")
        db.favouriteCityDao().delete(city)
    }

    suspend fun removeFavouriteByCityCountry(cityName: String, country: String) {
        Log.d(Constants.LOG_TAG, "Removing favourite: $cityName")
        db.favouriteCityDao().deleteByCityCountry(cityName, country)
    }
}
