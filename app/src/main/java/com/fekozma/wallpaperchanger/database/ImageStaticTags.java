package com.fekozma.wallpaperchanger.database;

import java.util.Calendar;

public enum ImageStaticTags {
	WEATHER_CLEAR("clear sky"),
	WEATHER_LO_CLOUD("lo clouds"),
	WEATHER_HI_CLOUD("hi clouds"),
	WEATHER_FOGGY("foggy"),
	WEATHER_SNOW("snow"),
	WEATHER_RAIN("rain"),
	WEATHER_DRIZZLE("drizzle"),
	WEATHER_THUNDERSTORM("thunderstorm"),

	TIME_MORNING("morning"),
	TIME_MIDDAY("midday"),
	TIME_EVENING("evening"),
	TIME_NIGHT("night"),

	WEEKDAY_MONDAY("monday"),
	WEEKDAY_TUESDAY("tuesday"),
	WEEKDAY_WEDNESDAY("wednesday"),
	WEEKDAY_THURSDAY("thursday"),
	WEEKDAY_FRIDAY("friday"),
	WEEKDAY_SATURDAY("saturday"),
	WEEKDAY_SUNDAY("sunday"),
	LOCATION("location");


	String name;

	ImageStaticTags(String name) {
		this.name = name;
	}

	public static ImageStaticTags getWeather(int id) {
		if (id < 300) {
			return WEATHER_THUNDERSTORM;
		} else if (id < 400) {
			return WEATHER_DRIZZLE;
		} else if (id < 600) {
			return WEATHER_RAIN;
		} else if (id < 700) {
			return WEATHER_SNOW;
		} else if (id < 800) {
			return WEATHER_FOGGY;
		} else if (id == 800) {
			return WEATHER_CLEAR;
		} else if (id < 803) {
			return WEATHER_LO_CLOUD;
		} else if (id < 805) {
			return WEATHER_HI_CLOUD;
		}
		return null;
	}

	public static ImageStaticTags getTime() {
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

		if (hour < 5) {
			return TIME_NIGHT;
		} else if (hour < 10) {
			return TIME_MORNING;
		} else if (hour < 19) {
			return TIME_MIDDAY;
		} else if (hour < 22) {
			return TIME_EVENING;
		} else {
			return TIME_NIGHT;
		}

	}

	public static ImageStaticTags getWeekday() {
		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		switch (day) {
			case Calendar.MONDAY:
				return WEEKDAY_MONDAY;
			case Calendar.TUESDAY:
				return WEEKDAY_TUESDAY;
			case Calendar.WEDNESDAY:
				return WEEKDAY_WEDNESDAY;
			case Calendar.THURSDAY:
				return WEEKDAY_THURSDAY;
			case Calendar.FRIDAY:
				return WEEKDAY_FRIDAY;
			case Calendar.SATURDAY:
				return WEEKDAY_SATURDAY;
			case Calendar.SUNDAY:
				return WEEKDAY_SUNDAY;
		}
		return WEEKDAY_SUNDAY;
	}

	public String getInternalName() {
		return name();
	}

	public String getVissibleName() {
		return name;
	}
}
