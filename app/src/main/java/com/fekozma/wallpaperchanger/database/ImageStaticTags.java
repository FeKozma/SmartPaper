package com.fekozma.wallpaperchanger.database;

import java.util.Calendar;

public enum ImageStaticTags {
	WEATHER_CLEAR("Clear sky"),
	WEATHER_LO_CLOUD("Lo clouds"),
	WEATHER_HI_CLOUD("Hi clouds"),
	WEATHER_FOGGY("Foggy"),
	WEATHER_SNOW("Snow"),
	WEATHER_RAIN("Rain"),
	WEATHER_DRIZZLE("Drizzle"),
	WEATHER_THUNDERSTORM("Thunderstorm"),

	TIME_MORNING("Morning"),
	TIME_MIDDAY("Midday"),
	TIME_EVENING("Evening"),
	TIME_NIGHT("Night"),

	WEEKDAY_MONDAY("Monday"),
	WEEKDAY_TUESDAY("Tuesday"),
	WEEKDAY_WEDNESDAY("Wednesday"),
	WEEKDAY_THURSDAY("Thursday"),
	WEEKDAY_FRIDAY("Friday"),
	WEEKDAY_SATURDAY("Saturday"),
	WEEKDAY_SUNDAY("Sunday"),

	LOCATION("Location");

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
