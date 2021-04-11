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
import android.graphics.Bitmap ;
import android.graphics.drawable.Drawable ;
import androidx.core.content.res.ResourcesCompat ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.storage.* ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.Date ;
import java.util.List ;
import java.util.Set ;

/**
 * Contain applications lists (complete and favorites) and the last update timestamp.
 */
class ApplicationsList
{
	// Constants
	public static final String FAVORITES_FILE = "favorites.txt" ;
	public static final String SHORTCUTS_FILE = "shortcuts.txt" ;
	public static final String SHORTCUTS_LEGACY_FILE = "shortcuts_legacy.txt" ;
	public static final String SHORTCUT_ICON_PREFIX = "icon_shortcut_" ;

	// Attributes
	private final ArrayList<Application> applications ;
	private final ArrayList<Application> favorites ;
	private final ArrayList<Application> hidden ;
	private String last_update ;
	private boolean update_in_progress ;


	/**
	 * Constructor to create the applications list.
	 */
	ApplicationsList()
	{
		applications = new ArrayList<>() ;
		favorites = new ArrayList<>() ;
		hidden = new ArrayList<>() ;
		last_update = "" ;
		update_in_progress = false ;
	}


	/**
	 * Update both the complete applications list and the favorite applications list.
	 * @param context To get the package manager, load icon pack and display a toast
	 */
	void update(Context context)
	{
		// Check if an update is already in progress
		if(update_in_progress) return ;
		update_in_progress = true ;

		// Initializations
		PackageManager apkManager = context.getPackageManager() ;
		applications.clear() ;

		// Retrieve the list of applications that can be launched by the user
		Intent intent = new Intent(Intent.ACTION_MAIN) ;
		intent.addCategory(Intent.CATEGORY_LAUNCHER) ;
		List<ResolveInfo> apkManagerList = apkManager.queryIntentActivities(intent, 0) ;

		// Define the icons size in pixels
		int icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;

		// If an icon pack is selected, load it
		IconPack iconPack = loadIconPack(context) ;

		// Browse the APK manager list and store the data of each application in the main list
		Drawable icon ;
		for(ResolveInfo entry : apkManagerList)
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

		// Add the shortcuts to the list as applications
		loadShortcuts(context) ;

		// Sort the applications list in alphabetic order based on display name
		Collections.sort(applications, new Comparator<Application>()
		{
			@Override
			public int compare(Application application1, Application application2)
			{
				return application1.getDisplayName().compareToIgnoreCase(application2.getDisplayName()) ;
			}
		}) ;

		// Hide application based on what is defined in the settings
		manageHiddenApplications(context) ;

		// Update the favorites applications list
		updateFavorites(context) ;

		// Save the last update timestamp and inform the user that the list has been refreshed
		last_update = SimpleDateFormat.getDateTimeInstance().format(new Date()) ;
		ShowDialog.toast(context, R.string.info_applications_list_refreshed) ;
		update_in_progress = false ;
	}


	/**
	 * Update the favorites applications list based on the favorites file and the complete list.
	 * @param context To get the file path
	 */
	void updateFavorites(Context context)
	{
		// Initializations
		favorites.clear() ;
		InternalFileTXT file = new InternalFileTXT(context, FAVORITES_FILE) ;
		if(!file.exists()) return ;

		// Retrieve and browse the internal names of all favorites applications
		for(String name : file.readAllLines())
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
		if(favorites.size() == 0) convertOldFavoritesFileFormat(context, file) ;
	}


