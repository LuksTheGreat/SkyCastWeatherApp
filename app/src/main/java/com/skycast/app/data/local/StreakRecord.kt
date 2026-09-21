package com.skycast.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

/**
 * Local Room representation of the gamification state described in Part 1, R7.
 * A single row is used (id is always 1) since there is one streak per device/user.
 */
@Entity(tableName = "streak_record")
@TypeConverters(BadgeListConverter::class)
data class StreakRecord(
    @PrimaryKey val id: Int = 1,
    val currentStreak: Int = 0,
    val lastCheckInDate: String? = null, // ISO-8601 yyyy-MM-dd
    val unlockedBadgeIds: List<String> = emptyList()
)

/** Stores the list of unlocked badge IDs as a comma-separated string in SQLite. */
class BadgeListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String = list.joinToString(",")

    @TypeConverter
    fun toList(value: String): List<String> =
        if (value.isBlank()) emptyList() else value.split(",")
}
