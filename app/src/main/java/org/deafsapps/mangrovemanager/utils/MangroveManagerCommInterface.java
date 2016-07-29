package org.deafsapps.mangrovemanager.utils;

import org.deafsapps.mangrovemanager.utils.MangroveTree;

import java.util.ArrayList;

public interface MangroveManagerCommInterface
{
    //These methods are used to retrieve data from an 'Activity' to a 'Fragment'
    void onActivity2Fragment(MangroveTree mTree);
    void onActivity2Fragment_plus(ArrayList<MangroveTree> mMangTree);

    // This method is used to get data returned from the Map ´Fragment to an 'Activity'
    void onMapMarkerUpdated(MangroveTree mTree);

    // Follow some 'interface's defined for inter-fragment communication
    void onTableRowPressed(MangroveTree mTree);
    void onTableNewSearch(ArrayList<MangroveTree> mTree);

    // This method is used to get data returned from a 'DialogFragment' ('TablesearchDialog')
    void onSearchResponse(String where, String[] whereArgs);

    // This method is used to get data returned from a 'DialogFragment' ('MarkerDialog')
    void onDialogResponse(Integer mId, String mExtras);

    // This method is used to get data returned from an 'AsyncTask' ('ThreadSafeQueryDB')
    void onATaskResponse(ArrayList<MangroveTree> mArray);
}
