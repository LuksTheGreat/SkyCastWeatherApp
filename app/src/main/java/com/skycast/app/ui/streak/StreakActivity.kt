package com.skycast.app.ui.streak

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.skycast.app.R
import com.skycast.app.data.repository.StreakRepository
import com.skycast.app.databinding.ActivityStreakBinding
import com.skycast.app.util.StreakManager
import kotlinx.coroutines.launch

/**
 * "Streaks & Badges Screen" from Part 1's navigation diagram (R7).
 * Highlights each badge once its unlock condition (streak length or
 * favourites count) has been met.
 */
class StreakActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStreakBinding
    private lateinit var streakRepository: StreakRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStreakBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Streaks & Badges"

        streakRepository = StreakRepository(applicationContext)

        lifecycleScope.launch {
            streakRepository.observeStreak().collect { record ->
                binding.tvStreakCount.text = "${record.currentStreak}-Day Streak"
                updateBadge(binding.badge3day, StreakManager.BADGE_3_DAY, record.unlockedBadgeIds)
                updateBadge(binding.badge7day, StreakManager.BADGE_7_DAY, record.unlockedBadgeIds)
                updateBadge(binding.badge30day, StreakManager.BADGE_30_DAY, record.unlockedBadgeIds)
                updateBadge(binding.badgeExplorer, StreakManager.BADGE_EXPLORER, record.unlockedBadgeIds)
            }
        }
    }

    private fun updateBadge(view: android.widget.TextView, badgeId: String, unlocked: List<String>) {
        val isUnlocked = unlocked.contains(badgeId)
        view.setBackgroundColor(
            ContextCompat.getColor(this, if (isUnlocked) R.color.flame_accent else R.color.badge_locked)
        )
        view.alpha = if (isUnlocked) 1.0f else 0.6f
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
