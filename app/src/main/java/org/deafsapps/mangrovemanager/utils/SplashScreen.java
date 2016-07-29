package org.deafsapps.mangrovemanager.utils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import org.deafsapps.mangrovemanager.R;
import org.deafsapps.mangrovemanager.activities.MangroverMain;

public class SplashScreen extends Activity 
{
	// Splash screen timer
	private static final int SPLASH_TIMEOUT = 2000;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		new Handler().postDelayed(new Runnable() 
		{			
			@Override
			public void run() 
			{
				// This method will be executed once the timer is over
                Intent mIntent = new Intent(getApplicationContext(), MangroverMain.class);
                startActivity(mIntent);
				
                finish();
			}
		}, SPLASH_TIMEOUT);
	}	
}