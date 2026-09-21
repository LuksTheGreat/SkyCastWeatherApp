package com.skycast.app.data.remote.model

import com.google.gson.annotations.SerializedName

/**
 * Maps the JSON body returned by OpenWeatherMap's "5 Day / 3 Hour Forecast" endpoint.
 * Docs: https://openweathermap.org/forecast5
 *
 * The API returns a flat list of 3-hour readings ("list"); SkyCast's
 * [com.skycast.app.util.ForecastAggregator] groups these by calendar day (Part 1, R5).
 */
data class ForecastResponse(
    @SerializedName("city") val city: City,
    @SerializedName("list") val entries: List<ForecastEntry>
) {
    data class City(@SerializedName("name") val name: String, @SerializedName("country") val country: String)

    data class ForecastEntry(
        @SerializedName("dt") val timestamp: Long,
        @SerializedName("dt_txt") val dateTimeText: String,
        @SerializedName("main") val main: Main,
        @SerializedName("weather") val weather: List<CurrentWeatherResponse.WeatherCondition>
    )

    data class Main(
        @SerializedName("temp_min") val tempMin: Double,
        @SerializedName("temp_max") val tempMax: Double
    )
}

/**
 * A single aggregated day in the 5-day forecast, produced by [com.skycast.app.util.ForecastAggregator]
 * from the raw 3-hour [ForecastResponse] entries. This is the shape the UI actually binds to.
 */
data class ForecastDay(
    val date: String,       // ISO 8601 (yyyy-MM-dd)
    val dayLabel: String,   // e.g. "Mon"
    val minTemp: Double,
    val maxTemp: Double,
    val icon: String,
    val condition: String
)
