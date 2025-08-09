package com.fekozma.wallpaperchanger.database;

import androidx.annotation.ColorInt;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.jobs.conditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum StaticValues {

	WEATHER("weather", R.color.weather, new WeatherCondition(), List.of(StaticTags.WEATHER_CLEAR, StaticTags.WEATHER_LO_CLOUD, StaticTags.WEATHER_HI_CLOUD, StaticTags.WEATHER_FOGGY, StaticTags.WEATHER_SNOW,StaticTags.WEATHER_RAIN,  StaticTags.WEATHER_DRIZZLE, StaticTags.WEATHER_THUNDERSTORM)),
	TIME("time", R.color.time, new TimeCondition(), List.of(StaticTags.TIME_MORNING, StaticTags.TIME_MIDDAY, StaticTags.TIME_EVENING, StaticTags.TIME_NIGHT)),
	WEEKDAY("weekday", R.color.weekday, new WeekdayCondition(), List.of(StaticTags.WEEKDAY_MONDAY, StaticTags.WEEKDAY_TUESDAY, StaticTags.WEEKDAY_WEDNESDAY, StaticTags.WEEKDAY_THURSDAY, StaticTags.WEEKDAY_FRIDAY, StaticTags.WEEKDAY_SATURDAY, StaticTags.WEEKDAY_SUNDAY)),
	LOCATION("location", R.color.location, new LocationCondition(), List.of());

	private final ConditionalImages condition;
	private final ConditionalImagesAndTags conditionWTag;
	private String category;
	private List<StaticTags> tags;
	private @ColorInt int color;

	StaticValues(String category, int color, ConditionalImages condition, List<StaticTags> tags) {
		this.condition = condition;
		this.conditionWTag = null;
		this.category = category;
		this.tags = tags;
		this.color = color;
	}

	StaticValues(String category, int color, ConditionalImagesAndTags condition, List<StaticTags> tags) {
		this.condition = null;
		this.conditionWTag = condition;
		this.category = category;
		this.tags = tags;
		this.color = color;
	}

	public List<String> getTags(DBImage[] image) {
		if (conditionWTag != null) {
			return conditionWTag.getTags(Arrays.asList(image));
		} else {
			return tags.stream().map(StaticTags::getName).collect(Collectors.toList());
		}
	}

	public static List<StaticTags> getSelections(StaticValues val, DBImage image) {

		List tags = List.of(image.tags);
		return val.tags.stream().filter(tag -> tags.contains(tag.getName())).collect(Collectors.toList());
	}

	public static List<StaticTags> getCommonSelections(StaticValues val, DBImage[] images) {
		if (images == null || images.length == 0) {
			return new ArrayList<>();
		}
		return val.tags.stream().filter(tag -> {
			long count = Arrays.stream(images)
				.filter(image -> StaticValues.hasTag(tag, image))
				.count();
			return count == images.length;
			
		}).collect(Collectors.toList());
	}

	public static boolean hasTag(StaticTags tag, DBImage image) {
		return List.of(image.tags).contains(tag.getName());
	}
	public static boolean hasTag(String tag, DBImage image) {

		return List.of(image.tags).contains(tag);
	}

	public String getCategory() {
		return category;
	}

	public int getColor() {
		return this.color;
	}

	public ConditionalImages getCondition() {
		return (condition == null) ? conditionWTag : condition;
	}
}
