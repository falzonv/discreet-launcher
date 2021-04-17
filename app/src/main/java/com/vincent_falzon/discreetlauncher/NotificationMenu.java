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
import android.app.NotificationChannel ;
import android.app.NotificationManager ;
import android.app.PendingIntent ;
import android.content.Context ;
import android.content.SharedPreferences ;
import android.os.Build ;
import androidx.core.app.NotificationCompat ;
import androidx.core.app.NotificationManagerCompat ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.core.Application ;

/**
 * Display an Android notification with the favorites applications.
 */
class NotificationMenu
{
	// Attributes
	private final NotificationManagerCompat manager ;


	/**
	 * Constructor to build the notification.
	 * @param context Provided by an activity
	 */
	NotificationMenu(Context context)
	{
		// Initializations
		manager = NotificationManagerCompat.from(context) ;

		// If the Android version is Oreo or higher, create the notification channel
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				NotificationChannel channel = new NotificationChannel("discreetlauncher", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH) ;
				manager.createNotificationChannel(channel) ;
			}
	}


	/**
	 * Display the notification.
	 * @param context Provided by an activity
	 */
	void display(Context context)
	{
		// Retrieve the current settings
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context) ;

		// Define the notification settings
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "discreetlauncher") ;
		builder.setSmallIcon(R.drawable.notification_icon) ;
		builder.setContentTitle(context.getString(R.string.app_name)) ;
		builder.setContentTitle(settings.getString(Constants.NOTIFICATION_TEXT, context.getString(R.string.set_notification_text_default))) ;
		builder.setShowWhen(false) ;                           // Hide the notification timer
		builder.setOngoing(true) ;                             // Sticky notification
		builder.setPriority(NotificationCompat.PRIORITY_MAX) ; // Needed to display the buttons
		builder.setNotificationSilent() ;                      // No sound or vibration

		// Check if the notification should be displayed or not on the lock screen
		if(settings.getBoolean(Constants.HIDE_ON_LOCK_SCREEN, true))
			builder.setVisibility(NotificationCompat.VISIBILITY_SECRET) ;

		// Retrieve the selected applications and set them as actions
		for(int i = 0 ; i < 3 ; i++)
		{
			// Check if an application has been selected
			String application_set = settings.getString(Constants.NOTIFICATION_APP + (i + 1), Constants.NONE) ;
			if((application_set == null) || application_set.equals(Constants.NONE)) continue ;

			// Retrieve the applications details
			String[] application_details = application_set.split(Constants.NOTIFICATION_SEPARATOR) ;
			if(application_details.length != 3) continue ;

			// Add the notification as an action
			Application application = new Application(application_details[0], application_details[1], application_details[2], null) ;
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, application.getActivityIntent(), PendingIntent.FLAG_CANCEL_CURRENT) ;
			builder.addAction(0, application.getDisplayName(), pendingIntent) ;
		}

		// Display the notification
		manager.notify(1, builder.build()) ;
	}


	/**
	 * Hide the notification.
	 */
	void hide()
	{
		manager.cancel(1) ;
	}
}
