package com.fekozma.wallpaperchanger.util;

import android.content.Context;
import android.os.Bundle;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseLogUtil {

	private static FirebaseAnalytics mFirebaseAnalytics;

	public static void init(Context context) {
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
	}

	public static void logScreenEvent(Class<?> clazz) {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, clazz.getSimpleName());
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle);
	}

	public static void logImagesAddEvent() {
		logImagesEvent(false, true);
	}

	public static void logImagesDeleteEvent() {
		logImagesEvent(true, false);
	}

	private static void logImagesEvent(boolean delete, boolean add) {
		Bundle bundle = new Bundle();
		bundle.putBoolean("deleteing_images", delete);
		bundle.putBoolean("adding_images", add);
		bundle.putLong("nr_images", DBImage.db.getImagesCount());
		mFirebaseAnalytics.logEvent("images_change", bundle);
	}

	public static void logImageWallpaperEvent() {
		Bundle bundle = new Bundle();
		mFirebaseAnalytics.logEvent("wallpaper_change", bundle);
	}
}
