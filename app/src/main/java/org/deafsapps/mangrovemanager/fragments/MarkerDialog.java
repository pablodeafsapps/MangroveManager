package org.deafsapps.mangrovemanager.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.deafsapps.mangrovemanager.utils.MangroveManagerCommInterface;
import org.deafsapps.mangrovemanager.R;
import org.deafsapps.mangrovemanager.db.DBProvider;

public class MarkerDialog extends DialogFragment implements OnClickListener, TextWatcher
{
	private EditText edt_comment;
	private Button btn_save;
	private Button btn_cancel;
	private Integer idMarker;
	private String mMangTreeExtras;
	private static MangroveManagerCommInterface mDialogResponse;
	
	// A no-arguments constructor is mandatory for any 'Fragment'
	public MarkerDialog() { }
	// This static method allows to receive special arguments such as interfaces
	public static void setUpFragment(Object mObject) { mDialogResponse = (MangroveManagerCommInterface) mObject; }
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		View rootView = inflater.inflate(R.layout.activity_marker_dialog, null);
		savedInstanceState = this.getArguments();
			this.idMarker = savedInstanceState.getInt("mId");   //mId;
			this.mMangTreeExtras = savedInstanceState.getString("mExtras");  // mExtras;		
		
		this.getDialog().setTitle("Add Comment");
		
		this.btn_save = (Button) rootView.findViewById(R.id.buttonDialog_save);
			this.btn_save.setEnabled(false);
			this.btn_save.setOnClickListener(this);
		this.btn_cancel = (Button) rootView.findViewById(R.id.buttonDialog_cancel);
			this.btn_cancel.setOnClickListener(this);		
		this.edt_comment = (EditText) rootView.findViewById(R.id.editTextDialog_comment);
			this.edt_comment.addTextChangedListener(this);
			// If the marker field 'extras' is not null, it is shown in the 'EditText'
			if (this.mMangTreeExtras != null) 
				this.edt_comment.setText(this.mMangTreeExtras);		

		return rootView;
	}

	@Override
	public void onClick(View v) 
	{
		switch (v.getId())
		{
			case R.id.buttonDialog_save:
				// Database update with the new value for 'extras'				
				@SuppressWarnings("unused")
				int rChanged = DBProvider.saveMangroveTreeExtras(getActivity().getContentResolver(), (int) this.idMarker, this.edt_comment.getText().toString());

				// Interface method trigger which allows us to get values just before dialog is dismissed
			    mDialogResponse.onDialogResponse(this.idMarker, this.edt_comment.getText().toString());
				break;
				
			case R.id.buttonDialog_cancel:
				break;		
		}
		this.dismiss();
	}

	@Override
	public void afterTextChanged(Editable edtText) 
	{
		if (edtText.toString().matches("^ ") || edtText.toString().equals(""))
			this.btn_save.setEnabled(false);
		else
			this.btn_save.setEnabled(true);		
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }

	/*
	// This interface is used to get data returned from a 'DialogFragment' ('MarkerDialog')
	public interface InterfOnMarkerDialogResponse 
	{
		void onDialogResponse(Integer mId, String mExtras);
	}
	*/
}