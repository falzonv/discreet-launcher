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
import java.net.URISyntaxException ;

/**
 * Represent an Android application with its names (displayed, internal and package) and icon.
 */
public class Application
{
	// Constants
	public static final String APK_SHORTCUT = "discreetlauncher.shortcut" ;
	public static final String APK_SHORTCUT_LEGACY = "discreetlauncher.shortcut_legacy" ;
	public static final String NOTIFICATION_SEPARATOR = "_discreet_" ;
	public static final String SHORTCUT_SEPARATOR = "--SHORT--CUT--" ;

	// Attributes
	private final String display_name ;
	private final String name ;
	private final String apk ;
	private final Drawable icon ;


	/**
	 * Constructor to represent an Android application or a shortcut
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
	 * Get the specific activity intent.
	 * @return An intent specially created to launch this activity as a new task
	 */
	Intent getActivityIntent()
	{
		// If the application is a shortcut with Oreo or higher, create a special Intent
		if(apk.equals(APK_SHORTCUT))
			{
				Intent intent = new Intent() ;
				intent.setClassName("com.vincent_falzon.discreetlauncher", "com.vincent_falzon.discreetlauncher.events.ShortcutListener") ;
				intent.putExtra(Application.APK_SHORTCUT, name) ;
				return intent ;
			}

		// If the application is a shortcut before Oreo, return its intent (cannot be null in practice)
		if(apk.equals(APK_SHORTCUT_LEGACY))
			{
				try { return Intent.parseUri(name, 0) ; }
				catch(URISyntaxException e) { return null ; }
			}

		// For a standard application, create a proper intent
		Intent intent = new Intent(Intent.ACTION_MAIN) ;
		intent.addCategory(Intent.CATEGORY_LAUNCHER) ;
		intent.setClassName(apk, name) ;
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
		return intent ;
	}


	/**
	 * Start the application as a new task.
	 * @param context Provided by an activity
	 */
	void start(Context context)
	{
		// Check if the application is a shortcut
		if(apk.startsWith(APK_SHORTCUT))
			{
				context.startActivity(getActivityIntent()) ;
				return ;
			}

		// Check if the application still exists (not uninstalled or disabled)
		PackageManager apkManager = context.getPackageManager() ;
		Intent packageIntent = apkManager.getLaunchIntentForPackage(apk) ;
		if(packageIntent == null)
			{
				// Display an error message and quit
				ShowDialog.alert(context, context.getString(R.string.error_application_not_found, name)) ;
				return ;
			}

		// Try to launch the specific intent of the application
		Intent activityIntent = getActivityIntent() ;
		if(activityIntent.resolveActivity(apkManager) != null)
			{
				context.startActivity(activityIntent) ;
				return ;
			}

		// If it was not found, launch the default intent of the package
		packageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
		context.startActivity(packageIntent) ;
	}
}
