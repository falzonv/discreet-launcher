package com.vincent_falzon.discreetlauncher.menu ;

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
import android.annotation.SuppressLint ;
import android.content.Context ;
import android.content.Intent ;
import android.view.LayoutInflater ;
import android.view.MotionEvent ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.TextView ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatDialog ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.settings.ActivitySettings ;

/**
 * Display the main menu of Discreet Launcher.
 */
public class DialogMenu extends AppCompatDialog implements View.OnClickListener, View.OnTouchListener
{
	// Attributes
	private final View dialogView ;


	/**
	 * Constructor.
	 * @param context Provided by a preference
	 *
	 */
	@SuppressWarnings({"RedundantCast", "RedundantSuppression"})
	public DialogMenu(Context context)
	{
		// Let the parent actions be performed
        super(context) ;

		// Load the XML layout
		dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_menu, (ViewGroup)null) ;
		setContentView(dialogView) ;

		// Initializations
		initializeMenuEntry(R.id.menu_favorites) ;
		initializeMenuEntry(R.id.menu_folders) ;
		initializeMenuEntry(R.id.menu_refresh_list) ;
		initializeMenuEntry(R.id.menu_settings) ;
		initializeMenuEntry(R.id.menu_help) ;
		initializeMenuEntry(R.id.menu_changelog) ;
		initializeMenuEntry(R.id.menu_about) ;
    }


	/**
	 * Initialize a menu entry.
	 * @param id Resource ID of the menu entry
	 */
	@SuppressLint("ClickableViewAccessibility")
	private void initializeMenuEntry(int id)
	{
		TextView menuEntry = dialogView.findViewById(id) ;
		menuEntry.setOnClickListener(this) ;
		menuEntry.setOnTouchListener(this) ;
	}


	/**
	 * Called when an element is clicked.
	 * @param view Target element
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
			else if(selection == R.id.menu_refresh_list)
			{
				// Refresh the list of apps and go back to the home screen
				ActivityMain.updateList(context) ;
				Intent homeIntent = new Intent() ;
				homeIntent.setClass(context, ActivityMain.class) ;
				homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
				context.startActivity(homeIntent) ;
			}
			else if(selection == R.id.menu_settings)
			{
				// Open the Settings and Help activity
				context.startActivity(new Intent().setClass(context, ActivitySettings.class)) ;
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
			else if(selection == R.id.menu_changelog)
			{
				// Display the Changelog without dismissing the menu
				DialogChangelog dialog = new DialogChangelog(context) ;
				dialog.show() ;
				return ;
			}
			else if(selection == R.id.menu_about)
			{
				// Display the About without dismissing the menu
				AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
				if(context.getString(R.string.translation_credit).isEmpty())
						dialog.setView(R.layout.dialog_about) ;
					else dialog.setView(R.layout.dialog_about_with_credit) ;
				dialog.setPositiveButton(R.string.button_close, null) ;
				dialog.show() ;
				return ;
			}

		// Dismiss the menu
        dismiss() ;
    }


	/**
	 * Called when an element is touched.
	 * @param view Target element
	 * @param event Type of event
	 * @return <code>true</code> if the event is consumed, <code>false</code> otherwise
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		// Do not continue if the view is not a TextView
		if(!(view instanceof TextView)) return false ;

		// Toggle visual feedback on the selected menu entry
		switch(event.getAction())
		{
			// Gesture started
			case MotionEvent.ACTION_DOWN :
				view.setBackgroundColor(view.getContext().getResources().getColor(R.color.light_blue)) ;
				break ;
			// Gesture finished or aborted
			case MotionEvent.ACTION_UP :
			case MotionEvent.ACTION_CANCEL :
				view.setBackground(null) ;
				break ;
		}
		return false ;
	}
}
