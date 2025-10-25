package com.fekozma.wallpaperchanger.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBSystemSettings extends DBManager {
	public static final String COL_NAME = "name";
	public static final String COL_VALUE = "value";

	public static DBSystemSettings db = new DBSystemSettings();

	protected DBSystemSettings() {
	}

	public void setSystemSetting(String name, Integer value) {
		synchronized (DBManager.DATABASE_NAME) {

			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues(2);
			values.put(COL_NAME, name);
			values.put(COL_VALUE, value);

			db.replace(TABLES.SYSTEM_SETTINGS.name, null, values);

			db.close();
		}
	}

	public Integer getIntValue(String name) {
		String value = getValue(name);
		if (value == null) {
			return null;
		}
		return Integer.parseInt(value);
	}

	private String getValue(String name) {
		String result = null;
		synchronized (DBManager.DATABASE_NAME) {
			Cursor cursor = getReadableDatabase().query(
				TABLES.SYSTEM_SETTINGS.name,
				TABLES.SYSTEM_SETTINGS.getColumns(),
				COL_NAME + " = ?",
				new String[]{name},
				null,
				null,
				null);
			if (cursor.moveToFirst()) {
				result = cursor.getString(cursor.getColumnIndexOrThrow(COL_VALUE));
			}
			cursor.close();
		}
		return result;
	}
}
