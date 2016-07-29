package org.deafsapps.mangrovemanager.db;

import java.util.ArrayList;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.deafsapps.mangrovemanager.utils.MangroveTree;

// This class will be used to define the 'ContentProvider'
public class DBProvider extends ContentProvider
{
	// 'ContentProvider' name declaration (or 'authority') and parsing
	private static final String PROVIDER_NAME = "org.deafsapps.mangrovemanager";
	public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME);
	
	// UriMatcher
	// Two codes are defined to difference the access to the whole table or to a specific '_id'
	private static final int MANGROVETREES = 1;
	private static final int MANGROVETREES_ID = 2;
	private static final UriMatcher uriMatcher;
	static
	{
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, DBParam.Table.TABLE_NAME, MANGROVETREES);
		uriMatcher.addURI(PROVIDER_NAME, DBParam.Table.TABLE_NAME + "/#", MANGROVETREES_ID);
	}
	
	// Declaring a 'handle' to an object which extends from 'SQLiteOpenHelder'
	private DBManager mDBManager;
	
	
	@Override
	public boolean onCreate() 
	{
		// The database name and the version number are already define in 'BaseDatosParam'
		// and included in the 'BDManager' constructor'
		this.mDBManager = new DBManager(this.getContext());
		// Create the database and read data from 'assets' (method "onCreate") 
		this.mDBManager.getWritableDatabase();
	
		return true;
	}
	
	@Override
	// 'projection' is an array with all the fields we want to be retrieved
	// 'selection' accepts the MySQL command WHERE
	// 'selectionArgs' is an array with values to check for all those fields included in 'selection'
	// 'sortOrder' points out which column we want to order
	public Cursor query(Uri uri, String[] projection, String where, String[] selectionArgs, String sortOrder) 
	{
		Log.d("DBProvider query", "Query: " + uri.toString());
		String groupBy = null;
		
		// This line creates the database in case it does not exist yet
		SQLiteDatabase db = this.mDBManager.getWritableDatabase();
		// The method 'match' retrieves the corresponding code to the matcher included in the definition
		if (uriMatcher.match(uri) == MANGROVETREES_ID)
			where = "_id=" + uri.getLastPathSegment();
			
		Cursor c = db.query(DBParam.Table.TABLE_NAME, projection, where, selectionArgs, groupBy, null, sortOrder);
		// Changes to be notified to the 'ContentProvider' for the "obervers"
		c.setNotificationUri(this.getContext().getContentResolver(), uri);

		return c;
	}
	
	// 'update' is similar to 'query', but changing the method used to access the database
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) 
	{
		String where = selection;
				
		SQLiteDatabase db = this.mDBManager.getWritableDatabase();
		 
		if (uriMatcher.match(uri) == MANGROVETREES_ID)
			where = "_id=" + uri.getLastPathSegment();
						
		int cont = db.update(DBParam.Table.TABLE_NAME, values, where, selectionArgs);
				
		return cont;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) 
	{
		String where = selection;
		
		SQLiteDatabase db = this.mDBManager.getWritableDatabase();
		 
		if (uriMatcher.match(uri) == MANGROVETREES_ID)
			where = "_id=" + uri.getLastPathSegment();
						
		int cont = db.delete(DBParam.Table.TABLE_NAME, where, selectionArgs);
				
		return cont;
	}
	
	// Retrieves the URI which refers to the inserted register
	@Override
	public Uri insert(Uri uri, ContentValues values) 
	{
		
		SQLiteDatabase db = this.mDBManager.getWritableDatabase();
	
		long regId = db.insert(DBParam.Table.TABLE_NAME, null, values);
		Uri newUri = ContentUris.withAppendedId(CONTENT_URI, regId);
		
		return newUri;
	}

	// Retrieves the 'MIME' type for a particular 'uri'
	@Override
	public String getType(Uri uri) 
	{
		int match = uriMatcher.match(uri);
		
		switch (match)
		{
			case MANGROVETREES:
				return "vnd.android.cursor.dir/vnd.deafsapps.mangrovemanager.mangrovetrees";
				
			case MANGROVETREES_ID:
				return "vnd.android.cursor.item/vnd.deafsapps.mangrovemanager.mangrovetrees";
			default:
				return null;			   
		}
	}
	
	// This static method allows to access 1 register in an easy way
	public static MangroveTree queryOneMangroveTreeDB(ContentResolver cr, String id)
	{
		String[] projection = new String[] {
				DBParam.Table._ID,
				DBParam.Table.TAG,
				DBParam.Table.LATITUDE,
				DBParam.Table.LONGITUDE,
				DBParam.Table.ZMSL,
				DBParam.Table.SPECIES,
				DBParam.Table.DBH,
				DBParam.Table.EXTRAS };
		
		// We only want to query one register, so we must include a an 'Uri' which includes 
		// the table name and the particular 'id'
		Cursor c = cr.query(Uri.parse(DBProvider.CONTENT_URI.toString() + "/" +
				DBParam.Table.TABLE_NAME + "/" + id), 
				projection, null, null, null);
	
		// Following, the retrieved 'Cursor' is obtained
		c.moveToFirst();
		
		int mId = c.getInt(c.getColumnIndex(DBParam.Table._ID));
		long mRef = c.getLong(c.getColumnIndex(DBParam.Table.TAG));
		float mLat = c.getFloat(c.getColumnIndex(DBParam.Table.LATITUDE));
		float mLong = c.getFloat(c.getColumnIndex(DBParam.Table.LONGITUDE));
		float mZmsl = c.getFloat(c.getColumnIndex(DBParam.Table.ZMSL));
		String mSpecies = c.getString(c.getColumnIndex(DBParam.Table.SPECIES));
		float mDbh = c.getFloat(c.getColumnIndex(DBParam.Table.DBH));
		String mExtras = c.getString(c.getColumnIndex(DBParam.Table.EXTRAS));
		// The 'Cursor' has to be released
		c.close();
		
		// We create a 'MangroveTree' with the obtained data
		MangroveTree mMangroveTree = new MangroveTree(mId, mRef, mLat, mLong, mZmsl, mSpecies, mDbh, mExtras);
		
		return mMangroveTree;
	}
	
	// This method retrieves (ordered) 1 field across the whole table of the DB 
	public static ArrayList<String> queryOneFieldMangroveTreeDB(ContentResolver cr, String field) 
	{
		ArrayList<String> mArray = new ArrayList<String>();
			
		String[] projection = new String[] { field };
		
		// 'sort' is concluded just in case it is not a 'null' input argument
		Cursor c = cr.query(DBProvider.CONTENT_URI, projection, null, null, field + " DESC");
		
		if (c.moveToFirst())
		{
			do
			{
				String mObject = c.getString(c.getColumnIndex(field));
								
				mArray.add(mObject);	
			}		
			while(c.moveToNext());	
		}		
		c.close();
		
		return mArray;
	}
	
	// This method accesses the whole table of the DB
	public static ArrayList<MangroveTree> queryMangroveTreeDB(ContentResolver cr, String where, String [] whereArgs, String sort) 
	{
		ArrayList<MangroveTree> mArray = new ArrayList<MangroveTree>();
			
		String[] projection = new String[] {
				DBParam.Table._ID,
				DBParam.Table.TAG,
				DBParam.Table.LATITUDE,
				DBParam.Table.LONGITUDE,
				DBParam.Table.ZMSL,
				DBParam.Table.SPECIES,
				DBParam.Table.DBH,
				DBParam.Table.EXTRAS };
		
		// 'sort' is concluded just in case it is not a 'null' input argument
		Cursor c = cr.query(DBProvider.CONTENT_URI, projection, where, whereArgs, sort);
		
		if (c.moveToFirst())
		{
			do
			{
				int mId = c.getInt(c.getColumnIndex(DBParam.Table._ID));
				long mRef = c.getLong(c.getColumnIndex(DBParam.Table.TAG));
				float mLat = c.getFloat(c.getColumnIndex(DBParam.Table.LATITUDE));
				float mLong = c.getFloat(c.getColumnIndex(DBParam.Table.LONGITUDE));
				float mZmsl = c.getFloat(c.getColumnIndex(DBParam.Table.ZMSL));
				String mSpecies = c.getString(c.getColumnIndex(DBParam.Table.SPECIES));
				float mDbh = c.getFloat(c.getColumnIndex(DBParam.Table.DBH));
				String mExtras = c.getString(c.getColumnIndex(DBParam.Table.EXTRAS));
								
				MangroveTree mMangroveTree = new MangroveTree(mId, mRef, mLat, mLong, mZmsl, mSpecies, mDbh, mExtras);
				mArray.add(mMangroveTree);	
			}		
			while(c.moveToNext());	
		}		
		c.close();
		
		return mArray;
	}
		
	public static Uri insertMangroveTreeDB(ContentResolver cr, MangroveTree mMangroveTree) 
	{
		// Insert into the database the info from the new MangroveTree with a 'ContentValues'
		ContentValues cv = new ContentValues();
		cv.put(DBParam.Table.TAG, mMangroveTree.getTag());
		cv.put(DBParam.Table.LATITUDE, mMangroveTree.getLatitude());
		cv.put(DBParam.Table.LONGITUDE, mMangroveTree.getLongitude());
		cv.put(DBParam.Table.ZMSL, mMangroveTree.getZ_msl());				
		cv.put(DBParam.Table.SPECIES, mMangroveTree.getSpecies());
		cv.put(DBParam.Table.DBH, mMangroveTree.getDbh());
		cv.put(DBParam.Table.EXTRAS, mMangroveTree.getExtras());
						
		Uri u = cr.insert(DBProvider.CONTENT_URI, cv);	
			
		return u;
	}
	
	// This static method allows to update 
	public static int saveMangroveTreeExtras(ContentResolver cr, Integer id, String mExtras) 
	{		
		ContentValues cv = new ContentValues();
		cv.put(DBParam.Table.EXTRAS, mExtras);				
		Log.d("DBProvider sameMangroveTreeExtras", "Update row: " + String.valueOf(id));
		int i = cr.update(Uri.parse(DBProvider.CONTENT_URI.toString() + "/" +
				DBParam.Table.TABLE_NAME + "/" + 
				String.valueOf(id)), cv, null, null);
			
		return i;
	}	
}