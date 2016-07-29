package org.deafsapps.mangrovemanager.fragments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.androidmapsextensions.ClusterOptions;
import com.androidmapsextensions.ClusterOptionsProvider;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
//import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.androidmapsextensions.GoogleMap.OnInfoWindowClickListener;
//import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.Marker;
//import com.google.android.gms.maps.model.Marker;
import com.androidmapsextensions.MarkerOptions;
//import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
//import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.deafsapps.mangrovemanager.utils.MangroveManagerCommInterface;
import org.deafsapps.mangrovemanager.utils.MangroveTree;
import org.deafsapps.mangrovemanager.R;
import org.deafsapps.mangrovemanager.utils.ThreadSafeQueryDB;
import org.deafsapps.mangrovemanager.activities.MangroverMain;
import org.deafsapps.mangrovemanager.db.DBProvider;
//import com.google.android.gms.maps.GoogleMap;

public class MangroverMap extends Fragment implements OnInfoWindowClickListener, OnMarkerClickListener,
													  OnCameraChangeListener, MangroveManagerCommInterface
{	
	private static final double CLUSTER_SIZE = 100;   //{180, 160, 144, 120, 96};
	
	private Bundle mBundle;
	private boolean clusterON = true;
	private static GoogleMap myMap;
	private static LatLngBounds.Builder mMapCentre;
	// The 'HashMap' will store statically a 'Marker' and its corresponding 'MangroveTree' id
	private static HashMap<MangroveTree, Marker> markersMap;
	private MangroveManagerCommInterface mMapMarkerListener;
	// Class method to select a particular marker icon according to an input parameter
	private Bitmap getMarkerIcon(String mSpecies)
	{
		// By default, the marker is a blue solid circle and "AA" is represented so.
		int iconDrawable = R.drawable.ic_marker_circle;

		if (mSpecies.matches("AO")) { iconDrawable = R.drawable.ic_marker_ring_cross; }
		else if (mSpecies.matches("AR")) { iconDrawable = R.drawable.ic_marker_ring_horline; }
		else if (mSpecies.matches("BC")) { iconDrawable = R.drawable.ic_marker_ring_verline; }
		else if (mSpecies.matches("BG")) { iconDrawable = R.drawable.ic_marker_ring; }
		else if (mSpecies.matches("EA")) { iconDrawable = R.drawable.ic_marker_square_siluet; }
		else if (mSpecies.matches("HL")) { iconDrawable = R.drawable.ic_marker_square_siluetcross; }
		else if (mSpecies.matches("LR")) { iconDrawable = R.drawable.ic_marker_square_siluetcross2; }
		else if (mSpecies.matches("Nypa")) { iconDrawable = R.drawable.ic_marker_square; }
		else if (mSpecies.matches("RA")) { iconDrawable = R.drawable.ic_marker_squareminuscircle; }
		else if (mSpecies.matches("RM")) { iconDrawable = R.drawable.ic_marker_star_siluet; }
		else if (mSpecies.matches("SA")) { iconDrawable = R.drawable.ic_marker_star; }
		else if (mSpecies.matches("SO")) { iconDrawable = R.drawable.ic_marker_triangle_siluet; }
		else if (mSpecies.matches("XG")) { iconDrawable = R.drawable.ic_marker_triangle_siluet_verline; }
		else if (mSpecies.matches("CT")) { iconDrawable = R.drawable.ic_marker_triangle; }

		return BitmapFactory.decodeResource(getActivity().getResources(), iconDrawable);
	}	
	// Private inner class which manipulates the map in the UI thread
	private class addMarkersAtOnce implements Runnable 
	{
		private ArrayList<MangroveTree> mArray;
		private ArrayList<MarkerOptions> mOptions;
		private LayoutInflater mInflater;
				
		public addMarkersAtOnce(ArrayList<MangroveTree> mMangTreeArray, ArrayList<MarkerOptions> mMarkerOptions, LayoutInflater mLayoutInflater) 
		{ 
			this.mArray = mMangTreeArray;
			this.mOptions = mMarkerOptions;
			this.mInflater = mLayoutInflater;
		}

		@Override
		public void run() 
		{ 
			Marker mMarker;
			
			for (int idx = 0; idx < this.mOptions.size(); idx++)
			{
				mMarker = myMap.addMarker(this.mOptions.get(idx));
				markersMap.put(this.mArray.get(idx), mMarker);
			}
			
			myMap.setInfoWindowAdapter(new CustomInfoWindow(this.mInflater));
		    myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapCentre.build(), 50));
		}		
	}

	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);

		if (context instanceof MangroveManagerCommInterface)
			this.mMapMarkerListener = (MangroveManagerCommInterface) context;
		else
			throw new ClassCastException(context.toString() +
					" must implemenet MarkerDialog.InterfOnMarkerDialogResponse");	
		
		((MangroverMain) context).mActivity2MapListener = this;
	}
	
	@Override
	public void onDetach() 
	{
		super.onDetach();
		// Release the listeners
		this.mMapMarkerListener = null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.activity_mangrover_map, container, false);	
		this.setHasOptionsMenu(true);
		
		this.mBundle = savedInstanceState;
		// Don't forget to change the fragment class in the corresponding layout
		myMap = ((SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.mangrover_mapFragment)).getExtendedMap();
			myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			myMap.setOnMarkerClickListener(this);   
			myMap.setOnInfoWindowClickListener(this);
			myMap.setOnCameraChangeListener(this);
			myMap.setMyLocationEnabled(true);
							
		// The interface 'IntOnAsyncTaskResponse' is used as the second argument of the constructor
		new ThreadSafeQueryDB(this.getActivity(), this).execute("", new String[] {""}, "");
		
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) 
	{
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.mangrover_map, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem mItem) 
	{
		super.onOptionsItemSelected(mItem);
		
		switch (mItem.getItemId())
		{
			case R.id.action_centre_map:
				// The map is re-centered 
				Toast.makeText(getActivity(), "Centring map", Toast.LENGTH_LONG).show();
			    myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mMapCentre.build(), 50));
				break;		
			case R.id.action_map_legend:
				LegendDialog mLDialog = new LegendDialog();
				FragmentManager fm = this.getFragmentManager();
				mLDialog.show(fm, "Map Legend");
				break;
		}
		
		return true;
	}

	@Override
	public boolean onMarkerClick(Marker mClusterMarker) 
	{	
		if (!mClusterMarker.isCluster())
		{ 
			for (Entry<MangroveTree, Marker> mEntry : markersMap.entrySet())
			{ 
				if (mEntry.getValue().equals(mClusterMarker))
					mEntry.getValue().showInfoWindow();   // Show info window
			}
		}
		
		return false;
	}
				
	@Override
	public void onInfoWindowClick(Marker mMarker) 
	{
		MangroveTree mMangTree;
		
		for (Entry<MangroveTree, Marker> mEntry : markersMap.entrySet()) 
		{ 
			if (mEntry.getValue().equals(mMarker))
			{
				mMangTree = DBProvider.queryOneMangroveTreeDB(getActivity().getContentResolver(), String.valueOf(mEntry.getKey().getId()));
				
				// The interface 'IntOnMarkerDialogResponse' is used as the third argument of the constructor
				Bundle mBundle = new Bundle();
					mBundle.putInt("mId", (int) mMangTree.getId());
					mBundle.putString("mExtras", mMangTree.getExtras());
				MarkerDialog mMDialog = new MarkerDialog();
					MarkerDialog.setUpFragment(this);
					mMDialog.setArguments(mBundle);
					mMDialog.show(this.getFragmentManager(), "Marker Dialog");
			}
		}
	}

	// The following method corresponds to the interface 'MangroveManagerCommInterface'
	@Override
	public void onATaskResponse(final ArrayList<MangroveTree> mMangTreeArray)
	{
		ClusteringSettings mClusteringSettings = new ClusteringSettings();
		mClusteringSettings.addMarkersDynamically(true);
		mClusteringSettings.clusterOptionsProvider(new MangroverClusterOptionsProvider(this.getResources()));
		mClusteringSettings.clusterSize(CLUSTER_SIZE);
		myMap.setClustering(mClusteringSettings);

		final ProgressDialog mProgBar = new ProgressDialog(getActivity());
		mProgBar.setCancelable(true);
		mProgBar.setMessage("Loading map...");
		mProgBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgBar.setProgress(0);
		mProgBar.setMax(100);
		//mProgBar.show();
		final Handler mHandler = new Handler();
		final LayoutInflater mInflater = this.getLayoutInflater(this.mBundle);
		mMapCentre = new LatLngBounds.Builder();

		new Thread(new Runnable()
		{
			private ArrayList<MarkerOptions> mMarkerOptionsArray = new ArrayList<>();

			@Override
			public void run()
			{
				if (mMangTreeArray != null)
				{
					// We use a 'HashMap' to identify each 'MangroveTree' with its corresponding 'Marker'.
					// We use the same 'id' retrieved by the DB, so that we know which marker has been tapped
					markersMap = new HashMap<>();   // HashMap<MangroveTree, Marker>

					for (MangroveTree mMangTree : mMangTreeArray)
					{
						Bitmap bitmapMarker = getMarkerIcon(mMangTree.getSpecies());
						// Marker size sets up to 16 pixels
						float sizeRatio = bitmapMarker.getHeight()/16;
						final Bitmap iconBitmap = Bitmap.createScaledBitmap(bitmapMarker,
								Math.round(bitmapMarker.getWidth()/sizeRatio), Math.round(bitmapMarker.getHeight()/sizeRatio), false);

						this.mMarkerOptionsArray.add(new MarkerOptions().position(new LatLng(mMangTree.getLatitude(), mMangTree.getLongitude()))
								.anchor((float) 0.5,(float) 0.5).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)));
						mMapCentre.include(new LatLng(mMangTree.getLatitude(), mMangTree.getLongitude()));
					}

					// The map is updated in the UI thread
					mHandler.post(new addMarkersAtOnce(mMangTreeArray, this.mMarkerOptionsArray, mInflater));
				}
			}
		}).start();

		//mMap.setInfoWindowAdapter(new CustomInfoWindow(this.getLayoutInflater(this.mBundle)));
		Log.d("onATaskResponse", "Map loaded");
	}
			
	// The following method corresponds to the interface 'MangroveManagerCommInterface'
	@Override
	public void onDialogResponse(Integer mMarkerId, String mExtras) 
	{ 
		for (Entry<MangroveTree, Marker> mEntry : markersMap.entrySet()) 
		{
			if (mEntry.getKey().getId() == mMarkerId.longValue()) 
			{ 	
				// Update the 'HashMap'
				mEntry.getKey().setExtras(mExtras);
				Toast.makeText(getActivity(), "Entry successfully updated", Toast.LENGTH_LONG).show();
				// The update has to be sent to the 'FragmentActivity'
				this.mMapMarkerListener.onMapMarkerUpdated(mEntry.getKey());
				// The information window has to be refreshed
				mEntry.getValue().showInfoWindow();
			}
		}
	}
	
	// The following method corresponds to the interface 'MangroveManagerCommInterface'
	@Override
	public void onActivity2Fragment(final MangroveTree mTree) 
	{ 	
		for (final Entry<MangroveTree, Marker> mEntry : markersMap.entrySet()) 
		{	
			if (mEntry.getKey().getId() == mTree.getId())
			{ 
				// Centre the map camera over the marker
				myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mEntry.getKey().getLatitude(), mEntry.getKey().getLongitude()), (float) 21.0),
									new GoogleMap.CancelableCallback() 
									{							
										@Override
										public void onFinish() 
										{ 
											Toast.makeText(getActivity(), "Map centred on tag: " + String.valueOf(mTree.getTag()), Toast.LENGTH_LONG).show();
											mEntry.getValue().showInfoWindow();
										}
										@Override 
										public void onCancel() { }
									});	
				//mEntry.getValue().showInfoWindow();
			}
        }		
	}

	// The following method corresponds to the interface 'MangroveManagerCommInterface'
	@Override
	public void onActivity2Fragment_plus(ArrayList<MangroveTree> mMangTree) 
	{		System.out.println("array size: " + mMangTree.size());
		for (Entry<MangroveTree, Marker> mEntry : markersMap.entrySet())
		{
			mEntry.getValue().setVisible(false);
			
			for (MangroveTree mTree : mMangTree)
			{								
				if (mTree.getId() == mEntry.getKey().getId())
				{
					mEntry.getValue().setVisible(true);
					//System.out.println(mTree.getId() + " => " + mEntry.getKey().getId());
				}
			}
		}			
	}

	// The following class modifies the layout of a marker's information window
	private class CustomInfoWindow implements InfoWindowAdapter
	{
		private LayoutInflater mInflater = null;
		
		public CustomInfoWindow(LayoutInflater mLayoutInflater) { this.mInflater = mLayoutInflater; }
		
		// 'getInfoWindow' allows to provide a view that will be used for the entire info window 
		@Override
		public View getInfoWindow(Marker mMarker) 
		{
			// If 'null' is returned, the default info window is used and 'getInfoContents' is called
			// Otherwise, the new 'View' should be retrieved
			return null;
		}
		
		// 'getInfoContents' allows to customize the contents of the window
		@Override
		public View getInfoContents(Marker mMarker) 
		{ 
			if (!mMarker.isCluster())
			{  
				// The corresponding layout is 'inflated' so that we get some customization
				View myView = this.mInflater.inflate(R.layout.marker_info_window, null);			
				MangroveTree mMangTree;
				
				for (Entry<MangroveTree, Marker> mEntry : markersMap.entrySet())
				{ 
					if (mEntry.getValue().equals(mMarker)) 
					{ 
						mMangTree = mEntry.getKey();
					
						TextView tv_id = (TextView) myView.findViewById(R.id.textView_id);
							tv_id.setText("Id: " + String.valueOf(mMangTree.getId()));
						TextView tv_tag = (TextView) myView.findViewById(R.id.textView_tag);
							tv_tag.setText("Tag: " + mMangTree.getTag());
						TextView tv_lat = (TextView) myView.findViewById(R.id.textView_latitude);
							tv_lat.setText("Lat: " + String.format("%.8f",mMangTree.getLatitude()));
						TextView tv_long = (TextView) myView.findViewById(R.id.textView_longitude);
							tv_long.setText("Long: " + String.format("%.8f",mMangTree.getLongitude()));
						TextView tv_zmsl = (TextView) myView.findViewById(R.id.textView_zmsl);
							tv_zmsl.setText("Z MSL: " + String.valueOf(mMangTree.getZ_msl()) + " m");
						TextView tv_species = (TextView) myView.findViewById(R.id.textView_species);
							tv_species.setText("Species: " + mMangTree.getSpecies());
						TextView tv_dbh = (TextView) myView.findViewById(R.id.textView_dbh);
							tv_dbh.setText("DBH: " + String.valueOf(mMangTree.getDbh()) + " cm");
						TextView tv_extras = (TextView) myView.findViewById(R.id.textView_extras);
							tv_extras.setText("Comments: " + mMangTree.getExtras());
					}
				}						
				// The new 'View' has to be returned so that the changes take effect
				return myView;
			}
			else
				return null;
		}
	}
			
	@Override
	public void onCameraChange(CameraPosition cameraPosition) 
	{ 
        ClusteringSettings mClusteringSettings = new ClusteringSettings();
	    	mClusteringSettings.addMarkersDynamically(true);
	    	mClusteringSettings.clusterOptionsProvider(new MangroverClusterOptionsProvider(this.getResources()));
	    	mClusteringSettings.clusterSize(CLUSTER_SIZE);
		
		if ((cameraPosition.zoom > 19.0) && this.clusterON)
		{ 
			mClusteringSettings.enabled(false);
			myMap.setClustering(mClusteringSettings);
			this.clusterON = false;
		}
		if ((cameraPosition.zoom <= 19.0) && !this.clusterON)
		{ 
			mClusteringSettings.enabled(true);
			myMap.setClustering(mClusteringSettings);
			this.clusterON = true;
		}
	}
		
	public class MangroverClusterOptionsProvider implements ClusterOptionsProvider 
	{
	    private int[] mResources = {R.drawable.m1, R.drawable.m2, R.drawable.m3, R.drawable.m4, R.drawable.m5};
	    private int[] forCounts = {10, 100, 500, 1000, Integer.MAX_VALUE};
	    private Bitmap[] baseBitmaps;
	    private LruCache<Integer, BitmapDescriptor> bitMapCache = new LruCache<>(128);
	    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	    private Rect bounds = new Rect();
	    private ClusterOptions clusterOptions = new ClusterOptions().anchor(0.5f, 0.5f);

	    public MangroverClusterOptionsProvider(Resources resources) 
	    {
	        this.baseBitmaps = new Bitmap[this.mResources.length];
	        for (int i = 0; i < this.mResources.length; i++) { this.baseBitmaps[i] = BitmapFactory.decodeResource(resources, this.mResources[i]); }
	        this.paint.setColor(Color.WHITE);
	        this.paint.setTextAlign(Align.CENTER);
	        paint.setTextSize(resources.getDimension(R.dimen.text_size));   // 15dp
	    }

	    @Override
	    public ClusterOptions getClusterOptions(List<Marker> markers) 
	    {
	        int markersCount = markers.size();
	        BitmapDescriptor cachedIcon = this.bitMapCache.get(markersCount);
	        if (cachedIcon != null) { return this.clusterOptions.icon(cachedIcon); }

	        Bitmap base;
	        int i = 0;
	        do { base = this.baseBitmaps[i]; } while (markersCount >= this.forCounts[i++]);

	        Bitmap bitmap = base.copy(Config.ARGB_8888, true);

	        String text = String.valueOf(markersCount);
	        this.paint.getTextBounds(text, 0, text.length(), this.bounds);
	        float x = bitmap.getWidth() / 2.0f;
	        float y = (bitmap.getHeight() - this.bounds.height()) / 2.0f - this.bounds.top;

	        Canvas canvas = new Canvas(bitmap);
	        canvas.drawText(text, x, y, this.paint);

	        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
	        this.bitMapCache.put(markersCount, icon);

	        return this.clusterOptions.icon(icon);
	    }
	}

	@Override
	public void onMapMarkerUpdated(MangroveTree mTree) { }
	@Override
	public void onTableRowPressed(MangroveTree mTree) { }
	@Override
	public void onTableNewSearch(ArrayList<MangroveTree> mTree) { }
	@Override
	public void onSearchResponse(String where, String[] whereArgs) { }

	/*
	// This interface is used to get data returned from a fragment to an activity
	public interface InterfGetMapMarkerUpdate 
	{
		void onMapMarkerUpdated(MangroveTree mTree);
	}
*/
}