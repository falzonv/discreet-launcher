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
import android.content.Context ;
import android.widget.Toast ;
import androidx.appcompat.app.AlertDialog ;

/**
 * Provide methods to display toasts and alert dialogs.
 */
abstract class ShowDialog
{
	/**
	 * Display an R.string message in a Toast for a short duration.
	 * @param context Provided by an activity
	 * @param message The message to display
	 */
	public static void toast(Context context, int message)
	{
		if(context == null) return ;
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show() ;
	}


	/**
	 * Display a message in a Toast for a long duration.
	 * @param context Provided by an activity
	 * @param message The message to display
	 */
	public static void toastLong(Context context, String message)
	{
		if(context == null) return ;
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show() ;
	}


	/**
	 * Display a message in an Alert Dialog box with a single "Close" button.
	 * @param context Provided by an activity
	 * @param message The message to display
	 */
	public static void alert(Context context, String message)
	{
		if(context == null) return ;
		AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
		dialog.setMessage(message) ;
		dialog.setNeutralButton(R.string.button_close, null) ;
		dialog.show() ;
	}
}
