package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.network.NetworkAsteroidContainer
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch
import java.lang.IllegalArgumentException

enum class ShownAsteroidsFilter { SHOW_WEEK_ASTEROIDS, SHOW_TODAY_ASTEROIDS, SHOW_SAVED_ASTEROIDS }


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var asteroidsContainerObserver: Observer<NetworkAsteroidContainer>
    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)
    val asteroids = asteroidsRepository.asteroids
    val asteroidsByToday = asteroidsRepository.asteroidsByToday
    val asteroidsUntilEndDate = asteroidsRepository.asteroidsUntilEndDate

    private val _filter = MutableLiveData<ShownAsteroidsFilter>()
    val shownAsteroidsFilter: LiveData<ShownAsteroidsFilter>
        get() = _filter


    init {
        viewModelScope.launch {
            asteroidsRepository.fetchAsteroids()
        }

        _filter.value = ShownAsteroidsFilter.SHOW_SAVED_ASTEROIDS

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
        _filter.value = filter
    }

}