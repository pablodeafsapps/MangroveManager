package org.deafsapps.mangrovemanager.fragments;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.deafsapps.mangrovemanager.utils.MangroveManagerCommInterface;
import org.deafsapps.mangrovemanager.utils.MangroveTree;
import org.deafsapps.mangrovemanager.R;
import org.deafsapps.mangrovemanager.utils.ThreadSafeQueryDB;
import org.deafsapps.mangrovemanager.activities.MangroverMain;
import org.deafsapps.mangrovemanager.db.DBParam;
import org.deafsapps.mangrovemanager.db.DBProvider;

// This class takes care of the table
// The custom interface 'OnAsyncTaskResponse' gets return values from an 'AsyncTask'
public class MangroverTable extends Fragment implements OnClickListener, MangroveManagerCommInterface
{
	private static final int MAX_TABLEITEMS = 50;
	
	private Bundle mBundle;
	private TableLayout mainTable;
	private Handler mTableLayoutHandler = new Handler();
	private ArrayList<MangroveTree> mMangTreeArray;
	private float minZmsl, maxZmsl, minDbh, maxDbh;
	private static int numPage = 1;
	private MangroveManagerCommInterface mTableRowListener;
	private MangroveManagerCommInterface mTableSearchListener;
	private String mWhere = null;
	private String[] mWhereArgs = null;
	private static boolean defaultOrder = true;

	// 'onAttach' is executed when the 'Fragment' is matched with a 'FragmentActivity'
	// It is a good moment to relate possible communications between them (set listeners)
	@Override
	public void onAttach(Context mContext)
	{
		super.onAttach(mContext);
		
		if (mContext instanceof MangroveManagerCommInterface)
			this.mTableRowListener = (MangroveManagerCommInterface) mContext;
		else
			throw new ClassCastException(mContext.toString() +
					" must implemenet MangroverTable.InterfOnTableRowPressed");
		
		if (mContext instanceof MangroveManagerCommInterface)
			this.mTableSearchListener = (MangroveManagerCommInterface) mContext;
		else
			throw new ClassCastException(mContext.toString() +
					" must implemenet MangroverTable.InterfOnTableNewSearch");
		
		// This line allows to receive arguments from the 'FragmentActivity' that hosts the 'Fragments'
		((MangroverMain) mContext).mActivity2TableListener = this;
	}
	
