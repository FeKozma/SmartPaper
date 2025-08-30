package com.fekozma.wallpaperchanger.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.fekozma.wallpaperchanger.database.ImageCategories;

public class SharedPreferencesUtil {
	static SharedPreferences sharedPreferences;

	static void setSharedPreferences() {
		sharedPreferences = ContextUtil.getContext().getSharedPreferences(ContextUtil.getContext().getApplicationInfo().name, Context.MODE_PRIVATE);
	}

	// Integer

	public static Integer getInt(KEYS key) {
		checkSharedPreferences();
		return sharedPreferences.getInt(key.key, (Integer) key.value);
	}

	public static Integer getInt(KEYS key, ImageCategories cat) {
		checkSharedPreferences();
		return sharedPreferences.getInt(getName(key, cat), (Integer) key.value);
	}

	public static void setInt(KEYS key, int value) {
		checkSharedPreferences();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(key.key, value);
		editor.apply();
	}

	public static void setInt(KEYS key, ImageCategories cat, int value) {
		checkSharedPreferences();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(getName(key, cat), value);
		editor.apply();
	}

	// Boolean
	public static Boolean getBoolean(KEYS key) {
		checkSharedPreferences();
		return sharedPreferences.getBoolean(key.key, (Boolean) key.value);
	}

	public static boolean getBoolean(KEYS key, ImageCategories cat) {
		checkSharedPreferences();
		return sharedPreferences.getBoolean(getName(key, cat), (boolean) key.value);
	}

	public static void setBoolean(KEYS key, Boolean value) {
		checkSharedPreferences();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(key.key, value);
		editor.apply();
	}

	public static void setBoolean(KEYS key, ImageCategories cat, boolean value) {
		checkSharedPreferences();
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putBoolean(getName(key, cat), value);
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

	public enum KEYS {
		ONLY_LOCKSCREEN("onlylockscreen", false),
		LOCATION_LAT("location_lat", null),
		LOCATION_LONG("location_long", null),
		WEATHER_CATEGORY("weather_category", null),
		USE_GPS("use_gps", true),
		CATEGORY_ACTIVE("category_active", true),
		LOCATION_RADIUS("location_radius", 5),
		CATEGORY_POSITION("category_position", -1);

		String key;
		Object value;

		KEYS(String key, Object defaultValue) {
			this.key = key;
			this.value = defaultValue;
		}
	}

	private static String getName(KEYS key, ImageCategories cat) {
		return key.key + "_" + cat.getCategory();
	}
}
