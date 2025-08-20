package com.fekozma.wallpaperchanger.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.*;
import java.util.stream.Collectors;

public class DBLocations extends DBManager {

	public static final String COL_IMAGE = "image";
	public static final String COL_LON = "lon";
	public static final String COL_LAT = "lat";
	public static final String COL_ADDRESS = "address";

	public String image;
	public Double lon;
	public Double lat;
	public String address;

	public static DBLocations db = new DBLocations();

	protected DBLocations() {}


	public DBLocations(String address, String image, Double lat, Double lon) {
		this.address = address;
		this.image = image;
		this.lat = lat;
		this.lon = lon;
	}

	private List<DBLocations> getLocations(Cursor cursor) {
		synchronized (DBManager.DATABASE_NAME) {
			List<DBLocations> locations = new ArrayList<>();

			if (cursor != null && cursor.moveToFirst()) {
				do {
					DBLocations dbLocation = new DBLocations();

					// Get the image column (assuming it's stored as a String path or Base64)
					dbLocation.image = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE));

					String lat = cursor.getString(cursor.getColumnIndexOrThrow(COL_LAT));
					if (lat != null && !lat.isEmpty()) {
						dbLocation.lat = Double.valueOf(lat.substring(0, 12));
					}

					String lon = cursor.getString(cursor.getColumnIndexOrThrow(COL_LON));
					if (lon != null && !lon.isEmpty()) {
						dbLocation.lon = Double.valueOf(lat.substring(0, 12));
					}
					dbLocation.address = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDRESS));

					locations.add(dbLocation);
				} while (cursor.moveToNext());

				cursor.close();
			}
			return locations;
		}
	}

	public DBLocations setLocation(String image, Double lat, Double lon, String address) {
		synchronized (DBManager.DATABASE_NAME) {

			SQLiteDatabase db = getWritableDatabase();
			try {

				String where = COL_IMAGE + " = ? AND " + COL_LAT + " = ? AND " + COL_LON + " = ?";
				String latStr = lat + "000000000000";
				String lonStr = lon + "000000000000";
				String[] whereArgs = { image, latStr.substring(0, 12), lonStr.substring(0, 12) };

				ContentValues updateValues = new ContentValues();
				updateValues.put(COL_ADDRESS, address);

				int updated = db.update(TABLES.LOCATIONS.name, updateValues, where, whereArgs);
				if (updated == 0) {
					// nothing updated → insert new row
					ContentValues insertValues = new ContentValues();
					insertValues.put(COL_IMAGE, image);
					insertValues.put(COL_LAT, latStr.substring(0, 12));
					insertValues.put(COL_LON, lonStr.substring(0, 12));
					insertValues.put(COL_ADDRESS, address);
					db.insert(TABLES.LOCATIONS.name, null, insertValues);
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Location inserted for image " + image);
				} else {
					DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Location updated for image " + image);
				}
			} catch (Exception e) {
				DBLog.db.addLog(DBLog.LEVELS.ERROR, "Could not set location; " + e.getMessage(), e);
			} finally {
				db.close();
			}

			return new DBLocations(address, image, lat, lon);
		}
	}

	public DBLocations[] getLocations(List<String> imagesNames) {
		return imagesNames.stream()
			.map(this::getLocationByImageName)
			.flatMap(List::stream)
			.toArray(DBLocations[]::new);
	}

	public List<DBLocations> getLocationByImageName(String imageName) {

		synchronized (DBManager.DATABASE_NAME) {

			Cursor cursor = getReadableDatabase().query(
				TABLES.LOCATIONS.name,                       // Table name
				TABLES.LOCATIONS.getColumns(),               // Columns to return
				COL_IMAGE + " = ?",                          // WHERE clause
				new String[]{imageName},                     // WHERE arguments
				null,                                        // groupBy
				null,                                        // having
				null,                                        // orderBy
				null                                         // Limit to 1 result
			);
			return getLocations(cursor);
		}

	}

	public void deleteLocations(String[] selected) {
		synchronized (DBManager.DATABASE_NAME) {

			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			try {
				for (String image : selected) {
					db.delete(
						TABLES.LOCATIONS.name,
						COL_IMAGE + " = ?",
						new String[] { image }
					);
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				db.close();
			}
		}
	}

	public void deleteLocation(DBImage image, String address) {
		synchronized (DBManager.DATABASE_NAME) {

			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			try {
				db.delete(
					TABLES.LOCATIONS.name,
					COL_IMAGE + " = ? AND " + COL_ADDRESS + " = ?",
					new String[] {
						image.image, address
					}
				);

				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
				db.close();
			}
		}
	}
}
