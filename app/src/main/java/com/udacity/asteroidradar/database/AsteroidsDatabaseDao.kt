package com.udacity.asteroidradar.database

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface AsteroidsDatabaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg asteroid: DatabaseAsteroid)

    @Query("DELETE FROM asteroids_near_earth_table WHERE close_approach_date < :date")
    suspend fun clearOlderAsteroidsThan(date: Date)

    @Query("SELECT * FROM asteroids_near_earth_table  ORDER BY close_approach_date ASC")
    fun getAllAsteroids(): LiveData<List<DatabaseAsteroid>>
}