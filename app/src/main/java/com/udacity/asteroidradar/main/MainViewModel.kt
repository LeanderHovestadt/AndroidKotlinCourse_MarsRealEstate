package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.getTodayFormattedDate
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.network.NetworkAsteroidContainer
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.*

enum class ShownAsteroidsFilter { SHOW_WEEK_ASTEROIDS, SHOW_TODAY_ASTEROIDS, SHOW_SAVED_ASTEROIDS }


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private var asteroidsContainerObserver: Observer<NetworkAsteroidContainer>
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)
    val pictureOfTheDayUrl = asteroidsRepository.pictureOfTheDayUrl
    val pictureOfTheDayContentDescription = asteroidsRepository.pictureOfTheDayContentDescription
    val asteroids = asteroidsRepository.asteroids

    private val _shownAsteroidsFilter = MutableLiveData<ShownAsteroidsFilter>()
    val shownAsteroidsFilter: LiveData<ShownAsteroidsFilter>
        get() = _shownAsteroidsFilter

    private val _showSnackbarEvent = MutableLiveData<Boolean?>()
    val showSnackbarEvent: LiveData<Boolean?>
        get() = _showSnackbarEvent


    init {
        viewModelScope.launch {
            asteroidsRepository.removeOldAsteroids()
            asteroidsRepository.fetchPictureOfTheDay()
            asteroidsRepository.fetchAsteroids()
        }

        _shownAsteroidsFilter.value = ShownAsteroidsFilter.SHOW_SAVED_ASTEROIDS
        _showSnackbarEvent.value = null

        asteroidsContainerObserver = Observer {
            viewModelScope.launch {
                asteroidsRepository.writeContainerToDatabase(it)
            }
        }
        asteroidsRepository.asteroidContainer.observeForever(asteroidsContainerObserver)
    }

    override fun onCleared() {
        super.onCleared()
        asteroidsRepository.asteroidContainer.removeObserver(asteroidsContainerObserver)
    }

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("unable to construct viewmodel")
        }
    }

    fun updateFilter(filter: ShownAsteroidsFilter) {
        _shownAsteroidsFilter.value = filter
    }

    fun getAsteroids(): List<Asteroid> {
        when (shownAsteroidsFilter.value) {
            ShownAsteroidsFilter.SHOW_TODAY_ASTEROIDS -> {
                Timber.i("selected asteroidsByToday")
                // calculate start and end date
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val currentDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                val endDate = calendar.time

                Timber.i("Filtering asteroids from $currentDate to $endDate")
                val filteredAsteroids = asteroids.value?.filter { asteroid ->
                    val include =
                        asteroid.closeApproachDate >= currentDate && asteroid.closeApproachDate < endDate
                    Timber.i("Will include asteroid:${include} ; With closeApproachDate ${asteroid.closeApproachDate}")
                    include
                }
                if (filteredAsteroids.isNullOrEmpty()) {
                    onFetchedAsteroidsEmpty()
                    Timber.w("filteredAsteroids are empty or null")
                }
                return filteredAsteroids ?: listOf()
            }
            ShownAsteroidsFilter.SHOW_WEEK_ASTEROIDS -> {
                Timber.i("selected asteroidsUntilEndDate")

                // calculate start and end date
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val currentDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, Constants.DEFAULT_END_DATE_DAYS)
                val endDate = calendar.time

                // filter and return asteroids
                Timber.i("Filtering asteroids from $currentDate to $endDate")
                val filteredAsteroids = asteroids.value?.filter { asteroid ->
                    val include =
                        asteroid.closeApproachDate >= currentDate && asteroid.closeApproachDate < endDate
                    Timber.i("Will include asteroid:${include} ; With closeApproachDate ${asteroid.closeApproachDate}")
                    include
                }
                if (filteredAsteroids.isNullOrEmpty()) {
                    onFetchedAsteroidsEmpty()
                    Timber.w("filteredAsteroids are empty or null")
                }
                return filteredAsteroids ?: listOf()
            }
            else -> {
                Timber.i("selected asteroids")

                if (asteroids.value.isNullOrEmpty()) {
                    onFetchedAsteroidsEmpty()
                    Timber.w("asteroids are empty or null")
                }
                return asteroids.value ?: listOf()
            }
        }
    }

    fun doneShowSnackbarEvent(){
        _showSnackbarEvent.value = null
    }

    private fun onFetchedAsteroidsEmpty() {
        _showSnackbarEvent.value = true
    }

}