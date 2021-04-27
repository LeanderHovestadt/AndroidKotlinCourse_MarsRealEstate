package com.udacity.asteroidradar.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.database.DatabaseAsteroid
import com.udacity.asteroidradar.domain.Asteroid
import timber.log.Timber
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@JsonClass(generateAdapter = true)
data class NetworkAsteroidContainer(val asteroids: List<NetworkAsteroid>)

@JsonClass(generateAdapter = true)
data class NetworkAsteroid(
    val id: Long, val codename: String, val closeApproachDate: String,
    val absoluteMagnitude: Double, val estimatedDiameter: Double,
    val relativeVelocity: Double, val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean
)

@JsonClass(generateAdapter = true)
data class PictureOfTheDay(
    @Json(name = "media_type")
    val media_type: String,
    val title: String,
    val url: String
)

fun NetworkAsteroidContainer.asDomainModel(): List<Asteroid> {
    val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    return asteroids.map {
        var parsedCloseApproachDate: Date
        try{
            dateFormat.parse(it.closeApproachDate).let { date -> parsedCloseApproachDate = date }
        }
        catch( exception: ParseException){
            Timber.w("Error during parsing closeApproachDate ${it.closeApproachDate}")
            parsedCloseApproachDate = Calendar.getInstance().time
        }

        Asteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = parsedCloseApproachDate,
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
        Timber.i("Parsing asteroid with id ${it.id} and closeApproachDate ${it.closeApproachDate}...")
        var parsedCloseApproachDate: Date
        try{
            dateFormat.parse(it.closeApproachDate).let { date -> parsedCloseApproachDate = date }
            Timber.i("Sucessfully parsed closeApproachDate $parsedCloseApproachDate for asteroid with id ${it.id}.")
        }
        catch( exception: ParseException){
            Timber.w("Error during parsing closeApproachDate ${it.closeApproachDate}")
            parsedCloseApproachDate = Calendar.getInstance().time
        }

        DatabaseAsteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = parsedCloseApproachDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }.toTypedArray()
}