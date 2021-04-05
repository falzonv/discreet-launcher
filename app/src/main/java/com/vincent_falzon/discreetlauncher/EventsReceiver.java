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
import android.content.BroadcastReceiver ;
import android.content.Context ;
import android.content.Intent ;
import android.os.Build ;
import android.widget.TextView ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

/**
 * Receive broadcast events and react accordingly when needed.
 */
class EventsReceiver extends BroadcastReceiver
{
	// Attributes
	private final TextView clockText ;


	/**
	 * Constructor to update the applications list when there is a change.
	 */
	EventsReceiver()
	{
		clockText = null ;
	}


	/**
	 * Constructor to update the clock TextView every minute (if the option is selected).
	 * @param view The TextView representing the clock
	 */
	EventsReceiver(TextView view)
	{
		// Display the clock forcing a "HH:mm" format
		clockText = view ;
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm") ;
		clockText.setText(clockFormat.format(new Date())) ;
	}


	/**
	 * Method called when a broadcast message is received.
	 * @param context Context of the message.
	 * @param intent Type and content of the message.
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Check if the intent as a valid action
		if(intent.getAction() == null) return ;

		// Check if the clock must be updated
		if((clockText != null) && intent.getAction().equals(Intent.ACTION_TIME_TICK))
			{
				// Update the clock forcing a "HH:mm" format
				@SuppressLint("SimpleDateFormat")
				SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm") ;
				clockText.setText(clockFormat.format(new Date())) ; ;
				return ;
			}

		// Check if a package has been added or removed (except during updates)
		if((intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
			&& !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
			{
				// Update the applications list
				ActivityMain.getApplicationsList().update(context) ;
				ActivityMain.setAdapterUpdateNeeded() ;
				ActivityDrawer.setAdapterUpdateNeeded() ;
			}

		// Execute the following code only if the Android version is before Oreo
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
			{
				// Check if a request to add a shortcut has been received
				if(intent.getAction().equals("com.android.launcher.action.INSTALL_SHORTCUT"))
					{
						// Retrive the name and intent of the shortcut
						String display_name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME) ;
						Intent shortcutIntent = (Intent)intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT) ;

						// If the request is invalid, display a message and quit
						if((display_name == null) || (shortcutIntent == null))
							{
								ShowDialog.alert(context, context.getString(R.string.error_shortcut_invalid_request)) ;
								return ;
							}

						// Add the shortcut and update the applications list
						String shortcut = display_name + Application.SHORTCUT_SEPARATOR + shortcutIntent.toUri(0) ;
						ActivityMain.getApplicationsList().addShortcut(context, display_name, shortcut, true) ;
						ActivityMain.setAdapterUpdateNeeded() ;
						ActivityDrawer.setAdapterUpdateNeeded() ;
					}
			}
	}
}
