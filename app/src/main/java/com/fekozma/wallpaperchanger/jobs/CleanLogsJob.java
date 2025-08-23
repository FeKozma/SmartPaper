package com.fekozma.wallpaperchanger.jobs;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fekozma.wallpaperchanger.database.DBLog;

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
