package com.fekozma.wallpaperchanger.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
	@GET("weather")
	Call<Response> getWeather(
		@Query("lat") double lat,
		@Query("lon") double lon,
		@Query("appid") String apiKey,
		@Query("units") String units  // e.g., "metric"
	);

	public class Response {
		@SerializedName("main")
		public Main main;

		@SerializedName("weather")
		public List<Weather> weather;

		public class Main {
			public float temp;
		}

		public class Weather {
			public String description;
			public int id;
		}
	}
}
