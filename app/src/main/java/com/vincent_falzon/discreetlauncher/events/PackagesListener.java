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
import android.content.BroadcastReceiver ;
import android.content.Context ;
import android.content.Intent ;
import android.content.IntentFilter ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;

/**
 * Listen for packages additions and deletions.
 */
public class PackagesListener extends BroadcastReceiver
{
	/**
	 * Provide the filter to use when registering this receiver.
	 * @return An IntentFilter allowing to listen for packages events
	 */
	public IntentFilter getFilter()
	{
		IntentFilter filter = new IntentFilter() ;
		filter.addAction(Intent.ACTION_PACKAGE_ADDED) ;
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED) ;
		filter.addDataScheme("package") ;
		return filter ;
	}


	/**
	 * Method called when a broadcast message is received.
	 * @param context Context of the message
	 * @param intent Type and content of the message
	 */
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// Check if the intent as a valid action
		if(intent.getAction() == null) return ;

		// Check if a package has been added or removed (except during updates)
		if((intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED))
			&& !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false))
			{
				// Indicate that the applications list should be updated
				ActivityMain.setListUpdateNeeded() ;
			}
	}
}
