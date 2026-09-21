package com.skycast.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data-access object for the FavouriteCity table (Part 1, R6).
 * Exposed as a Flow so the Favourites screen updates automatically whenever
 * a city is added or removed, without any manual refresh logic.
 */
@Dao
interface FavouriteCityDao {

    @Query("SELECT * FROM favourite_city ORDER BY cityName ASC")
    fun observeAll(): Flow<List<FavouriteCity>>

    @Query("SELECT * FROM favourite_city ORDER BY cityName ASC")
    suspend fun getAllOnce(): List<FavouriteCity>

    @Query("SELECT COUNT(*) FROM favourite_city")
    suspend fun count(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favourite_city WHERE cityName = :cityName AND country = :country)")
    suspend fun isFavourite(cityName: String, country: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(city: FavouriteCity): Long

    @Update
    suspend fun update(city: FavouriteCity)

    @Delete
    suspend fun delete(city: FavouriteCity)

    @Query("DELETE FROM favourite_city WHERE id = :id")
    suspend fun deleteById(id: Long)
}
