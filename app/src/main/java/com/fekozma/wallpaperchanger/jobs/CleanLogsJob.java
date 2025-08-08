package com.fekozma.wallpaperchanger.jobs;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.StaticValues;
import com.fekozma.wallpaperchanger.jobs.conditions.ConditionalImages;
import com.fekozma.wallpaperchanger.util.ImageUtil;
import com.fekozma.wallpaperchanger.util.WallpaperUtil;

import java.io.File;
import java.util.List;
import java.util.Random;

public class CleanLogsJob extends Worker {
	private static final String TAG = CleanLogsJob.class.getSimpleName();
	public CleanLogsJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}
	@NonNull
	@Override
	public Result doWork() {
		DBLog.db.clean();
		return Result.success();
	}
}
