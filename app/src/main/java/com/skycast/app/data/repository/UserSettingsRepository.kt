package com.skycast.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.skycast.app.util.Constants
import kotlinx.coroutines.tasks.await

data class UserSettings(
    val units: String = Constants.UNITS_METRIC,
    val darkTheme: Boolean = false
)

/**
 * Reads/writes the signed-in user's settings document in Cloud Firestore —
 * the internet-hosted database that satisfies the "API & Database... must be
 * hosted online" requirement. Falling back gracefully if the user is offline
 * (Firestore has its own local cache, so this also supports R8-style resilience).
 */
class UserSettingsRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun userDoc() = auth.currentUser?.uid?.let { firestore.collection("users").document(it) }

    suspend fun getSettings(): UserSettings {
        val doc = userDoc() ?: return UserSettings()
        return try {
            val snapshot = doc.get().await()
            UserSettings(
                units = snapshot.getString("units") ?: Constants.UNITS_METRIC,
                darkTheme = snapshot.getBoolean("darkTheme") ?: false
            )
        } catch (e: Exception) {
            Log.w(Constants.LOG_TAG, "Could not fetch settings, using defaults", e)
            UserSettings()
        }
    }

    suspend fun updateUnits(units: String) {
        Log.d(Constants.LOG_TAG, "Updating units preference to $units")
        userDoc()?.set(mapOf("units" to units), com.google.firebase.firestore.SetOptions.merge())?.await()
    }

    suspend fun updateDarkTheme(enabled: Boolean) {
        Log.d(Constants.LOG_TAG, "Updating dark theme preference to $enabled")
        userDoc()?.set(mapOf("darkTheme" to enabled), com.google.firebase.firestore.SetOptions.merge())?.await()
    }
}
