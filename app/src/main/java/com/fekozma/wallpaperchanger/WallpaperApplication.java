package com.fekozma.wallpaperchanger;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.*;

import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.DBManager;
import com.fekozma.wallpaperchanger.jobs.CleanLogsJob;
import com.fekozma.wallpaperchanger.jobs.HolidayJob;
import com.fekozma.wallpaperchanger.jobs.RandomImageJob;
import com.fekozma.wallpaperchanger.util.ContextUtil;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;

import java.util.Locale;
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
		DBManager dbManager = new DBManager();
		dbManager.checkAndUpgradeIfNeeded();
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "---- Application started ----");
		
		new MainActivity();

		setWallpaperJob(SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.UPDATE_FREQUENCY));

		WorkManager.getInstance(ContextUtil.getContext()).enqueueUniquePeriodicWork("holiday", ExistingPeriodicWorkPolicy.UPDATE,

			new PeriodicWorkRequest.Builder(HolidayJob.class, 3, TimeUnit.DAYS)
				.setConstraints(new Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresDeviceIdle(false)
					.build()).build());

		WorkManager.getInstance(ContextUtil.getContext()).enqueueUniquePeriodicWork("clean_logs", ExistingPeriodicWorkPolicy.UPDATE,

			new PeriodicWorkRequest.Builder(CleanLogsJob.class, 10, TimeUnit.HOURS)
				.setConstraints(new Constraints.Builder()
					.setRequiresDeviceIdle(false)
					.build()).build());


	}

	public static void updateWallpaperJob(String intervalTime) {
		WorkManager workManager = WorkManager.getInstance(ContextUtil.getContext());

		// Cancel existing work
		workManager.cancelUniqueWork(WallpaperApplication.wallpaperWorker);

		setWallpaperJob(intervalTime);

		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "RandomImageJob rescheduled to every " + intervalTime + ".");
	}

	private static void setWallpaperJob(int intervalMinutes) {
		WorkManager.getInstance(ContextUtil.getContext()).enqueueUniquePeriodicWork(wallpaperWorker, ExistingPeriodicWorkPolicy.UPDATE,

			new PeriodicWorkRequest.Builder(RandomImageJob.class, intervalMinutes, TimeUnit.MINUTES)
				.setConstraints(new Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresBatteryNotLow(true)
					.setRequiresDeviceIdle(false)
					.build()).build());
	}

	private static void setWallpaperJob(String intervalTime) {
		int intervalMinutes;
		if (intervalTime.endsWith("m")) {
			intervalMinutes = Integer.parseInt(intervalTime.replace("m", ""));
		} else if (intervalTime.endsWith("h")) {
			intervalMinutes = Integer.parseInt(intervalTime.replace("h", "")) * 60;
		} else {
			throw new RuntimeException("Cant parse update frequency -> " + intervalTime);
		}
		setWallpaperJob(intervalMinutes);
	}
}
