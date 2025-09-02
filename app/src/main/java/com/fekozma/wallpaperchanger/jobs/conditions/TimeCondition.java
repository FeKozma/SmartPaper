package com.fekozma.wallpaperchanger.jobs.conditions;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.ImageStaticTags;
import com.fekozma.wallpaperchanger.database.ImageCategories;

import java.util.List;
import java.util.stream.Collectors;

public class TimeCondition extends ConditionalImages {
	@Override
	public void getImages(List<DBImage> images, OnImagesLoaded onImagesLoaded) {


		List<DBImage> filteredImages = images.stream().filter(image -> List.of(image.tags).contains(ImageStaticTags.getTime().getInternalName())).collect(Collectors.toList());

		if (filteredImages.isEmpty()) {
			List<DBImage> noTimeTags = images.stream().filter(image -> noCommonElements(List.of(image.tags), ImageCategories.TIME.getTags())).collect(Collectors.toList());
			if (noTimeTags.isEmpty()) {
				onImagesLoaded.onImagesLoaded(images);
			} else {
				onImagesLoaded.onImagesLoaded(noTimeTags);
			}
		} else {
			onImagesLoaded.onImagesLoaded(filteredImages);
		}
	}

}
