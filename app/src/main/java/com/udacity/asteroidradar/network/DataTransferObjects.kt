package com.udacity.asteroidradar.network

import com.squareup.moshi.JsonClass
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.domain.Asteroid
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
data class NetworkAsteroidContainer(val asteroids: List<NetworkAsteroid>)

@JsonClass(generateAdapter = true)
data class NetworkAsteroid(val id: Long, val codename: String, val closeApproachDate: String,
                               val absoluteMagnitude: Double, val estimatedDiameter: Double,
                               val relativeVelocity: Double, val distanceFromEarth: Double,
                               val isPotentiallyHazardous: Boolean)

fun NetworkAsteroidContainer.asDomainModel(): List<Asteroid> {
    val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    return asteroids.map {
        Asteroid(
                id = it.id,
                codename = it.codename,
                closeApproachDate = dateFormat.parse(it.closeApproachDate) ?: Date(),
                absoluteMagnitude = it.absoluteMagnitude,
                estimatedDiameter = it.estimatedDiameter,
                relativeVelocity = it.relativeVelocity,
                distanceFromEarth = it.distanceFromEarth,
                isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }
}

fun NetworkAsteroidContainer.asDatabaseModel(): Array<DatabaseAsteroid> {
    val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    return asteroids.map {
        DatabaseAsteroid(
                id = it.id,
                codename = it.codename,
                closeApproachDate = dateFormat.parse(it.closeApproachDate) ?: Date(),
                absoluteMagnitude = it.absoluteMagnitude,
                estimatedDiameter = it.estimatedDiameter,
                relativeVelocity = it.relativeVelocity,
                distanceFromEarth = it.distanceFromEarth,
                isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }.toTypedArray()
}