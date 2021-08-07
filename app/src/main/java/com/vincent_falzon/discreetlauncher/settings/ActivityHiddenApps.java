package com.vincent_falzon.discreetlauncher.settings ;

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
import android.os.Bundle ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Display a multi-selection dialog allowing to select applications to hide.
 */
public class ActivityHiddenApps extends AppCompatActivity implements DialogInterface.OnDismissListener
{
	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Prepare the list of applications
		final ArrayList<Application> applications = new ArrayList<>(ActivityMain.getApplicationsList().getHidden()) ;
		for(Application application : ActivityMain.getApplicationsList().getApplications(false))
		{
			// Never hide the Discreet Launcher icon (as it can be the only access to the menu)
			if(application.getApk().equals(getPackageName())) continue ;
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
		final Context context = this ;
		AlertDialog.Builder dialog = new AlertDialog.Builder(this) ;
		dialog.setTitle(R.string.button_hidden_applications) ;
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
					for(i = 0 ; i < selected.length ; i++)
						if(selected[i]) file.writeLine(applications.get(i).getComponentInfo()) ;

					// Update the applications list
					ActivityMain.updateList(context) ;
				}
			}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.setOnDismissListener(this) ;
		dialog.show() ;
	}


	/**
	 * Called when a dialog is dismissed, whatever the result.
	 * @param dialog Dismissed dialog
	 */
	@Override
	public void onDismiss(DialogInterface dialog)
	{
		// Return to the previous screen
		onBackPressed() ;
	}
}
