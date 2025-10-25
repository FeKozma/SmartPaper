package com.fekozma.wallpaperchanger.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.fekozma.wallpaperchanger.models.TimeLabel;
import com.fekozma.wallpaperchanger.util.SharedPreferencesUtil;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.ArrayList;
import java.util.List;

public class DBTimes extends DBManager {
	public static final String COL_ID = "id";
	public static final String COL_NAME = "name";
	public static final String COL_START_HOUR = "start_hour";
	public static final String COL_START_MINUTE = "start_minute";
	public static final String COL_END_HOUR = "end_hour";
	public static final String COL_END_MINUTE = "end_minute";
	public static final String COL_IS_PERMANENT = "is_permanent";
	
	public static DBTimes db = new DBTimes();
	
	protected DBTimes() {
	}
	
	/**
	 * Get all time labels from the database
	 */
	public List<TimeLabel> getAllTimeLabels() {
		synchronized (DBManager.DATABASE_NAME) {
			Cursor cursor = getReadableDatabase().query(
				TABLES.TIMES.name,
				TABLES.TIMES.getColumns(),
				null,
				null,
				null,
				null,
				null
			);
			
			return getTimeLabelsFromCursor(cursor);
		}
	}
	
	/**
	 * Helper method to convert cursor to TimeLabel list
	 */
	private List<TimeLabel> getTimeLabelsFromCursor(Cursor cursor) {
		List<TimeLabel> timeLabels = new ArrayList<>();
		
		if (cursor != null && cursor.moveToFirst()) {
			do {
				String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
				int startHour = cursor.getInt(cursor.getColumnIndexOrThrow(COL_START_HOUR));
				int startMinute = cursor.getInt(cursor.getColumnIndexOrThrow(COL_START_MINUTE));
				int endHour = cursor.getInt(cursor.getColumnIndexOrThrow(COL_END_HOUR));
				int endMinute = cursor.getInt(cursor.getColumnIndexOrThrow(COL_END_MINUTE));
				boolean isPermanent = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_PERMANENT)) == 1;
				
				TimeLabel timeLabel = new TimeLabel(name, startHour, startMinute, endHour, endMinute, isPermanent, id);
				timeLabels.add(timeLabel);
			} while (cursor.moveToNext());
			
