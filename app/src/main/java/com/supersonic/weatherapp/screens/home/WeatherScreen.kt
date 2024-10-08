package com.supersonic.weatherapp.screens.home

import android.view.Gravity
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.supersonic.weatherapp.R
import com.supersonic.weatherapp.model.data.HourlyWeather
import com.supersonic.weatherapp.util.convertUnixToTime
import com.supersonic.weatherapp.util.getCountryName
import com.supersonic.weatherapp.util.getRelativeTime
import kotlin.math.roundToInt

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = hiltViewModel()) {
    val weatherState by viewModel.weatherState.collectAsState()
    val hourlyForecastState by viewModel.hourlyForecastState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF6F6F6)
    ) { paddingValues ->
        WeatherScreenContent(
            modifier = Modifier
                .padding(paddingValues)
                .padding(vertical = 32.dp, horizontal = 16.dp),
            weatherState = weatherState,
            hourlyForecastState = hourlyForecastState,
            recentSearches = viewModel.recentSearchesList,
            onAddSearch = viewModel::addSearch,
            onClearRecentSearches = { viewModel.clearSearches() },
            onFetchWeather = {
                viewModel.fetchHourlyForecast(it)
                viewModel.fetchWeather(it)
            }
        )
    }
}

@Composable
private fun WeatherScreenContent (
    modifier: Modifier = Modifier,
    weatherState: WeatherUiState,
    hourlyForecastState: HourlyForecastUiState,
    recentSearches: List<RecentSearch>,
    onAddSearch:(String, String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onFetchWeather: (String) -> Unit
) {
    val context = LocalContext.current
    var hourlyForecast by remember {
        mutableStateOf(listOf<HourlyWeather>())
    }
    var currentTemp by remember {
        mutableDoubleStateOf(0.0)
    }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showLoader by remember { mutableStateOf(false) }

    if (showLocationDialog){
        EnterLocationDialog(
            onDismissRequest = { showLocationDialog = false },
            onFetchWeather = { onFetchWeather(it) }
        )
    }

    LaunchedEffect(weatherState) {
        when (weatherState){
            is WeatherUiState.Loading -> { showLoader = true }
            is WeatherUiState.Success -> {
                showLoader = false
                currentTemp = weatherState.weather.main.temp
                onAddSearch(weatherState.weather.name, weatherState.weather.sys.country)
            }
            is WeatherUiState.Error -> {
                showLoader = false
                Toast.makeText(context, weatherState.message, Toast.LENGTH_SHORT).show()
            }
            else -> { showLoader = false }
        }
    }

    when (hourlyForecastState) {
        is HourlyForecastUiState.Loading -> {showLoader = true}
        is HourlyForecastUiState.Success -> {
            showLoader = false
            hourlyForecast = hourlyForecastState.hourlyForecast
        }
        else -> { showLoader = false }
    }



    Box(modifier = modifier){

        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CurrentTempCard(
                hourlyForecastList = hourlyForecast,
                currentTemp = currentTemp,
                onSearchNewLocation = { showLocationDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            RecentSearchesCard(
                recentSearches = recentSearches,
                onClearRecentSearches = onClearRecentSearches
            )
        }

        if (showLoader){
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }

    }
}

@Composable
private fun CurrentTempCard(
    modifier: Modifier = Modifier,
    currentTemp: Double = 0.0,
    hourlyForecastList: List<HourlyWeather> = listOf(),
    onSearchNewLocation: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )

    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            ){
            Column(
                modifier = Modifier
                    .padding(bottom = 16.dp)
            ) {
                Row {
                    Text(
                        text = "${currentTemp.roundToInt()}",
                        fontSize = 71.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Text(
                        text = "C",
                        fontSize = 31.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Top)
                            .padding(top = 8.dp)
                        )
                }
                Text(
                    text = stringResource(R.string.current_temp_card_title),
                    color = Color.LightGray
                )
            }

            if (hourlyForecastList.isEmpty()){
                Row {
                    HourlyItem()
                    HourlyItem()
                    HourlyItem()
                }
            } else {
                LazyRow(
                    modifier = Modifier,
                ) {
                    items(hourlyForecastList) { weather ->
                        HourlyItem(temp = weather.main.temp, time = convertUnixToTime(weather.dt))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            CustomButton(
                onClick = onSearchNewLocation,
                label = stringResource(R.string.current_temp_card_btn_label)
            )

        }
    }
}

@Composable
private fun RecentSearchesCard(
    modifier: Modifier = Modifier,
    recentSearches: List<RecentSearch>,
    onClearRecentSearches: () -> Unit
) {

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ){
            Text(
                text = stringResource(
                    if (recentSearches.isEmpty()) R.string.recent_searches_card_empty_title
                    else R.string.recent_searches_card_title
                ),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )
            AnimatedVisibility(recentSearches.isNotEmpty()){
                Spacer(modifier = Modifier.height(16.dp))
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentSearches){ recentSearch ->
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text(
                            text = "${recentSearch.city}, ${getCountryName(recentSearch.countryCode)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .align(Alignment.CenterStart)
                        )
                        Text(
                            text = getRelativeTime(recentSearch.timestamp),
                            color = Color.LightGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .align(Alignment.CenterEnd)
                        )
                    }
                }
                item {
                    AnimatedVisibility(recentSearches.isNotEmpty()){
                        Spacer(modifier = Modifier.height(16.dp))
                        CustomButton(
                            onClick = onClearRecentSearches,
                            label = stringResource(R.string.recent_searches_card_btn_label)
                        )
                    }
                }
            }

        }
    }

}

@Composable
private fun HourlyItem(
    modifier: Modifier = Modifier,
    temp: Double? = null,
    time: String? = null
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .background(
                color = Color(0xFFF9F9F9),
                shape = RoundedCornerShape(16.dp)
            )
            .size(104.dp),
    ){
        if (temp != null && time != null){
            Text(
                text = "${temp.roundToInt()}C",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = "$time",
                fontSize = 12.sp,
                color = Color.LightGray,
                modifier = Modifier.align(Alignment.BottomCenter)

            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnterLocationDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onFetchWeather: (String) -> Unit
) {
    var cityName by remember { mutableStateOf("") }
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier.padding(bottom = 16.dp)
    ) {
        val dialogWindowProvider = LocalView.current.parent as DialogWindowProvider
        dialogWindowProvider.window.setGravity(Gravity.BOTTOM)
        Box(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(24.dp))
                .height(360.dp)
                .padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.enter_location_dialog_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            TextField(
                value = cityName,
                onValueChange = {cityName = it},
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                ),
                placeholder = {
                    Text(
                        text = stringResource(R.string.enter_location_dialog_placeholder),
                        fontSize = 22.sp,
                        color = Color(0xFFB3B3B3),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                              },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    )
                ,
                modifier = Modifier.align(Alignment.Center)
            )

            Button(
                onClick = {
                    onFetchWeather(cityName)
                    onDismissRequest.invoke()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = stringResource(R.string.enter_location_dialog_button_title),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun CustomButton(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick =  onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(45.dp),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF9F9F9),
            contentColor = Color(0xAA040404)
        )
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}