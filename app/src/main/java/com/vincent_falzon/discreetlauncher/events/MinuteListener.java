package com.vincent_falzon.discreetlauncher.events;

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
import android.content.IntentFilter;
import android.widget.TextView ;
import java.text.SimpleDateFormat ;
import java.util.Date ;

/**
 * Listen for every minute on the system clock.
 */
public class MinuteListener extends BroadcastReceiver
{
	// Attributes
	private final TextView clockText ;


	/**
	 * Constructor to update the clock TextView every minute (if the option is selected).
	 * @param view The TextView representing the clock
	 */
	public MinuteListener(TextView view)
	{
		// Display the clock forcing a "HH:mm" format
		clockText = view ;
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm") ;
		clockText.setText(clockFormat.format(new Date())) ;
	}


	/**
	 * Provide the filter to use when registering this receiver.
	 * @return An IntentFilter allowing to listen for every minute on the system clock
	 */
	public IntentFilter getFilter()
	{
		return new IntentFilter(Intent.ACTION_TIME_TICK) ;
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
				clockText.setText(clockFormat.format(new Date())) ;
			}
	}
}
