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
import androidx.appcompat.app.AlertDialog ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Display a multi-selection dialog allowing to select applications that should be hidden.
 */
public abstract class DialogHiddenApps
{
	/**
	 * Display a multi-selection dialog allowing to select applications that should be hidden.
	 */
	public static void showHiddenAppsDialog(final Context context)
	{
		// Prepare the list of applications
		if(context == null) return ;
		final ArrayList<Application> applications = new ArrayList<>(ActivityMain.getApplicationsList().getHidden()) ;
		for(Application application : ActivityMain.getApplicationsList().getApplications(false))
		{
			// Never hide the Discreet Launcher icon (as it can be the only access to the menu)
			if(application.getApk().equals(context.getPackageName())) continue ;
			applications.add(application) ;
		}

		// List the names of all applications
		CharSequence[] app_names = new CharSequence[applications.size()] ;
		int i = 0 ;
		for(Application application : applications)
		{
			if(application instanceof Folder) app_names[i] = ((Folder)application).getDisplayNameWithCount() ;
				else app_names[i] = application.getDisplayName() ;
			i++ ;
		}

		// Retrieve the currently selected applications
		final InternalFileTXT file = new InternalFileTXT(Constants.FILE_HIDDEN) ;
		final boolean[] selected = new boolean[app_names.length] ;
		if(file.exists())
				for(i = 0 ; i < app_names.length ; i++)
					selected[i] = file.isLineExisting(applications.get(i).getComponentInfo()) ;
			else for(i = 0 ; i < app_names.length ; i++) selected[i] = false ;

		// Prepare and display the selection dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
		dialog.setTitle(context.getString(R.string.menu_hidden_apps)) ;
		dialog.setMultiChoiceItems(app_names, selected, (dialogInterface, position, checked) -> { }) ;
		dialog.setPositiveButton(R.string.button_apply, (dialogInterface, which) -> {
				// Remove the current file
				if(!file.remove()) return ;

				// Write the new selected applications to the file
				for(int j = 0 ; j < selected.length ; j++)
					if(selected[j]) file.writeLine(applications.get(j).getComponentInfo()) ;

				// Refresh the list of apps and go back to the home screen
				ActivityMain.updateList(context) ;
				Intent homeIntent = new Intent() ;
				homeIntent.setClass(context, ActivityMain.class) ;
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
				context.startActivity(homeIntent) ;
			}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.show() ;
	}
}
