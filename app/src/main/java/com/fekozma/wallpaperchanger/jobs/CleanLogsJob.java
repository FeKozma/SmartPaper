package com.fekozma.wallpaperchanger.jobs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fekozma.wallpaperchanger.database.DBLog;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CleanLogsJob extends Worker {
	private static final String TAG = CleanLogsJob.class.getSimpleName();

	public CleanLogsJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		try {
			DBLog.db.clean();
		} catch (Exception e) {

			FirebaseCrashlytics.getInstance().recordException(e);
			return Result.retry();
		}
		return Result.success();
	}
}
