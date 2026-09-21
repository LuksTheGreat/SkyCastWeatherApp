package com.skycast.app.util

/** Shared Intent-extra keys and SharedPreferences keys used across screens. */
object Constants {
    const val EXTRA_CITY_NAME = "extra_city_name"
    const val EXTRA_COUNTRY = "extra_country"
    const val EXTRA_LAT = "extra_lat"
    const val EXTRA_LON = "extra_lon"

    const val PREFS_NAME = "skycast_prefs"
    const val PREF_UNITS = "pref_units"
    const val PREF_DARK_THEME = "pref_dark_theme"
    const val PREF_LOCATION_GRANTED = "pref_location_granted"

    const val UNITS_METRIC = "metric"
    const val UNITS_IMPERIAL = "imperial"

    const val LOG_TAG = "SkyCast"
}
