package com.skycast.app

import com.skycast.app.util.StreakManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Unit tests for [StreakManager] (Part 1, R7 / Part 2 "detailed unit testing").
 */
class StreakManagerTest {

    private val today = LocalDate.of(2026, 9, 20)

    @Test
    fun `first ever check-in starts a streak of 1`() {
        val result = StreakManager.checkIn(StreakManager.StreakState(0, null), today)
        assertEquals(1, result.currentStreak)
        assertEquals("2026-09-20", result.lastCheckInDate)
    }

    @Test
    fun `checking in again on the same day does not increase the streak`() {
        val previous = StreakManager.StreakState(currentStreak = 4, lastCheckInDate = "2026-09-20")
        val result = StreakManager.checkIn(previous, today)
        assertEquals(4, result.currentStreak)
    }

    @Test
    fun `checking in on the very next day increases the streak by one`() {
        val previous = StreakManager.StreakState(currentStreak = 4, lastCheckInDate = "2026-09-19")
        val result = StreakManager.checkIn(previous, today)
        assertEquals(5, result.currentStreak)
    }

    @Test
    fun `missing one or more days resets the streak to 1`() {
        val previous = StreakManager.StreakState(currentStreak = 10, lastCheckInDate = "2026-09-17")
        val result = StreakManager.checkIn(previous, today)
        assertEquals(1, result.currentStreak)
    }

    @Test
    fun `a future last-checkin date (clock skew) does not penalise the user`() {
        val previous = StreakManager.StreakState(currentStreak = 6, lastCheckInDate = "2026-09-25")
        val result = StreakManager.checkIn(previous, today)
        assertEquals(6, result.currentStreak)
    }

    @Test
    fun `an unparsable stored date is treated as a first-ever check-in`() {
        val previous = StreakManager.StreakState(currentStreak = 6, lastCheckInDate = "not-a-date")
        val result = StreakManager.checkIn(previous, today)
        assertEquals(1, result.currentStreak)
    }

    @Test
    fun `badges unlock at 3, 7 and 30 days and stay unlocked`() {
        assertTrue(StreakManager.unlockedBadges(3, 0).contains(StreakManager.BADGE_3_DAY))
        assertFalse(StreakManager.unlockedBadges(2, 0).contains(StreakManager.BADGE_3_DAY))

        assertTrue(StreakManager.unlockedBadges(7, 0).contains(StreakManager.BADGE_7_DAY))
        assertTrue(StreakManager.unlockedBadges(30, 0).contains(StreakManager.BADGE_30_DAY))

        // Sticky: even if the streak later resets, previously earned badges are kept.
        val badges = StreakManager.unlockedBadges(
            streak = 1,
            favouritesCount = 0,
            alreadyUnlocked = listOf(StreakManager.BADGE_3_DAY, StreakManager.BADGE_7_DAY)
        )
        assertTrue(badges.contains(StreakManager.BADGE_3_DAY))
        assertTrue(badges.contains(StreakManager.BADGE_7_DAY))
    }

    @Test
    fun `explorer badge unlocks once five favourites are saved`() {
        assertFalse(StreakManager.unlockedBadges(0, 4).contains(StreakManager.BADGE_EXPLORER))
        assertTrue(StreakManager.unlockedBadges(0, 5).contains(StreakManager.BADGE_EXPLORER))
        assertTrue(StreakManager.unlockedBadges(0, 9).contains(StreakManager.BADGE_EXPLORER))
    }
}
