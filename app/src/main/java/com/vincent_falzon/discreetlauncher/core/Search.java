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
import android.content.Context ;
import android.content.Intent ;
import android.graphics.drawable.Drawable ;
import android.view.View ;
import com.vincent_falzon.discreetlauncher.ActivitySearch ;
import com.vincent_falzon.discreetlauncher.Constants ;

/**
 * Represent the search application.
 */
public class Search extends Application
{
	/**
	 * Constructor to represent the search application.
	 * @param display_name Displayed to the user
	 * @param icon Displayed to the user
	 */
	public Search(String display_name, Drawable icon)
	{
		super(display_name, Constants.APK_SEARCH, Constants.APK_SEARCH, icon) ;
	}


	/**
	 * Start the search application.
	 * @param parent Element from which the event originates
	 * @return Always <code>true</code>
	 */
	public boolean start(View parent)
	{
		Context context = parent.getContext() ;
		context.startActivity(new Intent().setClass(context, ActivitySearch.class)) ;
		return true ;
	}
}
