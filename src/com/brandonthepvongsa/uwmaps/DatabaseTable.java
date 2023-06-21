package com.brandonthepvongsa.uwmaps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.android.gms.maps.model.LatLng;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseTable {
	
	private static final String TAG = "BuildingDatabase";
	// Columns in the database
	public static final String COL_TITLE = "TITLE";
	public static final String COL_LAT = "LAT";
	public static final String COL_LNG = "LNG";
	
	private static final String DATABASE_NAME = "BUILDINGS";
	private static final String FTS_VIRTUAL_TABLE = "FTS";
	private static final int DATABASE_VERSION = 1;
	
	private final DatabaseOpenHelper databaseOpenHelper;

	public DatabaseTable(Context context) {
		databaseOpenHelper = new DatabaseOpenHelper(context);
	}
	
	
	/**
	 * Called to return a Cursor of matching buildings to the requested query.
	 * @param query
	 * @param columns
	 * @return
	 */
	public Cursor getBuildingMatches(String query, String[] columns) {
		String selection = COL_TITLE + " MATCH ?";
		String[] selectionArgs = new String[] {query+"*"};
		
		return query(selection, selectionArgs, columns);
	}
	
	// Cursor
	private Cursor query(String selection, String[] selectionArgs, String[] columns) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
	    builder.setTables(FTS_VIRTUAL_TABLE);
	    
	    Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(),
	            columns, selection, selectionArgs, null, null, null);
	    
	    if (cursor == null) {
	        return null;
	    } else if (!cursor.moveToFirst()) {
	        cursor.close();
	        return null;
	    }
	    return cursor;
	}

	private static class DatabaseOpenHelper extends SQLiteOpenHelper {
		
		private final Context helperContext;
		private SQLiteDatabase database;
		
		private static final String FTS_TABLE_CREATE = 
				"CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE + 
				" USING fts3 (" +
				COL_TITLE + ", " +
				COL_LAT + ", " +
				COL_LNG + ")";
		
		DatabaseOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			helperContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			database = db;
			database.execSQL(FTS_TABLE_CREATE);
			Log.e("app", "but did this get called?");
			loadDictionary();
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			 Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					 + newVersion + ", which will destroy all old data");
			 db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
			 onCreate(db);
			
		}
		
		/**
		 * Load the buildings into the dictionary in a new thread 
		 *
		 */
		private void loadDictionary() {
			new Thread(new Runnable() {
				public void run() {
					try {
						loadWords();
					} catch(IOException e) {
						throw new RuntimeException(e);
					}
				}
			}).start();
		}
		
		private void loadWords() throws IOException {
			final Resources resources = helperContext.getResources();
			InputStream inputStream = resources.openRawResource(R.raw.building_cords);
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			Log.e("app", "well we at least did this..");
			try {
				String line;
				int count = 0;
				while((line = reader.readLine()) != null) {
					
					String[] strings = TextUtils.split(line, ", ");
					Log.e("app", "and we did it: " + count + "times added. " + strings[0].trim() + " " + strings[1].trim() + " " + strings[2].trim());
					count++;
					
					if(strings.length < 3) continue;
					long id = addBuilding(strings[0].trim(), strings[1].trim(), strings[2].trim());
					
					if(id < 0) {
						Log.e(TAG, "unable to add building: " + strings[0].trim());
					} 
				} 
			} finally {
				reader.close();
			}
		}
		
		/**
		 * Helper method to put values into database
		 * @param title the title of the building to put into the database
		 * @param lat the latitude of the building to put into the database
		 * @param lng the longitude of the building to put into the database
		 * @return the insertion the row
		 */
		public long addBuilding(String title, String lat, String lng) {
			ContentValues initialValues = new ContentValues();

			initialValues.put(COL_TITLE, title);
			initialValues.put(COL_LAT, lat);
			initialValues.put(COL_LNG, lng);
			
			return database.insert(FTS_VIRTUAL_TABLE, null, initialValues);
		}
		
		
	}
}
