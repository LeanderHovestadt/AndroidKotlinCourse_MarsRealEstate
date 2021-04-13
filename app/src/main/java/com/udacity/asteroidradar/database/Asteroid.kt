package com.udacity.asteroidradar.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.udacity.asteroidradar.domain.Asteroid
import java.util.*

@Entity(tableName = "asteroids_near_earth_table")
data class DatabaseAsteroid(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0L,

        @ColumnInfo(name = "code_name")
        val codename: String,

        @ColumnInfo(name = "close_approach_date")
        val closeApproachDate: Date,

        @ColumnInfo(name = "absolute_Magnitude")
        val absoluteMagnitude: Double,

        @ColumnInfo(name = "estimated_diameter")
        val estimatedDiameter: Double,

        @ColumnInfo(name = "relative_velocity")
        val relativeVelocity: Double,

        @ColumnInfo(name = "distance_from_earth")
        val distanceFromEarth: Double,

        @ColumnInfo(name = "is_potentially_hazardous")
        val isPotentiallyHazardous: Boolean
)

fun List<DatabaseAsteroid>.asDomainModel(): List<Asteroid> {
    return map {
        Asteroid(
                id = it.id,
                codename = it.codename,
                closeApproachDate = it.closeApproachDate,
                absoluteMagnitude = it.absoluteMagnitude,
                estimatedDiameter = it.estimatedDiameter,
                relativeVelocity = it.relativeVelocity,
                distanceFromEarth = it.distanceFromEarth,
                isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }
}