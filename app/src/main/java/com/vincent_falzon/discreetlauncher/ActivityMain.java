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
import android.content.SharedPreferences ;
import android.content.pm.ActivityInfo ;
import android.graphics.drawable.Drawable ;
import android.os.Build ;
import android.os.Bundle ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.appcompat.app.AppCompatDelegate ;
import androidx.core.content.res.ResourcesCompat ;
import androidx.core.graphics.drawable.DrawableCompat ;
import androidx.core.view.GestureDetectorCompat ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import android.view.GestureDetector ;
import android.view.MotionEvent ;
import android.view.View ;
import android.widget.LinearLayout ;
import android.widget.TextView ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.ApplicationsList ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.core.Search ;
import com.vincent_falzon.discreetlauncher.events.ShortcutLegacyListener ;
import com.vincent_falzon.discreetlauncher.events.PackagesListener ;
import com.vincent_falzon.discreetlauncher.menu.DialogMenu ;
import com.vincent_falzon.discreetlauncher.notification.NotificationDisplayer ;
import com.vincent_falzon.discreetlauncher.settings.ActivitySettingsAppearance ;

/**
 * Main class activity managing the home screen and applications drawer.
 */
public class ActivityMain extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener
{
	// Attributes
	private static ApplicationsList applicationsList ;
	private static boolean ignore_settings_changes ;
	private static boolean adapters_update_needed ;
	private static String internal_folder ;
	private static int application_width ;
	private PackagesListener packagesListener ;
	private ShortcutLegacyListener shortcutLegacyListener ;
	private SharedPreferences settings ;
	private GestureDetectorCompat gestureDetector ;
	private NotificationDisplayer notification ;
	private DialogMenu dialogMenu ;
	private float density ;

	// Attributes related to the home screen
	private LinearLayout homeScreen ;
	private LinearLayout favorites ;
	private RecyclerAdapter favoritesAdapter ;
	private TextView menuButton ;
	private TextView targetFavorites ;
	private TextView targetApplications ;
	private boolean reverse_interface ;

	// Attributes related to the drawer
	private RecyclerView drawer ;
	private RecyclerAdapter drawerAdapter ;
	private GridLayoutManager drawerLayout ;
	private int drawer_position ;
	private int drawer_last_position ;
	private int drawer_close_gesture ;

	
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
		internal_folder = getApplicationContext().getFilesDir().getAbsolutePath() ;
		density = getResources().getDisplayMetrics().density ;

		// Assign default values to settings not configured yet
		PreferenceManager.setDefaultValues(this, R.xml.settings_appearance, true) ;
		PreferenceManager.setDefaultValues(this, R.xml.settings_operation, true) ;

		// Retrieve the current settings and start to listen for changes
		settings = PreferenceManager.getDefaultSharedPreferences(this) ;
		settings.registerOnSharedPreferenceChangeListener(this) ;
		ignore_settings_changes = false ;

		// Set the light or dark theme according to settings
		setApplicationTheme() ;

		// Check if the interface should be reversed and define the appropriate layout
		reverse_interface = settings.getBoolean(Constants.REVERSE_INTERFACE, false) ;
		if(reverse_interface) setContentView(R.layout.activity_main_reverse) ;
			else setContentView(R.layout.activity_main) ;

		// Initializations related to the interface
		dialogMenu = new DialogMenu(this) ;
		homeScreen = findViewById(R.id.home_screen) ;
		favorites = findViewById(R.id.favorites) ;
		drawer = findViewById(R.id.drawer) ;
		menuButton = findViewById(R.id.access_menu_button) ;
		targetFavorites = findViewById(R.id.target_favorites) ;
		targetApplications = findViewById(R.id.target_applications) ;
		menuButton.setOnClickListener(this) ;
		targetFavorites.setOnClickListener(this) ;
		targetApplications.setOnClickListener(this) ;
		gestureDetector = new GestureDetectorCompat(this, new GestureListener(homeScreen)) ;

		// If it does not exist yet, build the applications list
		if(applicationsList == null)
			{
				applicationsList = new ApplicationsList() ;
				applicationsList.update(this) ;
			}

