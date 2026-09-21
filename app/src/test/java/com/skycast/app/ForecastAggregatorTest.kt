package com.skycast.app

import com.skycast.app.data.remote.model.CurrentWeatherResponse
import com.skycast.app.data.remote.model.ForecastResponse
import com.skycast.app.util.ForecastAggregator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

/**
 * Unit tests for [ForecastAggregator] (Part 1, R5 / Part 2 "detailed unit testing").
 * Entries are built by hand with known UTC timestamps so the expected min/max
 * and representative-day values can be asserted precisely.
 */
class ForecastAggregatorTest {

    private fun utcTimestamp(day: Int, hour: Int): Long {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(2026, Calendar.SEPTEMBER, day, hour, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis / 1000L
    }

    private fun entry(day: Int, hour: Int, min: Double, max: Double, desc: String = "clear sky", icon: String = "01d") =
        ForecastResponse.ForecastEntry(
            timestamp = utcTimestamp(day, hour),
            dateTimeText = "2026-09-$day $hour:00:00",
            main = ForecastResponse.Main(tempMin = min, tempMax = max),
            weather = listOf(CurrentWeatherResponse.WeatherCondition("Clear", desc, icon))
        )

    @Test
    fun `empty input returns empty list`() {
        val result = ForecastAggregator.aggregate(emptyList())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `groups multiple 3-hour readings into a single day entry`() {
        val entries = listOf(
            entry(day = 1, hour = 0, min = 12.0, max = 14.0),
            entry(day = 1, hour = 3, min = 11.0, max = 13.0),
            entry(day = 1, hour = 6, min = 13.0, max = 15.0),
            entry(day = 1, hour = 9, min = 16.0, max = 20.0),
            entry(day = 1, hour = 12, min = 20.0, max = 27.0),
            entry(day = 1, hour = 15, min = 18.0, max = 25.0),
            entry(day = 1, hour = 18, min = 15.0, max = 19.0),
            entry(day = 1, hour = 21, min = 13.0, max = 15.0)
        )

        val result = ForecastAggregator.aggregate(entries)

        assertEquals(1, result.size)
        assertEquals(11.0, result[0].minTemp, 0.001)
        assertEquals(27.0, result[0].maxTemp, 0.001)
    }

    @Test
    fun `produces one entry per calendar day, ordered and capped at maxDays`() {
        val entries = (1..6).flatMap { day ->
            listOf(
                entry(day, 0, min = 10.0 + day, max = 20.0 + day),
                entry(day, 12, min = 12.0 + day, max = 22.0 + day)
            )
        }

        val result = ForecastAggregator.aggregate(entries, maxDays = 5)

        assertEquals(5, result.size)
        // Day 1 should come first (min/max should reflect day 1's values: 11 / 21)
        assertEquals(11.0, result[0].minTemp, 0.001)
        assertEquals(21.0, result[0].maxTemp, 0.001)
    }

    @Test
    fun `representative condition comes from the reading closest to midday`() {
        val entries = listOf(
            entry(day = 1, hour = 0, min = 10.0, max = 12.0, desc = "clear sky"),
            entry(day = 1, hour = 12, min = 15.0, max = 25.0, desc = "light rain"),
            entry(day = 1, hour = 21, min = 11.0, max = 13.0, desc = "clear sky")
        )

        val result = ForecastAggregator.aggregate(entries)

        assertEquals("Light rain", result[0].condition)
    }

    @Test
    fun `capitalises the first letter of the condition description`() {
        val entries = listOf(entry(day = 1, hour = 12, min = 10.0, max = 20.0, desc = "scattered clouds"))
        val result = ForecastAggregator.aggregate(entries)
        assertEquals("Scattered clouds", result[0].condition)
    }
}
