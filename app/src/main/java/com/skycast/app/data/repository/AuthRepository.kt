package com.skycast.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.skycast.app.util.Constants
import kotlinx.coroutines.tasks.await

/**
 * Handles registration and login via Firebase Authentication.
 *
 * Password encryption: SkyCast never stores or transmits raw passwords itself.
 * Firebase Authentication sends the password over TLS and stores only a
 * salted hash (using the industry-standard scrypt-based algorithm) in Google's
 * infrastructure — SkyCast's own backend/database never sees the plaintext
 * password at all. This satisfies the "password encryption" requirement while
 * following the recommended practice of not rolling your own auth/crypto.
 * Docs: https://firebase.google.com/docs/auth/android/password-auth
 */
class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun register(email: String, password: String): Result<FirebaseUser> = try {
        Log.d(Constants.LOG_TAG, "Registering new user: $email")
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Registration succeeded but no user returned")

        // Create a default settings document for this user in Firestore
        // (the hosted, internet-based database backing the app).
        firestore.collection("users").document(user.uid)
            .set(
                mapOf(
                    "email" to email,
                    "units" to Constants.UNITS_METRIC,
                    "darkTheme" to false,
                    "createdAt" to System.currentTimeMillis()
                )
            ).await()

        Result.success(user)
    } catch (e: Exception) {
        Log.e(Constants.LOG_TAG, "Registration failed", e)
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = try {
        Log.d(Constants.LOG_TAG, "Logging in user: $email")
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val user = result.user ?: throw IllegalStateException("Login succeeded but no user returned")
        Result.success(user)
    } catch (e: Exception) {
        Log.e(Constants.LOG_TAG, "Login failed", e)
        Result.failure(e)
    }

    fun logout() {
        Log.d(Constants.LOG_TAG, "Logging out user: ${auth.currentUser?.email}")
        auth.signOut()
    }
}
