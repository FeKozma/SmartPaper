package com.fekozma.wallpaperchanger.jobs.conditions;

import com.fekozma.wallpaperchanger.database.DBImage;

import java.util.List;

public abstract class ConditionalImages {

	public abstract void getImages(List<DBImage> images, OnImagesLoaded onImagesLoaded);

	public abstract static class OnImagesLoaded {
		public abstract void onImagesLoaded(List<DBImage> images);
	}

}
