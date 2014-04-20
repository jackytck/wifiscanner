package com.jackytck.wifiscanner;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FingerPrintDbAdapter {
	// Database fields

	public static final String KEY_ROWID = "_id";
	//public static final String KEY_LOCATION = "_id";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_STATION = "station";
	public static final String KEY_SSID = "ssid";
	public static final String KEY_BSSID = "bssid";
	public static final String KEY_RSS = "rss";
	public static final String KEY_WEP = "wep";
	public static final String KEY_INFASTRUCTURE = "infastructure";
	private static final String DATABASE_TABLE = "fingerprint";
	private Context context;
	private SQLiteDatabase database;
	private FingerPrintDatabaseHelper dbHelper;

	public FingerPrintDbAdapter(Context context) {
		this.context = context;
	}

	public FingerPrintDbAdapter open() throws SQLException {
		dbHelper = new FingerPrintDatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if(dbHelper != null) {
			dbHelper.close();
		}
	}

	public long insert(String station, String ssid, String bssid, int rss, boolean wep, boolean infastructure) {
		ContentValues values = createContentValues(station, ssid, bssid, rss, wep, infastructure);

		return database.insert(DATABASE_TABLE, null, values);
	}
	
	/*
	public boolean isExist(String location) {
		boolean ret = false;
		
		String query = "select * from " + DATABASE_TABLE + " where "
						+ "(" +  KEY_LOCATION + " = ?)";
		
		Cursor cursor = database.rawQuery(query, new String[] { location });
					
		if(cursor.getCount() > 0)
			ret = true;
		
		return ret;
	}

	public boolean updateLocation(long rowId, String location) {
		ContentValues updateValues = createContentValues(location);

		return database.update(DATABASE_TABLE, updateValues, KEY_ROWID + "="
				+ rowId, null) > 0;
	}
	
	public boolean deleteLocation(long rowId) {
		return database.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
*/
	public String curToStr(Cursor c, String k) {
		return c.getString(c.getColumnIndexOrThrow(k));
	}
	
	public Cursor fetchAll() {
		return database.query(DATABASE_TABLE, new String[] { 
				KEY_TIMESTAMP, KEY_STATION, KEY_SSID, KEY_BSSID, KEY_RSS, KEY_WEP, KEY_INFASTRUCTURE},
				null, null, null, null, KEY_TIMESTAMP);
	}
	
	public String fetchAllInCSVString() {
		String ret = "";
		Cursor c = fetchAll();
		c.moveToFirst();
        while (c.isAfterLast() == false) {
        	ret += curToStr(c, KEY_TIMESTAMP) + "," + curToStr(c, KEY_STATION) + "," + curToStr(c, KEY_SSID) + "," + curToStr(c, KEY_BSSID) + "," + curToStr(c, KEY_RSS) + "," + curToStr(c, KEY_WEP) + "," + curToStr(c, KEY_INFASTRUCTURE) + "\n"; 
       	    c.moveToNext();
        }
        c.close();
		return ret;
	}
	
	/*
	public Vector <Integer> fetchAllLocationID() {
		Vector <Integer> ret = new Vector <Integer>();
		Cursor c = fetchAll();
		c.moveToFirst();
        while (c.isAfterLast() == false) {
        	ret.add(c.getInt(c.getColumnIndexOrThrow(KEY_ROWID)));
       	    c.moveToNext();
        }
        c.close();
		return ret;
	}
	
	public boolean isEmpty() {
		boolean ret = true;
		if(fetchAllLocation().getCount() > 0)
			ret = false;
		return ret;
	}

	public Cursor fetchLocation(long rowId) throws SQLException {
		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_LOCATION },
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	*/
	
	private ContentValues createContentValues(String station, String ssid, String bssid, int rss, boolean wep, boolean infastructure) {
		ContentValues values = new ContentValues();
		values.put(KEY_STATION, station);
		values.put(KEY_SSID, ssid);
		values.put(KEY_BSSID, bssid);
		values.put(KEY_RSS, rss);
		values.put(KEY_WEP, wep ? 1 : 0);
		values.put(KEY_INFASTRUCTURE, infastructure ? 1 : 0);
		return values;
	}
}
