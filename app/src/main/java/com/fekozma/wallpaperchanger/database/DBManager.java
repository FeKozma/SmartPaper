package com.fekozma.wallpaperchanger.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.fekozma.wallpaperchanger.util.ContextUtil;

import java.util.*;
import java.util.stream.Collectors;

public class DBManager extends SQLiteOpenHelper {

	// IMPORTANT: An increment of the database version is needed if the database schema is changed!
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "wallpaperchanger.db";

	public DBManager() {
		// TODO: Do we want to add error handling here or where it's created instead?
		super(ContextUtil.getContext(), DATABASE_NAME, null, DATABASE_VERSION);
	}

	public enum TABLES {
		IMAGES("Images", Map.of(DBImage.COL_IMAGE, "varchar(100)", DBImage.COL_TAGS, "TEXT")),
		LOGS("Logs", Map.of(DBLog.COL_MESSAGE, "TEXT", DBLog.COL_DATE, "varchar(100)", DBLog.COL_LEVEL, "varchar(10)"));

		final String name;
		final Map<String, String> columns;

		TABLES(String name, Map<String, String> columns) {
			this.name = name;
			this.columns = columns;
		}

		String[] getColumns() {
			return this.columns.keySet().toArray(new String[0]);
		}
	}

	public void onCreate(SQLiteDatabase db) {
		synchronized (DATABASE_NAME) {
			Arrays.stream(TABLES.values()).map(table -> {
				String start = "CREATE TABLE " + table.name + " (";
				String end = ");";
				return table.columns.entrySet().stream().map(entry -> entry.getKey() + " " + entry.getValue()).collect(Collectors.joining(",", start, end));
			}).forEach(db::execSQL);
		}
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
