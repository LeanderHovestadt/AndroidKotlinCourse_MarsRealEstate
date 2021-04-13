package com.udacity.asteroidradar.database

import android.content.Context
import androidx.room.*
import java.util.*

@Database(entities = [DatabaseAsteroid::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AsteroidsDatabase : RoomDatabase() {
    abstract val asteroidsDatabaseDao: AsteroidsDatabaseDao
}

private lateinit var INSTANCE: AsteroidsDatabase

fun getDatabase(context: Context): AsteroidsDatabase {
    if (!::INSTANCE.isInitialized) {
        synchronized(AsteroidsDatabase::class.java) {
            INSTANCE = Room.databaseBuilder(
                    context.applicationContext,
                    AsteroidsDatabase::class.java,
                    "asteroids_near_earth_database"
            ).fallbackToDestructiveMigration()
                    .build()
        }
    }
    return INSTANCE
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}