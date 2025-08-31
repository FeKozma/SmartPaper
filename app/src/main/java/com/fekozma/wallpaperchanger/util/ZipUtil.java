package com.fekozma.wallpaperchanger.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.fekozma.wallpaperchanger.R;
import com.fekozma.wallpaperchanger.database.DBLog;
import com.fekozma.wallpaperchanger.database.DBManager;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

	private static TextView progressText; // TODO: Android context class would be a memory leak here. "A static field will leak contexts."
	private static AlertDialog dialog;
	private static int count = 0;
	private static Handler mainHandler = new Handler(Looper.getMainLooper());

	// Import

	public static void importFromZip(Context context, Uri zipUri) {

		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				// 1. Get target paths
				File wallpapersDir = new File(context.getFilesDir(), "wallpapers");
				File dbFile = context.getDatabasePath("wallpaperchanger.db");

				// 2. Clear old data
				deleteRecursive(wallpapersDir);
				if (dbFile.exists()) dbFile.delete();

				// 3. Open ZIP input stream
				InputStream inputStream = context.getContentResolver().openInputStream(zipUri);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream));
				ZipEntry entry;

				while ((entry = zis.getNextEntry()) != null) {
					String name = entry.getName();

					File outFile;
					if (name.startsWith("wallpapers/")) {
						outFile = new File(wallpapersDir, name.substring("wallpapers/".length()));
					} else if (name.equals("database/wallpaperchanger.db")) {
						outFile = dbFile;
					} else {
						continue; // skip unknown entries
					}

					// Ensure parent folders exist
					if (entry.isDirectory()) {
						outFile.mkdirs();
					} else {
						File parent = outFile.getParentFile();
						if (parent != null && !parent.exists()) parent.mkdirs();

						FileOutputStream fos = new FileOutputStream(outFile);
						byte[] buffer = new byte[1024];
						int count;
						while ((count = zis.read(buffer)) != -1) {
							fos.write(buffer, 0, count);
						}
						fos.close();
					}
				}

				zis.closeEntry();
				zis.close();

				mainHandler.post(() -> {
					Toast.makeText(context, "Import succeeded, restarting application", Toast.LENGTH_LONG).show();
				});
				throw new RuntimeException("Import succeeded\nThis is intential. Not beautiful, but it works");

			} catch (IOException e) {
				FirebaseCrashlytics.getInstance().recordException(e);
				mainHandler.post(() -> {
					Toast.makeText(context, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
				});
			}
		});
	}

	private static void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory == null || !fileOrDirectory.exists()) return;

		if (fileOrDirectory.isDirectory()) {
			for (File child : fileOrDirectory.listFiles()) {
				deleteRecursive(child);
			}
		}
		fileOrDirectory.delete();
	}

	// Export

	public static void exportAppDataAndShare(Activity activity) {
		String exportName = activity.getString(R.string.app_name) + "_export";


		showLoading(activity);
		Executors.newSingleThreadExecutor().execute(() -> {
			try {
				// 1. Create ZIP
				File wallpapersDir = new File(activity.getFilesDir(), "wallpapers");
				File dbFile = activity.getDatabasePath("wallpaperchanger.db");

				File exportDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), exportName);

				if (!exportDir.exists()) exportDir.mkdirs();

				File zipFile = getNewZipFile(exportDir, exportName);
				ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));

				if (wallpapersDir.exists()) {
					zipFileRecursive(zos, wallpapersDir, wallpapersDir.getAbsolutePath());
				}
				if (dbFile.exists()) {
					addFileToZip(zos, prepareSanitizedDatabase(activity), "database/wallpaperchanger.db");
				}
				zos.close();

				mainHandler.post(() -> {
					showExportDialog(activity, zipFile);
				});

			} catch (IOException e) {
				FirebaseCrashlytics.getInstance().recordException(e);
				DBLog.db.addLog(DBLog.LEVELS.ERROR, "Could not export dataset: " + e.getMessage(), e);
				Snackbar.make(activity.findViewById(android.R.id.content), "Export failed: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
			}
			dialog.dismiss();

		});
	}

	private static File getNewZipFile(File dir, String name) {
		File zipFile = new File(dir, name + ".zip");

		if (zipFile.exists()) {
			int count = 1;
			do {
				String newName = name + "(" + count + ").zip";
				zipFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), newName);
				count++;
			} while (zipFile.exists());
		}
		return zipFile;
	}

	private static File prepareSanitizedDatabase(Context context) throws IOException {
		File originalDb = context.getDatabasePath("wallpaperchanger.db");

		File tempDbDir = new File(context.getCacheDir(), "tempdb");
		if (!tempDbDir.exists()) tempDbDir.mkdirs();

		File tempDb = new File(tempDbDir, "wallpaperchanger_sanitized.db");

		// Copy original DB to temp location
		try (FileChannel src = new FileInputStream(originalDb).getChannel();
			 FileChannel dst = new FileOutputStream(tempDb).getChannel()) {
			dst.transferFrom(src, 0, src.size());
		}

		// Open the temp copy for modification
		SQLiteDatabase db = SQLiteDatabase.openDatabase(tempDb.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

		// Run cleanup queries
		db.execSQL("DELETE FROM " + DBManager.TABLES.LOGS.name());
		db.close();

		return tempDb;
	}

	private static void zipFileRecursive(ZipOutputStream zos, File file, String basePath) throws IOException {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				zipFileRecursive(zos, child, basePath);
			}
		} else {
			String relativePath = file.getAbsolutePath().substring(basePath.length() + 1);
			addFileToZip(zos, file, "wallpapers/" + relativePath);
		}
	}

	private static void addFileToZip(ZipOutputStream zos, File file, String zipPath) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		ZipEntry zipEntry = new ZipEntry(zipPath);
		zos.putNextEntry(zipEntry);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = fis.read(buffer)) > 0) {
			zos.write(buffer, 0, length);
		}

		zos.closeEntry();
		fis.close();

		mainHandler.post(() -> {
			count++;
			progressText.setText(count + " files zipped");
		});
	}

	public static void showExportDialog(Activity activity, File zipFile) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		AlertDialog dialog2 = builder.setTitle("Export Successful")
			.setMessage("Your backup ZIP file has been created.\n\nName: " + zipFile.getName())
			.setPositiveButton("Share", (dialog, which) -> {
				shareZipFile(activity, zipFile);
			})
			.setNegativeButton("Locate in Files", (dialog, which) -> {
				openZipInFileExplorer(activity, zipFile);
			})
			.setNeutralButton("Cancel", null).create();
		dialog2.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
		dialog2.show();
	}

	private static void shareZipFile(Activity activity, File zipFile) {
		Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", zipFile);

		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("application/zip");
		shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
		shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		activity.startActivity(Intent.createChooser(shareIntent, "Share backup ZIP"));
	}

	private static void openZipInFileExplorer(Activity activity, File zipFile) {
		Uri uri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", zipFile);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, "application/zip");
		intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

		try {
			activity.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Snackbar.make(activity.findViewById(android.R.id.content), "No app found to open ZIP file", Snackbar.LENGTH_LONG).show();
		}
	}

	private static void showLoading(Activity activity) {
		LayoutInflater inflater = LayoutInflater.from(activity);
		View dialogView = inflater.inflate(R.layout.dialog_export_progress, null);
		progressText = dialogView.findViewById(R.id.progress_text);
		count = 0;
		dialog = new AlertDialog.Builder(activity)
			.setView(dialogView)
			.setCancelable(false)
			.create();
		dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
		dialog.show();
	}

}
