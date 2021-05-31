package com.vincent_falzon.discreetlauncher.notification ;

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
import android.widget.RemoteViews ;
import androidx.core.app.NotificationCompat ;
import com.vincent_falzon.discreetlauncher.R ;

/**
 * Display or hide the Android notification giving access to the favorites applications.
 */
public class NotificationDisplayer
{
	// Attributes
	private final NotificationManager manager ;


	/**
	 * Constructor to build the notification.
	 * @param context Provided by an activity
	 */
	public NotificationDisplayer(Context context)
	{
		// Initialization
		manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE) ;

		// If the Android version is Oreo or higher, create the notification channel
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				NotificationChannel channel = new NotificationChannel("Discreet Launcher", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_LOW) ;
				channel.setSound(null, null) ;
				channel.setVibrationPattern(null) ;
				channel.setShowBadge(false) ;
				manager.createNotificationChannel(channel) ;

				// Delete the former notification channel (to remove after 31/08/2021)
				manager.deleteNotificationChannel("discreetlauncher") ;
			}
	}


	/**
	 * Display the notification.
	 * @param context Provided by an activity
	 */
	public void display(Context context)
	{
		// Define the notification settings
		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Discreet Launcher") ;
		builder.setSmallIcon(R.drawable.notification_icon) ;
		builder.setShowWhen(false) ; // Hide the notification timer
		builder.setOngoing(true) ;   // Sticky notification
		builder.setVisibility(NotificationCompat.VISIBILITY_SECRET) ; // Hidden on lock screen

		// Define a custom layout for the notification
		RemoteViews notificationLayout = new RemoteViews(context.getPackageName(), R.layout.notification) ;
		builder.setCustomContentView(notificationLayout) ;

		// Settings applied when the Android version doesn't support notification channels
		builder.setPriority(NotificationCompat.PRIORITY_LOW) ;
		builder.setSound(null) ;
		builder.setVibrate(null) ;

		// Prepare the intent to display the favorites popup
		Intent intent = new Intent(Intent.ACTION_MAIN) ;
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP) ;
		intent.setClassName(context.getPackageName(), context.getPackageName() + ".notification.ActivityNotification") ;

		// Define the notification action
		PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0) ;
		builder.setContentIntent(pendingIntent) ;

		// Display the notification
		manager.notify(1, builder.build()) ;
	}


	/**
	 * Hide the notification.
	 */
	public void hide()
	{
		manager.cancel(1) ;
	}
}
