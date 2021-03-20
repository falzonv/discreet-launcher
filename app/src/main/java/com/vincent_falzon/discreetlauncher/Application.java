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
import android.content.Intent ;
import android.content.pm.PackageManager ;
import android.graphics.drawable.Drawable ;

/**
 * Represent an Android application with its names (displayed, internal and package) and icon.
 */
class Application
{
	// Attributes
	private final String display_name ;
	private final String name ;
	private final String apk ;
	private final Drawable icon ;


	/**
	 * Constructor to represent an Android application.
	 * @param display_name Displayed to the user
	 * @param name Application name used internally
	 * @param apk Package name used internally
	 * @param icon Displayed to the user
	 */
	Application(String display_name, String name, String apk, Drawable icon)
	{
		this.display_name = display_name ;
		this.name = name ;
		this.apk = apk ;
		this.icon = icon ;
	}


	/**
	 * Get the disply name of the application.
	 * @return Name displayed in the menus
	 */
	String getDisplayName()
	{
		return display_name ;
	}


	/**
	 * Get the internal name of the application.
	 * @return Application name used internally
	 */
	String getName()
	{
		return name ;
	}


	/**
	 * Get the package name of the application.
	 * @return Package name used internally
	 */
	String getApk()
	{
		return apk ;
	}


	/**
	 * Get the icon of the application.
	 * @return Icon displayed in the menus
	 */
	Drawable getIcon()
	{
		return icon ;
	}


	/**
	 * Start the application as a new task.
	 * @param context Provided by an activity
	 */
	void start(Context context)
	{
		// Check if the application still exists (not uninstalled or disabled)
		PackageManager apkManager = context.getPackageManager() ;
		Intent package_intent = apkManager.getLaunchIntentForPackage(apk) ;
		if(package_intent == null)
		{
			// Display an error message and quit
			ShowDialog.alert(context, context.getString(R.string.error_application_not_found, apk)) ;
			return ;
		}

		// Try to launch the specific intent of the application
		Intent activity_intent = new Intent(Intent.ACTION_MAIN) ;
		activity_intent.addCategory(Intent.CATEGORY_LAUNCHER) ;
		activity_intent.setClassName(apk, name) ;
		activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
		if(activity_intent.resolveActivity(apkManager) != null)
		{
			context.startActivity(activity_intent) ;
			return ;
		}

		// If it was not found, launch the default intent of the package
		package_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
		context.startActivity(package_intent) ;
	}
}
