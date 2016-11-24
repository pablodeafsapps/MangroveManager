package org.deafsapps.mangrovemanager.activities;

// This app makes use of the external library 'android-maps-extension' (it works!)
// 'MangroverMain' is the main 'Activity' (actually 'FragmentActivity') of this App.
// It consists of a 'ViewPager' which works with a couple of tabs from 'ActionBar'
// To send and receive arguments to and from the 'Fragment'(s) which compound the App.
// some interfaces are defined within them, and are implemented by 'MangroverMain'. At
// the same time, a listener per interface class is defined in each fragment, so that 
// the main activity (which implements the fragment) can be included in the 'Fragment'
// scope. This binding can be done in the 'Fragment' method 'onAttach'. A similar logic
// applies to retrieve arguments from 'MangroverMain' to both 'Fragment'(s).
// A different logic applies when a 'Fragment' and a 'DialogFragment' want to exchange 
// information (constructors), but it is still valid that the 'Fragment' which receives 
// the info implements the interface.
//
// AS A GENERAL RULE, CUSTOM INTERFACES ARE DEFINED WITHIN THE 'Activity'/'Fragment' THAT
// WANTS TO TRANSMIT DATA AND ARE IMPLEMENTED BY THOSE 'Activity'/'Fragment' THAT RECEIVE THEM
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import org.deafsapps.mangrovemanager.utils.MangroveManagerCommInterface;
import org.deafsapps.mangrovemanager.utils.MangroveTree;
import org.deafsapps.mangrovemanager.fragments.MangroverMap;
import org.deafsapps.mangrovemanager.fragments.MangroverTable;
import org.deafsapps.mangrovemanager.R;

public class MangroverMain extends ActionBarActivity implements TabListener, OnPageChangeListener,
		MangroveManagerCommInterface
{
	public static final int MAP_TAB = 0;
	public static final int TABLE_TAB = 1;
	private ViewPager myViewPager;
	private myPageAdapter mPAdapter; 
	private ActionBar myAB;
	private String[] tabNames = {"Data Map", "Data Table"};
	public MangroveManagerCommInterface mActivity2MapListener, mActivity2TableListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mangrover_main);
									
		this.mPAdapter = new myPageAdapter(this.getSupportFragmentManager());
		
		this.myViewPager = (ViewPager) this.findViewById(R.id.mainActivity_viewPager);		
			this.myViewPager.setAdapter(this.mPAdapter);
			this.myViewPager.setOnPageChangeListener(this);

		this.myAB = this.getSupportActionBar();
			this.myAB.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		for (String tName : this.tabNames)
		{
			// Adding tabs to 'ActionBar'
			this.myAB.addTab(this.myAB.newTab().setText(tName).setTabListener(this));
		}			
	}

	// The following three methods correspond to the interface 'OnPageChangeListener'
	@Override
	public void onPageSelected(int position) 
	{
		// on changing the page make respected tab selected
		this.myAB.setSelectedNavigationItem(position);
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) { }
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) { }	
	
	// The following three methods correspond to the interface 'TabListener'
	@Override
	public void onTabSelected(Tab mTab, FragmentTransaction fTrans) 
	{
		Log.d("MangroverMain", "Tab: " + String.valueOf(mTab.getPosition()));
		// on tab selected show respected fragment view
        this.myViewPager.setCurrentItem(mTab.getPosition());
	}
	
	@Override
	public void onTabReselected(Tab arg0, FragmentTransaction arg1) { }
	@Override
	public void onTabUnselected(Tab arg0, FragmentTransaction arg1) { }
	
	// This class allows to load the different fragments
	class myPageAdapter extends FragmentPagerAdapter
	{
		public myPageAdapter(FragmentManager fm)
		{
			super(fm);
		}

		@Override
		public Fragment getItem(int index)
		{
			switch (index)
			{
				case MAP_TAB:
					return new MangroverMap();
				case TABLE_TAB:
					return new MangroverTable();
	        }
			return null;
		}

		@Override
		public int getCount() { return 2; }
	}
	
	@Override
	public void onTableRowPressed(MangroveTree mTree)
	{
		this.mActivity2MapListener.onActivity2Fragment(mTree);	
	}

	/*
	//This interface is used to retrieve data from an 'Activity' to a 'Fragment'
	public interface InterfOnActivity2Fragment 
	{
		void onActivity2Fragment(MangroveTree mTree);	
		void onActivity2Fragment_plus(ArrayList<MangroveTree> mMangTree);
	}
	*/

	@Override
	public void onTableNewSearch(ArrayList<MangroveTree> mTree)
	{
//		this.mActivity2MapListener.onActivity2Fragment_plus(mTree);
	}

	@Override
	public void onBackPressed()
	{
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle("Quit")
			.setMessage("Are you sure you want to quit?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
            	{
            		@Override
            		public void onClick(DialogInterface dialog, int which) 
            		{
            			finish();
            			System.exit(0); 
                    }
            	})
            .setNegativeButton("No", null).show();
	}

	// The following method corresponds to the interface 'MangroveManagerCommInterface'
	@Override
	public void onMapMarkerUpdated(MangroveTree mTree) { this.mActivity2TableListener.onActivity2Fragment(mTree); }

	@Override
	public void onActivity2Fragment(MangroveTree mTree) { }
	@Override
	public void onActivity2Fragment_plus(ArrayList<MangroveTree> mMangTree) { }
	@Override
	public void onSearchResponse(String where, String[] whereArgs) { }

	@Override
	public void onDialogResponse(Integer mId, String mExtras) { }
	@Override
	public void onATaskResponse(ArrayList<MangroveTree> mArray) { }
}
