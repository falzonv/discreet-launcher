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
import android.annotation.SuppressLint ;
import android.app.Activity ;
import android.content.Intent ;
import android.content.SharedPreferences ;
import android.net.Uri ;
import android.os.Bundle ;
import android.view.View ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.ShowDialog ;
import com.vincent_falzon.discreetlauncher.storage.* ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;

/**
 * Allow to import and export settings, favorites applications and shortcuts.
 */
public class ActivityExportImport extends AppCompatActivity implements View.OnClickListener
{
	// Attributes
	private SharedPreferences settings ;
	private SharedPreferences.Editor editor ;


	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
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
	}


	/**
	 * Detect a click on an element from the activity.
	 * @param view Element clicked
	 */
	public void onClick(View view)
	{
		// Identify which element has been clicked
		int selection = view.getId() ;
		if(selection == R.id.export_button)
			{
				// Retrieve the current day, month and year to form a timestamp
				@SuppressLint("SimpleDateFormat")
				String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) ;

				// Display the file selector for the user to select where the export file should be saved
				Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT) ;
				intent.addCategory(Intent.CATEGORY_OPENABLE) ;
				intent.setType("text/plain") ;
				intent.putExtra(Intent.EXTRA_TITLE, timestamp + "_discreetlauncher.txt") ;
				startActivityForResult(intent, 100) ;
			}
			else if (selection == R.id.import_button)
			{
				// Display the file selector for the user to select the import file
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT) ;
				intent.addCategory(Intent.CATEGORY_OPENABLE) ;
				intent.setType("text/plain") ;
				startActivityForResult(intent, 110) ;
			}
	}


	/**
	 * Method called after an activity has been performed.
	 * @param request Code provided when starting the finished activity
	 * @param result Result code of the activity
	 * @param data Data provided along with the result code
	 */
	@Override
	protected void onActivityResult(int request, int result, Intent data)
	{
		// Let the parent actions be performed
		super.onActivityResult(request, result, data) ;

		// Check if the activity was successful and if the data are valid
		if((result != Activity.RESULT_OK) || (data == null) || (data.getData() == null)) return ;

		// Check the type of request and perform the related actions
		switch(request)
		{
			case 100 :
				writeToExportFile(data.getData()) ;
				break ;
			case 110 :
				readFromImportFile(data.getData()) ;
				break ;
		}
	}


	/**
	 * Export all the application data and settings to the selected destination.
	 * @param location Location of the file on the system
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
		exportedData.addAll(new InternalFileTXT(Constants.FILE_HIDDEN).prepareForExport()) ;
		String[] folders_files = InternalFile.searchFilesStartingWith(this, Constants.FILE_FOLDER_PREFIX) ;
		if(folders_files != null)
			for(String folder : folders_files)
				exportedData.addAll(new InternalFileTXT(folder).prepareForExport()) ;
		exportedData.add("#") ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_SHORTCUTS).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.FILE_SHORTCUTS_LEGACY).prepareForExport()) ;

		// Save all settings
		exportedData.add("# " + getString(R.string.export_import_header_settings)) ;
		exportedData.add(exportBooleanSetting(Constants.NOTIFICATION, true)) ;
		exportedData.add(exportStringSetting(Constants.APPLICATION_THEME)) ;
		exportedData.add(exportStringSetting(Constants.BACKGROUND_COLOR)) ;
		exportedData.add(exportBooleanSetting(Constants.TRANSPARENT_STATUS_BAR, false)) ;
		exportedData.add(exportBooleanSetting(Constants.HIDE_MENU_BUTTON, false)) ;
		exportedData.add(exportStringSetting(Constants.CLOCK_FORMAT)) ;
		exportedData.add(exportStringSetting(Constants.ICON_PACK)) ;
		exportedData.add(exportBooleanSetting(Constants.HIDE_APP_NAMES, false)) ;
		exportedData.add(exportBooleanSetting(Constants.REMOVE_PADDING, false)) ;
		exportedData.add(exportBooleanSetting(Constants.FORCE_PORTRAIT, false)) ;
		exportedData.add(exportBooleanSetting(Constants.ALWAYS_SHOW_FAVORITES, false)) ;
		exportedData.add(exportBooleanSetting(Constants.IMMERSIVE_MODE, false)) ;
		exportedData.add(exportBooleanSetting(Constants.REVERSE_INTERFACE, false)) ;
		exportedData.add(exportBooleanSetting(Constants.TOUCH_TARGETS, false)) ;
		exportedData.add(exportBooleanSetting(Constants.DISABLE_APP_DRAWER, false)) ;
		exportedData.add("#") ;

		// Save all custom icons
		exportedData.add("# " + getString(R.string.export_import_header_icons)) ;
		String[] shortcuts_icons = InternalFile.searchFilesStartingWith(this, Constants.FILE_ICON_SHORTCUT_PREFIX) ;
		if(shortcuts_icons != null)
			for(String icon : shortcuts_icons) exportedData.add(new InternalFilePNG(icon).prepareForExport()) ;
		exportedData.add("#") ;

		// Write all lines in the export file
		if(ExternalFile.writeAllLines(this, location, exportedData))
				ShowDialog.toast(this, R.string.export_completed) ;
			else ShowDialog.toastLong(this, getString(R.string.error_export)) ;
	}


	/**
	 * Prepare the line of a boolean setting for writing in an export file.
	 * @param setting Key of the setting to export
	 * @param default_value Default value of the setting
	 */
	private String exportBooleanSetting(String setting, boolean default_value)
	{
		return setting + ": " + settings.getBoolean(setting, default_value) ;
	}


	/**
	 * Prepare the line of a String setting for writing in an export file.
	 * @param setting Key of the setting to export (default value is "none")
	 */
	private String exportStringSetting(String setting)
	{
		return setting + ": " + settings.getString(setting, Constants.NONE) ;
	}


	/**
	 * Load all the application data and settings from the selected source.
	 * @param location Location of the file on the system
	 */
	private void readFromImportFile(Uri location)
	{
		// Read the content of the file line by line
		ArrayList<String> importedData = ExternalFile.readAllLines(this, location) ;
		if(importedData == null)
			{
				// Display an error message and quit
				ShowDialog.toastLong(this, getString(R.string.error_import)) ;
				return ;
			}

		// Prepare the files that need to be replaced
		InternalFileTXT favorites = new InternalFileTXT(Constants.FILE_FAVORITES) ;
		InternalFileTXT hidden = new InternalFileTXT(Constants.FILE_HIDDEN) ;
		InternalFileTXT shortcuts = new InternalFileTXT(Constants.FILE_SHORTCUTS) ;
		InternalFileTXT shortcuts_legacy = new InternalFileTXT(Constants.FILE_SHORTCUTS_LEGACY) ;
		favorites.remove() ;
		hidden.remove() ;
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
		settings.edit().clear().apply() ;
		PreferenceManager.setDefaultValues(this, R.xml.settings, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_appearance, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_operation, true) ;

		// Browse the lines of the import file
		editor = settings.edit() ;
		boolean old_clock_found = false ;
		boolean old_clock_status = false ;
		for(String line : importedData)
		{
			// Skip the comments
			if(line.startsWith("#")) continue ;

			// Replace the content of the internal files
			if(line.startsWith(Constants.FILE_FAVORITES)) writeLineToInternalFile(favorites, line) ;
				else if(line.startsWith(Constants.FILE_HIDDEN)) writeLineToInternalFile(hidden, line) ;
				else if(line.startsWith(Constants.FILE_FOLDER_PREFIX))
				{
					if(line.indexOf(": ") <= 0) continue ;
					writeLineToInternalFile(new InternalFileTXT(line.substring(0, line.indexOf(": "))), line) ;
				}
				else if(line.startsWith(Constants.FILE_SHORTCUTS)) writeLineToInternalFile(shortcuts, line) ;
				else if(line.startsWith(Constants.FILE_SHORTCUTS_LEGACY)) writeLineToInternalFile(shortcuts_legacy, line) ;
				// Load the settings
				else if(line.startsWith(Constants.NOTIFICATION)) loadBooleanSetting(Constants.NOTIFICATION, line) ;
				else if(line.startsWith(Constants.APPLICATION_THEME)) loadStringSetting(Constants.APPLICATION_THEME, line) ;
				else if(line.startsWith(Constants.BACKGROUND_COLOR)) loadStringSetting(Constants.BACKGROUND_COLOR, line) ;
				else if(line.startsWith(Constants.TRANSPARENT_STATUS_BAR)) loadBooleanSetting(Constants.TRANSPARENT_STATUS_BAR, line) ;
				else if(line.startsWith(Constants.HIDE_MENU_BUTTON)) loadBooleanSetting(Constants.HIDE_MENU_BUTTON, line) ;
				else if(line.startsWith(Constants.DISPLAY_CLOCK))
				{
					// Note the configuration of the old clock setting (to remove later)
					old_clock_found = true ;
					old_clock_status = line.replace(Constants.DISPLAY_CLOCK + ": ", "").equals("true") ;
				}
				else if(line.startsWith(Constants.CLOCK_FORMAT))
				{
					// Merge the two clock settings into a single one (to remove later)
					if(old_clock_found && !old_clock_status) editor.putString(Constants.CLOCK_FORMAT, Constants.NONE) ;
						else loadStringSetting(Constants.CLOCK_FORMAT, line)  ;
				}
				else if(line.startsWith(Constants.ICON_PACK)) loadStringSetting(Constants.ICON_PACK, line) ;
				else if(line.startsWith(Constants.HIDE_APP_NAMES)) loadBooleanSetting(Constants.HIDE_APP_NAMES, line) ;
				else if(line.startsWith(Constants.REMOVE_PADDING)) loadBooleanSetting(Constants.REMOVE_PADDING, line) ;
				else if(line.startsWith(Constants.FORCE_PORTRAIT)) loadBooleanSetting(Constants.FORCE_PORTRAIT, line) ;
				else if(line.startsWith(Constants.ALWAYS_SHOW_FAVORITES)) loadBooleanSetting(Constants.ALWAYS_SHOW_FAVORITES, line) ;
				else if(line.startsWith(Constants.IMMERSIVE_MODE)) loadBooleanSetting(Constants.IMMERSIVE_MODE, line) ;
				else if(line.startsWith(Constants.REVERSE_INTERFACE)) loadBooleanSetting(Constants.REVERSE_INTERFACE, line) ;
				else if(line.startsWith(Constants.TOUCH_TARGETS)) loadBooleanSetting(Constants.TOUCH_TARGETS, line) ;
				else if(line.startsWith(Constants.DISABLE_APP_DRAWER)) loadBooleanSetting(Constants.DISABLE_APP_DRAWER, line) ;
				// Save the shortcuts icons
				else if(line.startsWith(Constants.FILE_ICON_SHORTCUT_PREFIX))
				{
					if(line.indexOf(": ") <= 0) continue ;
					InternalFilePNG icon_file = new InternalFilePNG(line.substring(0, line.indexOf(": "))) ;
					icon_file.loadFromImport(line) ;
				}
				// Convert the hidden applications from settings to internal file (to remove later)
				else if(line.startsWith(Constants.HIDDEN_APPLICATIONS))
				{
					String value = line.replace(Constants.HIDDEN_APPLICATIONS + ": ", "") ;
					String[] app_details = value.split(Constants.NOTIFICATION_SEPARATOR) ;
					if(app_details.length < 2) continue ;
					writeLineToInternalFile(hidden, Constants.FILE_HIDDEN + ": " + app_details[1]) ;
				}
		}
		editor.apply() ;

		// Update the applications list
		ActivityMain.updateList(this) ;
	}


	/**
	 * Write a line in an internal file
	 * @param file Internal file created before
	 * @param line Line to write in the internal file
	 */
	private void writeLineToInternalFile(InternalFileTXT file, String line)
	{
		String value = line.replace(file.getName() + ": ", "") ;
		if(value.equals(Constants.NONE)) return ;
		file.writeLine(value) ;
	}


	/**
	 * Modify a boolean setting based on its line in an import file.
	 * @param setting Key of the setting to modify
	 * @param line Line containing the new value
	 */
	private void loadBooleanSetting(String setting, String line)
	{
		editor.putBoolean(setting, line.replace(setting + ": ", "").equals("true")) ;
	}


	/**
	 * Modify a String setting based on its line in an import file.
	 * @param setting Key of the setting to modify
	 * @param line Line containing the new value
	 */
	private void loadStringSetting(String setting, String line)
	{
		editor.putString(setting, line.replace(setting + ": ", "")) ;
	}
}
