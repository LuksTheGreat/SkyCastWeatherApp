package com.skycast.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Maps OpenWeatherMap's Geocoding API, used by the Search screen (Part 1, R2) to resolve
 * a typed city name into candidate locations, each carrying a country code so that
 * cities sharing a name (e.g. multiple "Springfield"s) can be told apart.
 * Docs: https://openweathermap.org/api/geocoding-api
 */
data class GeoResult(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lon") val lon: Double,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String? = null
)
