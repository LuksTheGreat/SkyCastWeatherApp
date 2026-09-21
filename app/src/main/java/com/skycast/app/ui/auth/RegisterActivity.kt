package com.skycast.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skycast.app.data.repository.AuthRepository
import com.skycast.app.databinding.ActivityRegisterBinding
import com.skycast.app.ui.home.HomeActivity
import com.skycast.app.util.Constants
import kotlinx.coroutines.launch

/**
 * User authentication — registration (Part 2 requirement). Creates a Firebase
 * Auth account (password is hashed/encrypted server-side by Firebase — see
 * [AuthRepository]) and a matching Firestore settings document for the user.
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Register"

        binding.btnRegister.setOnClickListener { attemptRegister() }
        binding.tvGoLogin.setOnClickListener { finish() }
    }

    private fun attemptRegister() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()
        val confirm = binding.etConfirmPassword.text?.toString().orEmpty()

        if (!isValid(email, password, confirm)) return

        setLoading(true)
        lifecycleScope.launch {
            val result = authRepository.register(email, password)
            setLoading(false)
            result
                .onSuccess {
                    Log.i(Constants.LOG_TAG, "Registration succeeded for $email")
                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                    finish()
                }
                .onFailure { e ->
                    Log.w(Constants.LOG_TAG, "Registration failed for $email: ${e.message}")
                    binding.etEmail.error = "Registration failed: ${e.message ?: "please try again"}"
                }
        }
    }

    /** Defensive input validation — malformed or empty fields never crash the app. */
    private fun isValid(email: String, password: String, confirm: String): Boolean {
        var ok = true
        if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email address"
            ok = false
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            ok = false
        }
        if (confirm != password) {
            binding.etConfirmPassword.error = "Passwords do not match"
            ok = false
        }
        return ok
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnRegister.isEnabled = !loading
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
