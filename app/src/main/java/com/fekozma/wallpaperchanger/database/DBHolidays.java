package com.fekozma.wallpaperchanger.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fekozma.wallpaperchanger.models.Holiday;

import java.util.ArrayList;
import java.util.List;

public class DBHolidays {
	public static final String COL_ID = "id";
	public static final String COL_NAME = "name";
	public static final String COL_START_DATE = "start_date";
	public static final String COL_END_DATE = "end_date";
	public static final String COL_TYPE = "type";
	public static final String COL_NATIONWIDE = "nationwide";

	public static DBHolidays db = new DBHolidays();

	private DBHolidays() {
	}

	/**
	 * Add or update a holiday in the database
	 */
	public void addOrUpdateHoliday(String id, String name, String startDate, String endDate, String type, boolean nationwide) {
		SQLiteDatabase db = new DBManager().getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COL_ID, id);
		values.put(COL_NAME, name);
		values.put(COL_START_DATE, startDate);
		values.put(COL_END_DATE, endDate);
		values.put(COL_TYPE, type);
		values.put(COL_NATIONWIDE, nationwide ? 1 : 0);

		// Use REPLACE to insert or update
		db.replace(DBManager.TABLES.HOLIDAYS.name, null, values);
		db.close();
	}

	/**
	 * Get all holidays from the database
	 */
	public List<Holiday> getAllHolidays() {
		List<Holiday> holidays = new ArrayList<>();
		SQLiteDatabase db = new DBManager().getReadableDatabase();

		Cursor cursor = db.query(
			DBManager.TABLES.HOLIDAYS.name,
			DBManager.TABLES.HOLIDAYS.getColumns(),
			null,
			null,
			null,
			null,
			COL_START_DATE + " ASC"
		);

		if (cursor.moveToFirst()) {
			do {
				String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
				String startDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_DATE));
				String endDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_DATE));
				String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
				boolean nationwide = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NATIONWIDE)) == 1;

				holidays.add(new Holiday(id, name, startDate, endDate, type, nationwide));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		return holidays;
	}

	/**
	 * Get holidays that fall on a specific date
	 */
	public List<Holiday> getHolidaysForDate(String date) {
		List<Holiday> holidays = new ArrayList<>();
		SQLiteDatabase db = new DBManager().getReadableDatabase();

		// Query holidays where the date falls between start_date and end_date
		String selection = COL_START_DATE + " <= ? AND " + COL_END_DATE + " >= ?";
		String[] selectionArgs = {date, date};

		Cursor cursor = db.query(
			DBManager.TABLES.HOLIDAYS.name,
			DBManager.TABLES.HOLIDAYS.getColumns(),
			selection,
			selectionArgs,
			null,
			null,
			COL_START_DATE + " ASC"
		);

		if (cursor.moveToFirst()) {
			do {
				String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
				String startDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_START_DATE));
				String endDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_END_DATE));
				String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
				boolean nationwide = cursor.getInt(cursor.getColumnIndexOrThrow(COL_NATIONWIDE)) == 1;

				holidays.add(new Holiday(id, name, startDate, endDate, type, nationwide));
			} while (cursor.moveToNext());
		}

		cursor.close();
		db.close();
		return holidays;
	}

	/**
	 * Clear all holidays from the database
	 */
	public void clearAllHolidays() {
		SQLiteDatabase db = new DBManager().getWritableDatabase();
		db.delete(DBManager.TABLES.HOLIDAYS.name, null, null);
		db.close();
	}

	/**
	 * Delete old holidays (before a specific date)
	 */
	public void deleteOldHolidays(String beforeDate) {
		SQLiteDatabase db = new DBManager().getWritableDatabase();
		String whereClause = COL_END_DATE + " < ?";
		String[] whereArgs = {beforeDate};
		db.delete(DBManager.TABLES.HOLIDAYS.name, whereClause, whereArgs);
		db.close();
	}

	/**
	 * Check if holidays exist in the database
	 */
	public boolean hasHolidays() {
		SQLiteDatabase db = new DBManager().getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DBManager.TABLES.HOLIDAYS.name, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		db.close();
		return count > 0;
	}

	/**
	 * Initialize/update holidays from OpenHolidays API
	 * This should be called when the app starts to ensure holidays are up to date
	 */
	public void initializeHolidays(String countryCode, String languageCode) {
		com.fekozma.wallpaperchanger.api.OpenHolidaysApi holidaysApi = com.fekozma.wallpaperchanger.api.HttpClient.getHolidaysApi();
		
		// Get current year and next year
		java.util.Calendar calendar = java.util.Calendar.getInstance();
		int currentYear = calendar.get(java.util.Calendar.YEAR);
		
		String validFrom = currentYear + "-01-01";
		String validTo = (currentYear + 1) + "-12-31";
		
		DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Loading holidays from OpenHolidays API for " + countryCode);
		
		retrofit2.Call<java.util.List<com.fekozma.wallpaperchanger.api.OpenHolidaysApi.HolidayResponse>> call = 
			holidaysApi.getPublicHolidays(countryCode, languageCode, validFrom, validTo);
		
		call.enqueue(new retrofit2.Callback<java.util.List<com.fekozma.wallpaperchanger.api.OpenHolidaysApi.HolidayResponse>>() {
			@Override
			public void onResponse(retrofit2.Call<java.util.List<com.fekozma.wallpaperchanger.api.OpenHolidaysApi.HolidayResponse>> call, 
				retrofit2.Response<java.util.List<com.fekozma.wallpaperchanger.api.OpenHolidaysApi.HolidayResponse>> response) {
				
				if (response.isSuccessful() && response.body() != null) {
					java.util.List<com.fekozma.wallpaperchanger.api.OpenHolidaysApi.HolidayResponse> holidays = response.body();
					
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Loaded " + holidays.size() + " holidays from API");
					
					// Delete old holidays before current year
					deleteOldHolidays(currentYear + "-01-01");
					
					// Add or update each holiday
					for (com.fekozma.wallpaperchanger.api.OpenHolidaysApi.HolidayResponse holiday : holidays) {
						// Only store public holidays that are nationwide
						if ("Public".equals(holiday.type) && holiday.nationwide) {
							String name = holiday.getLocalizedName(languageCode);
							addOrUpdateHoliday(
								holiday.id,
								name,
								holiday.startDate,
								holiday.endDate,
								holiday.type,
								holiday.nationwide
							);
						}
					}
					
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Holidays initialized successfully, saved " + holidays.size() + " holidays");
				} else {
					DBLog.db.addLog(DBLog.LEVELS.WARNING, "Failed to load holidays: " + response.message());
				}
			}
			
			@Override
			public void onFailure(retrofit2.Call<java.util.List<com.fekozma.wallpaperchanger.api.OpenHolidaysApi.HolidayResponse>> call, Throwable t) {
				DBLog.db.addLog(DBLog.LEVELS.ERROR, "Failed to load holidays: " + t.getMessage(), t);
			}
		});
	}
}
