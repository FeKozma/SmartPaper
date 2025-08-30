package com.fekozma.wallpaperchanger;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.*;

import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.DBManager;
import com.fekozma.wallpaperchanger.jobs.CleanLogsJob;
import com.fekozma.wallpaperchanger.jobs.RandomImageJob;
import com.fekozma.wallpaperchanger.util.ContextUtil;

import java.util.concurrent.TimeUnit;

import cat.ereza.customactivityoncrash.config.CaocConfig;

public class WallpaperApplication extends Application {

	public static final String wallpaperWorker = "random_background_worker2";
	private static final String TAG = "WallpaperApplication";

	@Override
	public void onCreate() {
		super.onCreate();
		CaocConfig.Builder.create().errorActivity(CustomErrorActivity.class).apply();
		// Force dark mode
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

		ContextUtil.setContext(this.getApplicationContext());
		new DBManager();
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "---- Application started ----");
		new MainActivity();

		WorkManager.getInstance(ContextUtil.getContext()).enqueueUniquePeriodicWork(wallpaperWorker, ExistingPeriodicWorkPolicy.UPDATE,

			new PeriodicWorkRequest.Builder(RandomImageJob.class, 15, TimeUnit.MINUTES)
				.setConstraints(new Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresBatteryNotLow(true)
					.setRequiresDeviceIdle(false)
					.build()).build());

		WorkManager.getInstance(ContextUtil.getContext()).enqueueUniquePeriodicWork("clean_logs", ExistingPeriodicWorkPolicy.UPDATE,

			new PeriodicWorkRequest.Builder(CleanLogsJob.class, 10, TimeUnit.HOURS)
				.setConstraints(new Constraints.Builder()
					.setRequiresDeviceIdle(false)
					.build()).build());


	}
}
