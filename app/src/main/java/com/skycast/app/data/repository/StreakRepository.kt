package com.skycast.app.data.repository

import android.content.Context
import android.util.Log
import com.skycast.app.data.local.AppDatabase
import com.skycast.app.data.local.StreakRecord
import com.skycast.app.util.Constants
import com.skycast.app.util.StreakManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Bridges the pure [StreakManager] logic to persistent storage (Part 1, R7).
 * Called once per app-open / weather refresh from the Home screen.
 */
class StreakRepository(context: Context) {

    private val streakDao = AppDatabase.getInstance(context).streakDao()
    private val favouriteDao = AppDatabase.getInstance(context).favouriteCityDao()

    fun observeStreak(): Flow<StreakRecord> =
        streakDao.observe().map { it ?: StreakRecord() }

    /**
     * Records today's check-in, updates the streak count and re-evaluates
     * which badges are unlocked. Safe to call multiple times per day.
     */
    suspend fun checkInToday(): StreakRecord {
        val existing = streakDao.getOnce() ?: StreakRecord()
        val updatedState = StreakManager.checkIn(
            previous = StreakManager.StreakState(existing.currentStreak, existing.lastCheckInDate),
            today = LocalDate.now()
        )
        val favouritesCount = favouriteDao.count()
        val badges = StreakManager.unlockedBadges(
            streak = updatedState.currentStreak,
            favouritesCount = favouritesCount,
            alreadyUnlocked = existing.unlockedBadgeIds
        )
        val record = StreakRecord(
            currentStreak = updatedState.currentStreak,
            lastCheckInDate = updatedState.lastCheckInDate,
            unlockedBadgeIds = badges
        )
        Log.d(Constants.LOG_TAG, "Streak check-in -> streak=${record.currentStreak}, badges=${record.unlockedBadgeIds}")
        streakDao.upsert(record)
        return record
    }

    /** Re-checks the Explorer badge after a favourite is added/removed, without touching the streak. */
    suspend fun refreshExplorerBadge() {
        val existing = streakDao.getOnce() ?: StreakRecord()
        val favouritesCount = favouriteDao.count()
        val badges = StreakManager.unlockedBadges(existing.currentStreak, favouritesCount, existing.unlockedBadgeIds)
        if (badges != existing.unlockedBadgeIds) {
            streakDao.upsert(existing.copy(unlockedBadgeIds = badges))
        }
    }
}
