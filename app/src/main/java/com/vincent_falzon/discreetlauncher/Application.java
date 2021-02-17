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
import android.graphics.drawable.Drawable ;

/**
 * Represent an Android application with a name, an APK identifier and an icon.
 */
class Application
{
	// Attributes
	private final String name ;
	private final String apk ;
	private final Drawable icon ;


	/**
	 * Constructor to represent an Android application.
	 * @param name Displayed to the user
	 * @param apk Used internally for intents
	 * @param icon Displayed to the user
	 */
	Application(String name, String apk, Drawable icon)
	{
		this.name = name ;
		this.apk = apk ;
		this.icon = icon ;
	}


	/**
	 * Get the name of the application.
	 * @return Name displayed in the menus
	 */
	String getName()
	{
		return name ;
	}


	/**
	 * Get the APK identifier of the application.
	 * @return Used internally to start intents
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
}
