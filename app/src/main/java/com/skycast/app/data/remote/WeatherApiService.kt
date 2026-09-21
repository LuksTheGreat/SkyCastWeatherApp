package com.skycast.app.data.remote

import com.skycast.app.data.remote.model.CurrentWeatherResponse
import com.skycast.app.data.remote.model.ForecastResponse
import com.skycast.app.data.remote.model.GeoResult
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit definition of the OpenWeatherMap REST endpoints used by SkyCast
 * (Part 1, R1). Retrofit turns each function into an HTTP GET call and Gson
 * deserialises the JSON response into the matching data class.
 *
 * Reference: Square Inc. (2026) Retrofit Documentation. https://square.github.io/retrofit/
 */
interface WeatherApiService {

    /** "Current Weather Data" endpoint, looked up by exact coordinates or city name. */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("q") cityQuery: String? = null,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): CurrentWeatherResponse

    /** "5 Day / 3 Hour Forecast" endpoint. */
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null,
        @Query("q") cityQuery: String? = null,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): ForecastResponse

    /** Geocoding endpoint used by the Search screen (R2) to resolve a typed city name. */
    @GET("https://api.openweathermap.org/geo/1.0/direct")
    suspend fun searchCity(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<GeoResult>
}
