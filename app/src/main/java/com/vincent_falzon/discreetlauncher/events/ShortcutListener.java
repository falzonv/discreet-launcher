package com.vincent_falzon.discreetlauncher.events ;

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
import android.content.ActivityNotFoundException ;
import android.content.Context ;
import android.content.Intent ;
import android.content.pm.LauncherApps ;
import android.content.pm.ShortcutInfo ;
import android.graphics.Bitmap ;
import android.graphics.Canvas ;
import android.graphics.drawable.Drawable ;
import android.os.Build ;
import android.os.Bundle ;
import android.os.UserHandle ;
import androidx.appcompat.app.AppCompatActivity ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.ShowDialog ;
import com.vincent_falzon.discreetlauncher.storage.InternalFilePNG ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Activity called to add a shortcut (starting with Android Oreo).
 */
public class ShortcutListener extends AppCompatActivity
{
	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Call the constructor of the parent class
		super.onCreate(savedInstanceState) ;

		// Execute the following code only if the Android version is Oreo or higher
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				// Retrieve the intent which started this activity
				Intent intent = getIntent() ;

				// Check if a shortcut should be started
				if(intent.getExtras() != null)
					{
						// Retrieve the shortcut line provided by the caller
						String shortcut_line = intent.getExtras().getString(Constants.APK_SHORTCUT) ;
						if(shortcut_line != null)
							{
								// Extract the shortcut details
								String[] shortcut = shortcut_line.split(Constants.SHORTCUT_SEPARATOR) ;
								if(shortcut.length == 3)
								{
									// Try to retrieve the user ID, use 0 if not found (0 is "System", the most commonly used)
									int user_id ;
									try { user_id = Integer.parseInt(shortcut[2]) ; }
									catch(NumberFormatException e) { user_id = 0 ; }

									// Try to launch the shortcut
									LauncherApps launcher = (LauncherApps)getSystemService(Context.LAUNCHER_APPS_SERVICE) ;
									if(launcher.hasShortcutHostPermission())
									{
										try { launcher.startShortcut(shortcut[0], shortcut[1], null, null, UserHandle.getUserHandleForUid(user_id)) ; }
										catch(ActivityNotFoundException | IllegalStateException e)
										{ ShowDialog.toastLong(this, getString(R.string.error_shortcut_start)) ; }
									}
									else ShowDialog.toastLong(this, getString(R.string.error_shortcut_not_default_launcher)) ;
								}
								else ShowDialog.toastLong(this, getString(R.string.error_shortcut_missing_info)) ;
							}
					}

