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
import android.view.MenuItem ;
import android.view.View ;
import android.widget.PopupMenu ;
import com.vincent_falzon.discreetlauncher.ActivityFavorites ;
import com.vincent_falzon.discreetlauncher.ActivityFolders ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.ShowDialog ;
import com.vincent_falzon.discreetlauncher.settings.ActivitySettings ;

/**
 * Display the launcher menu.
 */
public class Menu extends Application
{
	/**
	 * Constructor to set the menu on the Discreet Launcher icon.
	 * @param display_name Displayed to the user
	 * @param name Application name used internally
	 * @param apk Package name used internally
	 * @param icon Displayed to the user
	 */
	public Menu(String display_name, String name, String apk, Drawable icon)
	{
		super(display_name, name, apk, icon) ;
	}


	/**
	 * Display the menu.
	 * @param view Element from which the event originates
	 * @return Always <code>true</code>
	 */
	public boolean start(View view)
	{
		open(view) ;
		return true ;
	}


	/**
	 * Open the launcher menu.
	 * @param view Element from which the event originates
	 */
	public static void open(View view)
	{
		// Create the popup menu
		final Context context = view.getContext() ;
		PopupMenu popupMenu = new PopupMenu(context, view) ;
		popupMenu.inflate(R.menu.menu) ;

		// Start to listen for clicks on menu items
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				// Identify which menu entry has been clicked
				int selection = item.getItemId() ;

				// Check if the applications list should be refreshed
				if(selection == R.id.menu_action_refresh_list)
					{
						ActivityMain.updateList(context) ;
						Intent homeIntent = new Intent() ;
						homeIntent.setClass(context, ActivityMain.class) ;
						homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
						context.startActivity(homeIntent) ;
					}
					// Check if another activity should be started
					else if(selection == R.id.menu_action_manage_favorites) context.startActivity(new Intent().setClass(context, ActivityFavorites.class)) ;
					else if(selection == R.id.menu_action_organize_folders) context.startActivity(new Intent().setClass(context, ActivityFolders.class)) ;
					else if(selection == R.id.menu_action_settings) context.startActivity(new Intent().setClass(context, ActivitySettings.class)) ;
					// Check if the dialog to hide applications should be displayed
					else if(selection == R.id.menu_action_hide_applications) ShowDialog.hideApplications(context) ;
					// In other cases, ignore the click
					else return false ;

				// Indicate that the event has been consumed
				return true ;
			}
		}) ;

		// Display the menu
		popupMenu.show() ;
	}
}
