package com.skycast.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * SkyCast's local SQLite database, accessed through Room (Part 1, §5/§6).
 * Holds favourite cities and the single streak/badge record so the app keeps
 * working — with the last-known data — even without a network connection (R8).
 */
@Database(
    entities = [FavouriteCity::class, StreakRecord::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favouriteCityDao(): FavouriteCityDao
    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "skycast.db"
                ).build().also { INSTANCE = it }
            }
    }
}
