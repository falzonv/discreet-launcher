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
import android.graphics.drawable.Drawable ;
import android.view.View ;
import com.vincent_falzon.discreetlauncher.menu.DialogMenu ;

/**
 * Assign the main menu to the Discreet Launcher icon.
 */
public class Menu extends Application
{
	/**
	 * Constructor.
	 */
	public Menu(String display_name, String name, String apk, Drawable icon)
	{
		super(display_name, name, apk, icon, null) ;
	}


	/**
	 * Display the main menu.
	 */
	public boolean start(View view)
	{
		new DialogMenu(view.getContext()).show() ;
		return true ;
	}
}
