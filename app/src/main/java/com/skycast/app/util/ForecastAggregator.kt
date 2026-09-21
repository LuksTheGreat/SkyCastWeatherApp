package com.skycast.app.util

import com.skycast.app.data.remote.model.ForecastDay
import com.skycast.app.data.remote.model.ForecastResponse
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Groups the raw 3-hour interval readings returned by OpenWeatherMap's forecast
 * endpoint into one summarised entry per calendar day (Part 1, R5).
 *
 * This is deliberately a small, pure (no Android framework, no I/O) object so
 * it can be exercised directly and thoroughly by JUnit tests (Part 2 requirement:
 * "conduct detailed unit testing").
 */
object ForecastAggregator {

    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    private val dayLabelFormat = SimpleDateFormat("EEE", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    /**
     * Aggregates raw forecast entries into per-day min/max buckets.
     *
     * The representative icon/condition for a day is taken from the reading
     * closest to midday (12:00), since that best reflects the "typical"
     * daytime weather rather than an early-morning or late-night reading.
     *
     * @param maxDays caps the number of days returned (OpenWeatherMap's free
     *   5-day/3-hour endpoint can span a partial 6th day at the boundary).
     */
    fun aggregate(entries: List<ForecastResponse.ForecastEntry>, maxDays: Int = 5): List<ForecastDay> {
        if (entries.isEmpty()) return emptyList()

        val byDay = LinkedHashMap<String, MutableList<ForecastResponse.ForecastEntry>>()
        for (entry in entries) {
            val dayKey = dayKeyFormat.format(entry.timestamp * 1000L)
            byDay.getOrPut(dayKey) { mutableListOf() }.add(entry)
        }

        return byDay.entries.take(maxDays).map { (dayKey, dayEntries) ->
            val minTemp = dayEntries.minOf { it.main.tempMin }
            val maxTemp = dayEntries.maxOf { it.main.tempMax }
            val representative = representativeEntry(dayEntries)
            val condition = representative.weather.firstOrNull()?.description?.replaceFirstChar {
                it.uppercase()
            } ?: ""
            val icon = representative.weather.firstOrNull()?.icon ?: ""

            ForecastDay(
                date = dayKey,
                dayLabel = dayLabelFormat.format(representative.timestamp * 1000L),
                minTemp = minTemp,
                maxTemp = maxTemp,
                icon = icon,
                condition = condition
            )
        }
    }

    /** Picks the reading whose hour-of-day (UTC) is closest to noon. */
    private fun representativeEntry(
        dayEntries: List<ForecastResponse.ForecastEntry>
    ): ForecastResponse.ForecastEntry {
        val hourFormat = SimpleDateFormat("HH", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return dayEntries.minByOrNull { entry ->
            val hour = hourFormat.format(entry.timestamp * 1000L).toInt()
            kotlin.math.abs(hour - 12)
        } ?: dayEntries.first()
    }
}
