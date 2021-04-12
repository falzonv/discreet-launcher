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
import android.annotation.SuppressLint ;
import android.app.Activity ;
import android.content.Intent ;
import android.content.SharedPreferences ;
import android.net.Uri ;
import android.os.Bundle ;
import android.view.View ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.storage.* ;
import java.io.File ;
import java.io.FilenameFilter ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Date ;
import java.util.HashSet ;
import java.util.Set ;

/**
 * Allow to import and export settings, favorites applications and shortcuts.
 */
public class ActivityExportImport extends AppCompatActivity
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
	}


	/**
	 * Detect a click on an element from the activity.
	 * @param view Element clicked
	 */
	public void onClickExportImportActivity(View view)
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
		exportedData.add("# Export " + getString(R.string.app_name) + " (" + SimpleDateFormat.getDateTimeInstance().format(new Date()) + ")") ;
		exportedData.add("# " + getString(R.string.export_import_file_edit_warning)) ;
		exportedData.add("#") ;

		// Save the content of all internal files
		exportedData.add("# " + getString(R.string.export_import_internal_files_header)) ;
		exportedData.addAll(new InternalFileTXT(Constants.FAVORITES_FILE).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.SHORTCUTS_FILE).prepareForExport()) ;
		exportedData.addAll(new InternalFileTXT(Constants.SHORTCUTS_LEGACY_FILE).prepareForExport()) ;
		exportedData.add("#") ;

		// Save all settings
		exportedData.add("# " + getString(R.string.button_settings)) ;
		exportedData.add(exportBooleanSetting(Constants.DISPLAY_CLOCK, false)) ;
		exportedData.add(exportBooleanSetting(Constants.TRANSPARENT_STATUS_BAR, false)) ;
		exportedData.add(exportBooleanSetting(Constants.FORCE_PORTRAIT, false)) ;
		exportedData.add(Constants.ICON_PACK + ": " + settings.getString(Constants.ICON_PACK, Constants.NONE)) ;
		Set<String> hiddenApplications = settings.getStringSet(Constants.HIDDEN_APPLICATIONS, null) ;
		if(hiddenApplications == null) exportedData.add(Constants.HIDDEN_APPLICATIONS + ": " + Constants.NONE) ;
			else for(String hidden_application : hiddenApplications) exportedData.add(Constants.HIDDEN_APPLICATIONS + ": " + hidden_application) ;
		exportedData.add(exportBooleanSetting(Constants.DISPLAY_NOTIFICATION, true)) ;
		String notification_text = settings.getString(Constants.NOTIFICATION_TEXT, getString(R.string.set_notification_text_default)) ;
		if(notification_text == null) exportedData.add(Constants.NOTIFICATION_TEXT + ": " + getString(R.string.set_notification_text_default)) ;
			else exportedData.add(Constants.NOTIFICATION_TEXT + ": " + notification_text.replace("\n", "\\n")) ;
		exportedData.add(exportBooleanSetting(Constants.HIDE_ON_LOCK_SCREEN, true)) ;
		exportedData.add(Constants.NOTIFICATION_APP + "1: " + settings.getString(Constants.NOTIFICATION_APP + "1", Constants.NONE)) ;
		exportedData.add(Constants.NOTIFICATION_APP + "2: " + settings.getString(Constants.NOTIFICATION_APP + "2", Constants.NONE)) ;
		exportedData.add(Constants.NOTIFICATION_APP + "3: " + settings.getString(Constants.NOTIFICATION_APP + "3", Constants.NONE)) ;
		exportedData.add("#") ;

		// Save all shortcuts icons
		exportedData.add("# " + getString(R.string.export_import_shortcuts_icons)) ;
		String[] shortcuts_icons = getFilesDir().list(new FilenameFilter()
		{
			@Override
			public boolean accept(File directory, String name)
			{
				return name.startsWith(Constants.SHORTCUT_ICON_PREFIX) ;
			}
		}) ;
		if(shortcuts_icons != null)
			for(String icon : shortcuts_icons)
				exportedData.add(new InternalFilePNG(icon).prepareForExport()) ;
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
		ActivityMain.setIgnoreSettingsChanges(true) ;
		InternalFileTXT favorites, shortcuts, shortcuts_legacy ;
		if(importedData.contains(Constants.FAVORITES_FILE + ": " + Constants.NONE)) favorites = null ;
			else favorites = new InternalFileTXT(Constants.FAVORITES_FILE) ;
		if(importedData.contains(Constants.SHORTCUTS_FILE + ": " + Constants.NONE)) shortcuts = null ;
			else shortcuts = new InternalFileTXT(Constants.SHORTCUTS_FILE) ;
		if(importedData.contains(Constants.SHORTCUTS_LEGACY_FILE + ": " + Constants.NONE)) shortcuts_legacy = null ;
			else shortcuts_legacy = new InternalFileTXT(Constants.SHORTCUTS_LEGACY_FILE) ;
		if(favorites != null) favorites.remove() ;
		if(shortcuts != null) shortcuts.remove() ;
		if(shortcuts_legacy != null) shortcuts_legacy.remove() ;

		// Reset the preference to default before importing the file
		settings.edit().clear().apply() ;
		PreferenceManager.setDefaultValues(this, R.xml.settings, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_display, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_notification, true) ;

		// Browse the lines of the import file
		Set<String> hiddenApplications = new HashSet<>() ;
		editor = settings.edit() ;
		for(String line : importedData)
		{
			// Skip the comments
			if(line.startsWith("#")) continue ;

			// Replace the content of the internal files
			if(line.startsWith(Constants.FAVORITES_FILE) && (favorites != null))
				{
					favorites.writeLine(line.replace(Constants.FAVORITES_FILE + ": ", "")) ;
				}
				else if(line.startsWith(Constants.SHORTCUTS_FILE) && (shortcuts != null))
				{
					shortcuts.writeLine(line.replace(Constants.SHORTCUTS_FILE + ": ", "")) ;
				}
				else if(line.startsWith(Constants.SHORTCUTS_LEGACY_FILE) && (shortcuts_legacy != null))
				{
					shortcuts_legacy.writeLine(line.replace(Constants.SHORTCUTS_LEGACY_FILE + ": ", "")) ;
				}
				// Load the settings
				else if(line.startsWith(Constants.DISPLAY_CLOCK)) loadBooleanSetting(Constants.DISPLAY_CLOCK, line) ;
				else if(line.startsWith(Constants.TRANSPARENT_STATUS_BAR)) loadBooleanSetting(Constants.TRANSPARENT_STATUS_BAR, line) ;
				else if(line.startsWith(Constants.FORCE_PORTRAIT)) loadBooleanSetting(Constants.FORCE_PORTRAIT, line) ;
				else if(line.startsWith(Constants.ICON_PACK)) loadStringSetting(Constants.ICON_PACK + "1", line) ;
				else if(line.startsWith(Constants.HIDDEN_APPLICATIONS)) hiddenApplications.add(line.replace(Constants.HIDDEN_APPLICATIONS + ": ", "")) ;
				else if(line.startsWith(Constants.DISPLAY_NOTIFICATION)) loadBooleanSetting(Constants.DISPLAY_NOTIFICATION, line) ;
				else if(line.startsWith(Constants.NOTIFICATION_TEXT)) loadStringSetting(Constants.NOTIFICATION_TEXT, line.replace("\\n", "\n")) ;
				else if(line.startsWith(Constants.HIDE_ON_LOCK_SCREEN)) loadBooleanSetting(Constants.HIDE_ON_LOCK_SCREEN, line) ;
				else if(line.startsWith(Constants.NOTIFICATION_APP + "1")) loadStringSetting(Constants.NOTIFICATION_APP + "1", line) ;
				else if(line.startsWith(Constants.NOTIFICATION_APP + "2")) loadStringSetting(Constants.NOTIFICATION_APP + "2", line) ;
				else if(line.startsWith(Constants.NOTIFICATION_APP + "3")) loadStringSetting(Constants.NOTIFICATION_APP + "3", line) ;
				// Save the shortcuts icons
				else if(line.startsWith(Constants.SHORTCUT_ICON_PREFIX))
				{
					if(line.indexOf(": ") <= 0) continue ;
					InternalFilePNG icon_file = new InternalFilePNG(line.substring(0, line.indexOf(": "))) ;
					icon_file.loadFromImport(line) ;
				}
		}
		if(hiddenApplications.size() > 0) editor.putStringSet(Constants.HIDDEN_APPLICATIONS, hiddenApplications) ;
		editor.apply() ;

		// Indicate that the applications list should be updated and start to listen again for settings changes
		ActivityMain.setListUpdateNeeded() ;
		ActivityMain.setIgnoreSettingsChanges(false) ;
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
