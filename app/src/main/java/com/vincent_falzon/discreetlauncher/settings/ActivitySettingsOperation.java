package com.vincent_falzon.discreetlauncher.settings ;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2022 Vincent Falzon

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program.  If not, see <https://www.gnu.org/licenses/>.

 */

// Imports
import android.content.SharedPreferences ;
import android.content.pm.ActivityInfo ;
import android.os.Build ;
import android.os.Bundle ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.ListPreference ;
import androidx.preference.PreferenceFragmentCompat ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import java.util.ArrayList ;

/**
 * Settings and Help activity.
 */
public class ActivitySettingsOperation extends AppCompatActivity
{
	// Attributes
	private static ArrayList<String> applicationsComponentInfos ;
	private static ArrayList<String> applicationsNames ;


	/**
	 * Constructor.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Initializations
		if(applicationsComponentInfos == null) applicationsComponentInfos = new ArrayList<>() ;
		if(applicationsNames == null) applicationsNames = new ArrayList<>() ;

		// Prepare the icon pack setting
		applicationsComponentInfos.clear() ;
		applicationsNames.clear() ;
		applicationsComponentInfos.add(Constants.NONE) ;
		applicationsNames.add(getString(R.string.set_swipe_leftrightwards_no_action)) ;
		loadInstalledApplications() ;

		// Load the general settings layout
		setContentView(R.layout.activity_settings) ;
		getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit() ;
	}


	/**
	 * Load the general settings from the XML file and prepare their values.
	 */
	public static class SettingsFragment extends PreferenceFragmentCompat
	{
		/**
		 * Constructor.
		 */
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
		{
			// Load the settings from the XML file
			setPreferencesFromResource(R.xml.settings_operation, rootKey) ;

			// Initialize the gestures selectors
			ListPreference swipeTowardsLeft = findPreference(Constants.SWIPE_LEFTWARDS) ;
			if(swipeTowardsLeft != null)
				{
					swipeTowardsLeft.setEntries(applicationsNames.toArray(new CharSequence[0])) ;
					swipeTowardsLeft.setEntryValues(applicationsComponentInfos.toArray(new CharSequence[0])) ;
				}
			ListPreference swipeTowardsRight = findPreference(Constants.SWIPE_RIGHTWARDS) ;
				if(swipeTowardsRight != null)
				{
					swipeTowardsRight.setEntries(applicationsNames.toArray(new CharSequence[0])) ;
					swipeTowardsRight.setEntryValues(applicationsComponentInfos.toArray(new CharSequence[0])) ;
				}
		}
	}


	/**
	 * Build a list of all installed applications.
	 */
	private void loadInstalledApplications()
	{
		// Retrieve the list of all installed applications
		ArrayList<Application> allApplications = ActivityMain.getApplicationsList().getApplications(false) ;

		// Store the retrieved information in the lists
		for(Application application : allApplications)
		{
			applicationsComponentInfos.add(application.getComponentInfo()) ;
			applicationsNames.add(application.getDisplayName()) ;
		}
	}


	/**
	 * Perfom actions when returning to the home screen.
	 */
	@Override
	protected void onDestroy()
	{
		// Fix an Android Oreo 8.1 bug (orientation is sometimes kept from an activity to another)
		if(Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1)
			{
				// Retrieve the current orientation setting
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this) ;
				String forced_orientation = settings.getString(Constants.FORCED_ORIENTATION, Constants.NONE) ;
				if(forced_orientation == null) forced_orientation = Constants.NONE ;

				// Migrate from the old setting if needed (to remove later)
				if(settings.getBoolean(Constants.FORCE_PORTRAIT, false))
					{
						forced_orientation = "portrait" ;
						SharedPreferences.Editor editor = settings.edit() ;
						editor.putBoolean(Constants.FORCE_PORTRAIT, false) ;
						editor.putString(Constants.FORCED_ORIENTATION, forced_orientation) ;
						editor.apply() ;
					}

				// Apply the requested orientation
				switch(forced_orientation)
				{
					case "portrait" :
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) ;
						break ;
					case "landscape" :
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ;
						break ;
					case "reverse_landscape" :
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) ;
						break ;
					default :
						setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) ;
						break ;
				}
			}

		// Let the parent actions be performed
		super.onDestroy() ;
	}
}
