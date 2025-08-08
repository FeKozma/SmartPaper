package com.fekozma.wallpaperchanger.jobs.conditions;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.StaticTags;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TimeCondition extends ConditionalImages{
	@Override
	public void getImages(List<DBImage> images, OnImagesLoaded onImagesLoaded) {


		List<DBImage> filteredImages = images.stream().filter(image -> List.of(image.tags).contains(StaticTags.getTime().getName())).collect(Collectors.toList());

		if (filteredImages.isEmpty()) {
			onImagesLoaded.onImagesLoaded(images);
		} else {
			onImagesLoaded.onImagesLoaded(filteredImages);
		}
	}
}
