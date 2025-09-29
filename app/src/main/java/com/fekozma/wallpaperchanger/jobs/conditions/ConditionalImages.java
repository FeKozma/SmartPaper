package com.fekozma.wallpaperchanger.jobs.conditions;

import com.fekozma.wallpaperchanger.database.DBImage;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ConditionalImages {

	public static boolean noCommonElements(List<String> list1, List<String> list2) {
		// Use a Set for fast lookup
		Set<String> set = new HashSet<>(list1);

		for (String s : list2) {
			if (set.contains(s)) {
				return false; // Found a match
			}
		}
		return true; // No matches
	}

	public abstract void getImages(List<DBImage> images, OnImagesLoaded onImagesLoaded);

	public abstract static class OnImagesLoaded {
		public abstract void onImagesLoaded(List<DBImage> images);
	}

}
