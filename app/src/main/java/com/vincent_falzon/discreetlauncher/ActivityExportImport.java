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
import android.os.ParcelFileDescriptor ;
import android.view.View ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.PreferenceManager ;
import java.io.BufferedReader ;
import java.io.FileReader ;
import java.io.FileWriter ;
import java.io.IOException ;
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
	// Constants
	public static final int EXPORT_REQUEST_CODE = 100 ;
	public static final int IMPORT_REQUEST_CODE = 110 ;

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
		// Call the constructor of the parent class
		super.onCreate(savedInstanceState) ;

		// Initializations
		setContentView(R.layout.activity_export_import) ;
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()) ;
	}


	/**
	 * Display the file selector where the export file should be saved.
	 */
	public void startExport(View view)
	{
		// Retrieve the current day, month and year to form a timestamp
		@SuppressLint("SimpleDateFormat")
		String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) ;

		// Prepare and display the file selector
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT) ;
		intent.addCategory(Intent.CATEGORY_OPENABLE) ;
		intent.setType("text/plain") ;
		intent.putExtra(Intent.EXTRA_TITLE, timestamp + "_discreetlauncher.txt") ;
		startActivityForResult(intent, EXPORT_REQUEST_CODE) ;
	}


	/**
	 * Display the file selector for the user to select the import file.
	 */
	public void startImport(View view)
	{
		// Prepare and display the file selector
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT) ;
		intent.addCategory(Intent.CATEGORY_OPENABLE) ;
		intent.setType("text/plain") ;
		startActivityForResult(intent, IMPORT_REQUEST_CODE) ;
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
			case EXPORT_REQUEST_CODE :
				writeToExportFile(data.getData()) ;
				break ;
			case IMPORT_REQUEST_CODE :
				readFromImportFile(data.getData()) ;
				break ;
		}
	}


	/**
	 * Export all the application data and settings to the selected destination.
	 * @param location File location selected by the user
	 */
	private void writeToExportFile(Uri location)
	{
		// Prepare the export file header
		ArrayList<String> exportedData = new ArrayList<>() ;
		exportedData.add("# Export " + getString(R.string.app_name) + " (" + SimpleDateFormat.getDateTimeInstance().format(new Date()) + ")") ;
		exportedData.add("# " + getString(R.string.text_warning_edit_file)) ;
		exportedData.add("#") ;

		// Save the content of all internal files
		exportedData.add("# " + getString(R.string.text_internal_files_content)) ;
		exportedData.addAll(prepareFileForExport(ApplicationsList.FAVORITES_FILE)) ;
		exportedData.addAll(prepareFileForExport(ApplicationsList.SHORTCUTS_FILE)) ;
		exportedData.addAll(prepareFileForExport(ApplicationsList.SHORTCUTS_LEGACY_FILE)) ;
		exportedData.add("#") ;

		// Save all settings
		exportedData.add("# " + getString(R.string.button_settings)) ;
		exportedData.add(ActivitySettings.DISPLAY_CLOCK + ": " + settings.getBoolean(ActivitySettings.DISPLAY_CLOCK, false)) ;
		exportedData.add(ActivitySettings.TRANSPARENT_STATUS_BAR + ": " + settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false)) ;
		exportedData.add(ActivitySettings.ICON_PACK + ": " + settings.getString(ActivitySettings.ICON_PACK, ActivitySettings.NONE)) ;
		Set<String> hiddenApplications = settings.getStringSet(ActivitySettings.HIDDEN_APPLICATIONS, null) ;
		if(hiddenApplications == null) exportedData.add(ActivitySettings.HIDDEN_APPLICATIONS + ": " + ActivitySettings.NONE) ;
			else for(String hidden_application : hiddenApplications) exportedData.add(ActivitySettings.HIDDEN_APPLICATIONS + ": " + hidden_application) ;
		exportedData.add(ActivitySettings.DISPLAY_NOTIFICATION + ": " + settings.getBoolean(ActivitySettings.DISPLAY_NOTIFICATION, true)) ;
		exportedData.add(ActivitySettings.NOTIFICATION_TEXT + ": " + settings.getString(ActivitySettings.NOTIFICATION_TEXT, getString(R.string.text_notification)).replace("\n", "\\n")) ;
		exportedData.add(ActivitySettings.HIDE_ON_LOCK_SCREEN + ": " + settings.getBoolean(ActivitySettings.HIDE_ON_LOCK_SCREEN, true)) ;
		exportedData.add(ActivitySettings.NOTIFICATION_APP + "1: " + settings.getString(ActivitySettings.NOTIFICATION_APP + "1", ActivitySettings.NONE)) ;
		exportedData.add(ActivitySettings.NOTIFICATION_APP + "2: " + settings.getString(ActivitySettings.NOTIFICATION_APP + "2", ActivitySettings.NONE)) ;
		exportedData.add(ActivitySettings.NOTIFICATION_APP + "3: " + settings.getString(ActivitySettings.NOTIFICATION_APP + "3", ActivitySettings.NONE)) ;
		exportedData.add("#") ;

		try
		{
			// Write all lines in the file
			ParcelFileDescriptor file = getContentResolver().openFileDescriptor(location, "w") ;
			FileWriter writer = new FileWriter(file.getFileDescriptor()) ;
			for(String line : exportedData)
			{
				writer.write(line) ;
				writer.write(System.lineSeparator()) ;
			}
			writer.close() ;
			file.close() ;
			ShowDialog.toast(this, R.string.text_export_completed) ;
		}
		catch(IOException e)
		{
			// Display an error message and quit
			ShowDialog.toastLong(this, getString(R.string.error_creating_export)) ;
		}
	}


	/**
	 * Retrieve the content of a file as an array of lines.
	 * @param filename Name of the file to export
	 * @return Content of the file or a mention that it does not exist
	 */
	private ArrayList<String> prepareFileForExport(String filename)
	{
		// Initializations
		ArrayList<String> content = new ArrayList<>() ;
		InternalFile file = new InternalFile(this, filename) ;

		// Return the content of the file or indicate that it does not exist
		if(file.isNotExisting()) content.add(filename + ": " + ActivitySettings.NONE) ;
			else for(String line : file.readAllLines()) content.add(filename + ": " + line) ;
		return content ;
	}


	/**
	 * Load all the application data and settings from the selected source.
	 * @param location File location selected by the user
	 */
	private void readFromImportFile(Uri location)
	{
		// Prepare the table used to store the lines
		ArrayList<String> importedData = new ArrayList<>() ;
		String buffer ;

		try
		{
			// Read the content of the file line by line
			ParcelFileDescriptor file = getContentResolver().openFileDescriptor(location, "r") ;
			BufferedReader reader = new BufferedReader(new FileReader(file.getFileDescriptor())) ;
			while((buffer = reader.readLine()) != null) importedData.add(buffer) ;
			reader.close() ;
			file.close() ;
		}
		catch(IOException e)
		{
			// Display an error message and quit
			ShowDialog.toastLong(this, getString(R.string.error_reading_import)) ;
			return ;
		}

		// Prepare the files that need to be replaced
		ActivityMain.setIgnoreSettingsChanges(true) ;
		InternalFile favorites, shortcuts, shortcuts_legacy ;
		if(importedData.contains(ApplicationsList.FAVORITES_FILE + ": " + ActivitySettings.NONE)) favorites = null ;
			else favorites = new InternalFile(this, ApplicationsList.FAVORITES_FILE) ;
		if(importedData.contains(ApplicationsList.SHORTCUTS_FILE + ": " + ActivitySettings.NONE)) shortcuts = null ;
			else shortcuts = new InternalFile(this, ApplicationsList.SHORTCUTS_FILE) ;
		if(importedData.contains(ApplicationsList.SHORTCUTS_LEGACY_FILE + ": " + ActivitySettings.NONE)) shortcuts_legacy = null ;
			else shortcuts_legacy = new InternalFile(this, ApplicationsList.SHORTCUTS_LEGACY_FILE) ;
		if(favorites != null) favorites.hasRemovalFailed(this) ;
		if(shortcuts != null) shortcuts.hasRemovalFailed(this) ;
		if(shortcuts_legacy != null) shortcuts_legacy.hasRemovalFailed(this) ;

		// Reset the preference to default before importing the file
		settings.edit().clear().apply() ;
		PreferenceManager.setDefaultValues(this, R.xml.settings, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_notification, true) ;

		// Browse the lines of the import file
		Set<String> hiddenApplications = new HashSet<>() ;
		editor = settings.edit() ;
		for(String line : importedData)
		{
			// Replace the content of the internal files
			if(line.startsWith(ApplicationsList.FAVORITES_FILE) && (favorites != null))
				{
					favorites.writeLine(line.replace(ApplicationsList.FAVORITES_FILE + ": ", "")) ;
				}
				else if(line.startsWith(ApplicationsList.SHORTCUTS_FILE) && (shortcuts != null))
				{
					shortcuts.writeLine(line.replace(ApplicationsList.SHORTCUTS_FILE + ": ", "")) ;
				}
				else if(line.startsWith(ApplicationsList.SHORTCUTS_LEGACY_FILE) && (shortcuts_legacy != null))
				{
					shortcuts_legacy.writeLine(line.replace(ApplicationsList.SHORTCUTS_LEGACY_FILE + ": ", "")) ;
				}
				// Load the settings
				else if(line.startsWith(ActivitySettings.DISPLAY_CLOCK)) loadBooleanSetting(ActivitySettings.DISPLAY_CLOCK, line) ;
				else if(line.startsWith(ActivitySettings.TRANSPARENT_STATUS_BAR)) loadBooleanSetting(ActivitySettings.TRANSPARENT_STATUS_BAR, line) ;
				else if(line.startsWith(ActivitySettings.ICON_PACK)) loadStringSetting(ActivitySettings.ICON_PACK + "1", line) ;
				else if(line.startsWith(ActivitySettings.HIDDEN_APPLICATIONS)) hiddenApplications.add(line.replace(ActivitySettings.HIDDEN_APPLICATIONS + ": ", "")) ;
				else if(line.startsWith(ActivitySettings.DISPLAY_NOTIFICATION)) loadBooleanSetting(ActivitySettings.DISPLAY_NOTIFICATION, line) ;
				else if(line.startsWith(ActivitySettings.NOTIFICATION_TEXT)) loadStringSetting(ActivitySettings.NOTIFICATION_TEXT, line.replace("\\n", "\n")) ;
				else if(line.startsWith(ActivitySettings.HIDE_ON_LOCK_SCREEN)) loadBooleanSetting(ActivitySettings.HIDE_ON_LOCK_SCREEN, line) ;
				else if(line.startsWith(ActivitySettings.NOTIFICATION_APP + "1")) loadStringSetting(ActivitySettings.NOTIFICATION_APP + "1", line) ;
				else if(line.startsWith(ActivitySettings.NOTIFICATION_APP + "2")) loadStringSetting(ActivitySettings.NOTIFICATION_APP + "2", line) ;
				else if(line.startsWith(ActivitySettings.NOTIFICATION_APP + "3")) loadStringSetting(ActivitySettings.NOTIFICATION_APP + "3", line) ;
		}
		if(hiddenApplications.size() > 0) editor.putStringSet(ActivitySettings.HIDDEN_APPLICATIONS, hiddenApplications) ;
		editor.apply() ;

		// Inform the user and start again to listen for settings changes
		ShowDialog.toastLong(this, getString(R.string.text_import_completed)) ;
		ActivityMain.getApplicationsList().updateNotificationApps(this) ;
		ActivityMain.setIgnoreSettingsChanges(false) ;
	}


	/**
	 * Load the new value of a Boolean setting from the line of an import file.
	 * @param setting The setting to modify
	 * @param line Line containing the new value
	 */
	private void loadBooleanSetting(String setting, String line)
	{
		editor.putBoolean(setting, line.replace(setting + ": ", "").equals("true")) ;
	}


	/**
	 * Load the new value of a String setting from the line of an import file.
	 * @param setting The setting to modify
	 * @param line Line containing the new value
	 */
	private void loadStringSetting(String setting, String line)
	{
		editor.putString(setting, line.replace(setting + ": ", "")) ;
	}
}
