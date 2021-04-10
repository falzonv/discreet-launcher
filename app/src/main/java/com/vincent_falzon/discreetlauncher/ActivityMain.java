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
import android.content.pm.ActivityInfo ;
import android.content.res.Configuration ;
import android.os.Build ;
import android.os.Bundle ;
import androidx.core.view.GestureDetectorCompat ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import android.util.DisplayMetrics ;
import android.view.ContextMenu ;
import android.view.GestureDetector ;
import android.view.MenuInflater ;
import android.view.MenuItem ;
import android.view.MotionEvent ;
import android.view.View ;
import android.widget.LinearLayout ;
import android.widget.TextView ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Main class and home screen activity.
 */
public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	// Constants
	public static final int COLUMNS_PORTRAIT = 4 ;
	public static final int COLUMNS_LANDSCAPE = 5 ;

	// Attributes
	private static ApplicationsList applicationsList ;
	private static boolean ignore_settings_changes ;
	private static boolean adapter_update_needed ;
	private EventsReceiver applicationsListUpdater ;
	private EventsReceiver legacyShortcutsCreator ;
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
		PreferenceManager.setDefaultValues(this, R.xml.settings_notification, true) ;
		settings = PreferenceManager.getDefaultSharedPreferences(this) ;
		settings.registerOnSharedPreferenceChangeListener(this) ;
		ignore_settings_changes = false ;

		// Make the status bar transparent if this option was selected
		if(settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false))
			getWindow().setStatusBarColor(getResources().getColor(R.color.color_transparent)) ;

		// If the option is selected, force the portrait mode
		togglePortraitMode() ;

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

		// Display a message if the user does not have any favorites applications yet
		if(applicationsList.getFavoritesCount() == 0)
			ShowDialog.toastLong(this, getString(R.string.info_no_favorites_yet)) ;

		// Prepare the display of the favorites panel
		RecyclerView recycler = findViewById(R.id.favorites_applications) ;
		adapter = new RecyclerAdapter(true) ;
		recycler.setAdapter(adapter) ;
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
				recycler.setLayoutManager(new GridLayoutManager(this, COLUMNS_LANDSCAPE)) ;
			else recycler.setLayoutManager(new GridLayoutManager(this, COLUMNS_PORTRAIT)) ;

		// Hide the favorites panel by default
		favoritesPanel = findViewById(R.id.favorites_panel) ;
		favoritesPanel.setVisibility(View.GONE) ;
		adapter_update_needed = false ;

		// Start to listen for packages added or removed
		applicationsListUpdater = new EventsReceiver() ;
		IntentFilter filter = new IntentFilter() ;
		filter.addAction(Intent.ACTION_PACKAGE_ADDED) ;
		filter.addAction(Intent.ACTION_PACKAGE_REMOVED) ;
		filter.addDataScheme("package") ;
		registerReceiver(applicationsListUpdater, filter) ;

		// When Android version is before, Oreo, start to listen for legacy shortcut requests
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
			{
				legacyShortcutsCreator = new EventsReceiver() ;
				registerReceiver(legacyShortcutsCreator, new IntentFilter("com.android.launcher.action.INSTALL_SHORTCUT")) ;
			}
	}


	/**
	 * Display or hide the clock on the main screen according to the settings.
	 */
	private void manageClock()
	{
		// Check in the settings if the clock should be displayed or not
		if(settings.getBoolean(ActivitySettings.DISPLAY_CLOCK, false))
			{
				// If not already done, display the clock and start to listen for updates (every minute)
				if(clockUpdater == null)
					{
						clockUpdater = new EventsReceiver(clockText) ;
						registerReceiver(clockUpdater, new IntentFilter(Intent.ACTION_TIME_TICK)) ;
					}
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
	 * Force or not the portrait mode according to the settings.
	 */
	private void togglePortraitMode()
	{
		if(settings.getBoolean(ActivitySettings.FORCE_PORTRAIT, false))
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) ;
			else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) ;
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
	 * Allow to temporary disable the SharedPreference changes listener.
	 * @param new_value <code>true</code> to disable, <code>false</code> to enable
	 */
	static void setIgnoreSettingsChanges(boolean new_value)
	{
		ignore_settings_changes = new_value ;
	}


	/**
	 * Inform the activity that an update of the RecyclerView is needed.
	 */
	static void setAdapterUpdateNeeded()
	{
		adapter_update_needed = true ;
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
		final InternalFileTXT file = new InternalFileTXT(getApplicationContext(), ApplicationsList.FAVORITES_FILE) ;
		final boolean[] selected = new boolean[app_names.length] ;
		if(file.exists()) for(i = 0 ; i < app_names.length ; i++) selected[i] = file.isLineExisting(applications.get(i).getName()) ;
			 else for(i = 0 ; i < app_names.length ; i++) selected[i] = false ;

		// Retrieve the total height available in portrait mode (navigation bar automatically removed)
		DisplayMetrics metrics = getResources().getDisplayMetrics() ;
		int button_height = findViewById(R.id.access_menu_button).getHeight() ;
		int total_size = Math.max(metrics.heightPixels, metrics.widthPixels)
				- Math.round(25 * metrics.density)	// Remove 25dp for the status bar
				- Math.round(20 * metrics.density)  // Remove 20dp for button margins and spare
				- button_height ;

		// Define the size of an app (text estimation + icon + margins) and the maximum number of favorites
		int app_size = button_height + Math.round(48 * metrics.density) + Math.round(25 * metrics.density) ;
		final int max_favorites = (total_size / app_size) * COLUMNS_PORTRAIT;

		// Prepare and display the selection dialog
		final Context context = this ;
		AlertDialog.Builder dialog = new AlertDialog.Builder(this) ;
		dialog.setTitle(R.string.dialog_check_favorites) ;
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
								ShowDialog.toastLong(context, context.getString(R.string.error_remove_file, file.getName())) ;
								return ;
							}

						// Write the new favorites list
						int favorites_number = 0 ;
						for(i = 0 ; i < selected.length ; i++)
						{
							// Check if an application is selected
							if(selected[i])
								{
									// If the maximum is not reached, add the application to the favorites list
									favorites_number++ ;
									if(favorites_number <= max_favorites)
										if(!file.writeLine(applications.get(i).getName()))
											{
												ShowDialog.toastLong(context, getString(R.string.error_favorite, applications.get(i).getDisplayName())) ;
												return ;
											}
								}
						}

						// Update the favorites panel and inform the user
						applicationsList.updateFavorites(context) ;
						adapter.notifyDataSetChanged() ;
						if(favorites_number > max_favorites) ShowDialog.toastLong(context, getString(R.string.error_too_many_favorites, max_favorites)) ;
							else ShowDialog.toast(getApplicationContext(), R.string.info_favorites_saved) ;
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
								if(settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false))
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
						if(settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false))
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
		if(ignore_settings_changes) return ;
		switch(key)
		{
			case ActivitySettings.ICON_PACK :
			case ActivitySettings.HIDDEN_APPLICATIONS :
				// Update the applications list
				applicationsList.update(this) ;
				adapter.notifyDataSetChanged() ;
				break ;
			case ActivitySettings.DISPLAY_NOTIFICATION :
				// Toggle the notification
				if(settings.getBoolean(ActivitySettings.DISPLAY_NOTIFICATION, true))
					{
						applicationsList.updateNotificationApps(this) ;
						notificationMenu.display(this) ;
					}
					else notificationMenu.hide() ;
				break ;
			case ActivitySettings.NOTIFICATION_TEXT :
			case ActivitySettings.NOTIFICATION_APP + "1" :
			case ActivitySettings.NOTIFICATION_APP + "2" :
			case ActivitySettings.NOTIFICATION_APP + "3" :
				// Update the notification
				applicationsList.updateNotificationApps(this) ;
				notificationMenu.hide() ;
				notificationMenu.display(this) ;
				break ;
		}
	}


	/**
	 * Perform actions when the user leaves the home screen.
	 */
	@Override
	public void onPause()
	{
		super.onPause() ;

		// If the option is selected, display the notification
		if(settings.getBoolean(ActivitySettings.DISPLAY_NOTIFICATION, true))
			notificationMenu.display(this) ;
	}


	/**
	 * Perform actions when the user come back to the home screen.
	 */
	@Override
	public void onResume()
	{
		super.onResume() ;

		// Hide the notification and update the display according to settings
		notificationMenu.hide() ;
		manageClock() ;
		togglePortraitMode() ;

		// Update the RecyclerView if needed
		if(adapter_update_needed)
			{
				adapter.notifyDataSetChanged() ;
				adapter_update_needed = false ;
			}
	}


	/**
	 * Unregister all remaining broadcast receivers when the activity is destroyed.
	 */
	@Override
	public void onDestroy()
	{
		if(clockUpdater != null) unregisterReceiver(clockUpdater) ;
		if(applicationsListUpdater != null) unregisterReceiver(applicationsListUpdater) ;
		if(legacyShortcutsCreator != null) unregisterReceiver(legacyShortcutsCreator) ;
		super.onDestroy() ;
	}
}