	/**
	 * To be removed later, file format conversion due to change in v1.2.0.
	 * This method is called during the favorites update in case the user updated the launcher from
	 * a version before v1.2.0. It updates the favorites file to new format with internal names.
	 * @param context To display an alert dialog
	 */
	private void convertOldFavoritesFileFormat(Context context, InternalFileTXT file)
	{
		// Retrieve and browse the package names of all favorites applications
		for(String apk : file.readAllLines())
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
			if(file.remove()) return ;
			for(Application application : favorites)
				if(!file.writeLine(application.getName())) return ;
			ShowDialog.alert(context, context.getString(R.string.error_file_format_changed)) ;
		}
	}


	/**
	 * Check if an icon pack is selected and load it if it does.
	 * @param context To get the settings and display alerts
	 * @return An icon pack loaded or <code>null</code> if none is selected
	 */
	private IconPack loadIconPack(Context context)
	{
		// Check if an icon pack is selected
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()) ;
		String pack_name = settings.getString(ActivitySettings.ICON_PACK, ActivitySettings.NONE) ;
		if((pack_name == null) || pack_name.equals(ActivitySettings.NONE)) return null ;

		// Try to load the icon pack resources
		IconPack iconPack = new IconPack(context, pack_name) ;
		if(!iconPack.loadResources())
			{
				// Display an error message and set the icon pack to none
				ShowDialog.alert(context, context.getString(R.string.error_application_not_found, pack_name)) ;
				SharedPreferences.Editor editor = settings.edit() ;
				editor.putString(ActivitySettings.ICON_PACK, ActivitySettings.NONE).apply() ;
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
	 * Hide applications based on what is defined in the settings.
	 * @param context To get the settings
	 */
	void manageHiddenApplications(Context context)
	{
		// Check if hidden applications have been defined
		hidden.clear() ;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()) ;
		Set<String> hiddenApplications = settings.getStringSet(ActivitySettings.HIDDEN_APPLICATIONS, null) ;
		if(hiddenApplications == null) return ;

		// Browse the list of applications that should be hidden
		String[] app_details ;
		for(String hidden_application : hiddenApplications)
		{
			// Retrieve the application internal name
			app_details = hidden_application.split(Application.NOTIFICATION_SEPARATOR) ;
			if(app_details.length < 2) continue ;

			// Search the internal name in the applications list
			for(Application application : applications)
				if(application.getName().equals(app_details[1]))
					{
						// Move the application in the hidden list
						hidden.add(application) ;
						applications.remove(application) ;
						break ;
					}
		}
	}


	/**
	 * Add shortcuts to the applications list based on the shortcuts files.
	 * @param context To get the file path and icons
	 */
	void loadShortcuts(Context context)
	{
		// Use the notification icon as default shortcut icon
		Drawable default_icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_icon, null) ;
		int icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;
		if(default_icon != null) default_icon.setBounds(0, 0, icon_size, icon_size) ;

		// If their file exists, browse the shortcuts
		InternalFileTXT file = new InternalFileTXT(context, SHORTCUTS_FILE) ;
		if(file.exists())
			{
				String[] shortcut ;
				for(String shortcut_line : file.readAllLines())
				{
					// Extrat the shortcut details
					shortcut = shortcut_line.split(Application.SHORTCUT_SEPARATOR) ;
					if(shortcut.length != 4) continue ;

					// Try to retrieve the shortcut icon or use the default icon
					InternalFilePNG icon_file = new InternalFilePNG(context, SHORTCUT_ICON_PREFIX + shortcut[0] + ".png") ;
					Drawable icon = icon_file.convertBitmapToDrawable(context, icon_file.readFromFile()) ;
					if(icon != null) icon.setBounds(0, 0, icon_size, icon_size) ;
						else icon = default_icon ;

					// Add the shortcut to the list of applications
					applications.add(new Application(shortcut[0],
							shortcut[1] + Application.SHORTCUT_SEPARATOR + shortcut[2] + Application.SHORTCUT_SEPARATOR + shortcut[3],
							Application.APK_SHORTCUT, icon)) ;
				}
			}

		// If their file exists, browse the legacy shortcuts
		InternalFileTXT legacyFile = new InternalFileTXT(context, SHORTCUTS_LEGACY_FILE) ;
		if(legacyFile.exists())
			{
				String[] legacy_shortcut ;
				for(String legacy_shortcut_line : legacyFile.readAllLines())
				{
					// Extrat the shortcut details
					legacy_shortcut = legacy_shortcut_line.split(Application.SHORTCUT_SEPARATOR) ;
					if(legacy_shortcut.length != 2) continue ;

					// Try to retrieve the shortcut icon or use the default icon
					InternalFilePNG icon_file = new InternalFilePNG(context, SHORTCUT_ICON_PREFIX + legacy_shortcut[0] + ".png") ;
					Drawable icon = icon_file.convertBitmapToDrawable(context, icon_file.readFromFile()) ;
					if(icon != null) icon.setBounds(0, 0, icon_size, icon_size) ;
						else icon = default_icon ;

					// Add the shortcut to the list of applications
					applications.add(new Application(legacy_shortcut[0], legacy_shortcut[1], Application.APK_SHORTCUT_LEGACY, icon)) ;
				}
			}
	}


	/**
	 * Method called when a request to add a shortcut has been received.
	 * @param context Provided by the receiver
	 * @param display_name Displayed to the user
	 * @param icon Displayed to the user
	 * @param shortcut Line to add to the shortcuts file
	 * @param legacy <code>true</code> if before Oreo, <code>false</code> otherwise
	 */
	void addShortcut(Context context, String display_name, String shortcut, Bitmap icon, boolean legacy)
	{
		// Check if the shortcut already exists in the file
		InternalFileTXT file = new InternalFileTXT(context, legacy ? SHORTCUTS_LEGACY_FILE : SHORTCUTS_FILE) ;
		if(file.exists())
			{
				// Browse all the saved shortcuts
				String[] saved_shortcut ;
				for(String shortcut_line : file.readAllLines())
				{
					// Do not continue if the shortcut already exists
					saved_shortcut = shortcut_line.split(Application.SHORTCUT_SEPARATOR) ;
					if(display_name.equals(saved_shortcut[0])) return ;
				}
			}

		// If it was not existing, add the shortcut to the file
		if(!file.writeLine(shortcut))
			{
				ShowDialog.alert(context, context.getString(R.string.error_shortcut, display_name)) ;
				return ;
			}

		// Save the shortcut icon to a file
		InternalFilePNG icon_file = new InternalFilePNG(context, SHORTCUT_ICON_PREFIX + display_name + ".png") ;
		if(!icon_file.writeToFile(icon)) ShowDialog.alert(context, context.getString(R.string.error_shortcut, display_name)) ;
	}


	/**
	 * Remove an entry from the shortcuts file and update the applications list
	 * @param context To get the file path
	 * @param toRemove The shortcut to remove
	 */
	void removeShortcut(Context context, Application toRemove)
	{
		// Save the current shortcuts list and remove the file
		InternalFileTXT file = new InternalFileTXT(context, toRemove.getApk().equals(Application.APK_SHORTCUT_LEGACY) ? SHORTCUTS_LEGACY_FILE : SHORTCUTS_FILE) ;
		ArrayList<String> currentShortcuts = file.readAllLines() ;
		if(!file.remove())
			{
				ShowDialog.toastLong(context, context.getString(R.string.error_remove_file, file.getName())) ;
				return ;
			}

		// Write the new shortcuts list in the file
		String display_name = toRemove.getDisplayName() ;
		String[] shortcut ;
		for(String shortcut_line : currentShortcuts)
		{
			// Extract the display name from the line and check if this is the shortcut to remove
			shortcut = shortcut_line.split(Application.SHORTCUT_SEPARATOR) ;
			if(shortcut[0].equals(display_name)) continue ;

			// Add all the other shortcuts to the list again
			if(!file.writeLine(shortcut_line))
				{
					ShowDialog.toastLong(context, context.getString(R.string.error_shortcut, shortcut[0])) ;
					return ;
				}
		}

		// Remove the shortcut icon
		InternalFilePNG icon = new InternalFilePNG(context, SHORTCUT_ICON_PREFIX + display_name + ".png") ;
		icon.remove() ;

		// Update the applications list
		update(context) ;
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
	 * Return the list of hidden applications.
	 * @return For display in the settings
	 */
	ArrayList<Application> getHidden()
	{
		return hidden ;
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
