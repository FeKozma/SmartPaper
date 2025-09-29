package com.fekozma.wallpaperchanger.jobs.conditions;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.database.ImageStaticTags;

import java.util.List;
import java.util.stream.Collectors;

public class WeekdayCondition extends ConditionalImages {
	@Override
	public void getImages(List<DBImage> images, OnImagesLoaded onImagesLoaded) {

		List<DBImage> filteredImages = images.stream()
			.filter(image -> List.of(image.tags).contains(ImageStaticTags.getWeekday().getInternalName()))
			.collect(Collectors.toList());

		if (filteredImages.isEmpty()) {
			List<DBImage> noWeekdayTags = images.stream()
				.filter(image -> noCommonElements(List.of(image.tags), ImageCategories.WEEKDAY.getTags()))
				.collect(Collectors.toList());

			if (noWeekdayTags.isEmpty()) {
				onImagesLoaded.onImagesLoaded(images);
			} else {
				onImagesLoaded.onImagesLoaded(noWeekdayTags);
			}

		} else {
			onImagesLoaded.onImagesLoaded(filteredImages);
		}
	}
}
