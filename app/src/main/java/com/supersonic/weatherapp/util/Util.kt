package com.supersonic.weatherapp.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCountryName(countryCode: String): String {
    val locale = Locale("", countryCode)
    return locale.displayCountry
}

fun convertUnixToTime(dt: Long): String {
    val date = Date(dt * 1000L)
    val sdf = SimpleDateFormat("ha", Locale.getDefault())
    return sdf.format(date).uppercase()
}