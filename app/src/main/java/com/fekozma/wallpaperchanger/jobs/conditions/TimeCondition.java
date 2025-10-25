package com.fekozma.wallpaperchanger.jobs.conditions;

import android.content.Context;
import android.location.Location;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBTimes;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.database.ImageStaticTags;
import com.fekozma.wallpaperchanger.lists.tags.TagsListHolder;
import com.fekozma.wallpaperchanger.models.TimeLabel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TimeCondition extends ConditionalImagesAndTags {
	@Override
	public void getImages(List<DBImage> images, Location location, OnImagesLoaded onImagesLoaded) {


		List<DBImage> filteredImages = images.stream().filter(image -> List.of(image.tags).contains(ImageStaticTags.getTime().getInternalName())).collect(Collectors.toList());

		if (filteredImages.isEmpty()) {
			List<DBImage> noTimeTags = images.stream().filter(image -> noCommonElements(List.of(image.tags), ImageCategories.TIME.getTagsInternalName())).collect(Collectors.toList());
			if (noTimeTags.isEmpty()) {
				onImagesLoaded.onImagesLoaded(images);
			} else {
				onImagesLoaded.onImagesLoaded(noTimeTags);
			}
		} else {
			onImagesLoaded.onImagesLoaded(filteredImages);
		}
	}

	@Override
	public List<String> getTags(List<DBImage> image) {
		ArrayList<String> tags = DBTimes.db.getAllTimeLabels().stream().map(TimeLabel::getName).collect(Collectors.toCollection(ArrayList::new));
		tags.addAll(ImageCategories.TIME.getTagsPresentationName());
		return new ArrayList<>(new LinkedHashSet<>(tags));
	}

	@Override
	public void edit(Context context, DBImage[] images, Consumer<List<String>> onTagsChanged) {

	}

	@Override
	public void setHolder(DBImage[] images, String address, TagsListHolder holder, Runnable onRemove) {

	}
}
