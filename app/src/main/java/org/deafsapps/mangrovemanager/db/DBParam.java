package org.deafsapps.mangrovemanager.db;

// This class defines certain constants to be used when managing the database
public class DBParam 
{	
	// Database name
	public static final String DB_NAME = "MangroverDB";
	// Database version
	public static final int DB_VERSION = 1;
	
	// This class does not have to be utilized; private constructor
	private DBParam() { }
	
	// Database table "Table" definition
	// 'static final' for security: not extended
	public static final class Table
	{
		private Table() { }
		// Abstraction with field names and constants to access easily the database
		public static final String TABLE_NAME = "MangroverTrees";
		public static final String _ID = "_id";
		public static final String TAG = "tag";
		public static final String LATITUDE = "latitude";
		public static final String LONGITUDE = "longitude";
		public static final String ZMSL = "z_msl";
		public static final String SPECIES = "species";
		public static final String DBH = "dbh";
		public static final String EXTRAS = "extras";
	}
}