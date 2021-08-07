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
import android.content.DialogInterface ;
import android.content.Intent ;
import android.graphics.drawable.Drawable ;
import android.view.View ;
import androidx.appcompat.app.AlertDialog ;
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
		// Prepare the selection dialog
		final Context context = view.getContext() ;
		AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
		dialog.setTitle(context.getString(R.string.app_name)) ;
		CharSequence[] options = {
				context.getString(R.string.button_favorites),
				context.getString(R.string.button_folders),
				context.getString(R.string.button_refresh_list),
				context.getString(R.string.title_settings_and_help)
			} ;

		// Assign actions to options and display the dialog
		dialog.setItems(options,
			new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int selection)
				{
					// Hide popups if some are still opened
					for(Application application : ActivityMain.getApplicationsList().getDrawer())
					{
						if(application instanceof Folder) ((Folder)application).closePopup() ;
						if(application instanceof Search) ((Search)application).closePopup() ;
					}

					// Check which option has been selected
					switch(selection)
					{
						case 0 :
							// Manage favorites
							context.startActivity(new Intent().setClass(context, ActivityFavorites.class)) ;
							break ;
						case 1 :
							// Organize folders
							context.startActivity(new Intent().setClass(context, ActivityFolders.class)) ;
							break ;
						case 2 :
							// Refresh the applications list (and go back to the home screen)
							ActivityMain.updateList(context) ;
							Intent homeIntent = new Intent() ;
							homeIntent.setClass(context, ActivityMain.class) ;
							homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
							context.startActivity(homeIntent) ;
							break ;
						case 3 :
							// Settings / Help
							context.startActivity(new Intent().setClass(context, ActivitySettings.class)) ;
							break ;
					}
				}
			}) ;
		dialog.show() ;
	}
}
