package org.deafsapps.mangrovemanager.utils;

public abstract class MangroveSite 
{
	private long id;
	private long tag;
	private float latitude;
	private float longitude;
	private float z_msl;
	
	public MangroveSite(long mId, long mTag, float mLatitude, float mLongitude, float mZ_msl)
	{
		this.id = mId;
		this.tag = mTag;
		this.latitude = mLatitude;
		this.longitude = mLongitude;
		this.z_msl = mZ_msl;
	}
	
	public long getId() { return id; }
	public void setId(Integer id) { this.id = id; }
	
	public long getTag() { return tag; }
	public void setTag(Integer tag) { this.tag = tag; }
	
	public float getLatitude() { return latitude; }
	public void setLatitude(float latitude) { this.latitude = latitude; }
	
	public float getLongitude() { return longitude; }
	public void setLongitude(float longitude) { this.longitude = longitude; }
	
	public float getZ_msl() { return z_msl; }
	public void setZ_msl(float z_msl) { this.z_msl = z_msl; }
}