package com.fekozma.wallpaperchanger.jobs.conditions;

import com.fekozma.wallpaperchanger.api.HttpClient;
import com.fekozma.wallpaperchanger.api.WeatherApi;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.StaticTags;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;
import com.fekozma.wallpaperchanger.BuildConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherCondition extends ConditionalImages{
	@Override
	public void getImages(List<DBImage> images, OnImagesLoaded onImagesLoaded) {

		String apiKey = BuildConfig.OPENWEATHER_API_KEY;

		LocationUtil.getCurrentLocation(location -> {

			if (location == null) {
				onImagesLoaded.onImagesLoaded(images);
				return;
			}
			WeatherApi weatherApi = HttpClient.getWeatherApi();
			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Retrieving weather information.");
			Call<WeatherApi.Response> call = weatherApi.getWeather(location.getLatitude(), location.getLongitude(), apiKey, "metric");

			call.enqueue(new Callback<WeatherApi.Response>() {
				@Override
				public void onResponse(Call<WeatherApi.Response> call, Response<WeatherApi.Response> response) {
					if (response.isSuccessful() && response.body() != null) {
						WeatherApi.Response data = response.body();

						String currentCondition = StaticTags.getWeather(data.weather.get(0).id).getName();
						DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Weather retrieved; " + data.weather.get(0).description);

						onImagesLoaded.onImagesLoaded(getWeatherImages(images, currentCondition));

					} else {
						DBLog.db.addLog(DBLog.LEVELS.ERROR, "Weather failed " + response.message());
						SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.WEATHER_CATEGORY, null);
						onImagesLoaded.onImagesLoaded(getWeatherImages(images, null));
					}
				}

				@Override
				public void onFailure(Call<WeatherApi.Response> call, Throwable t) {
					DBLog.db.addLog(DBLog.LEVELS.ERROR, "Weather failed " + t.getMessage(), t);
					onImagesLoaded.onImagesLoaded(getWeatherImages(images, null));
				}
			});


		});
	}

	private List<DBImage> getWeatherImages(List<DBImage> images, String weather) {
		if (weather == null) {
			weather = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.WEATHER_CATEGORY);

			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Getting weather from cache; " + weather);
			if (weather == null) {
				return images;
			}
		} else {
			SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.WEATHER_CATEGORY, weather);
		}

		String finalWeather = weather;
		List<DBImage> filteredRes = images.stream().filter(image -> Arrays.stream(image.tags).toList().contains(finalWeather)).collect(Collectors.toList());
		return (filteredRes.isEmpty() ? images: filteredRes);
	}
}
