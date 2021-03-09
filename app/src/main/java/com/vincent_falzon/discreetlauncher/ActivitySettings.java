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
	private static ArrayList<String> global_iconPacks;
	private static ArrayList<String> global_packsNames ;


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
					iconPack.setEntries(global_packsNames.toArray(new CharSequence[0]));
					iconPack.setEntryValues(global_iconPacks.toArray(new CharSequence[0]));
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

		// Search for installed icon packs
		searchIconPacks() ;

		// Load the settings from the XML file
		setContentView(R.layout.activity_settings) ;
		getSupportFragmentManager().beginTransaction().replace(R.id.settings_container, new SettingsFragment()).commit() ;
	}


	/**
	 * Build a list of the installed icon packs
	 */
	private void searchIconPacks()
	{
		// Initializations
		if(global_iconPacks == null) global_iconPacks = new ArrayList<>() ;
			else global_iconPacks.clear() ;
		if(global_packsNames == null) global_packsNames = new ArrayList<>() ;
			else global_packsNames.clear() ;
		PackageManager apkManager = getPackageManager() ;
		Intent filter = new Intent("org.adw.launcher.THEMES") ;

		// Add the default option first (no icon pack)
		global_iconPacks.add("none") ;
		global_packsNames.add(getString(R.string.text_no_icon_pack)) ;

		// Retrieve the list of installed icon packs
		List<ResolveInfo> packsList = apkManager.queryIntentActivities(filter, PackageManager.GET_META_DATA) ;

		// Browse the retrieved packs and store their information in the lists
		for(ResolveInfo pack:packsList)
		{
			global_iconPacks.add(pack.activityInfo.packageName) ;
			global_packsNames.add(pack.loadLabel(apkManager).toString()) ;
		}
	}
}
