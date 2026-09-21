package com.skycast.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skycast.app.data.repository.AuthRepository
import com.skycast.app.databinding.ActivityLoginBinding
import com.skycast.app.ui.home.HomeActivity
import com.skycast.app.util.Constants
import kotlinx.coroutines.launch

/**
 * User authentication — login (Part 2 requirement: "Registration and login
 * functionality with password encryption"). Encryption itself is handled by
 * Firebase Auth server-side; see [AuthRepository] for details. This Activity's
 * job is input validation and a crash-free UI (invalid inputs never throw).
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener { attemptLogin() }
        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        if (!isValid(email, password)) return

        setLoading(true)
        lifecycleScope.launch {
            val result = authRepository.login(email, password)
            setLoading(false)
            result
                .onSuccess {
                    Log.i(Constants.LOG_TAG, "Login succeeded for $email")
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                }
                .onFailure { e ->
                    Log.w(Constants.LOG_TAG, "Login failed for $email: ${e.message}")
                    binding.etPassword.error = "Login failed: ${e.message ?: "check your details and try again"}"
                }
        }
    }

    /** Defensive input validation so malformed input never crashes the app. */
    private fun isValid(email: String, password: String): Boolean {
        var ok = true
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email address"
            ok = false
        }
        if (password.isBlank()) {
            binding.etPassword.error = "Enter your password"
            ok = false
        }
        return ok
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnLogin.isEnabled = !loading
    }
}
