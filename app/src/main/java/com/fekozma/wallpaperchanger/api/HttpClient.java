package com.fekozma.wallpaperchanger.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {
	private static final String BASE_WEATHER_URL = "https://api.openweathermap.org/data/2.5/";
	private static Retrofit retrofit;

	public static WeatherApi getWeatherApi() {
		if (retrofit == null) {
			retrofit = new Retrofit.Builder()
				.baseUrl(BASE_WEATHER_URL)
				.addConverterFactory(GsonConverterFactory.create())
				.build();
		}
		return retrofit.create(WeatherApi.class);
	}
}
