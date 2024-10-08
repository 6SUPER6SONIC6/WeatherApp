package com.supersonic.weatherapp.screens.home

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.weatherapp.BuildConfig
import com.supersonic.weatherapp.model.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
): ViewModel() {
    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val weatherState: StateFlow<WeatherUiState> = _weatherState

    private val _hourlyForecastState = MutableStateFlow<HourlyForecastUiState>(HourlyForecastUiState.Idle)
    val hourlyForecastState: StateFlow<HourlyForecastUiState> = _hourlyForecastState

    private val _recentSearchesList = mutableStateListOf<RecentSearch>()
    val recentSearchesList: List<RecentSearch> = _recentSearchesList.asReversed()

    private val apiKey = BuildConfig.OPEN_WEATHER_API_KEY

    init {
        _recentSearchesList.addAll(repository.getRecentSearches())
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            val result = repository.getWeather(city, apiKey)
            _weatherState.value = when {
                result.isSuccess -> WeatherUiState.Success(result.getOrNull()!!)
                else -> WeatherUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun fetchHourlyForecast(city: String) {
        viewModelScope.launch {
            _hourlyForecastState.value = HourlyForecastUiState.Loading
            val result = repository.getHourlyForecast(city, apiKey)
            _hourlyForecastState.value = when {
                result.isSuccess -> HourlyForecastUiState.Success(result.getOrNull()!!)
                else -> HourlyForecastUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun addSearch(cityName: String, countryCode: String){
        val newSearch = RecentSearch(city = cityName, countryCode = countryCode)
        if (!_recentSearchesList.contains(newSearch)){
            _recentSearchesList.add(newSearch)
            saveSearches()
        }
    }

    fun clearSearches(){
        _weatherState.value = WeatherUiState.Idle
        _hourlyForecastState.value = HourlyForecastUiState.Idle
        _recentSearchesList.clear()
        repository.clearRecentSearches()
    }

    private fun saveSearches() {
        repository.saveRecentSearches(_recentSearchesList)
    }

}

data class RecentSearch(
    val city: String,
    val countryCode: String,
    val timestamp: Long = System.currentTimeMillis()
)