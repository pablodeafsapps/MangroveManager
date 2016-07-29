package org.deafsapps.mangrovemanager.utils;


import org.deafsapps.mangrovemanager.utils.MangroveSite;

// This class defines a particular mangrove tree
public class MangroveTree extends MangroveSite
{
	private String species;
	private float dbh;
	private String extras;
	
	// Constructor
	public MangroveTree(long mId, long mTag, float mLatitude, float mLongitude, 
			float mZ_msl, String mSpecies, float mDbh, String mExtras) 
	{
		super(mId, mTag, mLatitude, mLongitude, mZ_msl);
		
		this.species = mSpecies;
		this.dbh = mDbh;
		this.extras = mExtras;
	}

	// Following 'getters' and 'setters'
	public String getSpecies() { return species; }
	public void setSpecies(String species) { this.species = species; }

	public float getDbh() { return dbh; }
	public void setDbh(float dbh) { this.dbh = dbh; }

	public String getExtras() {	return extras; }
	public void setExtras(String extras) { this.extras = extras; }
}