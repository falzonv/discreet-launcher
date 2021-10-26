package com.vincent_falzon.discreetlauncher.settings ;

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
import android.content.SharedPreferences ;
import android.content.pm.PackageManager ;
import android.content.pm.ResolveInfo ;
import android.os.Bundle ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.ListPreference ;
import androidx.preference.PreferenceFragmentCompat ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import java.util.ArrayList ;
import java.util.List ;

/**
 * Settings and Help activity.
 */
public class ActivitySettingsAppearance extends AppCompatActivity
{
	// Attributes
	private static ArrayList<String> iconPacks ;
	private static ArrayList<String> packsNames ;


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

		// Prepare the icon pack setting
		iconPacks.clear() ;
		packsNames.clear() ;
		iconPacks.add(Constants.NONE) ;
		packsNames.add(getString(R.string.set_icon_pack_none)) ;
		searchIconPacks() ;

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
		 * @param savedInstanceState To retrieve the context
		 * @param rootKey Root of the settings hierarchy
		 */
		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
		{
			// Load the settings from the XML file
			setPreferencesFromResource(R.xml.settings_appearance, rootKey) ;

			// Initialize the icon pack selector
			ListPreference iconPack = findPreference(Constants.ICON_PACK) ;
			if(iconPack != null)
				{
					iconPack.setEntries(packsNames.toArray(new CharSequence[0])) ;
					iconPack.setEntryValues(iconPacks.toArray(new CharSequence[0])) ;
				}
		}
	}


	/**
	 * Build a list of the installed icon packs.
	 */
	private void searchIconPacks()
	{
		// Retrieve the list of installed icon packs
		PackageManager apkManager = getPackageManager() ;
		Intent filter = new Intent("org.adw.launcher.THEMES") ;
		List<ResolveInfo> packsList = apkManager.queryIntentActivities(filter, PackageManager.GET_META_DATA) ;

		// Browse the retrieved packs and store their information in the lists
		for(ResolveInfo pack : packsList)
		{
			iconPacks.add(pack.activityInfo.packageName) ;
			packsNames.add(pack.loadLabel(apkManager).toString()) ;
		}
	}


	/**
	 * Retrieve the currently selected color for a preference key.
	 * @param settings To get the settings
	 * @param key Reference in the settings
	 * @param fallback Color to display as fallback
	 * @return Selected color, or the fallback if it was not found
	 */
	public static int getColor(SharedPreferences settings, String key, String fallback)
	{
		// Try to load the color at the given key, or use the provided fallback
		String hexadecimal = settings.getString(key, Constants.NONE) ;
		if((hexadecimal == null) || hexadecimal.equals(Constants.NONE))
			hexadecimal = fallback ;

		// Convert the hexadecimal color to an "int" color
		return ColorPickerDialog.convertHexadecimalColorToInt(hexadecimal) ;
	}
}
