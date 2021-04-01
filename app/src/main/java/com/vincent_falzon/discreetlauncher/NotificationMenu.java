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

/**
 * Display an Android notification with the favorites applications.
 */
class NotificationMenu
{
	// Attributes
	public static final String CHANNEL_ID = "discreetlauncher" ;
	private final NotificationManagerCompat manager ;
	private final int notification_id ;


	/**
	 * Constructor to build the notification.
	 * @param context Provided by an activity
	 */
	NotificationMenu(Context context)
	{
		// Initializations
		manager = NotificationManagerCompat.from(context) ;
		notification_id = 1 ;

		// If the Android version is Oreo or higher, create the notification channel
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH) ;
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
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID) ;
		builder.setSmallIcon(R.drawable.notification_icon) ;
		builder.setContentTitle(context.getString(R.string.app_name)) ;
		builder.setContentTitle(settings.getString(ActivitySettings.NOTIFICATION_TEXT, context.getString(R.string.text_notification))) ;
		builder.setShowWhen(false) ;                           // Hide the notification timer
		builder.setOngoing(true) ;                             // Sticky notification
		builder.setPriority(NotificationCompat.PRIORITY_MAX) ; // Needed to display the buttons
		builder.setNotificationSilent() ;                      // No sound or vibration

		// Check if the notification should be displayed or not on the lock screen
		if(settings.getBoolean(ActivitySettings.HIDE_ON_LOCK_SCREEN, true))
			builder.setVisibility(NotificationCompat.VISIBILITY_SECRET) ;

		// Retrieve the selected applications
		Application[] app = ActivityMain.getApplicationsList().getNotificationApps() ;

		// Set the notification actions
		for(int i = 0 ; i < 3 ; i++)
		{
			if(app[i] == null) continue ;
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, app[i].getActivityIntent(), PendingIntent.FLAG_CANCEL_CURRENT) ;
			builder.addAction(0, app[i].getDisplayName(), pendingIntent) ;
		}

		// Display the notification
		manager.notify(notification_id, builder.build()) ;
	}


	/**
	 * Hide the notification.
	 */
	void hide()
	{
		manager.cancel(notification_id) ;
	}
}
