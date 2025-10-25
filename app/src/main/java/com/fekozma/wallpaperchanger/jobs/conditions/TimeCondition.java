package com.fekozma.wallpaperchanger.jobs.conditions;

import android.location.Location;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBTimes;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.database.ImageStaticTags;
import com.fekozma.wallpaperchanger.models.TimeLabel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class TimeCondition extends ConditionalImages {
	@Override
	public void getImages(List<DBImage> images, Location location, OnImagesLoaded onImagesLoaded) {
		// Get current time matching tags (both static and user-defined)
		List<String> currentTimeTags = getCurrentTimeTags();
		
		// Filter images that have any of the current time tags
		List<DBImage> filteredImages = images.stream()
			.filter(image -> {
				List<String> imageTags = List.of(image.tags);
				return currentTimeTags.stream().anyMatch(imageTags::contains);
			})
			.collect(Collectors.toList());

		if (filteredImages.isEmpty()) {
			List<DBImage> noTimeTags = images.stream().filter(image -> noCommonElements(List.of(image.tags), ImageCategories.TIME.getTags(null))).collect(Collectors.toList());
			if (noTimeTags.isEmpty()) {
				onImagesLoaded.onImagesLoaded(images);
			} else {
				onImagesLoaded.onImagesLoaded(noTimeTags);
			}
		} else {
			onImagesLoaded.onImagesLoaded(filteredImages);
		}
	}
	
	/**
	 * Get all time tags that match the current time (both static and user-defined)
	 */
	private List<String> getCurrentTimeTags() {
		List<String> matchingTags = new ArrayList<>();
		
		// Add user-defined time labels that match current time
		Calendar now = Calendar.getInstance();
		int currentHour = now.get(Calendar.HOUR_OF_DAY);
		int currentMinute = now.get(Calendar.MINUTE);
		int currentTimeInMinutes = currentHour * 60 + currentMinute;
		
		List<TimeLabel> userTimeLabels = DBTimes.db.getAllTimeLabels();
		for (TimeLabel label : userTimeLabels) {
			if (isCurrentTimeInRange(label, currentTimeInMinutes)) {
				matchingTags.add(label.getId());
			}
		}
		
		return matchingTags;
	}
	
	/**
	 * Check if the current time falls within a time label's range
	 */
	private boolean isCurrentTimeInRange(TimeLabel label, int currentTimeInMinutes) {
		int startTimeInMinutes = label.getStartHour() * 60 + label.getStartMinute();
		int endTimeInMinutes = label.getEndHour() * 60 + label.getEndMinute();
		
		// Handle time ranges that cross midnight
		if (startTimeInMinutes > endTimeInMinutes) {
			// Range crosses midnight (e.g., 22:00 - 04:00)
			return currentTimeInMinutes >= startTimeInMinutes || currentTimeInMinutes < endTimeInMinutes;
		} else {
			// Normal range within the same day
			return currentTimeInMinutes >= startTimeInMinutes && currentTimeInMinutes < endTimeInMinutes;
		}
	}

}
