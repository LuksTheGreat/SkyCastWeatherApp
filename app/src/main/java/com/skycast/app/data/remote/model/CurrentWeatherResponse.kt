package com.skycast.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Maps the JSON body returned by OpenWeatherMap's "Current Weather Data" endpoint.
 * Docs: https://openweathermap.org/current
 *
 * Only the fields SkyCast actually uses are declared; Gson silently ignores
 * everything else in the payload.
 */
data class CurrentWeatherResponse(
    @SerializedName("name") val cityName: String,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("main") val main: Main,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("dt") val timestamp: Long
) {
    data class Sys(@SerializedName("country") val country: String?)

    data class Main(
        @SerializedName("temp") val temp: Double,
        @SerializedName("feels_like") val feelsLike: Double,
        @SerializedName("humidity") val humidity: Int
    )

    data class Wind(@SerializedName("speed") val speedMetersPerSecond: Double) {
        /** OpenWeatherMap returns wind speed in m/s for metric units; SkyCast displays km/h. */
        val speedKmh: Double get() = speedMetersPerSecond * 3.6
    }

    data class WeatherCondition(
        @SerializedName("main") val main: String,
        @SerializedName("description") val description: String,
        @SerializedName("icon") val icon: String
    )
}
