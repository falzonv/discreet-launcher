package com.vincent_falzon.discreetlauncher.core ;

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
import androidx.core.content.res.ResourcesCompat ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.ShowDialog ;
import com.vincent_falzon.discreetlauncher.storage.* ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.List ;

/**
 * Provide and manage applications lists.
 */
public class ApplicationsList
{
	// Attributes
	private final ArrayList<Application> drawer ;
	private final ArrayList<Application> hidden ;
	private final ArrayList<Application> favorites ;


	/**
	 * Constructor to create the applications list.
	 */
	public ApplicationsList()
	{
		drawer = new ArrayList<>() ;
		hidden = new ArrayList<>() ;
		favorites = new ArrayList<>() ;
	}


	/**
	 * Update both the complete applications list and the favorite applications list.
	 * @param context To get the package manager, load icon pack and display a toast
	 */
	public void update(Context context)
	{
		// Initializations
		PackageManager apkManager = context.getPackageManager() ;
		drawer.clear() ;

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
			drawer.add(application) ;
		}

		// Add the shortcuts to the list as applications
		loadShortcuts(context) ;

		// Hide application based on the internal file
		manageHiddenApplications() ;

		// Prepare folders according to files
		prepareFolders(context) ;

		// Sort the applications list in alphabetic order based on display name
		Collections.sort(drawer, new Comparator<Application>()
		{
			@Override
			public int compare(Application application1, Application application2)
			{
				return application1.getDisplayName().compareToIgnoreCase(application2.getDisplayName()) ;
			}
		}) ;

