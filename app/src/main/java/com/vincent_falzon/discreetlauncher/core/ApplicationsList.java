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
import android.content.pm.LauncherActivityInfo ;
import android.content.pm.LauncherApps ;
import android.graphics.drawable.Drawable ;
import android.os.UserHandle ;
import android.os.UserManager ;
import androidx.core.content.ContextCompat ;
import androidx.core.content.res.ResourcesCompat ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.settings.ColorPickerDialog ;
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
		String apk_discreet_launcher = context.getPackageName() ;
		IconPack iconPack = new IconPack(context, Constants.ICON_PACK) ;
		drawer.clear() ;

		// Define the icons size in pixels
		int icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;
		Drawable icon ;

		// Retrieve the list of user profiles
		UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE) ;
		List<UserHandle> userProfiles = userManager.getUserProfiles() ;

		// Browse all user profiles
		LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE) ;
		for(UserHandle profile : userProfiles)
		{
			// Check if a specific user handle must be stored (for work profile apps)
			UserHandle userHandle = (userManager.getSerialNumberForUser(profile) == 0) ? null : profile ;

			// Browse all the activities of the current profile
			for(LauncherActivityInfo activity : launcherApps.getActivityList(null, profile))
			{
				// Retrieve information about the application
				String display_name = activity.getLabel().toString() ;
				String name = activity.getName() ;
				String apk = activity.getApplicationInfo().packageName ;

				// Try to find the icon in the pack, use the default icon if not found
				icon = iconPack.searchIcon(apk, name) ;
				if(icon == null) icon = activity.getIcon(0) ;
				icon.setBounds(0, 0, icon_size, icon_size) ;

				// Check if the application is the launcher to provide menu access using its icon
				Application application ;
				if(apk.equals(apk_discreet_launcher))
					application = new Menu(display_name, name, apk, icon) ;
				else application = new Application(display_name, name, apk, icon, userHandle) ;

				// Add the application to the list
				drawer.add(application) ;
			}
		}

		// Add the shortcuts to the list as applications
		loadShortcuts(context) ;

		// Rename applications if needed
		renameApplications() ;

		// Sort the applications list in alphabetic order based on display name
		Collections.sort(drawer, new Comparator<Application>()
		{
			@Override
			public int compare(Application application1, Application application2)
			{
				return application1.getDisplayName().compareToIgnoreCase(application2.getDisplayName()) ;
			}
		}) ;

		// Add the search icon on top of the list
		Drawable searchIcon = ContextCompat.getDrawable(context, R.drawable.icon_search) ;
		if(searchIcon != null) searchIcon.setBounds(0, 0, icon_size, icon_size) ;
		drawer.add(0, new Search(context.getString(R.string.search_app_name), searchIcon)) ;

		// Hide application based on the internal file
		manageHiddenApplications() ;

		// Prepare folders according to files
		prepareFolders(context) ;

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
		ArrayList<String> favorites_file = new InternalFileTXT(Constants.FILE_FAVORITES).readAllLines() ;
		if(favorites_file == null) return ;

		// Convert the favorites from the name format to ComponentInfo format if needed
		favorites_file = convertComponentInfo(Constants.FILE_FAVORITES, favorites_file) ;

		// Browse the internal file
		for(String line : favorites_file)
		{
			// Search the ComponentInfo in the applications list
			for(Application application : getApplications(true))
				if(application.getComponentInfo().equals(line))
					{
						// Add the application to the favorites and move to the next line
						if(!favorites.contains(application)) favorites.add(application) ;
						break ;
					}
		}
	}


	/**
	 * Prepare folders according to the folders files.
	 * @param context To get the icon
	 */
	private void prepareFolders(Context context)
	{
		// Initializations
		String[] folders_files = InternalFile.searchFilesStartingWith(context, Constants.FILE_FOLDER_PREFIX) ;
		if(folders_files == null) return ;
		int icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;

		// Retrieve fhe folders colors mapping file if it exists
		ArrayList<String> folders_colors_file = new InternalFileTXT(Constants.FILE_FOLDERS_COLORS).readAllLines() ;

		// Browse the name of all folders files
		ArrayList<Folder> folders = new ArrayList<>() ;
		for(String filename : folders_files)
		{
			// Load the file, or skip it if it does not exist
			ArrayList<String> folder_file = new InternalFileTXT(filename).readAllLines() ;
			if(folder_file == null) continue ;

			// Convert the folder from the name format to ComponentInfo format if needed
			folder_file = convertComponentInfo(filename, folder_file) ;

			// Check if a color has beed defined for this folder or use the default white
			int color = context.getResources().getColor(R.color.for_icon_added_in_drawer) ;
			if(folders_colors_file != null)
				for(String mapping : folders_colors_file)
					if(mapping.startsWith(filename))
						{
							color = ColorPickerDialog.convertHexadecimalColorToInt(mapping.replace(filename + Constants.SEPARATOR, "")) ;
							break ;
						}

			// Retrieve the name of the folder and create it
			String folder_name = filename.replace(Constants.FILE_FOLDER_PREFIX, "").replace(".txt", "") ;
			Folder folder = new Folder(folder_name, null, color) ;

			// Browse the lines of the file to get the list of applications to put in the folder
			for(String component_info : folder_file)
			{
				// Search the internal name in the applications list
				for(Application application : drawer)
					if(application.getComponentInfo().equals(component_info))
						{
							// Move the application in the folder
							folder.addToFolder(application) ;
							drawer.remove(application) ;
							break ;
						}
			}

			// Create the folder icon with the number of applications inside and the selected color
			Drawable icon = new FolderIcon(context, folder.getApplications().size(), folder.getColor()) ;
			icon.setBounds(0, 0, icon_size, icon_size) ;
			folder.setIcon(icon) ;

			// Sort the folder content and add it to the list of folders
			folder.sortFolder() ;
			folders.add(folder) ;
		}

		// Sort the folders and add them at the beginning of the list
		Collections.sort(folders, new Comparator<Folder>()
		{
			@Override
			public int compare(Folder folder1, Folder folder2)
			{
				return folder1.getDisplayName().compareToIgnoreCase(folder2.getDisplayName()) ;
			}
		}) ;
		drawer.addAll(0, folders) ;
	}


	/**
	 * Hide applications based on the internal file (to apply before folders).
	 */
	private void manageHiddenApplications()
	{
		// Check if hidden applications have been defined
		hidden.clear() ;
		ArrayList<String> hidden_file = new InternalFileTXT(Constants.FILE_HIDDEN).readAllLines() ;
		if(hidden_file == null) return ;

		// Convert the hidden from the name format to ComponentInfo format if needed
		hidden_file = convertComponentInfo(Constants.FILE_HIDDEN, hidden_file) ;

		// Browse the list of applications that should be hidden
		for(String line : hidden_file)
		{
			// Never hide the Discreet Launcher icon (as it can be the only access to the menu)
			if(line.equals("{com.vincent_falzon.discreetlauncher/com.vincent_falzon.discreetlauncher.ActivityMain}")) continue ;

			// Search the internal name in the applications list
			for(Application application : drawer)
				if(application.getComponentInfo().equals(line))
					{
						// Remove the application icon to lower memory footprint
						application.setIcon(null) ;

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
	private void loadShortcuts(Context context)
	{
		// Use the folder icon as default shortcut icon
		Drawable default_icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icon_folder, null) ;
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
					drawer.add(new Shortcut(shortcut[0],
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
					drawer.add(new Shortcut(legacy_shortcut[0], legacy_shortcut[1], Constants.APK_SHORTCUT_LEGACY, icon)) ;
				}
			}
	}


	/**
	 * Rename applications if needed.
	 */
	private void renameApplications()
	{
		// Check if apps have been renamed
		ArrayList<String> rename_apps_file = new InternalFileTXT(Constants.FILE_RENAME_APPS).readAllLines() ;
		if(rename_apps_file == null) return ;

		// Browse the internal file
		for(String line : rename_apps_file)
		{
			// Search the ComponentInfo in the applications list
			for(Application application : drawer)
				if(line.startsWith(application.getComponentInfo()))
				{
					// Rename the application and move to the next line
					String new_name = line.replace(application.getComponentInfo() + Constants.SEPARATOR, "") ;
					application.setDisplayName(new_name) ;
					break ;
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
		{
			// Skip the search application
			if(application instanceof Search) continue ;

			// Add all user applications outside folders
			if(!(application instanceof Folder)) result.add(application) ;
		}
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
		ArrayList<Folder> folders = new ArrayList<>() ;
		for(Application application : drawer)
		{
			// Add all applications whether or not they are in folders
			if(application instanceof Folder)
				{
					allApplications.addAll(((Folder)application).getApplications()) ;
					folders.add((Folder)application) ;
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

		// Retrive the index of the Search
		int search_index = 0 ;
		for(int i = 0 ; i < allApplications.size() ; i++)
		{
			if(allApplications.get(i) instanceof Search)
				{
					search_index = i ;
					break ;
				}
		}

		// Move the Search at the beginning of the list if needed
		if(search_index > 0)
			{
				Application search = allApplications.get(search_index) ;
				allApplications.remove(search_index) ;
				allApplications.add(0, search) ;
			}

		// If requested, add folders at the beginning of the list
		if(with_folders)
			{
				// Sort the folders and add them at the beginning of the list
				Collections.sort(folders, new Comparator<Folder>()
				{
					@Override
					public int compare(Folder folder1, Folder folder2)
					{
						return folder1.getDisplayName().compareToIgnoreCase(folder2.getDisplayName()) ;
					}
				}) ;
				allApplications.addAll(0, folders) ;
			}

		// Return the result
		return allApplications ;
	}


	/**
	 * For display in the drawer.
	 * @return List of what should appear in the applications drawer
	 */
	public ArrayList<Application> getDrawer()
	{
		return drawer ;
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
	 * For display in the settings.
	 * @return List of hidden applications
	 */
	public ArrayList<Application> getHidden()
	{
		return hidden ;
	}


	/**
	 * Convert an internal file from name format to ComponentInfo format if needed.
	 * (Added in v4.1.0 middle of 06/2021, to remove later)
	 * @param filename Name of the internal file
	 * @param content Current file content
	 * @return Converted file content
	 */
	private ArrayList<String> convertComponentInfo(String filename, ArrayList<String> content)
	{
		// Browse the internal file
		ArrayList<String> new_content = new ArrayList<>() ;
		for(String line : content)
		{
			// Do not modify the line if it is already converted
			if(line.startsWith("{"))
				{
					new_content.add(line) ;
					continue ;
				}

			// Search the internal name in the applications list
			for(Application application : getApplications(true))
				if(application.getName().equals(line))
					{
						// Retrieve the ComponentInfo of the application
						new_content.add(application.getComponentInfo()) ;
						break ;
					}
		}

		// If any, write the new content in the file
		if(new_content.size() > 0)
			{
				InternalFileTXT file = new InternalFileTXT(filename) ;
				if(file.remove()) for(String line : new_content) file.writeLine(line) ;
			}

		// Return the converted file
		return new_content ;
	}
}
