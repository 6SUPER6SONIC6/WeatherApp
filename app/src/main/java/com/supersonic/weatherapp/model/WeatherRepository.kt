package com.supersonic.weatherapp.model

import com.supersonic.weatherapp.model.data.HourlyWeather
import com.supersonic.weatherapp.model.data.WeatherResponse
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherRepository(private val weatherService: WeatherService) {
    suspend fun getWeather(cityName: String, apiKey: String): Result<WeatherResponse>{
        return try {
            val response = weatherService.getWeather(cityName, apiKey)
            if (response.isSuccessful){
                response.body()?.let {
                    Result.success(it)
                } ?: Result.failure(Exception("No weather data available"))
            } else {
                Result.failure(Exception(response.message()))
            }
        }
        catch (e: Exception){
            Result.failure(e)
        }
    }

    suspend fun getHourlyForecast(cityName: String, apiKey: String): Result<List<HourlyWeather>> {
        return try {
            val response = weatherService.getHourlyForecast(cityName, apiKey)
            if (response.isSuccessful) {
                val forecastResponse = response.body()
                forecastResponse?.let {
                    val todayForecast = filterTodayForecast(it.list)
                    Result.success(todayForecast)
                } ?: Result.failure(Exception("No hourly forecast data available"))
            } else {
                Result.failure(Exception( response.message()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun filterTodayForecast(weatherList: List<HourlyWeather>): List<HourlyWeather> {
        val calendar = Calendar.getInstance()
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)

        // Check if it's after a certain hour (e.g., 18:00 or 6 PM)
        val includeTomorrow = currentHour >= 18


        return weatherList.filter { weather ->

            val weatherDate = weather.dt_txt.split(" ")[0]
            // Check if it matches today or tomorrow if needed
            weatherDate == currentDate || (includeTomorrow && weatherDate == getNextDate(currentDate))
        }
    }

    private fun getNextDate(currentDate: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(currentDate)!!
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Increment the date by 1 day
        return sdf.format(calendar.time)
    }
}