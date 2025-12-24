package com.fekozma.wallpaperchanger.jobs;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.fekozma.wallpaperchanger.database.DBHolidays;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Locale;

public class HolidayJob extends Worker {
	private static final String TAG = HolidayJob.class.getSimpleName();

	public HolidayJob(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork() {
		try {
			// Initialize holidays from OpenHolidays API
			// Auto-detect country and language codes from device locale
			String countryCode = Locale.getDefault().getCountry().toUpperCase();
			String languageCode = Locale.getDefault().getLanguage().toUpperCase();

			// Fallback to SE if locale returns empty values
			if (countryCode.isEmpty()) {
				countryCode = "SE";
			}
			if (languageCode.isEmpty()) {
				languageCode = "SE";
			}


			DBHolidays.db.initializeHolidays(countryCode, languageCode);
		} catch (Exception e) {

			FirebaseCrashlytics.getInstance().recordException(e);
			return Result.retry();
		}
		return Result.success();
	}
}
