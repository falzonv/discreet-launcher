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
import android.content.ActivityNotFoundException ;
import android.content.Context ;
import android.content.Intent ;
import android.content.pm.LauncherApps ;
import android.content.pm.ShortcutInfo ;
import android.os.Build ;
import android.os.Bundle ;
import android.os.UserHandle ;
import androidx.appcompat.app.AppCompatActivity ;

/**
 * Activity called to add a shortcut (starting with Android Oreo).
 */
public class ActivityShortcut extends AppCompatActivity
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
				if(intent.getExtras().getString(Application.APK_SHORTCUT) != null)
					{
						// Retrieve the shortcut details
						String[] shortcut = intent.getExtras().getString(Application.APK_SHORTCUT).split(Application.SHORTCUT_SEPARATOR) ;
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
										+ Application.SHORTCUT_SEPARATOR + receivedShortcut.getPackage()
										+ Application.SHORTCUT_SEPARATOR + receivedShortcut.getId()
										+ Application.SHORTCUT_SEPARATOR + user_id.replace("UserHandle{", "").replace("}", "") ;
								ActivityMain.getApplicationsList().addShortcut(this, display_name, shortcut, false) ;
							}
							else ShowDialog.toastLong(this, getString(R.string.error_shortcut_invalid_request)) ;
					}
			}

		// Go back to the previous activity
		finish() ;
	}
}
