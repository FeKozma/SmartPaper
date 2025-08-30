package com.fekozma.wallpaperchanger.database;

import androidx.annotation.ColorInt;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.jobs.conditions.*;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ImageCategories {

	WEATHER(0, "Weather", R.color.weather, true, new WeatherCondition(), List.of(ImageStaticTags.WEATHER_CLEAR, ImageStaticTags.WEATHER_LO_CLOUD, ImageStaticTags.WEATHER_HI_CLOUD, ImageStaticTags.WEATHER_FOGGY, ImageStaticTags.WEATHER_SNOW, ImageStaticTags.WEATHER_RAIN, ImageStaticTags.WEATHER_DRIZZLE, ImageStaticTags.WEATHER_THUNDERSTORM)),
	LOCATION(1, "Nearby", R.color.location, true, new LocationCondition(), List.of()),
	TIME(2, "Time", R.color.time, new TimeCondition(), List.of(ImageStaticTags.TIME_MORNING, ImageStaticTags.TIME_MIDDAY, ImageStaticTags.TIME_EVENING, ImageStaticTags.TIME_NIGHT)),
	WEEKDAY(3, "Weekday", R.color.weekday, new WeekdayCondition(), List.of(ImageStaticTags.WEEKDAY_MONDAY, ImageStaticTags.WEEKDAY_TUESDAY, ImageStaticTags.WEEKDAY_WEDNESDAY, ImageStaticTags.WEEKDAY_THURSDAY, ImageStaticTags.WEEKDAY_FRIDAY, ImageStaticTags.WEEKDAY_SATURDAY, ImageStaticTags.WEEKDAY_SUNDAY));

	private final int startingPos;
	private final ConditionalImages condition;
	private final ConditionalImagesAndTags conditionWTag;
	private final String category;
	private final List<ImageStaticTags> tags;
	private final @ColorInt int color;
	private boolean needsGps = false;

	ImageCategories(int startingPos, String category, int color, ConditionalImages condition, List<ImageStaticTags> tags) {
		this.startingPos = startingPos;
		this.condition = condition;
		this.conditionWTag = null;
		this.category = category;
		this.tags = tags;
		this.color = color;
	}

	ImageCategories(int startingPos, String category, int color, boolean needsGps, ConditionalImages condition, List<ImageStaticTags> tags) {
		this.startingPos = startingPos;
		this.condition = condition;
		this.conditionWTag = null;
		this.category = category;
		this.tags = tags;
		this.color = color;
		this.needsGps = needsGps;
	}

	ImageCategories(int startingPos, String category, int color, boolean needsGps, ConditionalImagesAndTags condition, List<ImageStaticTags> tags) {
		this.startingPos = startingPos;
		this.condition = null;
		this.conditionWTag = condition;
		this.category = category;
		this.tags = tags;
		this.color = color;
		this.needsGps = needsGps;
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

	public List<String> getTags(DBImage[] image) {
		if (conditionWTag != null) {
			return conditionWTag.getTags(Arrays.asList(image));
		} else {
			return tags.stream().map(ImageStaticTags::getInternalName).collect(Collectors.toList());
		}
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

	public boolean isActive() {
		return SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.KEYS.CATEGORY_ACTIVE, this);
	}

	public void setActive(boolean isActive) {
		SharedPreferencesUtil.setBoolean(SharedPreferencesUtil.KEYS.CATEGORY_ACTIVE, this, isActive);
	}

	public boolean needsGps() {
		return this.needsGps;
	}

	public int getStartingPos() {
		int posFromSharedPref = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEYS.CATEGORY_POSITION, this);
		return posFromSharedPref < 0 ? startingPos : posFromSharedPref;
	}

	public void setStartingPos(int pos) {
		SharedPreferencesUtil.setInt(SharedPreferencesUtil.KEYS.CATEGORY_POSITION, this, pos);
	}
}
