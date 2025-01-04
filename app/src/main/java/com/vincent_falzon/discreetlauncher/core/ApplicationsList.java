package com.vincent_falzon.discreetlauncher.core ;

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
import android.content.Context ;
import android.content.SharedPreferences ;
import android.content.pm.LauncherActivityInfo ;
import android.content.pm.LauncherApps ;
import android.content.pm.PackageManager ;
import android.content.res.Resources ;
import android.graphics.Bitmap ;
import android.graphics.Canvas ;
import android.graphics.ColorMatrix ;
import android.graphics.ColorMatrixColorFilter ;
import android.graphics.Paint ;
import android.graphics.PorterDuff ;
import android.graphics.drawable.BitmapDrawable ;
import android.graphics.drawable.Drawable ;
import android.os.UserHandle ;
import android.os.UserManager ;
import androidx.appcompat.content.res.AppCompatResources ;
import androidx.core.content.ContextCompat ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.Utils ;
import com.vincent_falzon.discreetlauncher.settings.ColorPickerDialog ;
import com.vincent_falzon.discreetlauncher.storage.* ;
import java.text.Collator ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.List ;

/**
 * Provide and manage lists of applications.
 */
public class ApplicationsList
{
	// Constants
	private static final String TAG = "ApplicationsList" ;

	// Attributes
	private final ArrayList<Application> drawer ;
	private final ArrayList<Application> hidden ;
	private final ArrayList<Application> favorites ;
	private final Paint grayscalePaint ;
	private int icon_size ;


	/**
	 * Constructor.
	 */
	public ApplicationsList()
	{
		// Create the lists of applications
		drawer = new ArrayList<>() ;
		hidden = new ArrayList<>() ;
		favorites = new ArrayList<>() ;

		// Initialize the grayscale Paint used by the icon color filter
		ColorMatrix colorMatrix = new ColorMatrix() ;
		colorMatrix.setSaturation(0) ;
		grayscalePaint = new Paint() ;
		grayscalePaint.setColorFilter(new ColorMatrixColorFilter(colorMatrix)) ;
	}


	/**
	 * Update both the complete application list and the favorite applications list.
	 */
	public void update(Context context)
	{
		// Initializations
		Utils.logInfo(TAG, "updating the list of apps") ;
		String apk_discreet_launcher = context.getPackageName() ;
		IconPack iconPack1 = new IconPack(context, Constants.ICON_PACK) ;
		IconPack iconPack2 = new IconPack(context, Constants.ICON_PACK_SECONDARY) ;
		drawer.clear() ;

		// Retrieve the icon size in pixels
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context) ;
		icon_size = Utils.getIconSize(context, settings) ;

		// Check if a color tint must be applied to icons and retrieve it if needed
		String color_tint_setting = settings.getString(Constants.ICON_COLOR_FILTER, Constants.COLOR_TRANSPARENT) ;
		int color_tint ;
		if(color_tint_setting.equals(Constants.COLOR_TRANSPARENT)) color_tint = 0 ;
			else color_tint = ColorPickerDialog.convertHexadecimalColorToInt(color_tint_setting) ;

		// Retrieve the list of user profiles
		UserManager userManager = (UserManager)context.getSystemService(Context.USER_SERVICE) ;
		List<UserHandle> userProfiles = userManager.getUserProfiles() ;
		PackageManager apkManager = context.getPackageManager() ;

		// Browse all user profiles
		Drawable icon ;
		Resources resources = context.getResources() ;
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

				// Try to find the icon in the packs, use the default icon if not found
				icon = searchInMultipleIconPacks(iconPack1, iconPack2, apk, name) ;
				if(icon == null)
					{
						// Check if a color tint must be applied on the default icon
						if(color_tint == 0) icon = activity.getIcon(0) ;
							else icon = applyColorTint(resources, activity.getIcon(0), color_tint) ;
					}

				// Add a badge to the chosen icon if the app is in a work profile
				if(userHandle != null) icon = apkManager.getUserBadgedIcon(icon, profile) ;

				// Resize the icon to the user-defined size
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

		// Sort the applications list in alphabetical order based on display name
		Collator collator = Collator.getInstance() ;
		collator.setStrength(Collator.PRIMARY) ;
		Collections.sort(drawer, (application1, application2) -> collator.compare(application1.getDisplayName(), application2.getDisplayName())) ;

		// Check if the interface is reversed
		boolean reversed = settings.getBoolean(Constants.REVERSE_INTERFACE, false) ;

		// Add the search icon at the beginning or end of the list (based on layout)
		Drawable searchIcon = searchInMultipleIconPacks(iconPack1, iconPack2, Constants.APK_SEARCH, Constants.APK_SEARCH) ;
		if(searchIcon == null) searchIcon = AppCompatResources.getDrawable(context, R.drawable.icon_search) ;
		if(searchIcon != null) searchIcon.setBounds(0, 0, icon_size, icon_size) ;
		if(reversed) drawer.add(new Search(context.getString(R.string.search_app_name), searchIcon)) ;
			else drawer.add(0, new Search(context.getString(R.string.search_app_name), searchIcon)) ;

		// Hide application based on the internal file
		manageHiddenApplications() ;

		// Prepare folders according to files
		prepareFolders(context, reversed, iconPack1, iconPack2) ;

