package com.fekozma.wallpaperchanger.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
	static SharedPreferences sharedPreferences;

	static void setSharedPreferences() {
		sharedPreferences = ContextUtil.getContext().getSharedPreferences(ContextUtil.getContext().getApplicationInfo().name, Context.MODE_PRIVATE);
	}
	public enum KEYS {
		ONLY_LOCKSCREEN("onlylockscreen", false),
		LOCATION_LAT("location_lat", null),
		LOCATION_LONG("location_long", null),
		WEATHER_CATEGORY("weather_category", null),
		USE_GPS("use_gps", true);

		String key;
		Object value;
		KEYS(String key, Object defaultValue) {
			this.key = key;
			this.value = defaultValue;
		}
	}

	// Boolean
	public static Boolean getBoolean(KEYS key) {
		checkSharedPreferences();
		return sharedPreferences.getBoolean(key.key, (Boolean) key.value);
	}

	public static void setBoolean(KEYS key, Boolean value) {
		checkSharedPreferences();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(key.key, value);
		editor.apply();
	}

	// String
	public static String getString(KEYS key) {
		checkSharedPreferences();
		return sharedPreferences.getString(key.key, null);
	}

	public static void setString(KEYS key, String value) {
		checkSharedPreferences();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(key.key, value);
		editor.apply();
	}

	private static void checkSharedPreferences() {
		if (sharedPreferences == null) {
			setSharedPreferences();
		}
	}
}
