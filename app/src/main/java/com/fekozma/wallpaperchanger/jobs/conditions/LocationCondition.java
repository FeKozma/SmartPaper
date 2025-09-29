package com.fekozma.wallpaperchanger.jobs.conditions;

import android.content.Context;
import android.location.Location;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLocations;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.lists.tags.TagsListHolder;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class LocationCondition extends ConditionalImagesAndTags {
	@Override
	public void getImages(List<DBImage> images, Location location, OnImagesLoaded onImagesLoaded) {

		List<DBImage> insideRadius = images.stream().filter(image -> isWithinRadius(image, location)).collect(Collectors.toList());
		if (!insideRadius.isEmpty()) {
			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Found images inside radius; " + insideRadius.size());
			onImagesLoaded.onImagesLoaded(insideRadius);
		} else {
			List<DBImage> noRadiusImages = images.stream().filter(this::hasNoRadius).collect(Collectors.toList());
			if (!noRadiusImages.isEmpty()) {

				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Found images that has no locations; " + noRadiusImages.size());
				onImagesLoaded.onImagesLoaded(noRadiusImages);
			} else {
				onImagesLoaded.onImagesLoaded(images);
			}
		}
	}

	@Override
	public List<String> getTags(List<DBImage> images) {

		List<List<DBLocations>> locations = images.stream().map(image -> {
			return DBLocations.db.getLocationByImageName(image.image);
		}).collect(Collectors.toList());

		return new ArrayList<>(locations.stream()
			.map(inner -> inner.stream()
				.map(loc -> loc.address)
				.collect(Collectors.toSet()))
			.reduce((set1, set2) -> {
				set1.retainAll(set2);
				return set1;
			})
			.orElse(new HashSet<>()));
	}

	@Override
	public void edit(Context context, DBImage[] images, Consumer<List<String>> onTagsChanged) {
		LocationUtil.showMapDialog(true, context, () -> {
			},
			(lat, lon) -> {

				LocationUtil.getLocationName(lat, lon, (address) -> {
					for (DBImage image : images) {
						DBLocations.db.setLocation(image.image, lat, lon, address);
					}
					onTagsChanged.accept(Arrays.stream(DBLocations.db.getLocations(List.of(images).stream().map(i -> i.image).collect(Collectors.toList()))).map(loc -> loc.address).collect(Collectors.toList()));
				});
			});
	}

	@Override
	public void setHolder(DBImage[] images, String address, TagsListHolder holder, Runnable onRemove) {
		holder.setIcon(R.drawable.remove_24dp);
		holder.onClicklistener(view -> {
			for (DBImage image : images) {
				DBLocations.db.deleteLocation(image, address);
			}
			onRemove.run();
		});
	}

	private boolean isWithinRadius(DBImage image, Location location) {
		if (location == null) {
			return false;
		}

		float[] results = new float[1];
		int radius = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEYS.LOCATION_RADIUS);

		List<DBLocations> locations = DBLocations.db.getLocationByImageName(image.image);

		return locations.stream().anyMatch((dbLocation) -> {
			android.location.Location.distanceBetween(location.getLatitude(), location.getLongitude(), dbLocation.lat, dbLocation.lon, results);
			float distanceInMeters = results[0];
			return distanceInMeters <= radius * 1000;
		});
	}

	private boolean hasNoRadius(DBImage image) {
		return DBLocations.db.getLocationByImageName(image.image).isEmpty();
	}
}
