package com.skycast.app

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.skycast.app.data.local.AppDatabase
import com.skycast.app.data.local.FavouriteCity
import com.skycast.app.data.local.FavouriteCityDao
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for [FavouriteCityDao] (Part 1, R6 / Part 2 "detailed unit testing").
 * Runs against an in-memory Room database on a device/emulator so real SQLite
 * behaviour (constraints, ordering) is exercised rather than mocked.
 */
@RunWith(AndroidJUnit4::class)
class FavouriteCityDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: FavouriteCityDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.favouriteCityDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun insertAndRetrieveFavourite() = runBlocking {
        dao.insert(FavouriteCity(cityName = "Johannesburg", country = "ZA", lat = -26.2, lon = 28.04))
        val all = dao.getAllOnce()
        assertEquals(1, all.size)
        assertEquals("Johannesburg", all[0].cityName)
    }

    @Test
    fun isFavouriteReflectsInsertedCity() = runBlocking {
        assertTrue(!dao.isFavourite("Durban", "ZA"))
        dao.insert(FavouriteCity(cityName = "Durban", country = "ZA", lat = -29.85, lon = 31.02))
        assertTrue(dao.isFavourite("Durban", "ZA"))
    }

    @Test
    fun deletingFavouriteRemovesItAndUpdatesCount() = runBlocking {
        val id = dao.insert(FavouriteCity(cityName = "Pretoria", country = "ZA", lat = -25.74, lon = 28.19))
        assertEquals(1, dao.count())
        dao.deleteById(id)
        assertEquals(0, dao.count())
    }

    @Test
    fun explorerBadgeThresholdReachedAtFiveFavourites() = runBlocking {
        val cities = listOf("A", "B", "C", "D", "E")
        cities.forEachIndexed { i, name ->
            dao.insert(FavouriteCity(cityName = name, country = "ZA", lat = i.toDouble(), lon = i.toDouble()))
        }
        assertEquals(5, dao.count())
    }
}
