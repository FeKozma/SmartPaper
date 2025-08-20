package com.fekozma.wallpaperchanger.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpClient {
	private static final String BASE_WEATHER_URL = "https://api.openweathermap.org/data/2.5/";
	private static final String BASE_ADDRESS_URL = "https://nominatim.openstreetmap.org/";

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

	public static NominatimService getAddressApi() {
		OkHttpClient client = new OkHttpClient.Builder()
			.addInterceptor(new UserAgentInterceptor())
			.build();

		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl(BASE_ADDRESS_URL)
			.client(client)
			.addConverterFactory(GsonConverterFactory.create())
			.build();

		return retrofit.create(NominatimService.class);
	}
}