		// Update the favorites applications list
		updateFavorites() ;
	}


	/**
	 * Apply the given color tint over the given icon and return the result.
	 */
	private Drawable applyColorTint(Resources resources, Drawable originalIcon, int color_tint)
	{
		// Retrieve the original icon dimensions
		int height = originalIcon.getMinimumHeight() ;
		int width = originalIcon.getMinimumWidth() ;

		// Create a Bitmap from the original icon
		Bitmap bitmapOriginal = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) ;
		originalIcon.setBounds(0, 0, width, height) ;
		originalIcon.draw(new Canvas(bitmapOriginal)) ;

		// Create a grayscale Bitmap
		Bitmap bitmapGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) ;
		Canvas canvas = new Canvas(bitmapGrayscale) ;
		canvas.drawBitmap(bitmapOriginal, 0, 0, grayscalePaint) ;

		// Create the resulting drawable and apply the color tint
		Drawable newIcon = new BitmapDrawable(resources, bitmapGrayscale) ;
		newIcon.setTintMode(PorterDuff.Mode.MULTIPLY) ;
		newIcon.setTint(color_tint) ;

		// Perform cleanup and return the result
		bitmapOriginal.recycle() ;
		return newIcon ;
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
	 */
	private void prepareFolders(Context context, boolean reversed, IconPack iconPack1, IconPack iconPack2)
	{
		// Initializations
		String[] folders_files = InternalFile.searchFilesStartingWith(context, Constants.FILE_FOLDER_PREFIX) ;
		if(folders_files == null) return ;

		// Retrieve fhe folders colors mapping file if it exists
		ArrayList<String> folders_colors_file = new InternalFileTXT(Constants.FILE_FOLDERS_COLORS).readAllLines() ;

		// Search a generic folder icon in icon packs and retrieve the Discreet Launcher folder icon to use as fallback
		Drawable defaultIconPackIcon = searchInMultipleIconPacks(iconPack1, iconPack2, Constants.APK_FOLDER, Constants.APK_FOLDER) ;
		Drawable baseIcon = AppCompatResources.getDrawable(context, R.drawable.icon_folder) ;

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
			int color = ContextCompat.getColor(context, R.color.for_icon_added_in_drawer) ;
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

			// Retrieve the number of apps in the folder
			int folder_size = folder.getApplications().size() ;

			// Create a folder icon using a specific or generic icon pack icon, or the Discreet Launcher folder icon as fallback
			Drawable icon ;
			Drawable iconPackIcon = searchInMultipleIconPacks(iconPack1, iconPack2, Constants.APK_FOLDER, Constants.APK_FOLDER + folder_size) ;
			if(iconPackIcon != null) icon = new FolderIcon(iconPackIcon, icon_size, -1, -1, true) ;
				else if(defaultIconPackIcon != null) icon = new FolderIcon(defaultIconPackIcon, icon_size, folder_size, folder.getColor(), true) ;
				else icon = new FolderIcon(baseIcon, icon_size, folder_size, folder.getColor(), false) ;

			// Resize the icon to the user-defined size
			icon.setBounds(0, 0, icon_size, icon_size) ;
			folder.setIcon(icon) ;

			// Sort the folder content and add it to the list of folders
			folder.sortFolder() ;
			folders.add(folder) ;
		}

		// Sort the folders and add them at the beginning or end of the list (based on layout)
		Collator collator = Collator.getInstance() ;
		collator.setStrength(Collator.PRIMARY) ;
		Collections.sort(folders, (folder1, folder2) -> collator.compare(folder1.getDisplayName(), folder2.getDisplayName())) ;
		if(reversed) drawer.addAll(folders) ;
			else drawer.addAll(0, folders) ;
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
	 */
	private void loadShortcuts(Context context)
	{
		// Use the folder icon as default shortcut icon
		Drawable default_icon = AppCompatResources.getDrawable(context, R.drawable.icon_folder) ;
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
				if(line.startsWith(application.getComponentInfo() + Constants.SEPARATOR))
				{
					// Rename the application and move to the next line
					String new_name = line.replace(application.getComponentInfo() + Constants.SEPARATOR, "") ;
					application.setDisplayName(new_name) ;
					break ;
				}
		}
	}


	/**
	 * Return the list of folders.
	 */
	public ArrayList<Folder> getFolders()
	{
		ArrayList<Folder> result = new ArrayList<>() ;
		for(Application application : drawer)
			if(application instanceof Folder) result.add((Folder)application) ;
		return result ;
	}


	/**
	 * Return the list of applications which are not in folders.
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
	 * Return all applications (except hidden), including or not those which are in folders.
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
		Collator collator = Collator.getInstance() ;
		collator.setStrength(Collator.PRIMARY) ;
		Collections.sort(allApplications, (application1, application2) -> collator.compare(application1.getDisplayName(), application2.getDisplayName())) ;

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
				Collections.sort(folders, (folder1, folder2) -> collator.compare(folder1.getDisplayName(), folder2.getDisplayName())) ;
				allApplications.addAll(0, folders) ;
			}

		// Return the result
		return allApplications ;
	}


	/**
	 * Return the list of what should appear in the app drawer.
	 */
	public ArrayList<Application> getDrawer()
	{
		return drawer ;
	}


	/**
	 * Return the list of favorite application for display in the panel.
	 */
	public ArrayList<Application> getFavorites()
	{
		return favorites ;
	}


	/**
	 * Return the list of hidden applications for display in the settings.
	 */
	public ArrayList<Application> getHidden()
	{
		return hidden ;
	}


	/**
	 * Convert an internal file from name format to ComponentInfo format if needed.
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


	/**
	 * Search the icon of an application in the packs (returns <code>null</code> if not found).
	 */
	private Drawable searchInMultipleIconPacks(IconPack iconPack1, IconPack iconPack2, String apk, String name)
	{
		Drawable icon = iconPack1.searchIcon(apk, name) ;
		if(icon == null) icon = iconPack2.searchIcon(apk, name) ;
		return icon ;
	}
}
