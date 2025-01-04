package com.vincent_falzon.discreetlauncher.settings ;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2025 Vincent Falzon

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
import android.os.Build ;
import android.os.Bundle ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.ListPreference ;
import androidx.preference.Preference ;
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
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
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


	// ---------------------------------------------------------------------------------------------

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
			setPreferencesFromResource(R.xml.settings_appearance, rootKey) ;

			// Retrieve preferences that need a special setup
			Preference darkStatusBarIcons = findPreference(Constants.DARK_STATUS_BAR_ICONS) ;
			ListPreference iconPack1 = findPreference(Constants.ICON_PACK) ;
			ListPreference iconPack2 = findPreference(Constants.ICON_PACK_SECONDARY) ;
			Preference noIconPackMessage = findPreference("no_icon_pack_installed") ;

			// Check if the Android 5 warning for dark status bar icons should be displayed
			if((Build.VERSION.SDK_INT < Build.VERSION_CODES.M) && (darkStatusBarIcons != null))
				{
					darkStatusBarIcons.setSummary(R.string.set_dark_status_bar_icons_help) ;
					darkStatusBarIcons.setEnabled(false) ;
				}

			// Check if icon packs are installed (start at 1 to count the "none" option)
			boolean icon_packs_installed = (iconPacks.size() > 1) ;
			if(noIconPackMessage != null) noIconPackMessage.setVisible(!icon_packs_installed) ;

			// Initialize the icon pack selectors
			if(iconPack1 != null)
				{
					iconPack1.setEntries(packsNames.toArray(new CharSequence[0])) ;
					iconPack1.setEntryValues(iconPacks.toArray(new CharSequence[0])) ;
					iconPack1.setEnabled(icon_packs_installed) ;
				}
			if(iconPack2 != null)
				{
					iconPack2.setEntries(packsNames.toArray(new CharSequence[0])) ;
					iconPack2.setEntryValues(iconPacks.toArray(new CharSequence[0])) ;
					iconPack2.setEnabled(icon_packs_installed) ;
				}
		}
	}
}
