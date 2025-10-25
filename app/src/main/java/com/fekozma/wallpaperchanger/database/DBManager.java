package com.fekozma.wallpaperchanger.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fekozma.wallpaperchanger.util.ContextUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DBManager extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "wallpaperchanger.db";
	public static final String DB_VERSION_KEY = "db_version";
	private static DBManager dbManager;
	public DBManager() {
		super(ContextUtil.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		synchronized (DATABASE_NAME) {
			Arrays.stream(TABLES.values()).forEach(table -> {
				createTable(db, table);
			});
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Perform incremental upgrades
		if (oldVersion < 2) {
			// Upgrade from version 1 to 2
			createTable(db, TABLES.LOCATIONS);
		}
		if (oldVersion < 3) {
			// Upgrade from version 2 to 3
			createTable(db, TABLES.TIMES);
			createTable(db, TABLES.SYSTEM_SETTINGS);
		}
	}

	private void createTable(SQLiteDatabase db, TABLES table) {
		String start = "CREATE TABLE " + table.name + " (";
		String end = ");";
		db.execSQL(table.columns.entrySet().stream().map(entry -> entry.getKey() + " " + entry.getValue()).collect(Collectors.joining(",", start, end)));
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	/**
	 * Checks if a table exists in the database
	 */
	private boolean tableExists(SQLiteDatabase db, String tableName) {
		Cursor cursor = db.rawQuery(
			"SELECT name FROM sqlite_master WHERE type='table' AND name=?",
			new String[]{tableName}
		);
		boolean exists = cursor.getCount() > 0;
		cursor.close();
		return exists;
	}

	/**
	 * Checks database version and performs upgrade if necessary.
	 * This should be called after database initialization.
	 */
	public void checkAndUpgradeIfNeeded() {
		SQLiteDatabase db = getWritableDatabase();
		
		// Check if SystemSettings table exists
		boolean systemSettingsExists = tableExists(db, TABLES.SYSTEM_SETTINGS.name);
		
		if (!systemSettingsExists) {
			// SystemSettings table doesn't exist, assume DB is version 1
			// Run upgrade from version 1 to current version
			onUpgrade(db, 1, DATABASE_VERSION);
			
			// Store the current version in SystemSettings
			DBSystemSettings.db.setSystemSetting(DB_VERSION_KEY, DATABASE_VERSION);
		} else {
			// SystemSettings table exists, check stored version
			Integer storedVersion = DBSystemSettings.db.getIntValue(DB_VERSION_KEY);
			
			if (storedVersion != null && storedVersion < DATABASE_VERSION) {
				// Need to upgrade from stored version to current version
				onUpgrade(db, storedVersion, DATABASE_VERSION);
				
				// Update the stored version
				DBSystemSettings.db.setSystemSetting(DB_VERSION_KEY, DATABASE_VERSION);
			}
		}
		
		db.close();
	}

	public enum TABLES {
		IMAGES("Images", Map.of(DBImage.COL_IMAGE, "varchar(100)", DBImage.COL_TAGS, "TEXT")),
		SYSTEM_SETTINGS("SystemSettings", Map.of(DBSystemSettings.COL_NAME, "TEXT PRIMARY KEY", DBSystemSettings.COL_VALUE, "TEXT")),
		LOCATIONS("Locations", Map.of(DBLocations.COL_IMAGE, "varchar(100)", DBLocations.COL_ADDRESS, "TEXT", DBLocations.COL_LAT, "TEXT", DBLocations.COL_LON, "TEXT")),
		LOGS("Logs", Map.of(DBLog.COL_MESSAGE, "TEXT", DBLog.COL_DATE, "varchar(100)", DBLog.COL_LEVEL, "varchar(10)")),
		TIMES("Times", Map.of(
			DBTimes.COL_ID, "TEXT PRIMARY KEY",
			DBTimes.COL_NAME, "TEXT",
			DBTimes.COL_START_HOUR, "INTEGER",
			DBTimes.COL_START_MINUTE, "INTEGER",
			DBTimes.COL_END_HOUR, "INTEGER",
			DBTimes.COL_END_MINUTE, "INTEGER",
			DBTimes.COL_IS_PERMANENT, "INTEGER"
		));

		String name;
		Map<String, String> columns;

		TABLES(String name, Map<String, String> columns) {
			this.name = name;
			this.columns = columns;
		}

		String[] getColumns() {
			return columns.keySet().toArray(new String[0]);
		}

	}


}