				// Check if a new shortcut should be added and accept the request
				LauncherApps.PinItemRequest pinRequest = intent.getParcelableExtra(LauncherApps.EXTRA_PIN_ITEM_REQUEST) ;
				if((pinRequest != null) && (pinRequest.getShortcutInfo() != null) && pinRequest.accept())
					{
						// If the request is invalid, display a message and quit
						ShortcutInfo receivedShortcut = pinRequest.getShortcutInfo() ;
						if((receivedShortcut.getShortLabel() != null))
							{
								// Retrive the informations of the shortcut
								String display_name = receivedShortcut.getShortLabel().toString() ;
								String user_id = receivedShortcut.getUserHandle().toString() ;
								String shortcut = display_name
										+ Constants.SHORTCUT_SEPARATOR + receivedShortcut.getPackage()
										+ Constants.SHORTCUT_SEPARATOR + receivedShortcut.getId()
										+ Constants.SHORTCUT_SEPARATOR + user_id.replace("UserHandle{", "").replace("}", "") ;

								// Check if the launcher is allowed to retrieve the shortcut icon
								Bitmap icon = null ;
								LauncherApps launcher = (LauncherApps)getSystemService(Context.LAUNCHER_APPS_SERVICE) ;
								if(launcher.hasShortcutHostPermission())
									{
										// If its dimensions are valid, create a Bitmap from the icon
										Drawable shortcutIcon = launcher.getShortcutIconDrawable(receivedShortcut, 0) ;
										if((shortcutIcon.getIntrinsicWidth() > 0) && (shortcutIcon.getIntrinsicHeight() > 0))
											{
												icon = Bitmap.createBitmap(shortcutIcon.getIntrinsicWidth(), shortcutIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888) ;
												shortcutIcon.setBounds(0, 0, shortcutIcon.getIntrinsicWidth(), shortcutIcon.getIntrinsicHeight()) ;
												shortcutIcon.draw(new Canvas(icon)) ;
											}
									}
									else ShowDialog.toastLong(this, getString(R.string.error_shortcut_not_default_launcher)) ;

								// Add the shortcut
								addShortcut(this, display_name, shortcut, icon, false) ;
								ActivityMain.updateList(this) ;
							}
							else ShowDialog.toastLong(this, getString(R.string.error_shortcut_invalid_request)) ;
					}
			}

		// Go back to the home screen
		Intent homeScreenIntent = new Intent() ;
		homeScreenIntent.setClass(this, ActivityMain.class) ;
		homeScreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
		startActivity(homeScreenIntent) ;
	}


	/**
	 * Method called when a request to add a shortcut has been received.
	 * @param context Provided by the receiver
	 * @param display_name Displayed to the user
	 * @param icon Displayed to the user
	 * @param shortcut Line to add to the shortcuts file
	 * @param legacy <code>true</code> if before Oreo, <code>false</code> otherwise
	 */
	static void addShortcut(Context context, String display_name, String shortcut, Bitmap icon, boolean legacy)
	{
		// Check if the shortcut already exists in the file
		InternalFileTXT file = new InternalFileTXT(legacy ? Constants.SHORTCUTS_LEGACY_FILE : Constants.SHORTCUTS_FILE) ;
		if(file.exists())
			{
				// Browse all the saved shortcuts
				String[] saved_shortcut ;
				for(String shortcut_line : file.readAllLines())
				{
					// Do not continue if the shortcut already exists
					saved_shortcut = shortcut_line.split(Constants.SHORTCUT_SEPARATOR) ;
					if(display_name.equals(saved_shortcut[0])) return ;
				}
			}

		// If it was not existing, add the shortcut to the file and save its icon
		InternalFilePNG icon_file = new InternalFilePNG(Constants.SHORTCUT_ICON_PREFIX + display_name + ".png") ;
		if(!file.writeLine(shortcut) || !icon_file.writeToFile(icon))
			ShowDialog.alert(context, context.getString(R.string.error_shortcut, display_name)) ;
	}


	/**
	 * Remove an entry from the shortcuts file.
	 * @param context To get the file path
	 * @param display_name Name of the shortcut to remove
	 * @param shortcut_type Shortcut before or after Oreo
	 */
	public static void removeShortcut(Context context, String display_name, String shortcut_type)
	{
		// Save the current shortcuts list and remove the file
		InternalFileTXT file = new InternalFileTXT(shortcut_type.equals(Constants.APK_SHORTCUT_LEGACY) ? Constants.SHORTCUTS_LEGACY_FILE : Constants.SHORTCUTS_FILE) ;
		ArrayList<String> currentShortcuts = file.readAllLines() ;
		if(!file.remove())
			{
				ShowDialog.toastLong(context, context.getString(R.string.error_remove_file, file.getName())) ;
				return ;
			}

		// Write the new shortcuts list in the file
		String[] shortcut ;
		for(String shortcut_line : currentShortcuts)
		{
			// Extract the display name from the line and check if this is the shortcut to remove
			shortcut = shortcut_line.split(Constants.SHORTCUT_SEPARATOR) ;
			if(shortcut[0].equals(display_name)) continue ;

			// Add all the other shortcuts to the list again
			if(!file.writeLine(shortcut_line))
				{
					ShowDialog.toastLong(context, context.getString(R.string.error_shortcut, shortcut[0])) ;
					return ;
				}
		}

		// Remove the shortcut icon
		InternalFilePNG icon = new InternalFilePNG(Constants.SHORTCUT_ICON_PREFIX + display_name + ".png") ;
		icon.remove() ;
	}
}
