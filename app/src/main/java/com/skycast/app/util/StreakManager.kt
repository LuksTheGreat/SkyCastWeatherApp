package com.skycast.app.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Implements the daily check-in streak / badge logic from Part 1, R7:
 * - Checking in on the day immediately after the last check-in increases the streak.
 * - Checking in again on the *same* day is a no-op (doesn't double-count).
 * - Missing one or more days resets the streak back to 1 (today's check-in starts a new streak).
 * - Badges unlock at 3, 7 and 30 consecutive days; the Explorer badge unlocks at 5 favourites.
 *
 * Kept as a pure object (no Android/Room dependencies) so it is fully and
 * cheaply unit-testable (Part 2 requirement).
 */
object StreakManager {

    const val BADGE_3_DAY = "streak_3"
    const val BADGE_7_DAY = "streak_7"
    const val BADGE_30_DAY = "streak_30"
    const val BADGE_EXPLORER = "explorer"

    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    data class StreakState(val currentStreak: Int, val lastCheckInDate: String?)

    /**
     * Applies "today's" check-in to the previous streak state.
     */
    fun checkIn(previous: StreakState, today: LocalDate): StreakState {
        val lastDate = previous.lastCheckInDate?.let { runCatching { LocalDate.parse(it, isoFormatter) }.getOrNull() }

        val newStreak = when {
            lastDate == null -> 1
            lastDate.isEqual(today) -> previous.currentStreak // already checked in today
            lastDate.isEqual(today.minusDays(1)) -> previous.currentStreak + 1 // consecutive day
            lastDate.isAfter(today) -> previous.currentStreak // clock skew safety net; don't punish the user
            else -> 1 // gap of 2+ days: streak resets, today starts a new one
        }

        return StreakState(currentStreak = newStreak, lastCheckInDate = today.format(isoFormatter))
    }

    /**
     * Returns the full set of badge IDs that should be unlocked given the current
     * streak length and number of saved favourite cities. Badges are additive/sticky:
     * pass in [alreadyUnlocked] and the result will never drop a previously earned badge,
     * even if, e.g., the streak later resets.
     */
    fun unlockedBadges(
        streak: Int,
        favouritesCount: Int,
        alreadyUnlocked: List<String> = emptyList()
    ): List<String> {
        val earned = mutableSetOf<String>().apply { addAll(alreadyUnlocked) }
        if (streak >= 3) earned.add(BADGE_3_DAY)
        if (streak >= 7) earned.add(BADGE_7_DAY)
        if (streak >= 30) earned.add(BADGE_30_DAY)
        if (favouritesCount >= 5) earned.add(BADGE_EXPLORER)
        return earned.toList()
    }
}
