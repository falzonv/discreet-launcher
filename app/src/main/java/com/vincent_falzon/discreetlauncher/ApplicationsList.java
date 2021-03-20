package com.vincent_falzon.discreetlauncher ;

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
import android.content.Context ;
import android.content.Intent ;
import android.content.SharedPreferences ;
import android.content.pm.PackageManager ;
import android.content.pm.ResolveInfo ;
import android.graphics.drawable.Drawable ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.Date ;
import java.util.List ;

/**
 * Represent an Android application with its names (displayed, internal and package) and icon.
 */
class ApplicationsList
{
	// Attributes
	private final ArrayList<Application> applications ;
	private final ArrayList<Application> favorites ;
	private final int icon_size ;
	private final InternalFile favorites_file ;
	private final SharedPreferences settings ;
	private String last_update ;


	/**
	 * Constructor to create the applications list.
	 */
	ApplicationsList(Context context, SharedPreferences settings)
	{
		// Initialize the applications lists
		applications = new ArrayList<>() ;
		favorites = new ArrayList<>() ;

		// Define the icons size in pixels
		icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;

		// Other initializations
		favorites_file = new InternalFile(context, "favorites.txt") ;
		this.settings = settings ;
		last_update = "" ;
	}


	/**
	 * Update both the complete applications list and the favorite applications list.
	 */
	void update(Context context)
	{
		// Initializations
		PackageManager apkManager = context.getPackageManager() ;
		applications.clear() ;

		// Retrieve the list of applications that can be launched by the user
		Intent intent = new Intent(Intent.ACTION_MAIN) ;
		intent.addCategory(Intent.CATEGORY_LAUNCHER) ;
		List<ResolveInfo> apkManagerList = apkManager.queryIntentActivities(intent, 0) ;

		// Retrieve the icon pack setting
		String pack_name = settings.getString("icon_pack", "none") ;
		if(pack_name == null) pack_name = "none" ;

		// Check if and icon pack is selected and load it if it does
		IconPack iconPack = selectIconPack(context) ;

		// Browse the APK manager list and store the data of each application in the main list
		Drawable icon ;
		for(ResolveInfo entry:apkManagerList)
		{
			// Load the application icon
			if(iconPack == null) icon = entry.loadIcon(apkManager) ;
				else
				{
					// Retrieve the icon in the pack, use the real icon if not found
					icon = iconPack.searchIcon(entry.activityInfo.packageName, entry.activityInfo.name) ;
					if(icon == null) icon = entry.loadIcon(apkManager) ;
				}
			icon.setBounds(0, 0, icon_size, icon_size) ;

			// Add the application to the list
			Application application = new Application(
					entry.loadLabel(apkManager).toString(),
					entry.activityInfo.name,
					entry.activityInfo.packageName,
					icon) ;
			applications.add(application) ;
		}

		// Sort the applications list in alphabetic order based on display name
		Collections.sort(applications, new Comparator<Application>()
		{
			@Override
			public int compare(Application application1, Application application2)
			{
				return application1.getDisplayName().compareToIgnoreCase(application2.getDisplayName()) ;
			}
		}) ;

		// Update the favorites applications list
		updateFavorites(context) ;

		// Save the last update timestamp and inform the user that the list has been refreshed
		last_update = SimpleDateFormat.getDateTimeInstance().format(new Date()) ;
		ShowDialog.toast(context, R.string.text_applications_list_refreshed) ;
	}


	/**
	 * Update the favorites applications list based on the favorites file and the complete list.
	 * @param context For file format conversion (to be removed later)
	 */
	void updateFavorites(Context context)
	{
		// Initializations
		favorites.clear() ;

		// Check if the favorites file exists
		if(favorites_file.isExisting())
		{
			// Retrieve and browse the internal names of all favorites applications
			for(String name : favorites_file.readAllLines())
			{
				// Search the internal name in the applications list
				for(Application application : applications)
					if(application.getName().equals(name))
					{
						// Add the application to the favorites and move to the next line
						favorites.add(application) ;
						break ;
					}
			}

			// To remove later: manage old file format
			if(favorites.size() == 0) convertOldFavoritesFileFormat(context) ;
		}
	}


	/**
	 * To be removed later, file format conversion due to change in v1.2.0.
	 * This method is called during the favorites update in case the user updated the launcher from
	 * a version before v1.2.0. It updates the favorites file to new format with internal names.
	 * @param context To display an alert dialog
	 */
	private void convertOldFavoritesFileFormat(Context context)
	{
		// Retrieve and browse the package names of all favorites applications
		for(String apk : favorites_file.readAllLines())
		{
			// Search the package name in the applications list
			for(Application application : applications)
				if(application.getApk().equals(apk))
				{
					// Add the application to the favorites and move to the next line
					favorites.add(application) ;
					break ;
				}
		}

		// Check if favorites need to be migrated
		if(favorites.size() > 0)
		{
			// Try to migrate them to the new format
			if(!favorites_file.remove()) return ;
			for(Application application : favorites)
				if(!favorites_file.writeLine(application.getName())) return ;
			ShowDialog.alert(context, context.getString(R.string.error_file_format_changed)) ;
		}
	}


	/**
	 * Check if an icon pack is selected and load it if it does.
	 * @param context To display alerts if needed
	 * @return An icon pack loaded or <code>null</code> if none is selected
	 */
	private IconPack selectIconPack(Context context)
	{
		// Check if an icon pack is selected
		String pack_name = settings.getString("icon_pack", "none") ;
		if((pack_name == null) || pack_name.equals("none")) return null ;

		// Try to load the icon pack resources
		IconPack iconPack = new IconPack(context, pack_name) ;
		if(!iconPack.loadResources())
			{
				// Display an error message and set the icon pack to none
				ShowDialog.alert(context, context.getString(R.string.error_application_not_found, pack_name)) ;
				SharedPreferences.Editor editor = settings.edit() ;
				editor.putString("icon_pack", "none").apply() ;
				return null ;
			}

		// Try to find the resource ID of the appfilter.xml file in the icon pack
		if(!iconPack.findAppfilterID())
			{
				// Display an error message and do not use the icon pack
				ShowDialog.alert(context, context.getString(R.string.error_appfilter_not_found, pack_name)) ;
				return null ;
			}

		// Return the icon pack loaded
		return iconPack ;
	}


	/**
	 * Return the complete list of applications.
	 * @return For display in the Drawer activity
	 */
	ArrayList<Application> getApplications()
	{
		return applications ;
	}


	/**
	 * Return the list of favorites applications.
	 * @return For display in the favorites panel
	 */
	ArrayList<Application> getFavorites()
	{
		return favorites ;
	}


	/**
	 * Return the timestamp of the last time the applications list was updated.
	 * @return Date and time in text format
	 */
	String getLastUpdate()
	{
		return last_update ;
	}


	/**
	 * Return the number of favorites applications in the list.
	 * @return Number of favorites applications
	 */
	int getFavoritesCount()
	{
		return favorites.size() ;
	}
}