			cursor.close();
		}
		
		return timeLabels;
	}
	
	/**
	 * Save or update a time label. If a label with the same ID exists, it will be updated.
	 * If not, a new label will be inserted.
	 * Ensures names are unique by checking for existing names (excluding the current ID).
	 */
	public boolean saveTimeLabel(TimeLabel timeLabel) {
		synchronized (DBManager.DATABASE_NAME) {
			SQLiteDatabase db = getWritableDatabase();
			
			try {
				// Check if name is unique (excluding this ID)
				if (!isNameUnique(timeLabel.getName(), timeLabel.getId())) {
					DBLog.db.addLog(DBLog.LEVELS.ERROR, "Time label name '" + timeLabel.getName() + "' already exists");
					return false;
				}
				
				ContentValues values = new ContentValues();
				values.put(COL_ID, timeLabel.getId());
				values.put(COL_NAME, timeLabel.getName());
				values.put(COL_START_HOUR, timeLabel.getStartHour());
				values.put(COL_START_MINUTE, timeLabel.getStartMinute());
				values.put(COL_END_HOUR, timeLabel.getEndHour());
				values.put(COL_END_MINUTE, timeLabel.getEndMinute());
				values.put(COL_IS_PERMANENT, timeLabel.isPermanent() ? 1 : 0);
				
				// Try to update first
				String where = COL_ID + " = ?";
				String[] whereArgs = {timeLabel.getId()};
				
				int updated = db.update(TABLES.TIMES.name, values, where, whereArgs);
				
				if (updated == 0) {
					// No rows updated, insert new
					db.insert(TABLES.TIMES.name, null, values);
					//DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Time label inserted: " + timeLabel.getName());
				} else {
					//DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Time label updated: " + timeLabel.getName());
				}
				
				return true;
			} catch (Exception e) {
				FirebaseCrashlytics.getInstance().recordException(e);
				//DBLog.db.addLog(DBLog.LEVELS.ERROR, "Could not save time label; " + e.getMessage(), e);
				return false;
			} finally {
				db.close();
			}
		}
	}
	
	/**
	 * Save multiple time labels at once
	 */
	public void saveAllTimeLabels(List<TimeLabel> timeLabels) {
		synchronized (DBManager.DATABASE_NAME) {
			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			
			try {
				// Clear existing data
				db.delete(TABLES.TIMES.name, null, null);
				
				// Insert all time labels
				for (TimeLabel timeLabel : timeLabels) {
					ContentValues values = new ContentValues();
					values.put(COL_ID, timeLabel.getId());
					values.put(COL_NAME, timeLabel.getName());
					values.put(COL_START_HOUR, timeLabel.getStartHour());
					values.put(COL_START_MINUTE, timeLabel.getStartMinute());
					values.put(COL_END_HOUR, timeLabel.getEndHour());
					values.put(COL_END_MINUTE, timeLabel.getEndMinute());
					values.put(COL_IS_PERMANENT, timeLabel.isPermanent() ? 1 : 0);
					
					db.insert(TABLES.TIMES.name, null, values);
				}
				
				db.setTransactionSuccessful();
				//DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Saved " + timeLabels.size() + " time labels");
			} catch (Exception e) {
				FirebaseCrashlytics.getInstance().recordException(e);
				//DBLog.db.addLog(DBLog.LEVELS.ERROR, "Could not save all time labels; " + e.getMessage(), e);
			} finally {
				db.endTransaction();
				db.close();
			}
		}
	}
	
	/**
	 * Delete a time label by ID
	 */
	public void deleteTimeLabel(String id) {
		synchronized (DBManager.DATABASE_NAME) {
			SQLiteDatabase db = getWritableDatabase();
			
			try {
				db.delete(
					TABLES.TIMES.name,
					COL_ID + " = ?",
					new String[]{id}
				);
				//DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Time label deleted: " + id);
			} catch (Exception e) {
				FirebaseCrashlytics.getInstance().recordException(e);
				//DBLog.db.addLog(DBLog.LEVELS.ERROR, "Could not delete time label; " + e.getMessage(), e);
			} finally {
				db.close();
			}
		}
	}
	
	/**
	 * Check if a name is unique (case-insensitive)
	 * @param name The name to check
	 * @param excludeId ID to exclude from check (for updates)
	 * @return true if name is unique, false otherwise
	 */
	private boolean isNameUnique(String name, String excludeId) {
		synchronized (DBManager.DATABASE_NAME) {
			String where = "LOWER(" + COL_NAME + ") = ? AND " + COL_ID + " != ?";
			String[] whereArgs = {name.toLowerCase(), excludeId};
			
			Cursor cursor = getReadableDatabase().query(
				TABLES.TIMES.name,
				new String[]{COL_ID},
				where,
				whereArgs,
				null,
				null,
				null,
				"1"
			);
			
			boolean isUnique = true;
			if (cursor != null) {
				isUnique = !cursor.moveToFirst();
				cursor.close();
			}
			
			return isUnique;
		}
	}
	
	/**
	 * Initialize default time labels if they don't exist in the database.
	 * This method should be called during database creation or upgrade.
	 * It also handles migration from SharedPreferences if needed.
	 */
	public void initializeDefaultTimeLabels() {
		synchronized (DBManager.DATABASE_NAME) {
			// Check if time labels already exist
			List<TimeLabel> existingLabels = getAllTimeLabels();
			
			if (!existingLabels.isEmpty()) {
				// Time labels already exist, no initialization needed
				return;
			}
			
			// Create and save default labels
			createAndSaveDefaultLabels();
		}
	}
	
	/**
	 * Initialize default time labels using an existing database connection.
	 * This version is called during onUpgrade to avoid database locking issues.
	 * @param db The already-open database connection
	 */
	public void initializeDefaultTimeLabels(SQLiteDatabase db) {
		synchronized (DBManager.DATABASE_NAME) {
			// Check if time labels already exist using the provided connection
			Cursor cursor = db.query(
				TABLES.TIMES.name,
				new String[]{COL_ID},
				null,
				null,
				null,
				null,
				null,
				"1"
			);
			
			boolean hasData = cursor != null && cursor.moveToFirst();
			if (cursor != null) {
				cursor.close();
			}
			
			if (hasData) {
				// Time labels already exist, no initialization needed
				return;
			}
			
			// Create default labels and save using the provided connection
			List<TimeLabel> labels = createDefaultLabels();
			saveAllTimeLabels(db, labels);
		}
	}
	
	/**
	 * Create the list of default time labels with migration from SharedPreferences if available
	 */
	private List<TimeLabel> createDefaultLabels() {
		List<TimeLabel> labels = new ArrayList<>();
		
		// Check SharedPreferences for migration
		String savedLabels = SharedPreferencesUtil.getString(SharedPreferencesUtil.KEYS.TIME_LABELS);
		
		if (savedLabels != null && !savedLabels.isEmpty()) {
			// Migrate from SharedPreferences
			String[] labelStrings = savedLabels.split(";");
			for (String labelString : labelStrings) {
				TimeLabel label = TimeLabel.fromStorageString(labelString);
				if (label != null) {
					labels.add(label);
				}
			}
			
			// Clear SharedPreferences after migration
			if (!labels.isEmpty()) {
				SharedPreferencesUtil.setString(SharedPreferencesUtil.KEYS.TIME_LABELS, "");
				DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Migrated time labels from SharedPreferences to database");
			}
		} else {
			// No data in SharedPreferences, create defaults
			// Check if old morning values exist and migrate them
			int morningStart = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEYS.MORNING_START);
			int morningStartMinute = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEYS.MORNING_START_MINUTE);
			int morningEnd = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEYS.MORNING_END);
			int morningEndMinute = SharedPreferencesUtil.getInt(SharedPreferencesUtil.KEYS.MORNING_END_MINUTE);
			
			// If values exist, use them; otherwise use defaults
			if (morningStart == 0 && morningStartMinute == 0 && morningEnd == 0 && morningEndMinute == 0) {
				// Use default values
				labels.add(new TimeLabel("Morning", 6, 0, 12, 0, true, "morning"));
			} else {
				// Migrate old values
				labels.add(new TimeLabel("Morning", morningStart, morningStartMinute, morningEnd, morningEndMinute, true, "morning"));
			}
			
			labels.add(new TimeLabel("Midday", 12, 0, 17, 0, true, "midday"));
			labels.add(new TimeLabel("Evening", 17, 0, 21, 0, true, "evening"));
			labels.add(new TimeLabel("Night", 21, 0, 6, 0, true, "night"));
			
		}
		
		return labels;
	}
	
	/**
	 * Create and save default labels (convenience method)
	 */
	private void createAndSaveDefaultLabels() {
		List<TimeLabel> labels = createDefaultLabels();
		saveAllTimeLabels(labels);
	}
	
	/**
	 * Save multiple time labels using an existing database connection
	 */
	private void saveAllTimeLabels(SQLiteDatabase db, List<TimeLabel> timeLabels) {
		db.beginTransaction();
		
		try {
			// Clear existing data
			db.delete(TABLES.TIMES.name, null, null);
			
			// Insert all time labels
			for (TimeLabel timeLabel : timeLabels) {
				ContentValues values = new ContentValues();
				values.put(COL_ID, timeLabel.getId());
				values.put(COL_NAME, timeLabel.getName());
				values.put(COL_START_HOUR, timeLabel.getStartHour());
				values.put(COL_START_MINUTE, timeLabel.getStartMinute());
				values.put(COL_END_HOUR, timeLabel.getEndHour());
				values.put(COL_END_MINUTE, timeLabel.getEndMinute());
				values.put(COL_IS_PERMANENT, timeLabel.isPermanent() ? 1 : 0);
				
				db.insert(TABLES.TIMES.name, null, values);
			}
			
			db.setTransactionSuccessful();
		} catch (Exception e) {
			FirebaseCrashlytics.getInstance().recordException(e);
		} finally {
			db.endTransaction();
		}
	}
}
