package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.getLastDayFormattedDate
import com.udacity.asteroidradar.api.getTodayFormattedDate
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.network.Network
import com.udacity.asteroidradar.network.NetworkAsteroidContainer
import com.udacity.asteroidradar.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    private val _asteroidContainer = MutableLiveData<NetworkAsteroidContainer>()
    val asteroidContainer: LiveData<NetworkAsteroidContainer>
        get() = _asteroidContainer

    private val _pictureOfTheDayUrl = MutableLiveData<String>()
    val pictureOfTheDayUrl: LiveData<String>
        get() = _pictureOfTheDayUrl

    private val _pictureOfTheDayContentDescription = MutableLiveData<String?>()
    val pictureOfTheDayContentDescription: LiveData<String?>
        get() = _pictureOfTheDayContentDescription

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidsDatabaseDao.getAllAsteroids()) { asteroids ->
            Timber.i("Adding ${asteroids.size} asteroids to asteroids live data")
            asteroids.asDomainModel()
        }

    suspend fun fetchAsteroids() {
        Timber.i("fetchAsteroids called")
        withContext(Dispatchers.IO) {
            val today = getTodayFormattedDate()
            val lastDay = getLastDayFormattedDate()
            val filter = Constants.API_FILTER_FORMAT.format(today, lastDay, Constants.API_KEY)
            Timber.i("requesting filter: \"${filter}\" from API")

            try {
                val response = Network.retrofitService.getAsteroids(
                    mapOf(
                        "start_date" to today,
                        "end_date" to lastDay,
                        "api_key" to Constants.API_KEY
                    )
                )
                Timber.i("received successful response.")
                val asteroidContainer = parseAsteroidsJsonResult(JSONObject(response))
                Timber.i("received ${asteroidContainer.asteroids.size} asteroids.")
                _asteroidContainer.postValue(asteroidContainer)
            } catch (exception: Exception) {
                Timber.w("Could not fetch Asteroids. Error message: ${exception.message}")
            }
        }
    }

    suspend fun fetchPictureOfTheDay() {
        Timber.i("fetchPictureOfTheDay called")
        withContext(Dispatchers.IO) {
            try {
                val response = Network.retrofitService.getPictureOfTheDay(Constants.API_KEY)
                Timber.i("pictureOfTheDay url: ${response.url}")
                _pictureOfTheDayUrl.postValue(response.url)
                _pictureOfTheDayContentDescription.postValue(response.title)
            } catch (exception: Exception) {
                Timber.w("Could not fetch PictureOfTheDay. Error message: ${exception.message}")
                _pictureOfTheDayUrl.postValue("")
                _pictureOfTheDayContentDescription.postValue(null)
            }
        }
    }

    suspend fun removeOldAsteroids() {
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val currentTime = calendar.time

            database.asteroidsDatabaseDao.clearOlderAsteroidsThan(currentTime)
        }
    }

    suspend fun writeContainerToDatabase(asteroidContainer: NetworkAsteroidContainer) {
        withContext(Dispatchers.IO) {
            database.asteroidsDatabaseDao.insertAll(*asteroidContainer.asDatabaseModel())
        }
    }
}