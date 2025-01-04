package com.vincent_falzon.discreetlauncher.quickaccess ;

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
import android.app.Activity ;
import android.os.Bundle ;
import androidx.appcompat.app.AppCompatActivity ;

/**
 * Create a shortcut to display the favorites popup.
 */
public class ShortcutCreator extends AppCompatActivity
{
	/**
	 * Constructor.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Create the shortcut and close the activity
		setResult(Activity.RESULT_OK, PopupFavorites.getIntent(this)) ;
		finish() ;
	}
}
