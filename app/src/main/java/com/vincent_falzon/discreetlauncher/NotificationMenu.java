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
import android.content.Intent ;
import android.os.Build ;
import androidx.core.app.NotificationCompat ;
import androidx.core.app.NotificationManagerCompat ;

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
		// Define the notification settings
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "discreetlauncher") ;
		builder.setSmallIcon(R.drawable.notification_icon) ;
		builder.setContentTitle(context.getString(R.string.info_favorites_access)) ;
		builder.setShowWhen(false) ;      // Hide the notification timer
		builder.setOngoing(true) ;        // Sticky notification
		builder.setNotificationSilent() ; // No sound or vibration
		builder.setPriority(NotificationCompat.PRIORITY_DEFAULT) ;    // Default priority
		builder.setVisibility(NotificationCompat.VISIBILITY_SECRET) ; // Hidden on lock screen

		// Define the notification action
		Intent intent = new Intent(Intent.ACTION_MAIN) ;
		intent.setClassName(context.getPackageName(), "com.vincent_falzon.discreetlauncher.events.NotificationListener") ;
		PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0) ;
		builder.setContentIntent(pendingIntent) ;

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
