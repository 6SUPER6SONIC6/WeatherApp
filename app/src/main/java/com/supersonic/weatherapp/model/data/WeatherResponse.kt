package com.supersonic.weatherapp.model.data

data class WeatherResponse(
    val name: String,
    val sys: Sys,
    val main: Main,
    val weather: List<WeatherCondition>
)

data class HourlyForecastResponse(
    val list: List<HourlyWeather>
)

data class HourlyWeather(
    val dt: Long,
    val main: Main,
    val weather: List<WeatherCondition>,
    val dt_txt: String
)

data class Main(
    val temp: Double
)

data class WeatherCondition(
    val description: String
)

data class Sys(
    val country: String
)

