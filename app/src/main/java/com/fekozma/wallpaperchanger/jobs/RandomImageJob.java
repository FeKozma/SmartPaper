package com.fekozma.wallpaperchanger.jobs;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.ImageCategories;
import com.fekozma.wallpaperchanger.jobs.conditions.ConditionalImages;
import com.fekozma.wallpaperchanger.util.ImageUtil;
import com.fekozma.wallpaperchanger.util.WallpaperUtil;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import androidx.concurrent.futures.CallbackToFutureAdapter;


public class RandomImageJob extends ListenableWorker {

	private static Executor executor = Executors.newSingleThreadExecutor();

	private static final String TAG = RandomImageJob.class.getSimpleName();
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
					continueWork(ImageCategories.values(), DBImage.db.getImages(), completer);
				} catch (Exception e) {
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

	private void continueWork(ImageCategories[] values, List<DBImage> images, CallbackToFutureAdapter.Completer<Result> completer) {
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Running work " + values[0].name() + ", nr images before: " + images.size());

		if (values.length > 1) {
			values[0].getCondition().getImages(images, new ConditionalImages.OnImagesLoaded() {
				@Override
				public void onImagesLoaded(List<DBImage> images) {
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Running work " + values[0].name() + ", nr images after: " + images.size());
					continueWork(Arrays.copyOfRange(values, 1,values.length), images, completer);
				}
			});
		} else {
			values[0].getCondition().getImages(images, new ConditionalImages.OnImagesLoaded() {
				@Override
				public void onImagesLoaded(List<DBImage> images) {
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Running work " + values[0].name() + ", nr images after: " + images.size());
					WallpaperUtil.setWallpaperFromFile2(getRandomFile(images));
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Setting background image work completed");
					completer.set(Result.success());
				}
			});
		}


	}
}
