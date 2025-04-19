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
import android.annotation.SuppressLint ;
import android.content.ActivityNotFoundException ;
import android.content.SharedPreferences ;
import android.net.Uri ;
import android.os.Bundle ;
import android.view.View ;
import androidx.activity.result.ActivityResultLauncher ;
import androidx.activity.result.contract.ActivityResultContracts ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.Utils ;
import com.vincent_falzon.discreetlauncher.storage.* ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;

/**
 * Allow to import and export settings, favorites applications and shortcuts.
 */
public class ActivityExportImport extends AppCompatActivity implements View.OnClickListener
{
	// Constants
	private static final String TAG = "ActivityExportImport" ;

	// Attributes
	private SharedPreferences settings ;
	private SharedPreferences.Editor editor ;
	private ActivityResultLauncher<String> exportFilePicker ;
	private ActivityResultLauncher<String> importFilePicker ;


	/**
	 * Constructor.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Initializations
		setContentView(R.layout.activity_export_import) ;
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) ;
		findViewById(R.id.export_button).setOnClickListener(this) ;
		findViewById(R.id.import_button).setOnClickListener(this) ;

		// Prepare the file pickers callbacks
		exportFilePicker = registerForActivityResult(new ActivityResultContracts.CreateDocument("text/plain"), result -> {
				// Unless the selection has been cancelled, create the export file
				if(result != null)
					writeToExportFile(result) ;
			}) ;
		importFilePicker = registerForActivityResult(new ActivityResultContracts.GetContent(), result -> {
				// Unless the selection has been cancelled, read and load the import file
				if(result != null)
					readFromImportFile(result) ;
			}) ;
	}


	/**
	 * Called when an element is clicked.
	 */
	public void onClick(View view)
	{
		try
		{
			// Identify which element has been clicked
			int selection = view.getId() ;
			if(selection == R.id.export_button)
				{
					// Retrieve the current day, month and year to form a timestamp
					@SuppressLint("SimpleDateFormat")
					String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) ;

					// Display the file selector for the user to select where the export file should be saved
					exportFilePicker.launch(timestamp + "_discreetlauncher.txt") ;
				}
				else if (selection == R.id.import_button)
				{
					// Display the file selector for the user to select the import file
					importFilePicker.launch("text/plain") ;
				}
		}
		catch(ActivityNotFoundException exception)
		{
			// Display an error message if the file picker could not be displayed
			Utils.logError(TAG, exception.getMessage()) ;
			Utils.displayLongToast(view.getContext(), view.getContext().getString(R.string.error_app_not_found, "{android_file_picker}")) ;
		}
	}


	/**
	 * Export all the application data and settings to the selected destination.
	 */
	private void writeToExportFile(Uri location)
	{
		// Prepare the export file header
		ArrayList<String> exportedData = new ArrayList<>() ;
		exportedData.add("# Export " + getString(R.string.app_name) + " " + getString(R.string.app_version) + " (" + SimpleDateFormat.getDateTimeInstance().format(new Date()) + ")") ;
		exportedData.add("# " + getString(R.string.export_import_warning_file_edit)) ;
		exportedData.add("#") ;

		// Save the content of all internal files
		exportedData.add("# " + getString(R.string.export_import_header_internal_files)) ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_FAVORITES).prepareForExport()) ;
		String[] folders_files = InternalFile.searchFilesStartingWith(this, Constants.FILE_FOLDER_PREFIX) ;
		if(folders_files != null)
			for(String folder : folders_files)
				exportedData.addAll(new InternalFileTXT(folder).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_FOLDERS_COLORS).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_HIDDEN).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_RENAME_APPS).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_SHORTCUTS).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_SHORTCUTS_LEGACY).prepareForExport()) ;
		exportedData.add("#") ;

		// Save all settings
		exportedData.add("# " + getString(R.string.export_import_header_settings)) ;
		exportedData.add(exportStringSetting(Constants.APPLICATION_THEME)) ;
		exportedData.add(exportBooleanSetting(Constants.TRANSPARENT_STATUS_BAR, true)) ;
		exportedData.add(exportBooleanSetting(Constants.DARK_STATUS_BAR_ICONS, false)) ;
		exportedData.add(exportStringSetting(Constants.ICON_SIZE_DP)) ;
		exportedData.add(exportBooleanSetting(Constants.HIDE_APP_NAMES, false)) ;
		exportedData.add(exportBooleanSetting(Constants.HIDE_FOLDER_NAMES, false)) ;
		exportedData.add(exportBooleanSetting(Constants.REMOVE_PADDING, false)) ;
		exportedData.add(exportStringSetting(Constants.BACKGROUND_COLOR_FAVORITES)) ;
		exportedData.add(exportStringSetting(Constants.TEXT_COLOR_FAVORITES)) ;
		exportedData.add(exportStringSetting(Constants.BACKGROUND_COLOR_DRAWER)) ;
		exportedData.add(exportStringSetting(Constants.TEXT_COLOR_DRAWER)) ;
		exportedData.add(exportStringSetting(Constants.BACKGROUND_COLOR_FOLDERS)) ;
		exportedData.add(exportStringSetting(Constants.TEXT_COLOR_FOLDERS)) ;
		exportedData.add(exportStringSetting(Constants.CLOCK_FORMAT)) ;
		exportedData.add(exportStringSetting(Constants.CLOCK_COLOR)) ;
		exportedData.add(exportStringSetting(Constants.CLOCK_SHADOW_COLOR)) ;
		exportedData.add(exportStringSetting(Constants.CLOCK_POSITION)) ;
		exportedData.add(exportStringSetting(Constants.CLOCK_SIZE)) ;
		exportedData.add(exportStringSetting(Constants.ICON_PACK)) ;
		exportedData.add(exportStringSetting(Constants.ICON_PACK_SECONDARY)) ;
		exportedData.add(exportStringSetting(Constants.ICON_COLOR_FILTER)) ;
		exportedData.add(exportBooleanSetting(Constants.NOTIFICATION, false)) ;
		exportedData.add(exportStringSetting(Constants.FORCED_ORIENTATION)) ;
		exportedData.add(exportBooleanSetting(Constants.ALWAYS_SHOW_FAVORITES, false)) ;
		exportedData.add(exportBooleanSetting(Constants.REVERSE_INTERFACE, false)) ;
		exportedData.add(exportBooleanSetting(Constants.IMMERSIVE_MODE, false)) ;
		exportedData.add(exportBooleanSetting(Constants.TOUCH_TARGETS, false)) ;
		exportedData.add(exportBooleanSetting(Constants.INTERACTIVE_CLOCK, false)) ;
		exportedData.add(exportStringSetting(Constants.CLOCK_APP)) ;
		exportedData.add(exportBooleanSetting(Constants.HIDE_MENU_BUTTON, false)) ;
		exportedData.add(exportBooleanSetting(Constants.DISABLE_APP_DRAWER, false)) ;
		exportedData.add(exportStringSetting(Constants.DOUBLE_TAP)) ;
		exportedData.add(exportStringSetting(Constants.SWIPE_LEFTWARDS)) ;
		exportedData.add(exportStringSetting(Constants.SWIPE_RIGHTWARDS)) ;
		exportedData.add("#") ;

		// Save all custom icons
		exportedData.add("# " + getString(R.string.export_import_header_icons)) ;
		String[] shortcuts_icons = InternalFile.searchFilesStartingWith(this, Constants.FILE_ICON_SHORTCUT_PREFIX) ;
		if(shortcuts_icons != null)
			for(String icon : shortcuts_icons)
				exportedData.add(new InternalFilePNG(icon).prepareForExport()) ;
		exportedData.add("#") ;

		// Write all lines in the export file
		if(ExternalFile.writeAllLines(this, location, exportedData))
			{
				Utils.displayToast(this, R.string.export_completed) ;
				Utils.logInfo(TAG, "export completed") ;
			}
			else
			{
				Utils.displayLongToast(this, getString(R.string.error_export)) ;
				Utils.logError(TAG, "error when exporting to \"" + location + "\"") ;
			}
	}


	/**
	 * Prepare the line of a boolean setting for writing in an export file.
	 */
	private String exportBooleanSetting(String setting, boolean default_value)
	{
		return setting + ": " + settings.getBoolean(setting, default_value) ;
	}


	/**
	 * Prepare the line of a String setting for writing in an export file (default value is "none").
	 */
	private String exportStringSetting(String setting)
	{
		return setting + ": " + settings.getString(setting, Constants.NONE) ;
	}


	/**
	 * Load all the application data and settings from the selected source.
	 */
	private void readFromImportFile(Uri location)
	{
		// Read the content of the file line by line
		ArrayList<String> importedData = ExternalFile.readAllLines(this, location) ;
		if(importedData == null)
			{
				// Display an error message and quit
				Utils.displayLongToast(this, getString(R.string.error_import)) ;
				Utils.logError(TAG, "error when importing from \"" + location + "\"") ;
				return ;
			}

		// Prepare the files that need to be replaced
		InternalFileTXT favorites = new InternalFileTXT(Constants.FILE_FAVORITES) ;
		InternalFileTXT folders_colors = new InternalFileTXT(Constants.FILE_FOLDERS_COLORS) ;
		InternalFileTXT hidden = new InternalFileTXT(Constants.FILE_HIDDEN) ;
		InternalFileTXT rename_apps = new InternalFileTXT(Constants.FILE_RENAME_APPS) ;
		InternalFileTXT shortcuts = new InternalFileTXT(Constants.FILE_SHORTCUTS) ;
		InternalFileTXT shortcuts_legacy = new InternalFileTXT(Constants.FILE_SHORTCUTS_LEGACY) ;
		favorites.remove() ;
		folders_colors.remove() ;
		hidden.remove() ;
		rename_apps.remove() ;
		shortcuts.remove() ;
		shortcuts_legacy.remove() ;

		// Remove any existing folder
		String[] folders_files = InternalFile.searchFilesStartingWith(this, Constants.FILE_FOLDER_PREFIX) ;
		if(folders_files != null)
			for(String folder : folders_files) new InternalFileTXT(folder).remove() ;

		// Remove any existing shortcut icon
		String[] shortcuts_icons = InternalFile.searchFilesStartingWith(this, Constants.FILE_ICON_SHORTCUT_PREFIX) ;
		if(shortcuts_icons != null)
			for(String icon : shortcuts_icons) new InternalFilePNG(icon).remove() ;

		// Reset the preference to default before importing the file
		ActivityMain.setSkipListUpdate(true) ;
		settings.edit().clear().apply() ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_appearance, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_operation, true) ;
		ActivityMain.setSkipListUpdate(false) ;

		// Browse the lines of the import file
		editor = settings.edit() ;
		boolean old_clock_found = false ;
		boolean old_clock_status = false ;
		for(String line : importedData)
		{
			// Skip the comments
			if(line.startsWith("#")) continue ;

			// Extract the line target (can be a filename or a setting name) and value
			if(line.indexOf(": ") <= 0) continue ;
			String target = line.substring(0, line.indexOf(":")) ;
			String value = line.replace(target + ": ", "") ;

			// Create the internal files
			if(target.equals(Constants.FILE_FAVORITES)) writeLineToInternalFile(favorites, value) ;
				else if(target.equals(Constants.FILE_FOLDERS_COLORS)) writeLineToInternalFile(folders_colors, value) ;
				else if(target.equals(Constants.FILE_HIDDEN)) writeLineToInternalFile(hidden, value) ;
				else if(target.equals(Constants.FILE_RENAME_APPS)) writeLineToInternalFile(rename_apps, value) ;
				else if(target.equals(Constants.FILE_SHORTCUTS)) writeLineToInternalFile(shortcuts, value) ;
				else if(target.equals(Constants.FILE_SHORTCUTS_LEGACY)) writeLineToInternalFile(shortcuts_legacy, value) ;
				else if(target.startsWith(Constants.FILE_FOLDER_PREFIX)) writeLineToInternalFile(new InternalFileTXT(target), value) ;
				else if(target.startsWith(Constants.FILE_ICON_SHORTCUT_PREFIX)) new InternalFilePNG(target).loadFromImport(value) ;
				// Load the settings
				else if(target.equals(Constants.APPLICATION_THEME)) editor.putString(target, value) ;
				else if(target.equals(Constants.TRANSPARENT_STATUS_BAR)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.DARK_STATUS_BAR_ICONS)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.ICON_SIZE_DP)) editor.putString(target, value) ;
				else if(target.equals(Constants.HIDE_APP_NAMES)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.HIDE_FOLDER_NAMES)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.REMOVE_PADDING)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.BACKGROUND_COLOR_FAVORITES)) editor.putString(target, value) ;
				else if(target.equals(Constants.TEXT_COLOR_FAVORITES)) editor.putString(target, value) ;
				else if(target.equals(Constants.BACKGROUND_COLOR_DRAWER)) editor.putString(target, value) ;
				else if(target.equals(Constants.TEXT_COLOR_DRAWER)) editor.putString(target, value) ;
				else if(target.equals(Constants.BACKGROUND_COLOR_FOLDERS)) editor.putString(target, value) ;
				else if(target.equals(Constants.TEXT_COLOR_FOLDERS)) editor.putString(target, value) ;
				else if(target.equals(Constants.OLD_DISPLAY_CLOCK))
				{
					// Note the configuration of the old clock setting (v4.0.0 - 05/06/2021, to remove later)
					old_clock_found = true ;
					old_clock_status = value.equals("true") ;
				}
				else if(target.equals(Constants.CLOCK_FORMAT))
				{
					// Merge the two clock settings into a single one (v4.0.0 - 05/06/2021, to remove later)
					if(old_clock_found && !old_clock_status) editor.putString(target, Constants.NONE) ;
						else editor.putString(target, value) ;
				}
				else if(target.equals(Constants.CLOCK_COLOR)) editor.putString(target, value) ;
				else if(target.equals(Constants.CLOCK_SHADOW_COLOR)) editor.putString(target, value) ;
				else if(target.equals(Constants.CLOCK_POSITION)) editor.putString(target, value) ;
				else if(target.equals(Constants.CLOCK_SIZE)) editor.putString(target, value) ;
				else if(target.equals(Constants.ICON_PACK)) editor.putString(target, value) ;
				else if(target.equals(Constants.ICON_PACK_SECONDARY)) editor.putString(target, value) ;
				else if(target.equals(Constants.ICON_COLOR_FILTER)) editor.putString(target, value) ;
				else if(target.equals(Constants.NOTIFICATION)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.FORCED_ORIENTATION)) editor.putString(target, value) ;
				else if(target.equals(Constants.ALWAYS_SHOW_FAVORITES)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.REVERSE_INTERFACE)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.IMMERSIVE_MODE)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.TOUCH_TARGETS)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.INTERACTIVE_CLOCK)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.CLOCK_APP)) editor.putString(target, value) ;
				else if(target.equals(Constants.HIDE_MENU_BUTTON)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.DISABLE_APP_DRAWER)) editor.putBoolean(target, value.equals("true")) ;
				else if(target.equals(Constants.DOUBLE_TAP)) editor.putString(target, value) ;
				else if(target.equals(Constants.SWIPE_LEFTWARDS)) editor.putString(target, value) ;
				else if(target.equals(Constants.SWIPE_RIGHTWARDS)) editor.putString(target, value) ;
				// Convert old settings for compatibility
				else migrateFromOldFormat(target, value) ;
		}
		editor.apply() ;

		// Update the applications list
		ActivityMain.updateList(this) ;
		Utils.logInfo(TAG, "import completed") ;
	}


	/**
	 * Unless it is "none", write the given value as a line in an internal file.
	 */
	private void writeLineToInternalFile(InternalFileTXT file, String value)
	{
		if(value.equals(Constants.NONE)) return ;
		file.writeLine(value) ;
	}


	/**
	 * Migrate a removed/modified setting from its older format to the current one.
	 */
	private void migrateFromOldFormat(String setting, String value)
	{
		switch(setting)
		{
			case Constants.OLD_BACKGROUND_COLOR :
				editor.putString(Constants.BACKGROUND_COLOR_FAVORITES, value) ;
				editor.putString(Constants.BACKGROUND_COLOR_DRAWER, value) ;
				break ;
			case Constants.OLD_FORCE_PORTRAIT :
				if(value.equals("true"))
					editor.putString(Constants.FORCED_ORIENTATION, "portrait") ;
				break ;
			case Constants.OLD_HIDDEN_APPLICATIONS :
				String[] app_details = value.split(Constants.OLD_HIDDEN_APPS_SEPARATOR) ;
				if(app_details.length >= 2)
					new InternalFileTXT(Constants.FILE_HIDDEN).writeLine(app_details[1]) ;
				break ;
			case Constants.OLD_ICON_SIZE :
				int icon_size = Integer.parseInt(value) * 12 ;
				editor.putString(Constants.ICON_SIZE_DP, Integer.toString(icon_size)) ;
				break ;
		}
	}
}
