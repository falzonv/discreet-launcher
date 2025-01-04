package com.vincent_falzon.discreetlauncher.menu ;

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
import android.content.Context ;
import android.content.Intent ;
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatDialog ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.settings.ActivityExportImport ;
import com.vincent_falzon.discreetlauncher.settings.ActivitySettingsAppearance ;
import com.vincent_falzon.discreetlauncher.settings.ActivitySettingsOperation ;

/**
 * Display the main menu of Discreet Launcher.
 */
public class DialogMenu extends AppCompatDialog implements View.OnClickListener
{
	/**
	 * Constructor.
	 */
	@SuppressWarnings({"RedundantCast", "RedundantSuppression"})
	public DialogMenu(Context context)
	{
		// Let the parent actions be performed
		super(context) ;

		// Load the XML layout
		View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_menu, (ViewGroup)null) ;
		setContentView(dialogView) ;

		// Initializations
		dialogView.findViewById(R.id.menu_favorites).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_folders).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_hidden_apps).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_refresh_list).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_settings_appearance).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_settings_operation).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_export_import).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_help).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_about).setOnClickListener(this) ;
		dialogView.findViewById(R.id.menu_changelog).setOnClickListener(this) ;
	}


	/**
	 * Called when an element is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		// Retrieve the clicked element
		int selection = view.getId() ;
		Context context = view.getContext() ;

		// Perform the related actions
		if(selection == R.id.menu_favorites)
			{
				// Open the Favorites activity
				context.startActivity(new Intent().setClass(context, ActivityFavorites.class)) ;
			}
			else if(selection == R.id.menu_folders)
			{
				// Open the Folders activity
				context.startActivity(new Intent().setClass(context, ActivityFolders.class)) ;
			}
			else if(selection == R.id.menu_hidden_apps)
			{
				// Open the Hidden apps dialog
				DialogHiddenApps.showHiddenAppsDialog(context) ;
			}
			else if(selection == R.id.menu_refresh_list)
			{
				// Refresh the list of apps and go back to the home screen
				ActivityMain.updateList(context) ;
				Intent homeIntent = new Intent() ;
				homeIntent.setClass(context, ActivityMain.class) ;
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
				context.startActivity(homeIntent) ;
			}
			else if(selection == R.id.menu_settings_appearance)
			{
				// Open the Settings > Appearance activity
				context.startActivity(new Intent().setClass(context, ActivitySettingsAppearance.class)) ;
			}
			else if(selection == R.id.menu_settings_operation)
			{
				// Open the Settings > Operation activity
				context.startActivity(new Intent().setClass(context, ActivitySettingsOperation.class)) ;
			}
			else if(selection == R.id.menu_export_import)
			{
				// Open the Export / Import activity
				context.startActivity(new Intent().setClass(context, ActivityExportImport.class)) ;
			}
			else if(selection == R.id.menu_help)
			{
				// Display the Help without dismissing the menu
				AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
				dialog.setView(R.layout.dialog_help) ;
				dialog.setPositiveButton(R.string.button_close, null) ;
				dialog.show() ;
				return ;
			}
			else if(selection == R.id.menu_about)
			{
				// Display the About without dismissing the menu
				AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
				dialog.setView(R.layout.dialog_about) ;
				dialog.setPositiveButton(R.string.button_close, null) ;
				dialog.show() ;
				return ;
			}
			else if(selection == R.id.menu_changelog)
			{
				// Display the Changelog without dismissing the menu
				DialogChangelog dialog = new DialogChangelog(context) ;
				dialog.show() ;
				return ;
			}

		// Dismiss the menu
		dismiss() ;
	}
}
