package com.vincent_falzon.discreetlauncher;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2021 Vincent Falzon

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
import android.content.Intent ;
import android.content.pm.PackageManager ;
import android.content.pm.ResolveInfo ;
import android.os.Bundle ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.ListPreference ;
import androidx.preference.PreferenceFragmentCompat ;
import java.util.ArrayList ;
import java.util.List ;

/**
 * Settings and Help activity.
 */
public class ActivitySettings extends AppCompatActivity
{
	// Attributes
	private static ArrayList<String> iconPacks ;
	private static ArrayList<String> packsNames ;
	private static ArrayList<String> applicationsNames ;
	private static ArrayList<String> applicationsDisplayNames ;


	/**
	 * Load the settings from the XML file and prepare their values.
	 */
	public static class SettingsFragment extends PreferenceFragmentCompat
	{
		/**
		 * Constructor.
		 * @param savedInstanceState To retrieve the context
		 * @param rootKey Root of the settings hierarchy
		 */
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
		{
			// Load the settings from the XML file
			setPreferencesFromResource(R.xml.settings, rootKey) ;

			// Initialize the icon pack selector
			ListPreference iconPack = findPreference("icon_pack") ;
			if(iconPack != null)
				{
					iconPack.setEntries(packsNames.toArray(new CharSequence[0])) ;
					iconPack.setEntryValues(iconPacks.toArray(new CharSequence[0])) ;
				}

			// Initialize the notification applications selectors
			ListPreference app1 = findPreference("notification_app1") ;
			if(app1 != null)
				{
					app1.setEntries(applicationsDisplayNames.toArray(new CharSequence[0])) ;
					app1.setEntryValues(applicationsNames.toArray(new CharSequence[0])) ;
				}
			ListPreference app2 = findPreference("notification_app2") ;
			if(app2 != null)
				{
					app2.setEntries(applicationsDisplayNames.toArray(new CharSequence[0])) ;
					app2.setEntryValues(applicationsNames.toArray(new CharSequence[0])) ;
				}
			ListPreference app3 = findPreference("notification_app3") ;
			if(app3 != null)
				{
					app3.setEntries(applicationsDisplayNames.toArray(new CharSequence[0])) ;
					app3.setEntryValues(applicationsNames.toArray(new CharSequence[0])) ;
				}
		}
	}


	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Call the constructor of the parent class
		super.onCreate(savedInstanceState) ;

		// Initializations
		if(iconPacks == null) iconPacks = new ArrayList<>() ;
		if(packsNames == null) packsNames = new ArrayList<>() ;
		if(applicationsNames == null) applicationsNames = new ArrayList<>() ;
		if(applicationsDisplayNames == null) applicationsDisplayNames = new ArrayList<>() ;

		// Prepare the icon pack setting
		iconPacks.clear() ;
		packsNames.clear() ;
		iconPacks.add("none") ;
		packsNames.add(getString(R.string.text_no_icon_pack)) ;
		searchIconPacks() ;

		// Prepare the notification applications settings
		applicationsNames.clear() ;
		applicationsDisplayNames.clear() ;
		applicationsNames.add("none") ;
		applicationsDisplayNames.add(getString(R.string.text_no_application)) ;
		searchApplications() ;

		// Load the settings from the XML file
		setContentView(R.layout.activity_settings) ;
		getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit() ;
	}


	/**
	 * Build a list of the installed icon packs
	 */
	private void searchIconPacks()
	{
		// Retrieve the list of installed icon packs
		PackageManager apkManager = getPackageManager() ;
		Intent filter = new Intent("org.adw.launcher.THEMES") ;
		List<ResolveInfo> packsList = apkManager.queryIntentActivities(filter, PackageManager.GET_META_DATA) ;

		// Browse the retrieved packs and store their information in the lists
		for(ResolveInfo pack:packsList)
		{
			iconPacks.add(pack.activityInfo.packageName) ;
			packsNames.add(pack.loadLabel(apkManager).toString()) ;
		}
	}


	/**
	 * Build a list of the installed applications
	 */
	private void searchApplications()
	{
		// Browse the applications list and store their information in the lists
		ArrayList<Application> applicationsList = ActivityMain.getApplicationsList().getApplications() ;
		for(Application application : applicationsList)
		{
			applicationsNames.add(application.getDisplayName() + "_discreet_" + application.getName() + "_discreet_" + application.getApk()) ;
			applicationsDisplayNames.add(application.getDisplayName()) ;
		}
	}
}
