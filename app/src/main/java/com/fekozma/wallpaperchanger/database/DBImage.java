package com.fekozma.wallpaperchanger.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.fekozma.wallpaperchanger.util.ImageUtil;

import java.util.*;
import java.util.stream.Collectors;

public class DBImage extends DBManager implements Parcelable {

	public static final String COL_IMAGE = "image";
	public static final String COL_TAGS = "tags";

	public String image;
	public String[] tags;

	public static DBImage db = new DBImage();

	protected DBImage() {}

	private DBImage(String image, String[] tags) {
		this.image = image;
		this.tags = tags;
	}

	protected DBImage(Parcel in) {
		image = in.readString();
		tags = in.createStringArray();
	}

	public List<DBImage> getImages() {
		synchronized (DBManager.DATABASE_NAME) {

			Cursor cursor = getReadableDatabase().query(TABLES.IMAGES.name, TABLES.IMAGES.getColumns(), null, null, null, null, DBImage.COL_IMAGE + " DESC");

			return getImages(cursor);
		}
	}

	private List<DBImage> getImages(Cursor cursor) {
		List<DBImage> images = new ArrayList<>();

		if (cursor != null && cursor.moveToFirst()) {
			do {
				DBImage dbImage = new DBImage();

				// Get the image column (assuming it's stored as a String path or Base64)
				dbImage.image = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE));

				// Get the tags column (assuming it's stored as a comma-separated string)
				String tagsString = cursor.getString(cursor.getColumnIndexOrThrow(COL_TAGS));
				if (!tagsString.isEmpty()) {
					dbImage.tags = tagsString.split(",\\s*"); // split by comma and optional spaces
				} else {
					dbImage.tags = new String[0];
				}

				images.add(dbImage);
			} while (cursor.moveToNext());

			cursor.close();
		}
		return images;
	}

	public DBImage setImage(String image, String[] tags) {
		SQLiteDatabase db = getWritableDatabase();

		if (tags == null) {
			tags = new String[0];
		}

		// Join tags into a single comma-separated string
		String tagsString = String.join(",", tags);

		ContentValues values = new ContentValues();
		values.put(COL_IMAGE, image);
		values.put(COL_TAGS, tagsString);

		// Insert into the database
		db.insert(TABLES.IMAGES.name, null, values);
		db.close();

		return new DBImage(image, tags);
	}

	public void upsertImage(String image, String[] tags) {
		int rowsAffected;
		synchronized (DBManager.DATABASE_NAME) {

			SQLiteDatabase db = getWritableDatabase();

			if (tags == null) {
				tags = new String[0];
			}

			// Join tags into a single comma-separated string
			String tagsString = String.join(",", tags);

			ContentValues values = new ContentValues();
			values.put(COL_TAGS, tagsString);

			// Update tags where image matches
			rowsAffected = db.update(TABLES.IMAGES.name, values, COL_IMAGE + " = ?", new String[]{image});
			db.close();
		}

		if (rowsAffected == 0) {
			// Optional: Insert if image not found
			setImage(image, tags);
		}
	}

	public void upsertImageTag(DBImage org, String newTag) {
		Set<String> tags = new HashSet<>();
		org = getImageByName(org.image);
		tags.addAll(Arrays.stream(org.tags).toList());
		tags.add(newTag);
		upsertImage(org.image, tags.toArray(new String[0]));
	}

	public void removeImageTag(DBImage org, String deleteTag) {
		Set<String> tags = new HashSet<>();
		org = getImageByName(org.image);
		tags.addAll(Arrays.stream(org.tags).toList());
		tags.remove(deleteTag);
		upsertImage(org.image, tags.toArray(new String[0]));
	}

	public DBImage[] getImages(DBImage[] images) {
		return Arrays.stream(images).map(image -> getImageByName(image.image)).collect(Collectors.toList()).toArray(new DBImage[0]);
	}

	public DBImage[] getImages(List<String> imagesNames) {
		return imagesNames.stream().map(imageName -> getImageByName(imageName)).collect(Collectors.toList()).toArray(new DBImage[0]);
	}

	public DBImage getImageByName(String imageName) {
		DBImage dbImage = null;

		synchronized (DBManager.DATABASE_NAME) {

			Cursor cursor = getReadableDatabase().query(
				TABLES.IMAGES.name,                         // Table name
				TABLES.IMAGES.getColumns(),                  // Columns to return
				COL_IMAGE + " = ?",                          // WHERE clause
				new String[]{imageName},                     // WHERE arguments
				null,                                        // groupBy
				null,                                        // having
				null,                                        // orderBy
				"1"                                          // Limit to 1 result
			);

			if (cursor != null && cursor.moveToFirst()) {
				dbImage = new DBImage();

				// Get the image column
				dbImage.image = cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGE));

				// Get the tags column (assuming it's stored as a comma-separated string)
				String tagsString = cursor.getString(cursor.getColumnIndexOrThrow(COL_TAGS));
				if (!tagsString.isEmpty()) {
					dbImage.tags = tagsString.split(",\\s*"); // split by comma and optional spaces
				} else {
					dbImage.tags = new String[0];
				}

				cursor.close();
			}
		}

		return dbImage; // Returns null if not found
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int i) {
		dest.writeString(image);
		dest.writeStringArray(tags);
	}

	public static final Creator<DBImage> CREATOR = new Creator<>() {
		@Override
		public DBImage createFromParcel(Parcel in) {
			return new DBImage(in);
		}

		@Override
		public DBImage[] newArray(int size) {
			return new DBImage[size];
		}
	};

	public boolean deleteImages(DBImage[] selected) {
		boolean success = false;
		synchronized (DBManager.DATABASE_NAME) {

			SQLiteDatabase db = getWritableDatabase();
			db.beginTransaction();
			try {
				for (DBImage image : selected) {
					db.delete(TABLES.IMAGES.name, COL_IMAGE + " = ?", new String[]{image.image});
				}
				db.setTransactionSuccessful();
				success = true;
			} finally {
				db.endTransaction();
				db.close();
			}

			if (success) {
				//delete images from storage
				boolean tmpSuccess;
				for (DBImage dbImage : selected) {
					tmpSuccess = ImageUtil.getImageFromAppstorage(dbImage).map((image) -> {
						return image.delete();
					}).orElse(true);
					if (!tmpSuccess) {
						success = false;
					}
				}
			}
		}

		return success;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) return false;
		DBImage dbImage = (DBImage) o;
		return Objects.equals(image, dbImage.image);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(image);
	}

	public void deleteTags(DBImage[] selected) {
		for (DBImage dbImage : selected) {
			upsertImage(dbImage.image, new String[0]);
		}
	}

	public List<DBImage> getImageByTag(StaticTags tag) {
		synchronized (DBManager.DATABASE_NAME) {

			Cursor cursor = getReadableDatabase().query(
				TABLES.IMAGES.name,                          // Table name
				TABLES.IMAGES.getColumns(),                  // Columns to return
				COL_TAGS + " LIKE ?",                        // WHERE clause
				new String[]{"%" + tag.getName() + "%"},               // WHERE arguments
				null,                                        // groupBy
				null,                                        // having
				null                                        // orderBy
			);
			return getImages(cursor);
		}

	}
}
