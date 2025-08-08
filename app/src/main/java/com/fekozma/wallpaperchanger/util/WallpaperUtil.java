package com.fekozma.wallpaperchanger.util;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.fekozma.wallpaperchanger.database.DBLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class WallpaperUtil {
	private static final String TAG = "WallpaperUtil";

	public static void setWallpaperFromFile2(File jpgFile) {
		if (jpgFile == null || !jpgFile.exists()) {
			DBLog.db.addLog(DBLog.LEVELS.ERROR, "Error setting wallpaper: file does not exist. " + ((jpgFile == null) ? "null": jpgFile.getName()));
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

		} catch (IOException e) {
			DBLog.db.addLog(DBLog.LEVELS.ERROR, "Error setting wallpaper: " + e.getMessage(), e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
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
