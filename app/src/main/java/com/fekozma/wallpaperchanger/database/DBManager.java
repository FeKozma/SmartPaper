package com.fekozma.wallpaperchanger.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fekozma.wallpaperchanger.util.ContextUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class DBManager extends SQLiteOpenHelper {

	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 2;
	public static final String DATABASE_NAME = "wallpaperchanger.db";
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
		if (oldVersion == 1 && newVersion == 2) {
			createTable(db, TABLES.LOCATIONS);
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

	public enum TABLES {
		IMAGES("Images", Map.of(DBImage.COL_IMAGE, "varchar(100)", DBImage.COL_TAGS, "TEXT")),
		LOCATIONS("Locations", Map.of(DBLocations.COL_IMAGE, "varchar(100)", DBLocations.COL_ADDRESS, "TEXT", DBLocations.COL_LAT, "TEXT", DBLocations.COL_LON, "TEXT")),
		LOGS("Logs", Map.of(DBLog.COL_MESSAGE, "TEXT", DBLog.COL_DATE, "varchar(100)", DBLog.COL_LEVEL, "varchar(10)"));

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
