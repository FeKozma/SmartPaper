package com.fekozma.wallpaperchanger.util;

import android.net.Uri;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.fekozma.wallpaperchanger.database.DBImage;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.google.android.material.snackbar.Snackbar;

import java.io.*;
import java.util.Optional;

public class ImageUtil {

	private static final String IMAGE_FOLDER = "wallpapers";

	public static Optional<String> saveImageToAppstorage(Uri sourceUri, CoordinatorLayout snackbar) {
		File destinationDir = new File(ContextUtil.getContext().getFilesDir(), IMAGE_FOLDER);

		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}

		String newFileName = "image_" + System.currentTimeMillis() + (int) (Math.random() * 10001) + ".jpg";
		File destinationFile = new File(destinationDir, newFileName);

		try (InputStream in = ContextUtil.getContext().getContentResolver().openInputStream(sourceUri);
			 OutputStream out = new FileOutputStream(destinationFile)) {
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Image saved to app storage: " + destinationFile.getName());
		} catch (IOException e) {
			DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Image failed: " + e.getMessage(), e);
			Snackbar.make(snackbar, "Error saving image: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
			return Optional.ofNullable((String) null);
		}
		return Optional.of(newFileName);
	}

	public static Optional<File> getImageFromAppstorage(DBImage image) {
		File file = toFile(image);
		if (file.exists()) {
			return Optional.of(file);
		}
		return Optional.ofNullable((File) null);
	}

	public static File toFile(DBImage dbImage) {
		File destinationDir = new File(ContextUtil.getContext().getFilesDir(), IMAGE_FOLDER);
		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}
		return new File(destinationDir, dbImage.image);
	}
}
