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
import android.content.Context ;
import android.content.DialogInterface ;
import android.widget.Toast ;
import androidx.appcompat.app.AlertDialog ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Provide methods to display toasts and alert dialogs.
 */
public abstract class ShowDialog
{
	/**
	 * Display an R.string message in a Toast for a short duration.
	 * @param context Provided by an activity
	 * @param message The message to display
	 */
	public static void toast(Context context, int message)
	{
		if(context == null) return ;
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_SHORT).show() ;
	}


	/**
	 * Display a message in a Toast for a long duration.
	 * @param context Provided by an activity
	 * @param message The message to display
	 */
	public static void toastLong(Context context, String message)
	{
		if(context == null) return ;
		Toast.makeText(context.getApplicationContext(), message, Toast.LENGTH_LONG).show() ;
	}


	/**
	 * Display an applications list in a multi-selection dialog with a title.
	 * @param context Provided by an activity
	 * @param title R.string representing the dialog title
	 * @param applications List of the choices
	 * @param filename Reference to pre-select applications
	 * @param max Maximum number of selections (-1 to disable)
	 */
	public static void multiSelect(final Context context, int title, final ArrayList<Application> applications, String filename, final int max)
	{
		// List the names of all applications
		if((context == null) || (applications == null)) return ;
		CharSequence[] app_names = new CharSequence[applications.size()] ;
		int i = 0 ;
		for(Application application : applications)
		{
			if(application instanceof Folder) app_names[i] = ((Folder)application).getDisplayNameWithCount() ;
				else app_names[i] = application.getDisplayName() ;
			i++ ;
		}

		// Retrieve the currently selected applications
		final InternalFileTXT file = new InternalFileTXT(filename) ;
		final boolean[] selected = new boolean[app_names.length] ;
		if(file.exists()) for(i = 0 ; i < app_names.length ; i++) selected[i] = file.isLineExisting(applications.get(i).getName()) ;
			else for(i = 0 ; i < app_names.length ; i++) selected[i] = false ;

		// Prepare the title
		String dialog_title = context.getString(title) ;
		if(max != -1) dialog_title += " (max " + max + ")" ;

		// Prepare and display the selection dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
		dialog.setTitle(dialog_title) ;
		dialog.setMultiChoiceItems(app_names, selected,
			new DialogInterface.OnMultiChoiceClickListener()
			{
				@Override
				public void onClick(DialogInterface dialogInterface, int i, boolean b) { }
			}) ;
		dialog.setPositiveButton(R.string.button_apply,
			new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialogInterface, int i)
				{
					// Remove the current file
					if(!file.remove()) return ;

					// Write the new selected applications to the file
					int selections_number = 0 ;
					for(i = 0 ; i < selected.length ; i++)
						if(selected[i])
							{
								// Add the application only if the maximum is not reached
								selections_number++ ;
								if((max == -1) || (selections_number <= max)) file.writeLine(applications.get(i).getName()) ;
									else
									{
										toastLong(context, context.getString(R.string.error_too_many_selections, max)) ;
										break ;
									}
							}

					// Update the applications list
					ActivityMain.updateList(context) ;
				}
			}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.show() ;
	}
}