	@Override
	public void onDetach() 
	{
		super.onDetach();
		// Release the listeners
		this.mTableRowListener = null;
	}
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.activity_mangrover_table, container, false);
		this.setHasOptionsMenu(true);
		this.mBundle = savedInstanceState;
		this.mainTable = (TableLayout) rootView.findViewById(R.id.table_Principal);
		
		// Seven column headers are declared as clickable, to order the list according to that tag. Listener attached
		((TextView) ((TableRow) ((TableLayout) rootView.findViewById(R.id.table_Header)).getChildAt(0)).findViewById(R.id.tableHeader_Id)).setOnClickListener(this);
		((TextView) ((TableRow) ((TableLayout) rootView.findViewById(R.id.table_Header)).getChildAt(0)).findViewById(R.id.tableHeader_Tag)).setOnClickListener(this);
		((TextView) ((TableRow) ((TableLayout) rootView.findViewById(R.id.table_Header)).getChildAt(0)).findViewById(R.id.tableHeader_Lat)).setOnClickListener(this);
		((TextView) ((TableRow) ((TableLayout) rootView.findViewById(R.id.table_Header)).getChildAt(0)).findViewById(R.id.tableHeader_Long)).setOnClickListener(this);
		((TextView) ((TableRow) ((TableLayout) rootView.findViewById(R.id.table_Header)).getChildAt(0)).findViewById(R.id.tableHeader_Zmsl)).setOnClickListener(this);
		((TextView) ((TableRow) ((TableLayout) rootView.findViewById(R.id.table_Header)).getChildAt(0)).findViewById(R.id.tableHeader_Species)).setOnClickListener(this);
		((TextView) ((TableRow) ((TableLayout) rootView.findViewById(R.id.table_Header)).getChildAt(0)).findViewById(R.id.tableHeader_Dbh)).setOnClickListener(this);
		
		// The interface 'InterfOnAsyncTaskResponse' is used as the second argument of the constructor
		new ThreadSafeQueryDB(this.getActivity(), this).execute("", new String[] { "" }, "");
		// Maximum and minimum values of the fields "Z MSL" and "DBH", used on the 'SeekBar's		
		ArrayList<String> mZmslArray = DBProvider.queryOneFieldMangroveTreeDB(this.getActivity().getContentResolver(), DBParam.Table.ZMSL);
			this.maxZmsl = Float.parseFloat(mZmslArray.get(0));
			this.minZmsl = Float.parseFloat(mZmslArray.get(mZmslArray.size() - 1));
		ArrayList<String> mDbhArray = DBProvider.queryOneFieldMangroveTreeDB(this.getActivity().getContentResolver(), DBParam.Table.DBH);
			this.maxDbh = Float.parseFloat(mDbhArray.get(0));
			this.minDbh = Float.parseFloat(mDbhArray.get(mDbhArray.size() - 1));
	
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		super.onCreateOptionsMenu(menu, inflater);		
		inflater.inflate(R.menu.mangrover_table, menu);		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem mItem) 
	{
		super.onOptionsItemSelected(mItem);
		
		switch (mItem.getItemId())
		{
			case R.id.action_table_lookup:
				// The interface 'IntOnMarkerDialogResponse' is used as the third argument of the constructor
				Bundle mBundle = new Bundle();
					mBundle.putFloat("maxZmsl", this.maxZmsl);
					mBundle.putFloat("minZmsl", this.minZmsl);
					mBundle.putFloat("maxDbh", this.maxDbh);
					mBundle.putFloat("minDbh", this.minDbh);
				TablesearchDialog mTsDialog = new TablesearchDialog();
					TablesearchDialog.setUpFragment(this);
					mTsDialog.setArguments(mBundle);
					mTsDialog.show(this.getFragmentManager(), "Table Search Dialog");
				break;
				
			case R.id.action_table_previous:
				if (this.mMangTreeArray.size() != 0)
				{
					if ((this.mMangTreeArray.size() <= MAX_TABLEITEMS) || (numPage == 1))
						Toast.makeText(getActivity(), "1/1", Toast.LENGTH_SHORT).show();				
					else
					{
						numPage--;					
						// Remove all the rows except the first one (it keeps the columns aligned!)
						this.mainTable.removeViews(1, this.mainTable.getChildCount() - 1);
						this.loadNewPage();
						Toast.makeText(getActivity(), "Showing page " + String.valueOf(numPage) + "/" + String.valueOf(Math.round(this.mMangTreeArray.size()/MAX_TABLEITEMS) + 1), Toast.LENGTH_SHORT).show();
					}	
				}
				break;
				
			case R.id.action_table_next:
				if (this.mMangTreeArray.size() != 0)
				{
					if ((this.mMangTreeArray.size() <= MAX_TABLEITEMS) || (numPage == Math.round(this.mMangTreeArray.size()/MAX_TABLEITEMS) + 1))
						Toast.makeText(getActivity(), "1/1", Toast.LENGTH_SHORT).show();				
					else
					{
						numPage++;
						// Remove all the rows except the first one (it keeps the columns aligned!)
						this.mainTable.removeViews(1, this.mainTable.getChildCount() - 1);
						this.loadNewPage();
						Toast.makeText(getActivity(), "Showing page " + String.valueOf(numPage) + "/" + String.valueOf(Math.round(this.mMangTreeArray.size()/MAX_TABLEITEMS) + 1), Toast.LENGTH_LONG).show();
					}
				}
				break;
				
			case R.id.action_app_about:
				AboutDialog mAboutDialog = new AboutDialog();
				mAboutDialog.show(this.getFragmentManager(), "About Dialog");
				break;		
		}
		
		return true;
	}
	
	// This method is called whenever the fragment is visible (equivalent to 'onResume' for an 'Activity')
	@Override
	public void setMenuVisibility(boolean menuVisible) 
	{
		super.setMenuVisibility(menuVisible);
		
		if (menuVisible && this.mMangTreeArray.size() != 0)
			Toast.makeText(getActivity(), "Showing page " + String.valueOf(numPage) + "/" + String.valueOf(Math.round(this.mMangTreeArray.size()/MAX_TABLEITEMS) + 1), Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onClick(View v) 
	{
		// The clicking areas are either the column headers ('TextView'(s)) or the table rows
		if (v instanceof TextView)
		{
			if (this.mMangTreeArray.size() != 0)
			{
				String mRef = null;
				
				if (v.getId() == R.id.tableHeader_Id) { mRef = DBParam.Table._ID; }
				else if (v.getId() == R.id.tableHeader_Tag) { mRef = DBParam.Table.TAG; }
				else if (v.getId() == R.id.tableHeader_Lat) { mRef = DBParam.Table.LATITUDE; }
				else if (v.getId() == R.id.tableHeader_Long) { mRef = DBParam.Table.LONGITUDE; }
				else if (v.getId() == R.id.tableHeader_Zmsl) { mRef = DBParam.Table.ZMSL; }
				else if (v.getId() == R.id.tableHeader_Species) { mRef = DBParam.Table.SPECIES; }
				else if (v.getId() == R.id.tableHeader_Dbh) { mRef = DBParam.Table.DBH; }
				
				// Remove all the rows except the first one (it keeps the columns aligned!)
				this.mainTable.removeViews(1, this.mainTable.getChildCount() - 1);
					
				if (MangroverTable.defaultOrder)
					new ThreadSafeQueryDB(this.getActivity(), this).execute(this.mWhere, this.mWhereArgs, mRef);
				else
					new ThreadSafeQueryDB(this.getActivity(), this).execute(this.mWhere, this.mWhereArgs, mRef + " DESC");
				
				// Toggle 'SORT BY' argument
				MangroverTable.defaultOrder = !MangroverTable.defaultOrder;
			}
		}
		else
		{
			TextView mId = (TextView) v.findViewById(R.id.tableTextView_Id);
			MangroveTree mTree = DBProvider.queryOneMangroveTreeDB(this.getActivity().getContentResolver(), mId.getText().toString());
			this.mTableRowListener.onTableRowPressed(mTree);
			((ActionBarActivity) this.getActivity()).getSupportActionBar().setSelectedNavigationItem(MangroverMain.MAP_TAB);
		}
	}
	
	public void loadNewPage()
	{ 
		List<MangroveTree> subArray = null;
		if (numPage != Math.round(this.mMangTreeArray.size()/MAX_TABLEITEMS) + 1)
			subArray = this.mMangTreeArray.subList((numPage - 1) * MAX_TABLEITEMS, (1 + (numPage - 1)) * MAX_TABLEITEMS);
		else
			subArray = this.mMangTreeArray.subList((numPage - 1) * MAX_TABLEITEMS, this.mMangTreeArray.size());
		
		Log.d("subArray size", String.valueOf(subArray.size()));
		
		final List<MangroveTree> mSubArray = subArray;
		final LayoutInflater mInflater = this.getLayoutInflater(this.mBundle);
		final TableLayout mTableLayout = this.mainTable;
		final OnClickListener mListener = this;
		final Handler mHandler = this.mTableLayoutHandler;
		final Object threadBlock = new Object();
				
		// The table is filled in within a background thread
		new Thread(new Runnable() 
		{			
			@Override
			public void run() 
			{
				if (mSubArray != null && mSubArray.size() != 0)
				{
					int numRow = 0;
					
					synchronized (threadBlock) 
					{					
						for (MangroveTree mMangTree : mSubArray)
						{
							numRow++;
							final TableRow oneRow = (TableRow) mInflater.inflate(R.layout.table_row, null);
							((TextView) oneRow.findViewById(R.id.tableTextView_Id)).setText(String.valueOf(mMangTree.getId()));
							((TextView) oneRow.findViewById(R.id.tableTextView_Tag)).setText(String.valueOf(mMangTree.getTag()));
							((TextView) oneRow.findViewById(R.id.tableTextView_Lat)).setText(String.format("%.8f", mMangTree.getLatitude()));
							((TextView) oneRow.findViewById(R.id.tableTextView_Long)).setText(String.format("%.8f", mMangTree.getLongitude()));
							((TextView) oneRow.findViewById(R.id.tableTextView_Zmsl)).setText(String.valueOf(mMangTree.getZ_msl()));
							((TextView) oneRow.findViewById(R.id.tableTextView_Species)).setText(String.valueOf(mMangTree.getSpecies()));
							((TextView) oneRow.findViewById(R.id.tableTextView_Dbh)).setText(String.valueOf(mMangTree.getDbh()));
							((TextView) oneRow.findViewById(R.id.tableTextView_Extras)).setText(String.valueOf(mMangTree.getExtras()));
	
							final int idx = numRow;
							mHandler.post(new Runnable() 
							{									
								@Override
								public void run() 
								{
									mTableLayout.addView(oneRow);
									oneRow.setId(idx);
									oneRow.setClickable(true);
									oneRow.setOnClickListener(mListener);										
								}
							});													
						}
						
						threadBlock.notify();
					}
				}	
			}
		}).start();	
		
		// Wait until the table is filled in
		synchronized (threadBlock) 
		{
			try 
			{ threadBlock.wait(); } 
			catch (InterruptedException e) { }			
		}
		
		Log.d("loadNewPage", "Table loaded");
	}

	// The following method corresponds to the interface 'InterfOnAsyncTaskResponse'
	@Override
	public void onATaskResponse(final ArrayList<MangroveTree> mArray)
	{
		this.mMangTreeArray = mArray;
		// If the query retrieves an empty array, no data is loaded to the table
		if (mArray.size() != 0)
		{
			numPage = 1;
			this.loadNewPage();
			Toast.makeText(getActivity(), String.valueOf(this.mMangTreeArray.size()) + " matching records", Toast.LENGTH_LONG).show();
			Log.d("onATaskResponse", "Table loaded");
		}
		else
			Toast.makeText(getActivity(), "No matching records", Toast.LENGTH_LONG).show();

		// We now update the map turning on visibility for the queried markers
		this.mTableSearchListener.onTableNewSearch(this.mMangTreeArray);
	}

	// The following method corresponds to the interface 'InterfOnTablesearchDialogResponse'
	@Override
	public void onSearchResponse(String mWh, String[] mWhArgs) 
	{
		this.mWhere = mWh;
		this.mWhereArgs = mWhArgs;
		
		// Remove all the rows except the first one (it keeps the columns aligned!)
		this.mainTable.removeViews(1, this.mainTable.getChildCount() - 1);
		// Query
		new ThreadSafeQueryDB(getActivity(), this).execute(this.mWhere, this.mWhereArgs, "");		
	}

	// The following methods correspond to the interface 'InterfOnActivity2Fragment'
	@Override
	public void onActivity2Fragment(MangroveTree mTree) 
	{
		int nRow = this.mainTable.getChildCount();
		// Starting at 'i = 1' to avoid the table's header row
		for (int i = 1; i < nRow; i++)
		{ 
			if (((TextView) this.mainTable.getChildAt(i).findViewById(R.id.tableTextView_Id)).getText().toString().equals(String.valueOf(mTree.getId())))
				((TextView) this.mainTable.getChildAt(i).findViewById(R.id.tableTextView_Extras)).setText(mTree.getExtras());							
		}
	}

	@Override
	public void onActivity2Fragment_plus(ArrayList<MangroveTree> mMangTree) { }
	@Override
	public void onMapMarkerUpdated(MangroveTree mTree) { }
	@Override
	public void onTableRowPressed(MangroveTree mTree) { }
	@Override
	public void onTableNewSearch(ArrayList<MangroveTree> mTree) { }
	@Override
	public void onDialogResponse(Integer mId, String mExtras) { }

	/*
	@Override
	public void onActivity2Fragment_plus(ArrayList<MangroveTree> mMangTree) { }
	
	// Follow some 'interface's defined for inter-fragment communication
	public interface InterfOnTableRowPressed
	{
		void onTableRowPressed(MangroveTree mTree);
	}	
	
	public interface InterfOnTableNewSearch
	{
		void onTableNewSearch(ArrayList<MangroveTree> mTree);
	}
	*/
}