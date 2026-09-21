package com.skycast.app.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.skycast.app.data.repository.AuthRepository
import com.skycast.app.data.repository.UserSettingsRepository
import com.skycast.app.databinding.ActivitySettingsBinding
import com.skycast.app.ui.auth.LoginActivity
import com.skycast.app.util.Constants
import kotlinx.coroutines.launch

/**
 * "Settings Screen (units, theme)" from Part 1's navigation diagram, extended
 * for Part 2 with account info and logout. Preferences are written to the
 * user's Firestore document via [UserSettingsRepository] — the hosted,
 * internet-based database backing the app.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val settingsRepository = UserSettingsRepository()
    private val authRepository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        binding.tvAccountEmail.text = "Signed in as ${FirebaseAuth.getInstance().currentUser?.email ?: "unknown"}"

        loadSettings()

        binding.switchUnits.setOnCheckedChangeListener { _, isChecked ->
            val units = if (isChecked) Constants.UNITS_IMPERIAL else Constants.UNITS_METRIC
            lifecycleScope.launch { settingsRepository.updateUnits(units) }
        }
        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch { settingsRepository.updateDarkTheme(isChecked) }
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                if (isChecked) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        binding.btnLogout.setOnClickListener {
            authRepository.logout()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }
    }

    private fun loadSettings() {
        lifecycleScope.launch {
            val settings = settingsRepository.getSettings()
            binding.switchUnits.isChecked = settings.units == Constants.UNITS_IMPERIAL
            binding.switchTheme.isChecked = settings.darkTheme
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
