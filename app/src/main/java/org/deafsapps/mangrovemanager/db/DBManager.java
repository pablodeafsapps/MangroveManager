package org.deafsapps.mangrovemanager.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// This class makes easier to manage the database. It will create it in case it 
// does not exist and update it if a new version comes out
public class DBManager extends SQLiteOpenHelper
{
	private Context mContext;
	// Command 'CREATE TABLE' for MySQL
	private static final String sqlCreate = "CREATE TABLE " + DBParam.Table.TABLE_NAME + " ( " 
			+ DBParam.Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
			+ DBParam.Table.TAG + " INT(6) NOT NULL, "
			+ DBParam.Table.LATITUDE + " DECIMAL(11,8) NOT NULL, " 
			+ DBParam.Table.LONGITUDE + " DECIMAL(11,8) NOT NULL, "
			+ DBParam.Table.ZMSL + " DECIMAL(7,5), "
			+ DBParam.Table.SPECIES + " VARCHAR(4) NOT NULL, "
			+ DBParam.Table.DBH + " DECIMAL(4,2), "
			+ DBParam.Table.EXTRAS + " TEXT )";
	
	public DBManager(Context context) 
	{
		super(context, DBParam.DB_NAME, null, DBParam.DB_VERSION);
		this.setMiContext(context);		
	}
	
	public Context getMiContext() { return this.mContext; }
	public void setMiContext(Context miContext) { this.mContext = miContext; }

	public static String getSqlcreate() { return DBManager.sqlCreate; }

	// This method will only be called when the app is first run
	// To make changes take effect the app must be reinstalled

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// Write access to the database if
		if (db.isReadOnly())
			db = this.getWritableDatabase();

		try
		{
			// MySQL statement executed to create the database
			db.execSQL(DBManager.sqlCreate);

			// MySQL statement read to load data from file
			StringBuilder mStrBuilder = new StringBuilder();
			InputStream mInStream = this.mContext.getAssets().open("MangroverDB_complete.sql", AssetManager.ACCESS_STREAMING);
			//InputStream mInStream = this.mContext.getAssets().open("MangroverDB_bunch.sql", AssetManager.ACCESS_STREAMING);
			//InputStream mInStream = this.mContext.getAssets().open("MangroverDB.sql", AssetManager.ACCESS_STREAMING);

			if (mInStream == null) { Log.d("Open file", "input is NULL"); }
			if (mInStream != null)
			{
				BufferedReader mBuf = new BufferedReader(new InputStreamReader(mInStream));
				String str = "";
				while ((str = mBuf.readLine()) != null ) { mStrBuilder.append(str); }

				mInStream.close();
			}

			final String queryStrings = mStrBuilder.toString();
			String[] queries = queryStrings.split(";");
			for(String oneQuery : queries)
				db.execSQL(oneQuery);
		}
		catch (SQLException e1) { Log.e("SQLException", e1.toString()); }
		catch (IOException e2) { Log.e("IOException", e2.toString()); }
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }	
}