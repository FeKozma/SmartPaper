package com.fekozma.wallpaperchanger.jobs.conditions;

import android.location.Location;

import com.fekozma.wallpaperchanger.database.DBHolidays;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.models.Holiday;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HolidayCondition extends ConditionalImages {
	@Override
	public void getImages(List<DBImage> images, Location location, OnImagesLoaded onImagesLoaded) {
		// Get current date in YYYY-MM-DD format
		String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
		
		// Get holidays for current date
		List<Holiday> todaysHolidays = DBHolidays.db.getHolidaysForDate(currentDate);
		
		if (todaysHolidays.isEmpty()) {
			// No holidays today, return images without holiday tags
			List<DBImage> noHolidayTags = images.stream()
				.filter(image -> noCommonElements(List.of(image.tags), ImageCategories.HOLIDAY.getTagsInternalName()))
				.collect(Collectors.toList());
			
			if (noHolidayTags.isEmpty()) {
				onImagesLoaded.onImagesLoaded(images);
			} else {
				onImagesLoaded.onImagesLoaded(noHolidayTags);
			}
		} else {
			// Get tag IDs for today's holidays
			List<String> holidayTags = new ArrayList<>();
			for (Holiday holiday : todaysHolidays) {
				holidayTags.add(holiday.getTagId());
			}
			
			// Filter images that have any of today's holiday tags
			List<DBImage> filteredImages = images.stream()
				.filter(image -> {
					List<String> imageTags = List.of(image.tags);
					return holidayTags.stream().anyMatch(imageTags::contains);
				})
				.collect(Collectors.toList());

			if (filteredImages.isEmpty()) {
				// No images with holiday tags, return images without any holiday tags
				List<DBImage> noHolidayTags = images.stream()
					.filter(image -> noCommonElements(List.of(image.tags), ImageCategories.HOLIDAY.getTagsInternalName()))
					.collect(Collectors.toList());
				
				if (noHolidayTags.isEmpty()) {
					onImagesLoaded.onImagesLoaded(images);
				} else {
					onImagesLoaded.onImagesLoaded(noHolidayTags);
				}
			} else {
				onImagesLoaded.onImagesLoaded(filteredImages);
			}
		}
	}
}
