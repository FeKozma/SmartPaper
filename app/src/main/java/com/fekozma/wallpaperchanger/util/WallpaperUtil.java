package com.fekozma.wallpaperchanger.util;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.fekozma.wallpaperchanger.database.DBLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WallpaperUtil {
	private static final String TAG = "WallpaperUtil";

	public static void setWallpaperFromFile(File jpgFile) {
		if (jpgFile == null || !jpgFile.exists()) {
			DBLog.db.addLog(DBLog.LEVELS.ERROR, "Error setting wallpaper; File does not exist: " + ((jpgFile == null) ? "null" : jpgFile.getName()));
			return;
		}
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(ContextUtil.getContext());
		FileInputStream fis = null;

		try {
			fis = new FileInputStream(jpgFile);
			Bitmap bitmap = BitmapFactory.decodeStream(fis);

			// Get EXIF orientation
			ExifInterface exif = new ExifInterface(jpgFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			// Rotate if needed
			bitmap = rotateBitmapIfNeeded(bitmap, orientation);

			// Set as wallpaper
			if (SharedPreferencesUtil.getBoolean(SharedPreferencesUtil.KEYS.ONLY_LOCKSCREEN)) {
				wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
			} else {
				wallpaperManager.setBitmap(bitmap);
			}
			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Updated wallpaper; " + jpgFile.getName());
			FirebaseLogUtil.logImageWallpaperEvent();

		} catch (IOException e) {
			FirebaseCrashlytics.getInstance().recordException(e);
			DBLog.db.addLog(DBLog.LEVELS.ERROR, "Error setting wallpaper: " + e.getMessage(), e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					FirebaseCrashlytics.getInstance().recordException(e);
					DBLog.db.addLog(DBLog.LEVELS.ERROR, "Error closing file in WallpaperUtil: " + e.getMessage(), e);
				}
			}
		}
	}

	private static Bitmap rotateBitmapIfNeeded(Bitmap bitmap, int orientation) {
		Matrix matrix = new Matrix();

		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				matrix.postRotate(90);
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				matrix.postRotate(180);
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				matrix.postRotate(270);
				break;
			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				matrix.preScale(-1, 1);
				break;
			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				matrix.preScale(1, -1);
				break;
			default:
				return bitmap; // No rotation needed
		}

		try {
			Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			bitmap.recycle(); // Recycle old bitmap to free memory
			return rotated;
		} catch (OutOfMemoryError e) {
			// Fallback: return original
			DBLog.db.addLog(DBLog.LEVELS.ERROR, "OOM rotating bitmap: " + e.getMessage(), e);
			return bitmap;
		}
	}
}