		// Update the favorites applications list
		updateFavorites() ;
	}


	/**
	 * Update the favorites applications list based on the favorites file and the complete list.
	 */
	public void updateFavorites()
	{
		// Initializations
		favorites.clear() ;
		InternalFileTXT file = new InternalFileTXT(Constants.FILE_FAVORITES) ;
		if(!file.exists()) return ;

		// Retrieve and browse the internal names of all favorites applications
		for(String name : file.readAllLines())
		{
			// Search the internal name in the applications list
			for(Application application : getApplications(true))
				if(application.getName().equals(name))
					{
						// Add the application to the favorites and move to the next line
						if(!favorites.contains(application)) favorites.add(application) ;
						break ;
					}
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
		String pack_name = settings.getString(Constants.ICON_PACK, Constants.NONE) ;
		if((pack_name == null) || pack_name.equals(Constants.NONE)) return null ;

		// Try to load the icon pack resources
		IconPack iconPack = new IconPack(pack_name) ;
		if(!iconPack.loadResources(context))
			{
				// Display an error message and set the icon pack to none
				ShowDialog.toastLong(context, context.getString(R.string.error_application_not_found, pack_name)) ;
				SharedPreferences.Editor editor = settings.edit() ;
				editor.putString(Constants.ICON_PACK, Constants.NONE).apply() ;
				return null ;
			}

		// Try to find the resource ID of the appfilter.xml file in the icon pack
		if(!iconPack.findAppfilterID())
			{
				// Display an error message and do not use the icon pack
				ShowDialog.toastLong(context, context.getString(R.string.error_appfilter_not_found, pack_name)) ;
				return null ;
			}

		// Return the icon pack loaded
		return iconPack ;
	}


	/**
	 * Prepare folders according to the folders files.
	 * @param context To get the icon
	 */
	private void prepareFolders(Context context)
	{
		String[] folders_files = InternalFile.searchFilesStartingWith(context, Constants.FILE_FOLDER_PREFIX) ;
		if(folders_files == null) return ;

		// Use the notification icon as folder icon
		Drawable icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_icon, null) ;
		int icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;
		if(icon != null) icon.setBounds(0, 0, icon_size, icon_size) ;

		// Browse the name of all folders files
		for(String filename : folders_files)
		{
			// Load the file, or skip it if it does not exist
			InternalFileTXT file = new InternalFileTXT(filename) ;
			if(!file.exists()) continue ;

			// Retrieve the name of the folder and create it
			String display_name = filename.replace(Constants.FILE_FOLDER_PREFIX, "").replace(".txt", "") ;
			Folder folder = new Folder(display_name, icon) ;

			// Browse the lines of the file to get the list of applications to put in the folder
			for(String name : file.readAllLines())
			{
				// Search the internal name in the applications list
				for(Application application : drawer)
					if(application.getName().equals(name))
						{
							// Move the application in the folder
							folder.addToFolder(application) ;
							drawer.remove(application) ;
							break ;
						}
			}

			// Sort the folder and add it to the general applications list
			folder.sortFolder() ;
			drawer.add(folder) ;
		}
	}


	/**
	 * Hide applications based on the internal file (to apply before folders).
	 */
	void manageHiddenApplications()
	{
		// Check if hidden applications have been defined
		hidden.clear() ;
		InternalFileTXT file = new InternalFileTXT(Constants.FILE_HIDDEN) ;
		if(!file.exists()) return ;

		// Browse the list of applications that should be hidden
		for(String name : file.readAllLines())
		{
			// Search the internal name in the applications list
			for(Application application : drawer)
				if(application.getName().equals(name))
					{
						// Move the application in the hidden list
						hidden.add(application) ;
						drawer.remove(application) ;
						break ;
					}
		}
	}


	/**
	 * Add shortcuts to the applications list based on the shortcuts files.
	 * @param context To get the icons
	 */
	void loadShortcuts(Context context)
	{
		// Use the notification icon as default shortcut icon
		Drawable default_icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_icon, null) ;
		int icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;
		if(default_icon != null) default_icon.setBounds(0, 0, icon_size, icon_size) ;

		// If their file exists, browse the shortcuts
		InternalFileTXT file = new InternalFileTXT(Constants.FILE_SHORTCUTS) ;
		if(file.exists())
			{
				String[] shortcut ;
				for(String shortcut_line : file.readAllLines())
				{
					// Extrat the shortcut details
					shortcut = shortcut_line.split(Constants.SHORTCUT_SEPARATOR) ;
					if(shortcut.length != 4) continue ;

					// Try to retrieve the shortcut icon or use the default icon
					InternalFilePNG icon_file = new InternalFilePNG(Constants.FILE_ICON_SHORTCUT_PREFIX + shortcut[0] + ".png") ;
					Drawable icon = icon_file.convertBitmapToDrawable(context, icon_file.readFromFile()) ;
					if(icon != null) icon.setBounds(0, 0, icon_size, icon_size) ;
						else icon = default_icon ;

					// Add the shortcut to the list of applications
					drawer.add(new Application(shortcut[0],
							shortcut[1] + Constants.SHORTCUT_SEPARATOR + shortcut[2] + Constants.SHORTCUT_SEPARATOR + shortcut[3],
							Constants.APK_SHORTCUT, icon)) ;
				}
			}

		// If their file exists, browse the legacy shortcuts
		InternalFileTXT legacyFile = new InternalFileTXT(Constants.FILE_SHORTCUTS_LEGACY) ;
		if(legacyFile.exists())
			{
				String[] legacy_shortcut ;
				for(String legacy_shortcut_line : legacyFile.readAllLines())
				{
					// Extrat the shortcut details
					legacy_shortcut = legacy_shortcut_line.split(Constants.SHORTCUT_SEPARATOR) ;
					if(legacy_shortcut.length != 2) continue ;

					// Try to retrieve the shortcut icon or use the default icon
					InternalFilePNG icon_file = new InternalFilePNG(Constants.FILE_ICON_SHORTCUT_PREFIX + legacy_shortcut[0] + ".png") ;
					Drawable icon = icon_file.convertBitmapToDrawable(context, icon_file.readFromFile()) ;
					if(icon != null) icon.setBounds(0, 0, icon_size, icon_size) ;
						else icon = default_icon ;

					// Add the shortcut to the list of applications
					drawer.add(new Application(legacy_shortcut[0], legacy_shortcut[1], Constants.APK_SHORTCUT_LEGACY, icon)) ;
				}
			}
	}


	/**
	 * For display in selection dialog.
	 * @return List of folders
	 */
	public ArrayList<Folder> getFolders()
	{
		ArrayList<Folder> result = new ArrayList<>() ;
		for(Application application : drawer)
			if(application instanceof Folder) result.add((Folder)application) ;
		return result ;
	}


	/**
	 * For display in selection dialog.
	 * @return List of applications not in folders
	 */
	public ArrayList<Application> getApplicationsNotInFolders()
	{
		ArrayList<Application> result = new ArrayList<>() ;
		for(Application application : drawer)
			if(!(application instanceof Folder)) result.add(application) ;
		return result ;
	}


	/**
	 * For display in the settings and the favorites selection dialog.
	 * @param with_folders To include or not the folders in the result
	 * @return List of all applications (except hidden) whether or not they are in folders
	 */
	public ArrayList<Application> getApplications(boolean with_folders)
	{
		// Aggregate all applications in one list
		ArrayList<Application> allApplications = new ArrayList<>() ;
		for(Application application : drawer)
		{
			// Add all applications whether or not they are in folders
			if(application instanceof Folder)
				{
					allApplications.addAll(((Folder)application).getApplications()) ;
					if(with_folders) allApplications.add(application) ;
				}
				else allApplications.add(application) ;
		}

		// Sort the list in alphabetic order based on display name
		Collections.sort(allApplications, new Comparator<Application>()
		{
			@Override
			public int compare(Application application1, Application application2)
			{
				return application1.getDisplayName().compareToIgnoreCase(application2.getDisplayName()) ;
			}
		}) ;

		// Return the result
		return allApplications ;
	}


	/**
	 * For display in the favorites panel.
	 * @return List of favorites applications
	 */
	public ArrayList<Application> getFavorites()
	{
		return favorites ;
	}


	/**
	 * For display in the drawer.
	 * @return List of folders and applications not in folders
	 */
	public ArrayList<Application> getDrawer()
	{
		return drawer;
	}


	/**
	 * For display in the settings.
	 * @return List of hidden applications
	 */
	public ArrayList<Application> getHidden()
	{
		return hidden ;
	}
}
