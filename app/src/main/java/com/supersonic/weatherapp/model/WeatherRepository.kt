package com.supersonic.weatherapp.model

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.supersonic.weatherapp.model.data.HourlyWeather
import com.supersonic.weatherapp.model.data.WeatherResponse
import com.supersonic.weatherapp.screens.home.RecentSearch
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val weatherService: WeatherService,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val PREFS_NAME = "recent_searches_prefs"
        private const val RECENT_SEARCHES_KEY = "recent_searches"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor = sharedPreferences.edit()
    private val gson = Gson()

    // Convert List<RecentSearch> to JSON
    fun saveRecentSearches(searches: List<RecentSearch>) {
        val jsonString = gson.toJson(searches)
        editor.putString(RECENT_SEARCHES_KEY, jsonString)
        editor.apply()
    }

    // Retrieve the JSON from SharedPreferences and convert it back to List<RecentSearch>
    fun getRecentSearches(): List<RecentSearch> {
        val jsonString = sharedPreferences.getString(RECENT_SEARCHES_KEY, null)

        return if (jsonString != null) {
            val type = object : TypeToken<List<RecentSearch>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    }

    fun clearRecentSearches() {
        editor.remove(RECENT_SEARCHES_KEY).apply()
    }

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
        val includeTomorrow = currentHour >= 18


        return weatherList.filter { weather ->

            val weatherDate = weather.dt_txt.split(" ")[0]
            weatherDate == currentDate || (includeTomorrow && weatherDate == getNextDate(currentDate))
        }
    }

    private fun getNextDate(currentDate: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = sdf.parse(currentDate)!!
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return sdf.format(calendar.time)
    }


}