		// Update the display according to settings
		togglePortraitMode() ;
		keepMenuAccessible() ;
		toggleTouchTargets() ;
		if(settings.getBoolean(Constants.IMMERSIVE_MODE, false)) displaySystemBars(false) ;

		// Prepare the notification
		notification = new NotificationDisplayer(this) ;
		if(settings.getBoolean(Constants.NOTIFICATION, true)) notification.display(this) ;
			else notification.hide() ;

		// Define the width of an application item
		int padding ;
		if(settings.getBoolean(Constants.HIDE_APP_NAMES, false)
				&& settings.getBoolean(Constants.REMOVE_PADDING, false)) padding = 0 ;
			else padding = 30 ;
		application_width = Math.round((50 + padding) * getResources().getDisplayMetrics().density) ;

		// Initialize the content of the favorites panel
		favoritesAdapter = new RecyclerAdapter(this, applicationsList.getFavorites()) ;
		RecyclerView favoritesRecycler = findViewById(R.id.favorites_applications) ;
		favoritesRecycler.setAdapter(favoritesAdapter) ;
		favoritesRecycler.setLayoutManager(new FlexibleGridLayout(this, application_width)) ;

		// Initialize the content of the full applications list
		drawerAdapter = new RecyclerAdapter(this, applicationsList.getDrawer()) ;
		drawer.setAdapter(drawerAdapter) ;
		drawerLayout = new FlexibleGridLayout(this, application_width) ;
		drawer.setLayoutManager(drawerLayout) ;
		drawer.addOnScrollListener(new DrawerScrollListener()) ;

