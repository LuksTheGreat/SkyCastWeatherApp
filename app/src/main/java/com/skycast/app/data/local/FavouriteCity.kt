package com.skycast.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local Room representation of a saved city (Part 1, R6).
 * Kept per-device (no account syncing in this MVP) — see Part 1 §5/§6 for the rationale.
 */
@Entity(tableName = "favourite_city")
data class FavouriteCity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cityName: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    /** Last temperature fetched for this city, cached for the Favourites list (R8 offline resilience). */
    val lastKnownTempC: Double? = null,
    val lastUpdatedEpochMs: Long = 0L
)
