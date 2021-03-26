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
import android.content.Intent ;
import android.content.IntentFilter ;
import android.content.SharedPreferences ;
import android.os.Bundle ;
import androidx.core.view.GestureDetectorCompat ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import android.view.ContextMenu ;
import android.view.GestureDetector ;
import android.view.MenuInflater ;
import android.view.MenuItem ;
import android.view.MotionEvent ;
import android.view.View ;
import android.widget.LinearLayout ;
import android.widget.TextView ;
import java.util.ArrayList ;

/**
 * Main class and home screen activity.
 */
public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	// Attributes
	private static ApplicationsList applicationsList ;
	private EventsReceiver applicationsListUpdater ;
	private SharedPreferences settings ;
	private GestureDetectorCompat detector ;
	private RecyclerAdapter adapter ;
	private LinearLayout favoritesPanel ;
	private EventsReceiver clockUpdater ;
	private TextView clockText ;
	private NotificationMenu notificationMenu ;

	
	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Call the constructor of the parent class
		super.onCreate(savedInstanceState) ;

		// Initialize the layout and navigation elements
		setContentView(R.layout.activity_main) ;
		detector = new GestureDetectorCompat(this, new GestureListener()) ;
		registerForContextMenu(findViewById(R.id.access_menu_button)) ;

		// Retrieve the settings and listen for changes
		PreferenceManager.setDefaultValues(this, R.xml.settings, true) ;
		settings = PreferenceManager.getDefaultSharedPreferences(this) ;
		settings.registerOnSharedPreferenceChangeListener(this) ;

		// Make the status bar transparent if this option was selected
		if(settings.getBoolean("transparent_status_bar", false))
			getWindow().setStatusBarColor(getResources().getColor(R.color.color_transparent)) ;

		// Initialize the text clock
		clockText = findViewById(R.id.clock_text) ;
		manageClock() ;

		// If they do not exist yet, build the applications lists (complete and favorites)
		if(applicationsList == null)
			{
				applicationsList = new ApplicationsList() ;
				applicationsList.update(this) ;
			}

		// Prepare the notification menu but hide it when the user is on the home screen
		notificationMenu = new NotificationMenu(this) ;
		applicationsList.updateNotificationApps(this) ;
		notificationMenu.hide() ;

		// Display a message if the user doesn't have any favorites applications yet
		if(applicationsList.getFavoritesCount() == 0)
			ShowDialog.toastLong(this, getString(R.string.text_no_favorites_yet)) ;

		// Prepare the display of the favorites panel over 4 columns
		RecyclerView recycler = findViewById(R.id.favorites_applications) ;
		adapter = new RecyclerAdapter(true) ;
		recycler.setAdapter(adapter) ;
		recycler.setLayoutManager(new GridLayoutManager(this, 4)) ;
		favoritesPanel = findViewById(R.id.favorites_panel) ;
		favoritesPanel.setVisibility(View.GONE) ;

		// Start to listen for packages added or removed
		applicationsListUpdater = new EventsReceiver(adapter) ;
		IntentFilter filter = new IntentFilter() ;
		filter.addAction(Intent.ACTION_PACKAGE_ADDED) ;
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED) ;
		filter.addDataScheme("package") ;
		registerReceiver(applicationsListUpdater, filter) ;
	}


	/**
	 * Display or hide the clock on the main screen according to the settings.
	 */
	private void manageClock()
	{
		// Check in the settings if the clock should be displayed or not
		if(settings.getBoolean("display_clock", false))
			{
				// Display the clock and start to listen for updates (every minute)
				clockUpdater = new EventsReceiver(clockText) ;
				registerReceiver(clockUpdater, new IntentFilter(Intent.ACTION_TIME_TICK)) ;
			}
			else
			{
				// Stop to listen for updates and hide the clock
				if(clockUpdater != null)
					{
						unregisterReceiver(clockUpdater) ;
						clockUpdater = null ;
					}
				clockText.setText("") ;
			}
	}


	/**
	 * Return the list of applications.
	 * @return Contains the complete list, the favorites list and the last update timestamp
	 */
	static ApplicationsList getApplicationsList()
	{
		return applicationsList ;
	}

	
	/**
	 * Create the contextual menu.
	 * @param menu Used by the parent class
	 * @param view Used by the parent class
	 * @param menuInfo Used by the parent class
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo)
	{
		// Call the method of the parent class
		super.onCreateContextMenu(menu, view, menuInfo) ;

		// Create the contextual menu
		MenuInflater inflater = getMenuInflater() ;
		inflater.inflate(R.menu.contextual_menu, menu) ;
	}


	/**
	 * Detect a click on an item from the contextual menu
	 * @param item Entry clicked
	 * @return <code>true</code> if event is consumed, <code>false</code> otherwise
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		// Get the ID of the clicked item
		int selection = item.getItemId() ;

		// Identify which menu entry has been clicked
		if(selection == R.id.menu_action_refresh_list)
			{
				// Update the applications list
				applicationsList.update(this) ;
				adapter.notifyDataSetChanged() ;
				return true ;
			}
			else if(selection == R.id.menu_action_manage_favorites)
			{
				// Display a menu to select the favorites applications
				displayManageFavoritesDialog() ;
				return true ;
			}
			else if(selection == R.id.menu_action_settings)
			{
				// Display the Settings and Help activity
				startActivity(new Intent().setClass(this, ActivitySettings.class)) ;
				return true ;
			}
		return false ;
	}


	/**
	 * Prepare and display the favorites applications management dialog.
	 */
	private void displayManageFavoritesDialog()
	{
		// List the names of all applications
		final ArrayList<Application> applications = applicationsList.getApplications() ;
		CharSequence[] app_names = new CharSequence[applications.size()] ;
		int i = 0 ;
		for(Application application : applications)
		{
			app_names[i] = application.getDisplayName() ;
			i++ ;
		}

		// Retrieve the current favorites applications
		final InternalFile file = new InternalFile(this, "favorites.txt") ;
		final boolean[] selected = new boolean[app_names.length] ;
		if(file.isExisting())
			for(i = 0 ; i < app_names.length ; i++)
				selected[i] = file.isLineExisting(applications.get(i).getName()) ;
		else for(i = 0 ; i < app_names.length ; i++)
			selected[i] = false ;

		// Prepare and display the selection dialog
		final Context context = this ;
		AlertDialog.Builder dialog = new AlertDialog.Builder(this) ;
		dialog.setTitle(R.string.text_check_favorites) ;
		dialog.setMultiChoiceItems(app_names, selected,
				new DialogInterface.OnMultiChoiceClickListener()
				{
					@Override
					public void onClick(DialogInterface dialogInterface, int i, boolean b) { }
				}) ;
		dialog.setPositiveButton(R.string.button_apply,
				new DialogInterface.OnClickListener()
				{
					// Save the new list of favorites applications
					@Override
					public void onClick(DialogInterface dialogInterface, int i)
					{
						// Remove the current favorites list
						if(!file.remove())
							{
								// An error happened while trying to remove the current list
								ShowDialog.toast(context, R.string.error_remove_favorites) ;
								return ;
							}

						// Write the new favorites list
						for(i = 0 ; i < selected.length ; i++)
						{
							if(selected[i])
								if(!file.writeLine(applications.get(i).getName()))
									{
										ShowDialog.toastLong(context, getString(R.string.error_add_new_favorite, applications.get(i).getDisplayName())) ;
										return ;
									}
						}

						// Update the favorites panel and inform the user
						applicationsList.updateFavorites(context) ;
						adapter.notifyDataSetChanged() ;
						ShowDialog.toast(getApplicationContext(), R.string.text_favorites_saved) ;
					}
				}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.show() ;
	}


	/**
	 * Detect a user action on the screen
	 * @param event Gesture event
	 * @return <code>true</code> if event is consumed, <code>false</code> otherwise
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		detector.onTouchEvent(event) ;
		return super.onTouchEvent(event) ;
	}


	/**
	 * Detect and recognize a gesture on the screen.
	 */
	class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		/**
		 * Implemented because all gestures start with an onDown() message.
		 * @param event Gesture event
		 * @return <code>true</code>
		 */
		@Override
		public boolean onDown(MotionEvent event)
		{
			return true ;
		}

		/**
		 * Detect a gesture over a distance.
		 * @param event1 Starting point
		 * @param event2 Ending point
		 * @param velocityX On horizontal axis
		 * @param velocityY On vertical axis
		 * @return <code>true</code> if event is consumed, <code>false</code> otherwise
		 */
		@Override
		public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY)
		{
			// Calculate the traveled distances on both axes
			float x_distance = Math.abs(event1.getX() - event2.getX()) ;
			float y_distance = event1.getY() - event2.getY() ;

			// Check if this is a vertical gesture over a distance and not a single tap
			if((Math.abs(y_distance) > x_distance) && (Math.abs(y_distance) > 100))
			{
				// Check the gesture direction
				if(y_distance > 0)
					{
						// Going up, check if the favorites panel is opened
						if(favoritesPanel.getVisibility() == View.VISIBLE)
							{
								// Close the favorites panel
								favoritesPanel.setVisibility(View.GONE) ;

								// Make the status bar transparent if this option was selected
								if(settings.getBoolean("transparent_status_bar", false))
									getWindow().setStatusBarColor(getResources().getColor(R.color.color_transparent)) ;
							}
							else
							{
								// Open the applications drawer
								startActivity(new Intent().setClass(getApplicationContext(), ActivityDrawer.class)) ;
							}
					}
					else
					{
						// Going down, open the favorites panel
						favoritesPanel.setVisibility(View.VISIBLE) ;

						// Make the status bar translucent if it was transparent
						if(settings.getBoolean("transparent_status_bar", false))
							getWindow().setStatusBarColor(getResources().getColor(R.color.color_applications_drawer_background)) ;
					}

				// Indicate that the event has been consumed
				return true ;
			}

			// Ignore other gestures
			return false ;
		}
	}


	/**
	 * When the user presses "Back" from the Main Activity, either close the favorites panel or
	 * do nothing to stay on the home screen
	 */
	@Override
	public void onBackPressed()
	{
		if(favoritesPanel.getVisibility() == View.VISIBLE)
			favoritesPanel.setVisibility(View.GONE) ;
	}


	/**
	 * Listen for changes in the settings and react accordingly.
	 * @param sharedPreferences Settings where the change happened
	 * @param key The value which has changed
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		switch(key)
		{
			case "display_clock" :
				// Toggle the clock
				manageClock() ;
				break ;
			case "icon_pack" :
				// If the icon pack has changed, update the icons
				applicationsList.update(this) ;
				adapter.notifyDataSetChanged() ;
				break ;
			case "display_notification" :
				// Toggle the notification
				if(settings.getBoolean("display_notification", true)) notificationMenu.display(this) ;
					else notificationMenu.hide() ;
				break ;
			case "notification_app1" :
			case "notification_app2" :
			case "notification_app3" :
				// If one of the applications has changed, update the notification
				applicationsList.updateNotificationApps(this) ;
				notificationMenu.hide() ;
				notificationMenu.display(this) ;
				break ;
		}
	}


	/**
	 * Display the notification when the user leaves the home screen (if option selected).
	 */
	@Override
	public void onPause()
	{
		super.onPause() ;
		if(settings.getBoolean("display_notification", true))
			notificationMenu.display(this) ;
	}


	/**
	 * Hide the notification when the user is on the home screen.
	 */
	@Override
	public void onResume()
	{
		super.onResume() ;
		notificationMenu.hide() ;
	}


	/**
	 * Unregister all remaining broadcast receivers when the activity is destroyed.
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy() ;
		if(clockUpdater != null) unregisterReceiver(clockUpdater) ;
		if(applicationsListUpdater != null) unregisterReceiver(applicationsListUpdater) ;
	}
}
