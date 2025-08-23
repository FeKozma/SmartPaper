package com.fekozma.wallpaperchanger.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DBLog extends DBManager {

	public static DBLog db = new DBLog();

	public static final String COL_MESSAGE = "message";
	public static final String COL_DATE = "date";
	public static final String COL_LEVEL = "level";
	private static final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");


	public String message;
	public String date;
	public String level;

	private DBLog(String message, String date, String level) {
		this.message = message;
		this.date = date;
		this.level = level;
	}

	private DBLog() {}

	public void clean() {
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Cleaning logs");
		synchronized (DBManager.DATABASE_NAME) {

			SQLiteDatabase db = getWritableDatabase();
			db.execSQL("DELETE FROM " + TABLES.LOGS.name + " WHERE date NOT IN (SELECT date FROM " + TABLES.LOGS.name + " ORDER BY date DESC LIMIT 1000)");
			db.close();
		}
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Logs cleaned");
	}

	public enum LEVELS {
		DEBUG("d"),
		INFO("i"),
		WARNING("w"),
		ERROR("e");

		private final String name;

		LEVELS(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public List<DBLog> getLogs() {
		synchronized (DBManager.DATABASE_NAME) {

			Cursor cursor = getReadableDatabase().query(TABLES.LOGS.name, TABLES.LOGS.getColumns(), null, null, null, null, COL_DATE + " DESC");
			return getLogs(cursor);
		}
	}

	public DBLog addLog(LEVELS level, String message) {
		return addLog(level, message, null);
	}

	public DBLog addLog(LEVELS level, String message, Throwable e) {
		if (level.equals(LEVELS.DEBUG)) {
			Log.d("SmartPage", message, e);
		} else if (level.equals(LEVELS.WARNING)) {
			Log.w("SmartPage", message, e);
		} else if (level.equals(LEVELS.ERROR)) {
			Log.e("SmartPage", message, e);
		} else if (level.equals(LEVELS.INFO)) {
			Log.i("SmartPage", message, e);
		}



		synchronized (DBManager.DATABASE_NAME) {
			SQLiteDatabase db = getWritableDatabase();

			// Join tags into a single comma-separated string

			ContentValues values = new ContentValues();
			values.put(COL_DATE, LocalDateTime.now().format(dateFmt));
			values.put(COL_LEVEL, level.name);
			values.put(COL_MESSAGE, message);

			// Insert into the database
			db.insert(TABLES.LOGS.name, null, values);
			db.close();
		}

		return new DBLog(message, LocalDate.now().toString(), level.name);
	}


	private List<DBLog> getLogs(Cursor cursor) {
		List<DBLog> dbLogs = new ArrayList<>();

		if (cursor != null && cursor.moveToFirst()) {
			do {
				DBLog dbLog = new DBLog();

				// Get the image column (assuming it's stored as a String path or Base64)
				dbLog.date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));

				// Get the tags column (assuming it's stored as a comma-separated string)
				dbLog.message = cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE));
				dbLog.level = cursor.getString(cursor.getColumnIndexOrThrow(COL_LEVEL));

				dbLogs.add(dbLog);
			} while (cursor.moveToNext());

			cursor.close();
		}
		return dbLogs;
	}

}
