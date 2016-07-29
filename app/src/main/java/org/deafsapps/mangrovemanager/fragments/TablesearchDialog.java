package org.deafsapps.mangrovemanager.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import org.deafsapps.mangrovemanager.utils.MangroveManagerCommInterface;
import org.deafsapps.mangrovemanager.R;
import org.deafsapps.mangrovemanager.db.DBParam;

public class TablesearchDialog extends DialogFragment implements OnClickListener, OnSeekBarChangeListener,
																 OnItemSelectedListener, TextWatcher
{
	private float maxZmsl, minZmsl, maxDbh, minDbh;
	private EditText edt_id, edt_tag;
	private TextView tView_zmslValue, tView_dbhValue;
	private Spinner spn_species;
	private SeekBar sBar_zmsl, sBar_dbh;
	private Button btn_save, btn_cancel;
	private static MangroveManagerCommInterface mSearchDialogResponse;
	
	// A no-arguments constructor is mandatory for any 'Fragment'
	public TablesearchDialog() { }
	// This static method allows to receive special arguments such as interfaces
	public static void setUpFragment(Object mObject) { mSearchDialogResponse = (MangroveManagerCommInterface) mObject; }
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.activity_tablesearch_dialog, null);
		savedInstanceState = this.getArguments();
			this.maxZmsl = savedInstanceState.getFloat("maxZmsl");
			this.minZmsl = savedInstanceState.getFloat("minZmsl");
			this.maxDbh = savedInstanceState.getFloat("maxDbh");
			this.minDbh = savedInstanceState.getFloat("minDbh");

		this.getDialog().setTitle("Search Entries");
				
		this.edt_id = (EditText) rootView.findViewById(R.id.editTextSearch_id);	
			this.edt_id.addTextChangedListener(this);
		this.edt_tag = (EditText) rootView.findViewById(R.id.editTextSearch_tag);
			this.edt_tag.addTextChangedListener(this);
		this.spn_species = (Spinner) rootView.findViewById(R.id.spinnerSearch_species);	
			this.spn_species.setOnItemSelectedListener(this);
		this.sBar_zmsl = (SeekBar) rootView.findViewById(R.id.seekBarSearch_zmsl);
			this.sBar_zmsl.setProgress(0);
			this.sBar_zmsl.setMax(100);
			this.sBar_zmsl.setOnSeekBarChangeListener(this);
		this.sBar_dbh = (SeekBar) rootView.findViewById(R.id.seekBarSearch_dbh);
			this.sBar_dbh.setProgress(0);
			this.sBar_dbh.setMax(100);
			this.sBar_dbh.setOnSeekBarChangeListener(this);
		this.tView_zmslValue = (TextView) rootView.findViewById(R.id.textViewSearch_zmslMeasure);
			this.tView_zmslValue.setText(String.format("%.3f", this.minZmsl));
		this.tView_dbhValue = (TextView) rootView.findViewById(R.id.textViewSearch_dbhMeasure);
			this.tView_dbhValue.setText(String.format("%.2f", this.minDbh));
		this.btn_save = (Button) rootView.findViewById(R.id.buttonSearch_ok);
			this.btn_save.setOnClickListener(this);
		this.btn_cancel = (Button) rootView.findViewById(R.id.buttonSearch_cancel);
			this.btn_cancel.setOnClickListener(this);	
		
		return rootView;
	}

	@Override
	public void onClick(View v) 
	{		
		switch (v.getId())
		{
			case R.id.buttonSearch_ok:
				String mWhere = null;
				String[] mWhereArgs = null;
				
				// 'mWhere' and 'mWhereArgs' values change according to the parameters filled in			
				if (this.edt_id.isEnabled())   // 'Id'
				{
					if (!this.edt_id.getText().toString().matches(""))
					{
						mWhere = DBParam.Table._ID + "=?";
						mWhereArgs = new String[] { this.edt_id.getText().toString() };
					}
					else
					{
						mWhere = null; 
						mWhereArgs = null;
					}
				}
				else if (this.edt_tag.isEnabled())   // 'Tag'
				{
					if (!this.edt_tag.getText().toString().matches(""))
					{
						mWhere = DBParam.Table.TAG + "=?"; 
						mWhereArgs = new String[] { this.edt_tag.getText().toString() };
					}
					else
					{
						mWhere = null; 
						mWhereArgs = null;
					}
				}
				else   // 'Spinner' and 'SeekBar's
				{
					if (this.spn_species.getSelectedItemPosition() == 0)
					{
						mWhere = DBParam.Table.ZMSL + ">=? AND " + DBParam.Table.DBH + ">=?";
						mWhereArgs = new String[] { String.format("%.3f", this.sBar_zmsl.getProgress()*(this.maxZmsl/this.sBar_zmsl.getMax()) + this.minZmsl),
													String.format("%.2f", this.sBar_dbh.getProgress()*(this.maxDbh/this.sBar_dbh.getMax()) + this.minDbh) };
					}
					else
					{
						mWhere = DBParam.Table.SPECIES + "=? AND " + DBParam.Table.ZMSL + ">=? AND " + DBParam.Table.DBH + ">=?";
						mWhereArgs = new String[] { this.spn_species.getSelectedItem().toString(),
													String.format("%.3f", this.sBar_zmsl.getProgress()*(this.maxZmsl/this.sBar_zmsl.getMax()) + this.minZmsl),
													String.format("%.2f", this.sBar_dbh.getProgress()*(this.maxDbh/this.sBar_dbh.getMax()) + this.minDbh) };						
					}
				}					

				mSearchDialogResponse.onSearchResponse(mWhere, mWhereArgs);
				break;
			case R.id.buttonSearch_cancel:
				break;
		}		
		this.dismiss();
	}		

	// The following methods corresponds to the interface 'TextWatcher'
	@Override
	public void afterTextChanged(Editable edtText) 
	{ 
		if (edtText.toString().length() == 0)
		{	
			if (edtText.hashCode() == this.edt_id.getText().hashCode())
				this.edt_tag.setEnabled(true);
			else
				this.edt_id.setEnabled(true);

			this.spn_species.setEnabled(true);
			this.sBar_zmsl.setEnabled(true);
			this.sBar_dbh.setEnabled(true);			
		}
		else
		{
			if (edtText.hashCode() == this.edt_id.getText().hashCode())
				this.edt_tag.setEnabled(false);
			else
				this.edt_id.setEnabled(false);
			
			this.spn_species.setEnabled(false);
			this.sBar_zmsl.setEnabled(false);
			this.sBar_dbh.setEnabled(false);			
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }
	
	// The following methods corresponds to the interface 'OnItemSelectedListener'
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) 
	{
		if (position != 0)
		{
			this.edt_id.setEnabled(false);
			this.edt_tag.setEnabled(false);
		}
		else
			if (this.sBar_zmsl.getProgress() == 0 && this.sBar_dbh.getProgress() == 0)
			{
				this.edt_id.setEnabled(true); 
				this.edt_tag.setEnabled(true); 
			}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) { }
	
	// The following methods corresponds to the interface 'OnSeekBarChangeListener'
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
	{
		if (seekBar.getId() == R.id.seekBarSearch_zmsl)
			this.tView_zmslValue.setText(String.format("%.3f", progress*(this.maxZmsl/this.sBar_zmsl.getMax()) + this.minZmsl));
		else
			this.tView_dbhValue.setText(String.format("%.2f", progress*(this.maxDbh/this.sBar_dbh.getMax()) + this.minDbh));	

		if (!(this.sBar_zmsl.getProgress() == 0) || !(this.sBar_dbh.getProgress() == 0))
		{
			this.edt_id.setEnabled(false);
			this.edt_tag.setEnabled(false);			
		}
		else
			if (this.spn_species.getSelectedItem().toString().equals("N/A"))
			{
				this.edt_id.setEnabled(true); 
				this.edt_tag.setEnabled(true);
			}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) { }
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) { }	

	/*
	// This interface is used to get data returned from a 'DialogFragment' ('TablesearchDialog')
	public interface InterfOnTablesearchDialogResponse 
	{
		void onSearchResponse(String where, String[] whereArgs);
	}
	*/
}
