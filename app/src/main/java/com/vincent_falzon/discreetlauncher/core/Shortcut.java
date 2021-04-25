package com.vincent_falzon.discreetlauncher.core ;

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
import android.content.Intent ;
import android.graphics.drawable.Drawable ;
import android.view.View ;
import com.vincent_falzon.discreetlauncher.Constants ;
import java.net.URISyntaxException ;

/**
 * Represent a web shortcut with its name and icon.
 */
public class Shortcut extends Application
{
	/**
	 * Constructor to represent a shortcut
	 * @param display_name Displayed to the user
	 * @param name Application name used internally
	 * @param apk Package name used internally
	 * @param icon Displayed to the user
	 */
	public Shortcut(String display_name, String name, String apk, Drawable icon)
	{
		super(display_name, name, apk, icon) ;
	}


	/**
	 * Get the specific activity intent.
	 * @return An intent specially created to launch this activity as a new task
	 */
	public Intent getActivityIntent()
	{
		// If the application is a shortcut before Oreo, return its intent (cannot be null in practice)
		if(apk.equals(Constants.APK_SHORTCUT_LEGACY))
			{
				try { return Intent.parseUri(name, 0) ; }
				catch(URISyntaxException e) { return null ; }
			}

		// If the application is a shortcut with Oreo or higher, create a special Intent
		Intent intent = new Intent() ;
		intent.setClassName("com.vincent_falzon.discreetlauncher", "com.vincent_falzon.discreetlauncher.events.ShortcutListener") ;
		intent.putExtra(Constants.APK_SHORTCUT, name) ;
		return intent ;
	}


	/**
	 * Start the web shortcut as a new task.
	 * @param view Element from which the event originates
	 * @return <code>true</code> if the application was found, <code>false</code> otherwise
	 */
	public boolean start(View view)
	{
		view.getContext().startActivity(getActivityIntent()) ;
		return true ;
	}
}
