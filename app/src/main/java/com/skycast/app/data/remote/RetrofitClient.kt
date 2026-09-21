package com.skycast.app.data.remote

import com.skycast.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Lazily-built singleton Retrofit client for the OpenWeatherMap base URL.
 * The API key is read from [BuildConfig], which in turn is populated from
 * gradle.properties (local dev) or a CI secret — never hard-coded here.
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val apiKey: String get() = BuildConfig.OPEN_WEATHER_API_KEY

    val service: WeatherApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}
