package com.fekozma.wallpaperchanger.jobs;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.jobs.conditions.ConditionalImages;
import com.fekozma.wallpaperchanger.util.ImageUtil;
import com.fekozma.wallpaperchanger.util.LocationUtil;
import com.fekozma.wallpaperchanger.util.WallpaperUtil;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RandomImageJob extends ListenableWorker {

	private static final String TAG = RandomImageJob.class.getSimpleName();
	private static final Executor executor = Executors.newSingleThreadExecutor();

	public RandomImageJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public ListenableFuture<Result> startWork() {
		return CallbackToFutureAdapter.getFuture(completer -> {
			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Setting background image work started");

			executor.execute(() -> {
				try {
					List<ImageCategories> categories =
						Stream.of(ImageCategories.values())
							.filter(ImageCategories::isActive)
							.collect(Collectors.toList());

					if (categories.isEmpty()) {
						completer.set(Result.success());
						return;
					}

					categories.sort(Comparator.comparingInt(ImageCategories::getStartingPos));
					if (categories.stream().anyMatch(ImageCategories::needsGps)) {
						LocationUtil.getCurrentLocation((location) -> {
							continueWork(categories, DBImage.db.getImages(), location, completer);
						});
					} else {
						continueWork(categories, DBImage.db.getImages(), null, completer);
					}

				} catch (Exception e) {
					FirebaseCrashlytics.getInstance().recordException(e);
					DBLog.db.addLog(DBLog.LEVELS.ERROR, "Setting background image work failed with error: " + e.getMessage(), e);
					completer.set(Result.retry());
				}
			});

			return TAG;
		});
	}

	private File getRandomFile(List<DBImage> files) {

		if (files == null || files.isEmpty()) {
			return null; // no images
		}

		Random random = new Random();
		int randomIndex = random.nextInt(files.size()); // random index
		return ImageUtil.toFile(files.get(randomIndex));
	}

	private void continueWork(List<ImageCategories> categories, List<DBImage> images, Location location, CallbackToFutureAdapter.Completer<Result> completer) {
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Running work " + categories.get(0).name() + ", nr images before: " + images.size());

		if (categories.size() > 1) {
			categories.get(0).getCondition().getImages(images, location, new ConditionalImages.OnImagesLoaded() {
				@Override
				public void onImagesLoaded(List<DBImage> images) {
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Running work " + categories.get(0).name() + ", nr images after: " + images.size());
					continueWork(categories.subList(1, categories.size()), images, location, completer);
				}
			});
		} else {
			categories.get(0).getCondition().getImages(images, location, new ConditionalImages.OnImagesLoaded() {
				@Override
				public void onImagesLoaded(List<DBImage> images) {
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Running work " + categories.get(0).name() + ", nr images after: " + images.size());
					WallpaperUtil.setWallpaperFromFile(getRandomFile(images));
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Setting background image work completed");
					completer.set(Result.success());
				}
			});
		}


	}
}