		// Load the favorites panel if it should always be shown
		if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false)) displayFavorites(true) ;

		// Hide the favorites panel and the drawer by default
		displayFavorites(false) ;
		displayDrawer(false) ;

		// Start to listen for packages added or removed
		packagesListener = new PackagesListener() ;
		registerReceiver(packagesListener, packagesListener.getFilter()) ;

		// When Android version is before Oreo, start to listen for legacy shortcut requests
		if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
			{
				shortcutLegacyListener = new ShortcutLegacyListener() ;
				registerReceiver(shortcutLegacyListener, shortcutLegacyListener.getFilter()) ;
			}
	}


	/**
	 * Set the application theme to light or dark according to system settings.
	 */
	private void setApplicationTheme()
	{
		String theme = settings.getString(Constants.APPLICATION_THEME, Constants.NONE) ;
		if(theme == null) return ;
		switch(theme)
		{
			case "light" :
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) ;
				break ;
			case "dark" :
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) ;
				break ;
			case Constants.NONE :
				AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) ;
		}
	}


	/**
	 * Force or not the portrait mode according to the settings.
	 */
	private void togglePortraitMode()
	{
		if(settings.getBoolean(Constants.FORCE_PORTRAIT, false))
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) ;
			else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) ;
	}


	/**
	 * Enable or disable the touch targets according to the settings.
	 */
	private void toggleTouchTargets()
	{
		if(settings.getBoolean(Constants.TOUCH_TARGETS, false))
			{
				// Display or not targets according to the settings
				if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false))
						targetFavorites.setVisibility(View.GONE) ;
					else targetFavorites.setVisibility(View.VISIBLE) ;
				if(settings.getBoolean(Constants.DISABLE_APP_DRAWER, false))
						targetApplications.setVisibility(View.GONE) ;
					else targetApplications.setVisibility(View.VISIBLE) ;
			}
			else
			{
				targetFavorites.setVisibility(View.GONE) ;
				targetApplications.setVisibility(View.GONE) ;
			}
	}


	/**
	 * Make sure that the launcher menu always stays accessible.
	 */
	private void keepMenuAccessible()
	{
		// Do not continue if none of the risky settings is enabled
		if(!(settings.getBoolean(Constants.DISABLE_APP_DRAWER, false) ||
				settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false))) return ;

		// Browse all favorites
		String launcher = "{com.vincent_falzon.discreetlauncher/com.vincent_falzon.discreetlauncher.ActivityMain}" ;
		for(Application application : applicationsList.getFavorites())
		{
			// Search if the launcher icon or Search app is in favorites
			if(application instanceof Search) return ;
				else if(application.getComponentInfo().equals(launcher)) return ;
				else if(application instanceof Folder)
				{
					// Also search the launcher icon in folders
					for(Application folder_application : ((Folder)application).getApplications())
						if(folder_application.getComponentInfo().equals(launcher)) return ;
				}
		}

		// Check if the menu button is visible
		if(!settings.getBoolean(Constants.HIDE_MENU_BUTTON, false))
			{
				// Retrieve the total height available in portrait mode (navigation bar automatically removed)
				int menu_button_height = Math.round(32 * density) ;
				int total_size = Math.max(getResources().getDisplayMetrics().heightPixels, getResources().getDisplayMetrics().widthPixels)
						- Math.round(25 * density)	// Remove 25dp for the status bar
						- Math.round(20 * density)  // Remove 20dp for button margins and spare
						- menu_button_height ;

				// Define the size of an app (icon + margins + text estimation) and the maximum number of favorites
				int app_size = Math.round(48 * density) ;
				if(!settings.getBoolean(Constants.REMOVE_PADDING, false)) app_size += Math.round(20 * density) ;
				if(!settings.getBoolean(Constants.HIDE_APP_NAMES, false)) app_size += menu_button_height ;
				int max_favorites = 4 * (total_size / app_size) ;

				// Check if the number of favorites still allows to see the menu button
				if(applicationsList.getFavorites().size() <= max_favorites) return ;

				// If favorites cannot be always shown safely, display a message and disable the setting
				if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false))
					{
						ShowDialog.toastLong(this, getString(R.string.error_always_show_favorites_not_safe)) ;
						SharedPreferences.Editor editor = settings.edit() ;
						editor.putBoolean(Constants.ALWAYS_SHOW_FAVORITES, false).apply() ;
					}
			}

		// If the drawer cannot be safely disabled, display a message and disable the setting
		if(settings.getBoolean(Constants.DISABLE_APP_DRAWER, false))
			{
				ShowDialog.toastLong(this, getString(R.string.error_disable_app_drawer_not_safe)) ;
				SharedPreferences.Editor editor = settings.edit() ;
				editor.putBoolean(Constants.DISABLE_APP_DRAWER, false).apply() ;
			}
	}


	/**
	 * Display or hide the favorites panel.
	 * @param display <code>true</code> to display, <code>false</code> to hide
	 */
	private void displayFavorites(boolean display)
	{
		if(display)
			{
				// Update the recyclers (favorites panel and applications drawer) if needed
				if(adapters_update_needed) updateAdapters() ;

				// Display a message if the user does not have any favorites applications yet
				if(applicationsList.getFavorites().size() == 0) findViewById(R.id.info_no_favorites_yet).setVisibility(View.VISIBLE) ;
					else findViewById(R.id.info_no_favorites_yet).setVisibility(View.GONE) ;

				// Retrieve the background color
				int background_color = ActivitySettingsAppearance.getColor(settings, Constants.BACKGROUND_COLOR, getResources().getColor(R.color.for_overlay)) ;

				// Check if the interface is reversed and adjust the display accordingly
				Drawable tab_shape ;
				if(reverse_interface)
					{
						// Reversed interface
						tab_shape = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_tab_reverse, null) ;
						getWindow().setNavigationBarColor(background_color) ;
					}
					else
					{
						// Classic interface
						tab_shape = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_tab, null) ;
						getWindow().setStatusBarColor(background_color) ;
					}

				// Color the menu button and favorites panel
				Drawable menuButtonBackground ;
				if(tab_shape != null)
					{
						menuButtonBackground = DrawableCompat.wrap(tab_shape) ;
						menuButtonBackground.setTint(background_color) ;
						menuButton.setBackground(DrawableCompat.unwrap(menuButtonBackground)) ;
					}
				findViewById(R.id.favorites_applications).setBackgroundColor(background_color) ;

				// If the option is selected, hide the menu button
				if(settings.getBoolean(Constants.HIDE_MENU_BUTTON, false)) menuButton.setVisibility(View.GONE) ;
					else menuButton.setVisibility(View.VISIBLE) ;

				// Display the favorites panel
				favorites.setVisibility(View.VISIBLE) ;
				targetFavorites.setText(R.string.target_close_favorites) ;
			}
			else
			{
				// Do not continue if the option to always show the panel is selected
				if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false)) return ;

				// Hide the favorites panel
				favorites.setVisibility(View.GONE) ;
				targetFavorites.setText(R.string.target_open_favorites) ;

				// If the option is selected, make the status bar fully transparent
				if(settings.getBoolean(Constants.TRANSPARENT_STATUS_BAR, false))
						getWindow().setStatusBarColor(getResources().getColor(R.color.transparent)) ;
					else getWindow().setStatusBarColor(ActivitySettingsAppearance.getColor(settings, Constants.BACKGROUND_COLOR, getResources().getColor(R.color.for_overlay))) ;

				// Make the navigation bar transparent
				getWindow().setNavigationBarColor(getResources().getColor(R.color.transparent)) ;
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
				// Do not continue if the drawer has been disabled
				if(settings.getBoolean(Constants.DISABLE_APP_DRAWER, false)) return ;

				// Update the recyclers (favorites panel and applications drawer) if needed
				if(adapters_update_needed) updateAdapters() ;

				// Color the system bars and the drawer background
				int background_color = ActivitySettingsAppearance.getColor(settings, Constants.BACKGROUND_COLOR, getResources().getColor(R.color.for_overlay)) ;
				drawer.setBackgroundColor(background_color) ;
				getWindow().setStatusBarColor(background_color) ;
				getWindow().setNavigationBarColor(background_color) ;

				// Display the applications drawer
				drawer_position = 0 ;
				drawer_last_position = 0 ;
				drawer_close_gesture = 0 ;
				homeScreen.setVisibility(View.GONE) ;
				drawer.setVisibility(View.VISIBLE) ;
			}
			else
			{
				// Hide the applications drawer
				drawer.scrollToPosition(0) ;
				homeScreen.setVisibility(View.VISIBLE) ;
				drawer.setVisibility(View.GONE) ;

				// If the option is selected, make the status bar fully transparent
				if(settings.getBoolean(Constants.TRANSPARENT_STATUS_BAR, false))
					getWindow().setStatusBarColor(getResources().getColor(R.color.transparent)) ;

				// Make the navigation bar transparent, unless in reverse interface with favorites always shown
				if(!(reverse_interface && settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false)))
					getWindow().setNavigationBarColor(getResources().getColor(R.color.transparent)) ;
			}
	}


	/**
	 * Display or hide the system bars (immersive mode).
	 * @param display <code>true</code> to display, <code>false</code> to hide
	 */
	private void displaySystemBars(boolean display)
	{
		View decorView = getWindow().getDecorView() ;
		if(display) decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE) ;
			else decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) ;
	}


	/**
	 * Return the list of applications.
	 * @return Contains the complete list, the favorites list and the last update timestamp
	 */
	public static ApplicationsList getApplicationsList()
	{
		return applicationsList ;
	}


	/**
	 * Allow to temporary disable the SharedPreference changes listener.
	 * @param new_value <code>true</code> to disable, <code>false</code> to enable
	 */
	public static void setIgnoreSettingsChanges(boolean new_value)
	{
		ignore_settings_changes = new_value ;
	}


	/**
	 * Return the internal files folder location (must be initialized by ActivityMain).
	 * @return Internal files folder location on the system or <code>null</code> if not initialized
	 */
	public static String getInternalFolder()
	{
		return internal_folder ;
	}


	/**
	 * Return the application width in pixels (must be initialized by ActivityMain).
	 * @return Based on settings or 0 if not initialized
	 */
	public static int getApplicationWidth()
	{
		return application_width ;
	}


	/**
	 * Update the favorites applications list
	 */
	public static void updateFavorites()
	{
		applicationsList.updateFavorites() ;
		adapters_update_needed = true ;
	}


	/**
	 * Update the applications list and inform the user.
	 * @param context Needed to update the list
	 */
	public static void updateList(Context context)
	{
		applicationsList.update(context) ;
		adapters_update_needed = true ;
		ShowDialog.toast(context, R.string.info_list_apps_refreshed) ;
	}


	/**
	 * Update the display in the favorites panel and applications drawer.
	 */
	private void updateAdapters()
	{
		favoritesAdapter.notifyDataSetChanged() ;
		drawerAdapter.notifyDataSetChanged() ;
		adapters_update_needed = false ;
	}


	/**
	 * Detect a click on an element from the activity.
	 * @param view Element clicked
	 */
	public void onClick(View view)
	{
		// Check which view was selected and react accordingly
		int selection = view.getId() ;
		if(selection == R.id.access_menu_button) dialogMenu.show() ;
			else if(selection == R.id.target_favorites) displayFavorites(favorites.getVisibility() != View.VISIBLE) ;
			else if(selection == R.id.target_applications)
			{
				displayFavorites(false) ;
				displayDrawer(true) ;
			}
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
		// Attributes
		private final View homeScreen ;


		/**
		 * Constructor.
		 * @param homeScreen To launch activities with horizontal swipes
		 */
		GestureListener(View homeScreen)
		{
			this.homeScreen = homeScreen ;
		}


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
			float x_distance = event1.getX() - event2.getX() ;
			float y_distance = event1.getY() - event2.getY() ;

			// Check if this is a vertical gesture over a distance and not a single tap
			int swipe_trigger_distance = Math.round(34 * density) ;
			if((Math.abs(y_distance) > Math.abs(x_distance)) && (Math.abs(y_distance) > swipe_trigger_distance))
				{
					// Adapt the gesture direction to the interface direction
					boolean swipe_drawer ;
					if(reverse_interface) swipe_drawer = y_distance < 0 ;
						else swipe_drawer = y_distance > 0 ;

					// Check if the favorites panel should always be shown
					if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false) && swipe_drawer)
						{
							displayDrawer(true) ;
							return true ;
						}

					// Check if the gesture is going up (if) or down (else), based on classic interface
					if(swipe_drawer)
						{
							// Display the applications drawer only if the favorites panel is closed
							if(favorites.getVisibility() == View.VISIBLE) displayFavorites(false) ;
								else displayDrawer(true) ;
						}
						else displayFavorites(true) ;

					// Indicate that the event has been consumed
					return true ;
				}

			// Check if this is an horizontal gesture over a distance and not a single tap
			if((Math.abs(x_distance) > Math.abs(y_distance)) && (Math.abs(x_distance) > swipe_trigger_distance))
				{
					// Check if the swipe is going towards left or right and retrieve the related setting
					String component_info ;
					if(x_distance > 0) component_info = settings.getString(Constants.SWIPE_TOWARDS_LEFT, Constants.NONE) ;
						else component_info = settings.getString(Constants.SWIPE_TOWARDS_RIGHT, Constants.NONE) ;

					// Do not continue if the setting is not set
					if((component_info == null) || component_info.equals(Constants.NONE))
						return false ;

					// Try to start the application and consider the event as consumed
					searchAndStartApplication(component_info) ;
					return true ;
				}

			// Ignore other gestures
			return false ;
		}


		/**
		 * Start an app from the list using its ComponentInfo, or show an error message.
		 */
		private void searchAndStartApplication(String component_info)
		{
			// Search the application in the list
			for(Application application : applicationsList.getApplications(false))
				if(application.getComponentInfo().equals(component_info))
					{
						// Start the application
						application.start(homeScreen) ;
						return ;
					}

			// The application was not found, display an error message
			Context context = homeScreen.getContext() ;
			ShowDialog.toastLong(context, context.getString(R.string.error_app_not_found, component_info)) ;
		}


		/**
		 * Detect a long click on the home screen.
		 * @param event Click point
		 */
		@Override
		public void onLongPress(MotionEvent event)
		{
			// Update the display according to settings
			if(settings.getBoolean(Constants.IMMERSIVE_MODE, false)) displaySystemBars(false) ;
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
			// ========= Appearance settings ==========
			case Constants.BACKGROUND_COLOR :
			case Constants.HIDE_MENU_BUTTON :
				// Force update of favorites panel color if it is always shown
				if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false))
					displayFavorites(true) ;
				break ;
			case Constants.APPLICATION_THEME :
				// Update the theme
				setApplicationTheme() ;
				break ;
			case Constants.ICON_PACK :
				// Update the applications list
				updateList(this) ;
				break ;
			case Constants.HIDE_APP_NAMES :
			case Constants.REMOVE_PADDING :
				// Update the column width
				recreate() ;
				break ;
			// ========= Operation settings ==========
			case Constants.NOTIFICATION :
				// Toggle the notification
				if(settings.getBoolean(Constants.NOTIFICATION, true)) notification.display(this) ;
					else notification.hide() ;
				break ;
			case Constants.REVERSE_INTERFACE:
				// Change the interface direction
				reverse_interface = settings.getBoolean(Constants.REVERSE_INTERFACE, false) ;
				if(reverse_interface) setContentView(R.layout.activity_main_reverse) ;
					else setContentView(R.layout.activity_main) ;
				recreate() ;
				break ;
			case Constants.ALWAYS_SHOW_FAVORITES :
				// Cover cases where this setting is enabled while favorites were closed
				if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false)) displayFavorites(true) ;
			case Constants.TOUCH_TARGETS :
			case Constants.DISABLE_APP_DRAWER :
				// Make safe-check and display or not the touch targets
				keepMenuAccessible() ;
				toggleTouchTargets() ;
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
		 * @param newState 1 (Active scrolling) then 2 (Scrolling inerty) then 0 (Not scrolling)
		 */
		@Override
		public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
		{
			// Let the parent actions be performed
			super.onScrollStateChanged(recyclerView, newState) ;

			// Keep track of the state to avoid accidental drawer closure
			switch(newState)
			{
				case RecyclerView.SCROLL_STATE_DRAGGING :
					if(drawer_close_gesture == 0) drawer_close_gesture = 1 ;
						else drawer_close_gesture = 0 ;
					break ;
				case RecyclerView.SCROLL_STATE_SETTLING :
					if(drawer_close_gesture == 1) drawer_close_gesture = 2 ;
						else drawer_close_gesture = 0 ;
					break ;
				case RecyclerView.SCROLL_STATE_IDLE :
					if(drawer_close_gesture == 2) drawer_close_gesture = 3 ;
						else drawer_close_gesture = 0 ;
			}

			// Wait for the gesture to be finished
			if(newState == RecyclerView.SCROLL_STATE_IDLE)
				{
					// If the scrolling is stuck on top, close the drawer activity
					if((drawer_position == 0) && (drawer_last_position == 0) && (drawer_close_gesture == 3))
						displayDrawer(false) ;

					// Consider the gesture finished
					if(drawer_close_gesture == 3) drawer_close_gesture = 0 ;

					// Update the last position to detect the stuck state
					drawer_last_position = drawer_position ;
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

		// Always show the system bars
		displaySystemBars(true) ;

		// Hide popups if some are still opened
		for(Application application : applicationsList.getDrawer())
		{
			if(application instanceof Folder) ((Folder)application).closePopup() ;
			if(application instanceof Search) ((Search)application).closePopup() ;
		}
	}


	/**
	 * Perform actions when the user come back to the home screen.
	 */
	@Override
	public void onResume()
	{
		// Let the parent actions be performed
		super.onResume() ;

		// Hide the favorites panel and the applications drawer
		keepMenuAccessible() ;
		displayFavorites(false) ;
		displayDrawer(false) ;

		// Update the display according to settings
		dialogMenu.hide() ;
		togglePortraitMode() ;
		toggleTouchTargets() ;
		if(settings.getBoolean(Constants.IMMERSIVE_MODE, false)) displaySystemBars(false) ;

		// Update the favorites panel and applications drawer display if needed
		if(adapters_update_needed) updateAdapters() ;
	}


	/**
	 * Perform actions when the activity is destroyed.
	 */
	@Override
	public void onDestroy()
	{
		// Unregister all remaining broadcast receivers
		if(packagesListener != null) unregisterReceiver(packagesListener) ;
		if(shortcutLegacyListener != null) unregisterReceiver(shortcutLegacyListener) ;

		// Let the parent actions be performed
		super.onDestroy() ;
	}
}
