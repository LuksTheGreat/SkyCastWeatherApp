package com.skycast.app.ui.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.skycast.app.databinding.ActivitySplashBinding
import com.skycast.app.ui.auth.LoginActivity
import com.skycast.app.ui.home.HomeActivity
import com.skycast.app.util.Constants

/**
 * "Splash Screen (loading + permission check)" from Part 1's navigation
 * diagram. Decides whether to route to Login/Register or straight to Home,
 * based on whether Firebase already has a signed-in user.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.postDelayed({ routeNext() }, 600)
    }

    private fun routeNext() {
        val user = FirebaseAuth.getInstance().currentUser
        Log.d(Constants.LOG_TAG, "Splash routing, signed-in user = ${user?.email}")
        val destination = if (user != null) HomeActivity::class.java else LoginActivity::class.java
        startActivity(Intent(this, destination))
        finish()
    }
}
