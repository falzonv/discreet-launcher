package com.vincent_falzon.discreetlauncher ;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2022 Vincent Falzon

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
import android.content.SharedPreferences ;
import android.widget.Toast ;
import com.vincent_falzon.discreetlauncher.settings.ColorPickerDialog ;

/**
 * Provide utility methods used in various places of Discreet Launcher.
 */
public abstract class Utils
{
	/**
	 * Display an R.string message in a Toast for a short duration.
	 */
	public static void displayToast(Context context, int message)
	{
		if(context == null) return ;
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show() ;
	}


	/**
	 * Display a message in a Toast for a long duration.
	 */
	public static void displayLongToast(Context context, String message)
	{
		if(context == null) return ;
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show() ;
	}


	/**
	 * Retrieve the currently selected color for the given preference key.
	 */
	public static int getColor(SharedPreferences settings, String key, String fallback)
	{
		// Try to load the color at the given key, or use the provided fallback
		String hexadecimal = settings.getString(key, Constants.NONE) ;
		if((hexadecimal == null) || hexadecimal.equals(Constants.NONE))
			hexadecimal = fallback ;

		// Convert the hexadecimal color to an "int" color
		return ColorPickerDialog.convertHexadecimalColorToInt(hexadecimal) ;
	}
}
