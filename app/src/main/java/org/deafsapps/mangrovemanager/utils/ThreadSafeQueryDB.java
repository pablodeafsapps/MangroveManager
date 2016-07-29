package org.deafsapps.mangrovemanager.utils;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.deafsapps.mangrovemanager.db.DBProvider;

public class ThreadSafeQueryDB extends AsyncTask<Object, Integer, ArrayList<MangroveTree>>
{
	private Context mContext;
	private MangroveManagerCommInterface mATaskResponse;
	private ProgressDialog mProgress;
	
	public ThreadSafeQueryDB(Context mActivityContext, MangroveManagerCommInterface mAsyncResponse)
	{ 
		this.mContext = mActivityContext;
		this.mATaskResponse = mAsyncResponse;
	}
	
	@Override
	protected void onPreExecute() 
	{ 
		super.onPreExecute();
		this.mProgress = ProgressDialog.show(this.mContext, "Loading data...", null);
	}
	
	@Override
		
	protected ArrayList<MangroveTree> doInBackground(Object... params) 
	{
		ContentResolver mCR = this.mContext.getContentResolver();
		String sort = null, where = null; 
		String[] whereArgs = null;
		if(params[0] != "")
		{
			where = (String) params[0];	
			whereArgs = (String[]) params[1];
		}
		if(params[2] != "")
		{	
			sort = (String) params[2];
			System.out.println(sort);			
		}
			
		ArrayList<MangroveTree> queryArray = DBProvider.queryMangroveTreeDB(mCR, where, whereArgs, sort);
		if (queryArray == null) { Log.d("ThreadSafe query", "queryArray returned NULL"); }
		else { Log.d("ThreadSafe query", String.valueOf(queryArray.size())); }
		return queryArray;
	}	
	
	@Override
	protected void onPostExecute(ArrayList<MangroveTree> rArray) 
	{
		// Interface method trigger which allows us to get values back
		super.onPostExecute(rArray);
		
		this.mATaskResponse.onATaskResponse(rArray);		
		this.mProgress.dismiss();		
	}

	/*
	// This interface is used to get data returned from an 'AsyncTask' ('ThreadSafeQueryDB')
	public interface InterfOnAsyncTaskResponse 
	{
		    void onATaskResponse(ArrayList<MangroveTree> mArray);
	}
	*/
}