package com.supersonic.weatherapp.screens.home

import com.supersonic.weatherapp.model.data.HourlyWeather
import com.supersonic.weatherapp.model.data.WeatherResponse

sealed class WeatherUiState {
    data object Idle : WeatherUiState()
    data object Loading : WeatherUiState()
    data class Success(val weather: WeatherResponse) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

sealed class HourlyForecastUiState {
    object Idle : HourlyForecastUiState()
    object Loading : HourlyForecastUiState()
    data class Success(val hourlyForecast: List<HourlyWeather>) : HourlyForecastUiState()
    data class Error(val message: String) : HourlyForecastUiState()
}