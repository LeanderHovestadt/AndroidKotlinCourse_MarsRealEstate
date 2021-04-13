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
import retrofit2.Response
import timber.log.Timber
import java.util.*

class AsteroidsRepository(private val database: AsteroidsDatabase) {

    private val _asteroidContainer = MutableLiveData<NetworkAsteroidContainer>()
    val asteroidContainer: LiveData<NetworkAsteroidContainer>
        get() = _asteroidContainer

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidsDatabaseDao.getAllAsteroids()) {
            it.asDomainModel()
        }

    val asteroidsByToday: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidsDatabaseDao.getAllAsteroids()) { asteroids ->
            val calendar = Calendar.getInstance()
            val currentDate = calendar.time
            asteroids.filter { asteroid ->
                asteroid.closeApproachDate == currentDate
            }.asDomainModel()
        }

    val asteroidsUntilEndDate: LiveData<List<Asteroid>> =
        Transformations.map(database.asteroidsDatabaseDao.getAllAsteroids()) { asteroids ->

            val calendar = Calendar.getInstance()
            val currentDate = calendar.time

            calendar.add(Calendar.DAY_OF_YEAR, Constants.DEFAULT_END_DATE_DAYS)
            val endDate = calendar.time

            asteroids.filter { asteroid ->
                asteroid.closeApproachDate >= currentDate && asteroid.closeApproachDate <= endDate
            }.asDomainModel()
        }

    suspend fun fetchAsteroids() {
        Timber.i("fetchAsteroids called")
        withContext(Dispatchers.IO) {
            val today = getTodayFormattedDate()
            val lastDay = getLastDayFormattedDate()
            val filter = Constants.API_FILTER_FORMAT.format(today, lastDay, Constants.API_KEY)
            Timber.i("requesting filter: \"${filter}\" from API")
            Network.retrofitService.getAsteroids(
                mapOf(
                    "start_date" to today,
                    "end_date" to lastDay,
                    "api_key" to Constants.API_KEY
                )
            ).enqueue(object: Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Timber.i("received successful response.")
                    val asteroidContainer = parseAsteroidsJsonResult(JSONObject(response.body()))
                    Timber.i("received ${asteroidContainer.asteroids.size} asteroids.")
                    _asteroidContainer.value = asteroidContainer
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    Timber.w("Fetching Asteroids failed with failure ${t.message}.")
                }
            })
        }
    }

    suspend fun removeOldAsteroids() {
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
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