package org.deafsapps.mangrovemanager.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import org.deafsapps.mangrovemanager.R;

public class LegendDialog extends DialogFragment implements OnClickListener
{
	private Button btn_Ok;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.activity_legend_dialog, null);
		this.getDialog().setTitle("Map Legend - Species");
		
		this.btn_Ok = (Button) rootView.findViewById(R.id.buttonDialog_legend);
			this.btn_Ok.setOnClickListener(this);
		
		return rootView;
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId())
		{
			case R.id.buttonDialog_legend:
				this.dismiss();
				break;		
		}		
	}
	
}