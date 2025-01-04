package com.vincent_falzon.discreetlauncher.core ;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2025 Vincent Falzon

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
import android.content.ComponentName ;
import android.content.Context ;
import android.content.Intent ;
import android.content.pm.LauncherApps ;
import android.content.pm.PackageManager ;
import android.graphics.drawable.Drawable ;
import android.net.Uri ;
import android.os.UserHandle ;
import android.view.View ;

/**
 * Represent an Android application (userHandle is used to identify work profile apps).
 */
public class Application
{
	// Attributes
	String display_name ;
	String name ;
	String component_info ;
	final String apk ;
	final UserHandle userHandle ;
	Drawable icon ;


	/**
	 * Constructor.
	 */
	public Application(String display_name, String name, String apk, Drawable icon, UserHandle userHandle)
	{
		// Initializations
		this.display_name = display_name ;
		this.name = name ;
		this.apk = apk ;
		this.icon = icon ;
		this.userHandle = userHandle ;

		// Build the ComponentInfo
		component_info = "{" + apk + "/" + name + "}" ;
		if(userHandle != null)
			component_info += userHandle.toString() ;
	}


	/**
	 * Return the disply name of the application.
	 */
	public String getDisplayName()
	{
		return display_name ;
	}


	/**
	 * Set the display name of the application.
	 */
	public void setDisplayName(String new_name)
	{
		display_name = new_name ;
	}


	/**
	 * Return the internal name of the application.
	 */
	public String getName()
	{
		return name ;
	}


	/**
	 * Return the package name of the application.
	 */
	public String getApk()
	{
		return apk ;
	}


	/**
	 * Return the ComponentInfo{package/name} of the application.
	 */
	public String getComponentInfo()
	{
		return component_info ;
	}


	/**
	 * Return the icon of the application.
	 */
	public Drawable getIcon()
	{
		return icon ;
	}


	/**
	 * Set a new icon for the application.
	 */
	public void setIcon(Drawable new_icon)
	{
		icon = new_icon ;
	}


	/**
	 * Start the application as a new task.
	 * @return <code>true</code> if the application was found, <code>false</code> otherwise
	 */
	public boolean start(View view)
	{
		try
		{
			// Check if the application is in a work profile
			Context context = view.getContext() ;
			if(userHandle != null)
				{
					// Try to launch the work profile application
					LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE) ;
					launcherApps.startMainActivity(new ComponentName(apk, name), userHandle, null, null) ;
					return true ;
				}

			// Applications not in work profiles can be launched directly with Intents like below.
			// This is preferred as it allows to resume the application to its previous state.
			// (Not possible with LauncherApps which sets the FLAG_ACTIVITY_RESET_TASK_IF_NEEDED.)

			// Check if the application still exists (not removed or disabled)
			PackageManager apkManager = context.getPackageManager() ;
			Intent packageIntent = apkManager.getLaunchIntentForPackage(apk) ;
			if(packageIntent == null) return false ;

			// Try to launch the specific intent of the application
			Intent activityIntent = new Intent(Intent.ACTION_MAIN) ;
			activityIntent.addCategory(Intent.CATEGORY_LAUNCHER) ;
			activityIntent.setClassName(apk, name) ;
			activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
			if(activityIntent.resolveActivity(apkManager) != null) context.startActivity(activityIntent) ;
				else
				{
					// If it was not found, launch the default intent of the package
					packageIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
					context.startActivity(packageIntent) ;
				}
			return true ;
		}
		catch(Exception exception)
		{
			// An error happened (ex: application not found)
			return false ;
		}
	}


	/**
	 * Open the application system settings.
	 */
	public void showSettings(Context context)
	{
		// Check if the application is in a work profile
		if(userHandle != null)
			{
				// Open the application system settings
				LauncherApps launcherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE) ;
				launcherApps.startAppDetailsActivity(new ComponentName(apk, name), userHandle, null, null) ;
				return ;
			}

		// Open the application system settings
		Intent settingsIntent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS) ;
		settingsIntent.setData(Uri.parse("package:" + apk)) ;
		settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
		context.startActivity(settingsIntent) ;
	}
}
