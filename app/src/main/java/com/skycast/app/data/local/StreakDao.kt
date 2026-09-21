package com.skycast.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {

    @Query("SELECT * FROM streak_record WHERE id = 1")
    fun observe(): Flow<StreakRecord?>

    @Query("SELECT * FROM streak_record WHERE id = 1")
    suspend fun getOnce(): StreakRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: StreakRecord)

    @Update
    suspend fun update(record: StreakRecord)
}
