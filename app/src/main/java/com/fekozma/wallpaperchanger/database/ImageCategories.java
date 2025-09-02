package com.fekozma.wallpaperchanger.database;

import androidx.annotation.ColorInt;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.jobs.conditions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ImageCategories {

	WEATHER("weather", R.color.weather, new WeatherCondition(), List.of(ImageStaticTags.WEATHER_CLEAR, ImageStaticTags.WEATHER_LO_CLOUD, ImageStaticTags.WEATHER_HI_CLOUD, ImageStaticTags.WEATHER_FOGGY, ImageStaticTags.WEATHER_SNOW, ImageStaticTags.WEATHER_RAIN,  ImageStaticTags.WEATHER_DRIZZLE, ImageStaticTags.WEATHER_THUNDERSTORM)),
	TIME("time", R.color.time, new TimeCondition(), List.of(ImageStaticTags.TIME_MORNING, ImageStaticTags.TIME_MIDDAY, ImageStaticTags.TIME_EVENING, ImageStaticTags.TIME_NIGHT)),
	WEEKDAY("weekday", R.color.weekday, new WeekdayCondition(), List.of(ImageStaticTags.WEEKDAY_MONDAY, ImageStaticTags.WEEKDAY_TUESDAY, ImageStaticTags.WEEKDAY_WEDNESDAY, ImageStaticTags.WEEKDAY_THURSDAY, ImageStaticTags.WEEKDAY_FRIDAY, ImageStaticTags.WEEKDAY_SATURDAY, ImageStaticTags.WEEKDAY_SUNDAY)),
	LOCATION("location", R.color.location, new LocationCondition(), List.of());

	private final ConditionalImages condition;
	private final ConditionalImagesAndTags conditionWTag;
	private String category;
	private List<ImageStaticTags> tags;
	private @ColorInt int color;

	ImageCategories(String category, int color, ConditionalImages condition, List<ImageStaticTags> tags) {
		this.condition = condition;
		this.conditionWTag = null;
		this.category = category;
		this.tags = tags;
		this.color = color;
	}

	ImageCategories(String category, int color, ConditionalImagesAndTags condition, List<ImageStaticTags> tags) {
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
			return tags.stream().map(ImageStaticTags::getInternalName).collect(Collectors.toList());
		}
	}

	public static List<ImageStaticTags> getSelections(ImageCategories category, DBImage image) {

		List tags = List.of(image.tags);
		return category.tags.stream().filter(tag -> tags.contains(tag.getInternalName())).collect(Collectors.toList());
	}

	public static List<ImageStaticTags> getCommonSelections(ImageCategories category, DBImage[] images) {
		if (images == null || images.length == 0) {
			return new ArrayList<>();
		}
		return category.tags.stream().filter(tag -> {
			long count = Arrays.stream(images)
				.filter(image -> ImageCategories.hasTag(tag, image))
				.count();
			return count == images.length;
			
		}).collect(Collectors.toList());
	}

	public static boolean hasTag(ImageStaticTags tag, DBImage image) {
		return List.of(image.tags).contains(tag.getInternalName());
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

	public List<String> getTags() {
		return tags.stream().map(ImageStaticTags::getInternalName).collect(Collectors.toList());
	}
}
