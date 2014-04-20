package com.jackytck.wifiscanner;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class FingerPrintDatabaseHelper  extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "wifiscanner.sqlite";

	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table fingerprint (_id integer primary key autoincrement, "
			+ "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, station text not null, ssid text not null, bssid text not null, rss integer not null, wep integer not null, infastructure integer not null);";

	public FingerPrintDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(FingerPrintDatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS ips_table");
		onCreate(database);
	}
}