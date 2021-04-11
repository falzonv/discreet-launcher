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
import androidx.annotation.NonNull ;
import androidx.core.view.GestureDetectorCompat ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import android.util.DisplayMetrics ;
import android.view.ContextMenu ;
import android.view.GestureDetector ;
import android.view.MenuItem ;
import android.view.MotionEvent ;
import android.view.View ;
import android.widget.LinearLayout ;
import android.widget.TextView ;
import com.vincent_falzon.discreetlauncher.events.PackagesListener ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Main class activity managing the home screen and applications drawer.
 */
public class ActivityMain extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
	// Constants
	public static final int COLUMNS_PORTRAIT = 4 ;
	public static final int COLUMNS_LANDSCAPE = 5 ;

	// Attributes
	private static ApplicationsList applicationsList ;
	private static boolean ignore_settings_changes ;
	private static boolean list_update_needed ;
	private PackagesListener packagesListener ;
	private EventsReceiver legacyShortcutsCreator ;
	private SharedPreferences settings ;
	private GestureDetectorCompat gestureDetector ;
	private NotificationMenu notificationMenu ;

	// Attributes related to the home screen
	private LinearLayout homeScreen ;
	private LinearLayout favorites ;
	private RecyclerAdapter favoritesAdapter ;
	private TextView clock ;
	private EventsReceiver clockUpdater ;

	// Attributes related to the drawer
	private LinearLayout drawer ;
	private RecyclerAdapter drawerAdapter ;
	private GridLayoutManager drawerLayout ;
	private int drawer_position ;
	private int drawer_last_position ;

	
	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Initializations
		setContentView(R.layout.activity_main) ;
		homeScreen = findViewById(R.id.home_screen) ;
		favorites = findViewById(R.id.favorites) ;
		drawer = findViewById(R.id.drawer) ;
		list_update_needed = false ;

		// Assign default values to settings not configured yet
		PreferenceManager.setDefaultValues(this, R.xml.settings, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_display, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_notification, true) ;

		// Retrieve the current settings and start to listen for changes
		settings = PreferenceManager.getDefaultSharedPreferences(this) ;
		settings.registerOnSharedPreferenceChangeListener(this) ;
		ignore_settings_changes = false ;

		// Hide the favorites panel and the drawer by default
		displayFavorites(false) ;
		displayDrawer(false) ;

		// If the option is selected, force the portrait mode
		togglePortraitMode() ;

		gestureDetector = new GestureDetectorCompat(this, new GestureListener()) ;
		registerForContextMenu(findViewById(R.id.access_menu_button)) ;

		// Initialize the text clock
		clock = findViewById(R.id.clock_text) ;
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

		// Define the favorites panel and applications list layouts based on screen orientation
		GridLayoutManager favoritesLayout ;
		if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
			{
				favoritesLayout = new GridLayoutManager(this, ActivityMain.COLUMNS_LANDSCAPE) ;
				drawerLayout = new GridLayoutManager(this, ActivityMain.COLUMNS_LANDSCAPE) ;
			}
			else
			{
				favoritesLayout = new GridLayoutManager(this, COLUMNS_PORTRAIT) ;
				drawerLayout = new GridLayoutManager(this, ActivityMain.COLUMNS_PORTRAIT) ;
			}

		// Initialize the content of the favorites panel
		RecyclerView favoritesRecycler = findViewById(R.id.favorites_applications) ;
		favoritesAdapter = new RecyclerAdapter(true) ;
		favoritesRecycler.setAdapter(favoritesAdapter) ;
		favoritesRecycler.setLayoutManager(favoritesLayout) ;

		// Initialize the content of the full applications list
		RecyclerView fullListRecycler = findViewById(R.id.applications_list) ;
		drawerAdapter = new RecyclerAdapter(false) ;
		fullListRecycler.setAdapter(drawerAdapter) ;
		fullListRecycler.setLayoutManager(drawerLayout) ;
		drawer_position = 0 ;
		drawer_last_position = -1 ;
		fullListRecycler.addOnScrollListener(new DrawerScrollListener()) ;

		// Start to listen for packages added or removed
		packagesListener = new PackagesListener() ;
		registerReceiver(packagesListener, packagesListener.getFilter()) ;

		// When Android version is before Oreo, start to listen for legacy shortcut requests
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
						clockUpdater = new EventsReceiver(clock) ;
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
				clock.setText("") ;
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
	 * Display or hide the favorites panel.
	 * @param display <code>true</code> to display, <code>false</code> to hide
	 */
	private void displayFavorites(boolean display)
	{
		if(display)
			{
				// Display the favorites panel
				favorites.setVisibility(View.VISIBLE) ;

				// If the status bar was transparent, make it translucent as the panel
				if(settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false))
					getWindow().setStatusBarColor(getResources().getColor(R.color.color_applications_drawer_background)) ;
			}
			else
			{
				// Hide the favorites panel
				favorites.setVisibility(View.GONE) ;

				// If the option is selected, make the status bar fully transparent
				if(settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false))
					getWindow().setStatusBarColor(getResources().getColor(R.color.color_transparent)) ;
			}
	}


	/**
	 * Display or hide the applications drawer.
	 * @param display <code>true</code> to display, <code>false</code> to hide
	 */
	private void displayDrawer(boolean display)
	{
		if(display)
			{
				// Indicate the last time the applications list was updated
				TextView lastUpdate = findViewById(R.id.last_update_datetime) ;
				lastUpdate.setText(getString(R.string.info_applications_list_last_update, applicationsList.getLastUpdate())) ;

				// If the status bar was transparent, make it translucent as the drawer
				if(settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false))
					getWindow().setStatusBarColor(getResources().getColor(R.color.color_applications_drawer_background)) ;

				// Display the applications drawer
				drawer_position = 0 ;
				drawer_last_position = -1 ;
				homeScreen.setVisibility(View.GONE) ;
				drawer.setVisibility(View.VISIBLE) ;
			}
			else
			{
				// Hide the applications drawer
				homeScreen.setVisibility(View.VISIBLE) ;
				drawer.setVisibility(View.GONE) ;

				// If the option is selected, make the status bar fully transparent
				if(settings.getBoolean(ActivitySettings.TRANSPARENT_STATUS_BAR, false))
					getWindow().setStatusBarColor(getResources().getColor(R.color.color_transparent)) ;
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
	 * Allow to temporary disable the SharedPreference changes listener.
	 * @param new_value <code>true</code> to disable, <code>false</code> to enable
	 */
	static void setIgnoreSettingsChanges(boolean new_value)
	{
		ignore_settings_changes = new_value ;
	}


	/**
	 * Inform the activity that an update of the applications list is needed.
	 */
	public static void setListUpdateNeeded()
	{
		list_update_needed = true ;
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
		super.onCreateContextMenu(menu, view, menuInfo) ;
		getMenuInflater().inflate(R.menu.contextual_menu, menu) ;
	}


	/**
	 * Detect a click on an item from the contextual menu
	 * @param item Entry clicked
	 * @return <code>true</code> if event is consumed, <code>false</code> otherwise
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		// Identify which menu entry has been clicked
		int selection = item.getItemId() ;
		if(selection == R.id.menu_action_refresh_list)
			{
				// Update the applications list
				applicationsList.update(this) ;
				favoritesAdapter.notifyDataSetChanged() ;
				drawerAdapter.notifyDataSetChanged() ;
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
						favoritesAdapter.notifyDataSetChanged() ;
						if(favorites_number > max_favorites) ShowDialog.toastLong(context, getString(R.string.error_too_many_favorites, max_favorites)) ;
							else ShowDialog.toast(getApplicationContext(), R.string.info_favorites_saved) ;
					}
				}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.show() ;
	}


	/**
	 * Detect a user action on the screen.
	 * @param event Gesture event
	 * @return <code>true</code> if event is consumed, <code>false</code> otherwise
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		gestureDetector.onTouchEvent(event) ;
		return super.onTouchEvent(event) ;
	}


	/**
	 * Detect and recognize a gesture on the home screen.
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
			// Ignore the gesture if the applications drawer is opened
			if(drawer.getVisibility() == View.VISIBLE) return false ;

			// Calculate the traveled distances on both axes
			float x_distance = Math.abs(event1.getX() - event2.getX()) ;
			float y_distance = event1.getY() - event2.getY() ;

			// Check if this is a vertical gesture over a distance and not a single tap
			if((Math.abs(y_distance) > x_distance) && (Math.abs(y_distance) > 100))
				{
					// Check if the gesture is going up (if) or down (else)
					if(y_distance > 0)
						{
							// Display the applications drawer only if the favorites panel is closed
							if(favorites.getVisibility() == View.VISIBLE) displayFavorites(false) ;
								else displayDrawer(true) ;
						}
						else displayFavorites(true) ;

					// Indicate that the event has been consumed
					return true ;
				}

			// Ignore other gestures
			return false ;
		}
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
				favoritesAdapter.notifyDataSetChanged() ;
				drawerAdapter.notifyDataSetChanged() ;
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
	 * Detect a scrolling action on the applications drawer.
	 */
	class DrawerScrollListener extends RecyclerView.OnScrollListener
	{
		/**
		 * When the scrolling ends, check if it is stuck on top.
		 * @param recyclerView Scrolled RecyclerView
		 * @param newState 0 (Not scrolling), 1 (Active scrolling) or 2 (Scrolling inerty)
		 */
		@Override
		public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
		{
			// Let the parent actions be performed
			super.onScrollStateChanged(recyclerView, newState) ;

			// Wait for the gesture to be finished
			if(newState == RecyclerView.SCROLL_STATE_IDLE)
			{
				// If the scrolling is stuck on top, close the drawer activity
				if((drawer_position == 0) && (drawer_last_position == 0)) displayDrawer(false) ;

				// Update the last position to detect the stuck state
				drawer_last_position = drawer_position;
			}
		}


		/**
		 * Update the position of the first visible item as the user is scrolling.
		 * @param recyclerView Scrolled RecyclerView
		 * @param dx Horizontal distance
		 * @param dy Vertical distance
		 */
		@Override
		public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
		{
			// Let the parent actions be performed
			super.onScrolled(recyclerView, dx, dy) ;

			// Update the position of the first visible item
			drawer_position = drawerLayout.findFirstCompletelyVisibleItemPosition() ;
		}
	}


	/**
	 * Close the applications drawer or favorites panel if opened, otherwise do nothing.
	 */
	@Override
	public void onBackPressed()
	{
		if(drawer.getVisibility() == View.VISIBLE) displayDrawer(false) ;
			else if(favorites.getVisibility() == View.VISIBLE) displayFavorites(false) ;
	}


	/**
	 * Perform actions when the user leaves the home screen.
	 */
	@Override
	public void onPause()
	{
		// Let the parent actions be performed
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
		// Let the parent actions be performed
		super.onResume() ;

		// Hide the notification and update the display according to settings
		notificationMenu.hide() ;
		manageClock() ;
		togglePortraitMode() ;

		// Update the favorites panel and applications drawer if needed
		if(list_update_needed)
			{
				applicationsList.update(this) ;
				favoritesAdapter.notifyDataSetChanged() ;
				drawerAdapter.notifyDataSetChanged() ;
				list_update_needed = false ;
			}
	}


	/**
	 * Perform actions when the activity is destroyed.
	 */
	@Override
	public void onDestroy()
	{
		// Unregister all remaining broadcast receivers
		if(clockUpdater != null) unregisterReceiver(clockUpdater) ;
		if(packagesListener != null) unregisterReceiver(packagesListener) ;
		if(legacyShortcutsCreator != null) unregisterReceiver(legacyShortcutsCreator) ;

		// Let the parent actions be performed
		super.onDestroy() ;
	}
}
