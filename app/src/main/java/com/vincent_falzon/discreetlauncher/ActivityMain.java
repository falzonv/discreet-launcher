package com.vincent_falzon.discreetlauncher ;

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
import android.annotation.SuppressLint ;
import android.app.role.RoleManager ;
import android.content.Context ;
import android.content.Intent ;
import android.content.SharedPreferences ;
import android.content.pm.ActivityInfo ;
import android.graphics.drawable.Drawable ;
import android.os.Build ;
import android.os.Bundle ;
import androidx.activity.result.ActivityResultLauncher ;
import androidx.activity.result.contract.ActivityResultContracts ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.appcompat.app.AppCompatDelegate ;
import androidx.appcompat.content.res.AppCompatResources ;
import androidx.core.content.ContextCompat ;
import androidx.core.graphics.drawable.DrawableCompat ;
import androidx.core.view.GestureDetectorCompat ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import android.view.GestureDetector ;
import android.view.MotionEvent ;
import android.view.View ;
import android.widget.ImageView ;
import android.widget.RelativeLayout ;
import android.widget.TextView ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.ApplicationsList ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.core.Search ;
import com.vincent_falzon.discreetlauncher.events.ShortcutLegacyListener ;
import com.vincent_falzon.discreetlauncher.events.PackagesListener ;
import com.vincent_falzon.discreetlauncher.menu.DialogMenu ;
import com.vincent_falzon.discreetlauncher.quickaccess.NotificationDisplayer ;

/**
 * Main activity managing the home screen and app drawer.
 */
public class ActivityMain extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener
{
	// Constants
	private static final String TAG = "ActivityMain" ;

	// Attributes
	private static ApplicationsList applicationsList ;
	private static boolean skip_list_update ;
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
	private int scroll_position ;
	private int scroll_last_position ;
	private int scroll_close_gesture ;
	private boolean reverse_interface ;

	// Attributes related to the home screen
	private RelativeLayout homeScreen ;
	private RecyclerView favorites ;
	private RecyclerAdapter favoritesAdapter ;
	private GridLayoutManager favoritesLayout ;
	private ImageView menuButton ;
	private TextView noFavoritesYet ;
	private TextView targetFavorites ;
	private TextView targetApplications ;
	private TextView defaultLauncherButton ;
	private View infoTouchTarget ;

	// Attributes related to the drawer
	private RecyclerView drawer ;
	private RecyclerAdapter drawerAdapter ;
	private GridLayoutManager drawerLayout ;

	// Activity Results components
	private final ActivityResultLauncher<Intent> activityResultLauncher =
		registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> { }) ;

	
	/**
	 * Constructor.
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
		skip_list_update = false ;

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
		noFavoritesYet = findViewById(R.id.info_no_favorites_yet) ;
		defaultLauncherButton = findViewById(R.id.default_launcher_button) ;
		infoTouchTarget = findViewById(R.id.info_touch_targets) ;
		drawer = findViewById(R.id.drawer) ;
		menuButton = findViewById(R.id.access_menu_button) ;
		targetFavorites = findViewById(R.id.target_favorites) ;
		targetApplications = findViewById(R.id.target_applications) ;
		menuButton.setOnClickListener(this) ;
		targetFavorites.setOnClickListener(this) ;
		targetApplications.setOnClickListener(this) ;
		defaultLauncherButton.setOnClickListener(this) ;
		gestureDetector = new GestureDetectorCompat(this, new GestureListener(homeScreen)) ;

		// Starting with Android 12, disable the elastic animation on favorites and drawer
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			{
				favorites.setOverScrollMode(View.OVER_SCROLL_NEVER) ;
				drawer.setOverScrollMode(View.OVER_SCROLL_NEVER) ;
			}

		// If it does not exist yet, build the applications list
		if(applicationsList == null)
			{
				applicationsList = new ApplicationsList() ;
				applicationsList.update(this) ;
			}

		// Prepare the notification
		notification = new NotificationDisplayer(this) ;
		if(settings.getBoolean(Constants.NOTIFICATION, true)) notification.display(this) ;
			else notification.hide() ;

		// Define the width of an application item
		int app_size_pixels = Utils.getIconSize(this, settings) ;
		int padding_pixels ;
		if(settings.getBoolean(Constants.HIDE_APP_NAMES, false))
				padding_pixels = settings.getBoolean(Constants.REMOVE_PADDING, false) ? 0 : Math.round(25 * density) ;
			else padding_pixels = Math.round(Math.min(app_size_pixels / 1.5f, 32 * density)) ;
		application_width = app_size_pixels + padding_pixels ;

		// Update the display according to settings
		maybeForceOrientation() ;
		keepMenuAccessible() ;
		toggleTouchTargets() ;
		maybeHideSystemBars(false) ;

		// Initialize the content of the favorites panel
		favoritesAdapter = new RecyclerAdapter(this, applicationsList.getFavorites(), Constants.FAVORITES_PANEL) ;
		favoritesAdapter.setTextColor(Utils.getColor(settings, Constants.TEXT_COLOR_FAVORITES, Constants.COLOR_FOR_TEXT_ON_OVERLAY)) ;
		favorites.setAdapter(favoritesAdapter) ;
		favoritesLayout = new FlexibleGridLayout(this, application_width) ;
		favorites.setLayoutManager(favoritesLayout) ;
		favorites.addOnScrollListener(new ScrollListener(Constants.FAVORITES_PANEL)) ;

		// Initialize the content of the full applications list
		drawerAdapter = new RecyclerAdapter(this, applicationsList.getDrawer(), Constants.APP_DRAWER) ;
		drawerAdapter.setTextColor(Utils.getColor(settings, Constants.TEXT_COLOR_DRAWER, Constants.COLOR_FOR_TEXT_ON_OVERLAY)) ;
		drawer.setAdapter(drawerAdapter) ;
		drawerLayout = new FlexibleGridLayout(this, application_width) ;
		drawer.setLayoutManager(drawerLayout) ;
		drawer.addOnScrollListener(new ScrollListener(Constants.APP_DRAWER)) ;

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
		// Retrieve the selected theme (if any) and react accordingly
		String theme = settings.getString(Constants.APPLICATION_THEME, Constants.NONE) ;
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
	 * Maybe apply a forced orientation according to the settings.
	 */
	@SuppressLint("SourceLockedOrientationActivity")
	private void maybeForceOrientation()
	{
		// Retrieve the selected orientation and apply it
		String forced_orientation = settings.getString(Constants.FORCED_ORIENTATION, Constants.NONE) ;
		switch(forced_orientation)
		{
			case "portrait" :
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) ;
				break ;
			case "landscape" :
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) ;
				break ;
			case "reverse_landscape" :
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) ;
				break ;
			default :
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) ;
				break ;
		}
	}


	/**
	 * Enable or disable the touch targets according to the settings.
	 */
	private void toggleTouchTargets()
	{
		// Check the user preference for touch targets
		if(settings.getBoolean(Constants.TOUCH_TARGETS, false))
			{
				// Display or not touch targets according to the other settings
				infoTouchTarget.setContentDescription(null) ;
				if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false))
						targetFavorites.setVisibility(View.GONE) ;
					else targetFavorites.setVisibility(View.VISIBLE) ;
				if(settings.getBoolean(Constants.DISABLE_APP_DRAWER, false))
						targetApplications.setVisibility(View.GONE) ;
					else targetApplications.setVisibility(View.VISIBLE) ;
			}
			else
			{
				// Hide touch targets
				targetFavorites.setVisibility(View.GONE) ;
				targetApplications.setVisibility(View.GONE) ;
				infoTouchTarget.setContentDescription(getString(R.string.help_touch_targets)) ;
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

		// Check if the menu button is visible
		if(!settings.getBoolean(Constants.HIDE_MENU_BUTTON, false)) return ;

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

		// If the drawer cannot be safely disabled, display a message and disable the setting
		if(settings.getBoolean(Constants.DISABLE_APP_DRAWER, false))
			{
				Utils.displayLongToast(this, getString(R.string.error_disable_app_drawer_not_safe)) ;
				SharedPreferences.Editor editor = settings.edit() ;
				editor.putBoolean(Constants.DISABLE_APP_DRAWER, false).apply() ;
				return ;
			}

		// Retrieve the total height available in portrait mode (navigation bar automatically removed)
		int menu_button_height = Math.round(32 * density) ;
		int total_size = Math.max(getResources().getDisplayMetrics().heightPixels, getResources().getDisplayMetrics().widthPixels)
				- Math.round(25 * density)	// Remove 25dp for the status bar
				- Math.round(20 * density)  // Remove 20dp for button margins and spare
				- menu_button_height ;

		// Define the size of an app (icon + margins + text estimation) and the maximum number of favorites
		int app_size = Utils.getIconSize(this, settings) ;
		if(!settings.getBoolean(Constants.REMOVE_PADDING, false)) app_size += Math.round(20 * density) ;
		if(!settings.getBoolean(Constants.HIDE_APP_NAMES, false)) app_size += menu_button_height ;
		int max_favorites = Math.min(4, getResources().getDisplayMetrics().widthPixels / application_width) * (total_size / app_size) ;

		// Check if the number of favorites still allows to see the menu button
		if(applicationsList.getFavorites().size() <= max_favorites) return ;

		// If favorites cannot be always shown safely, display a message and disable the setting
		if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false))
			{
				Utils.displayLongToast(this, getString(R.string.error_always_show_favorites_not_safe)) ;
				SharedPreferences.Editor editor = settings.edit() ;
				editor.putBoolean(Constants.ALWAYS_SHOW_FAVORITES, false).apply() ;
			}
	}


	/**
	 * Display or hide the favorites panel.
	 */
	private void displayFavorites(boolean display)
	{
		if(display)
			{
				// Update the recyclers (favorites panel and applications drawer) if needed
				if(adapters_update_needed) updateAdapters() ;

				// Starting with Android 10, maybe display the default launcher button
				boolean show_default_launcher_button = false ;
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
					{
						RoleManager roleManager = (RoleManager)getSystemService(Context.ROLE_SERVICE) ;
						if(roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME))
							show_default_launcher_button = true ;
					}
				defaultLauncherButton.setVisibility(show_default_launcher_button ? View.VISIBLE : View.GONE) ;

				// Display a message if the user does not have any favorites applications yet
				if(applicationsList.getFavorites().size() == 0) noFavoritesYet.setVisibility(View.VISIBLE) ;
					else noFavoritesYet.setVisibility(View.GONE) ;

				// Retrieve the background color
				int background_color = Utils.getColor(settings, Constants.BACKGROUND_COLOR_FAVORITES, Constants.COLOR_FOR_OVERLAY) ;

				// Check if the interface is reversed and adjust the display accordingly
				Drawable tab_shape ;
				if(reverse_interface)
					{
						// Reversed interface
						tab_shape = AppCompatResources.getDrawable(this, R.drawable.shape_tab_reverse) ;
						getWindow().setNavigationBarColor(background_color) ;
					}
					else
					{
						// Classic interface
						tab_shape = AppCompatResources.getDrawable(this, R.drawable.shape_tab) ;
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
				favorites.setBackgroundColor(background_color) ;
				noFavoritesYet.setBackgroundColor(background_color) ;

				// If the option is selected, hide the menu button
				if(settings.getBoolean(Constants.HIDE_MENU_BUTTON, false)) menuButton.setVisibility(View.GONE) ;
					else menuButton.setVisibility(View.VISIBLE) ;

				// Display the favorites panel
				scroll_position = 0 ;
				scroll_last_position = 0 ;
				scroll_close_gesture = 0 ;
				favorites.setVisibility(View.VISIBLE) ;
				targetFavorites.setText(R.string.target_close_favorites) ;
			}
			else
			{
				// Do not continue if the option to always show the panel is selected
				if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false)) return ;

				// Hide the favorites panel
				favorites.setVisibility(View.GONE) ;
				menuButton.setVisibility(View.GONE) ;
				noFavoritesYet.setVisibility(View.GONE) ;
				defaultLauncherButton.setVisibility(View.GONE) ;
				targetFavorites.setText(R.string.target_open_favorites) ;

				// If the option is selected, make the status bar fully transparent
				if(settings.getBoolean(Constants.TRANSPARENT_STATUS_BAR, true))
						getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent)) ;
					else getWindow().setStatusBarColor(Utils.getColor(settings, Constants.BACKGROUND_COLOR_FAVORITES, Constants.COLOR_FOR_OVERLAY)) ;

				// Make the navigation bar transparent
				getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.transparent)) ;
			}
	}


	/**
	 * Display or hide the applications drawer.
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
				int background_color = Utils.getColor(settings, Constants.BACKGROUND_COLOR_DRAWER, Constants.COLOR_FOR_OVERLAY) ;
				drawer.setBackgroundColor(background_color) ;
				getWindow().setStatusBarColor(background_color) ;
				getWindow().setNavigationBarColor(background_color) ;

				// Display the applications drawer
				if(reverse_interface) scroll_position = applicationsList.getDrawer().size() - 1 ;
					else scroll_position = 0 ;
				scroll_last_position = scroll_position ;
				scroll_close_gesture = 0 ;
				homeScreen.setVisibility(View.GONE) ;
				drawer.setVisibility(View.VISIBLE) ;
				targetApplications.setText(R.string.target_close_apps) ;
			}
			else
			{
				// Hide the applications drawer
				if(reverse_interface) drawer.scrollToPosition(applicationsList.getDrawer().size() - 1) ;
					else drawer.scrollToPosition(0) ;
				homeScreen.setVisibility(View.VISIBLE) ;
				drawer.setVisibility(View.GONE) ;
				targetApplications.setText(R.string.target_open_apps) ;

				// Retrieve the background color for favorites
				int favorites_background_color = Utils.getColor(settings, Constants.BACKGROUND_COLOR_FAVORITES, Constants.COLOR_FOR_OVERLAY) ;

				// If the option is selected, make the status bar fully transparent
				if(settings.getBoolean(Constants.TRANSPARENT_STATUS_BAR, true))
						getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.transparent)) ;
					else getWindow().setStatusBarColor(favorites_background_color) ;

				// Make the navigation bar transparent, unless in reverse interface with favorites always shown
				if(reverse_interface && settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false))
						getWindow().setNavigationBarColor(favorites_background_color) ;
					else getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.transparent)) ;
			}
	}


	/**
	 * Display or hide the system bars based on settings, also sets and unsets the dark icons.
	 */
	private void maybeHideSystemBars(boolean force_display)
	{
		// Check if the status bar icons must be displayed darker
		int dark_icons_flag = 0 ;
		if(settings.getBoolean(Constants.DARK_STATUS_BAR_ICONS, false))
			{
				// Use dark icons wherever it is possible
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) dark_icons_flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR ;
					else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) dark_icons_flag = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR ;
			}

		// Check if the system bars must be hidden (immersive mode)
		int immersive_mode_flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE ;
		if(!force_display && settings.getBoolean(Constants.IMMERSIVE_MODE, false))
			immersive_mode_flag = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION ;

		// Update the system bars flags
		getWindow().getDecorView().setSystemUiVisibility(immersive_mode_flag | dark_icons_flag) ;
	}


	/**
	 * Return the list of applications.
	 */
	public static ApplicationsList getApplicationsList()
	{
		return applicationsList ;
	}


	/**
	 * Allow to skip updates of the list of applications when editing some settings.
	 */
	public static void setSkipListUpdate(boolean skip)
	{
		skip_list_update = skip ;
	}


	/**
	 * Return the internal files location (<code>null </code> if not initialized by ActivityMain).
	 */
	public static String getInternalFolder()
	{
		return internal_folder ;
	}


	/**
	 * Return the application width in pixels (0 if not initialized by ActivityMain).
	 */
	public static int getApplicationWidth()
	{
		return application_width ;
	}


	/**
	 * Update the list of favorite applications (provide <code>null</code> to hide the message).
	 */
	public static void updateFavorites(Context context)
	{
		applicationsList.updateFavorites() ;
		adapters_update_needed = true ;
		if(context != null)
			Utils.displayToast(context, R.string.info_favorites_refreshed) ;
	}


	/**
	 * Update the applications list and inform the user.
	 */
	public static void updateList(Context context)
	{
		if(skip_list_update) return ;
		applicationsList.update(context) ;
		adapters_update_needed = true ;
		Utils.displayToast(context, R.string.info_list_apps_refreshed) ;
	}


	/**
	 * Update the display in the favorites panel and applications drawer.
	 */
	@SuppressLint("NotifyDataSetChanged")
	private void updateAdapters()
	{
		favoritesAdapter.notifyDataSetChanged() ;
		drawerAdapter.notifyDataSetChanged() ;
		adapters_update_needed = false ;
	}


	/**
	 * Called when an element is clicked.
	 */
	public void onClick(View view)
	{
		// Check which view was selected and react accordingly
		int selection = view.getId() ;
		if(selection == R.id.access_menu_button) dialogMenu.show() ;
			else if(selection == R.id.target_favorites) displayFavorites(favorites.getVisibility() != View.VISIBLE) ;
			else if(selection == R.id.target_applications) displayDrawer(drawer.getVisibility() != View.VISIBLE) ;
			else if(selection == R.id.default_launcher_button)
			{
				// Starting with Android 10, display the list of available launchers
				if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
					{
						RoleManager roleManager = (RoleManager)getSystemService(Context.ROLE_SERVICE) ;
						activityResultLauncher.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)) ;
					}
			}
	}


	/**
	 * Detect a user action on the screen and process it.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		gestureDetector.onTouchEvent(event) ;
		return super.onTouchEvent(event) ;
	}


	/**
	 * Called when displaying the home screen, including on app creation.
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
		maybeForceOrientation() ;
		toggleTouchTargets() ;
		maybeHideSystemBars(false) ;
		if(settings.getBoolean(Constants.ALWAYS_SHOW_FAVORITES, false)) displayFavorites(true) ;

		// Update the favorites panel and applications drawer display if needed
		if(adapters_update_needed) updateAdapters() ;
	}


	/**
	 * Close the app drawer or favorites if opened, otherwise do nothing.
	 */
	@Override
	public void onBackPressed()
	{
		if(drawer.getVisibility() == View.VISIBLE) displayDrawer(false) ;
			else if(favorites.getVisibility() == View.VISIBLE) displayFavorites(false) ;
	}


	/**
	 * Called before leaving the home screen.
	 */
	@Override
	public void onPause()
	{
		// Let the parent actions be performed
		super.onPause() ;

		// Always show the system bars
		maybeHideSystemBars(true) ;

		// Hide popups if some are still opened
		for(Application application : applicationsList.getDrawer())
		{
			if(application instanceof Folder) ((Folder)application).closePopup() ;
			if(application instanceof Search) ((Search)application).closePopup() ;
		}
	}


	/**
	 * Listen for changes in the settings and react accordingly.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		if(key == null) return ;
		Utils.logDebug(TAG, "preference \"" + key + "\" has changed") ;
		switch(key)
		{
			// ========= Appearance settings ==========
			case Constants.APPLICATION_THEME :
				// Update the theme
				setApplicationTheme() ;
				break ;
			case Constants.ICON_SIZE_DP :
				// Update the applications list and column width
				updateList(this) ;
				recreate() ;
				break ;
			case Constants.HIDE_APP_NAMES :
			case Constants.HIDE_FOLDER_NAMES :
			case Constants.REMOVE_PADDING :
				// Update the column width
				recreate() ;
				break ;
			case Constants.TEXT_COLOR_FAVORITES :
				// Update the text color of the favorites panel
				favoritesAdapter.setTextColor(Utils.getColor(settings, Constants.TEXT_COLOR_FAVORITES, Constants.COLOR_FOR_TEXT_ON_OVERLAY)) ;
				break ;
			case Constants.TEXT_COLOR_DRAWER :
				// Update the text color of the drawer
				drawerAdapter.setTextColor(Utils.getColor(settings, Constants.TEXT_COLOR_DRAWER, Constants.COLOR_FOR_TEXT_ON_OVERLAY)) ;
				break ;
			case Constants.ICON_PACK :
			case Constants.ICON_PACK_SECONDARY :
			case Constants.ICON_COLOR_FILTER :
				// Update the applications list
				updateList(this) ;
				break ;
			// ========= Operation settings ==========
			case Constants.NOTIFICATION :
				// Toggle the notification
				if(settings.getBoolean(Constants.NOTIFICATION, true))
					{
						if(notification.isAllowed()) notification.display(this) ;
							else Utils.displayLongToast(this, getString(R.string.set_notification_warning_disabled)) ;
					}
					else notification.hide() ;
				break ;
			case Constants.REVERSE_INTERFACE :
				// Change the interface direction
				reverse_interface = settings.getBoolean(Constants.REVERSE_INTERFACE, false) ;
				if(reverse_interface) setContentView(R.layout.activity_main_reverse) ;
					else setContentView(R.layout.activity_main) ;
				recreate() ;
				updateList(this) ;
				break ;
		}
	}


	/**
	 * Called when this activity is destroyed.
	 */
	@Override
	public void onDestroy()
	{
		// Unregister all remaining broadcast receivers
		if(packagesListener != null) unregisterReceiver(packagesListener) ;
		if(shortcutLegacyListener != null) unregisterReceiver(shortcutLegacyListener) ;
		settings.unregisterOnSharedPreferenceChangeListener(this) ;
		activityResultLauncher.unregister() ;

		// Let the parent actions be performed
		super.onDestroy() ;
	}


	// ---------------------------------------------------------------------------------------------

	/**
	 * Detect and recognize a gesture on the home screen.
	 */
	class GestureListener extends GestureDetector.SimpleOnGestureListener
	{
		// Attributes
		private final View homeScreen ;


		/**
		 * Constructor.
		 */
		GestureListener(View homeScreen)
		{
			this.homeScreen = homeScreen ;
		}


		/**
		 * Implemented because all gestures start with an onDown() message.
		 */
		@Override
		public boolean onDown(@NonNull MotionEvent event)
		{
			return true ;
		}


		/**
		 * Detect a gesture over a distance.
		 */
		@Override
		public boolean onFling(MotionEvent event1, @NonNull MotionEvent event2, float velocityX, float velocityY)
		{
			// Ignore the gesture if the applications drawer is opened
			if(drawer.getVisibility() == View.VISIBLE) return false ;

			// Ignore incomplete gestures (may happen due to the interactive clock)
			if(event1 == null) return false ;

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
					// Check if the swipe is going towards left or right
					String swipe_direction_setting_key ;
					if(x_distance > 0) swipe_direction_setting_key = Constants.SWIPE_LEFTWARDS ;
						else swipe_direction_setting_key = Constants.SWIPE_RIGHTWARDS ;

					// Try to start the related application
					return Utils.searchAndStartApplication(homeScreen, settings, swipe_direction_setting_key) ;
				}

			// Ignore other gestures
			return false ;
		}


		/**
		 * Detect a double-tap.
		 */
		@Override
		public boolean onDoubleTap(@NonNull MotionEvent event)
		{
			return Utils.searchAndStartApplication(homeScreen, settings, Constants.DOUBLE_TAP) ;
		}


		/**
		 * Detect a long click on the home screen.
		 */
		@Override
		public void onLongPress(@NonNull MotionEvent event)
		{
			// Update the display according to settings
			maybeHideSystemBars(false) ;
		}
	}


	// ---------------------------------------------------------------------------------------------

	/**
	 * Listen for scrolls on the app drawer or the favorites panel.
	 */
	class ScrollListener extends RecyclerView.OnScrollListener
	{
		// Attributes
		private final int target ;


		/**
		 * Constructor (target says if we listen on the app drawer or the favorites panel).
		 */
		ScrollListener(int target)
		{
			this.target = target ;
		}


		/**
		 * When the scrolling ends, check if it is stuck on top/bottom.
		 */
		@Override
		public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
		{
			// Let the parent actions be performed
			super.onScrollStateChanged(recyclerView, newState) ;

			// Keep track of the state to limit accidental closures
			switch(newState)
			{
				case RecyclerView.SCROLL_STATE_DRAGGING : // Active scrolling
					if(scroll_close_gesture == 0) scroll_close_gesture = 1 ;
						else scroll_close_gesture = 0 ;
					break ;
				case RecyclerView.SCROLL_STATE_SETTLING : // Scrolling inerty
					if(scroll_close_gesture == 1) scroll_close_gesture = 2 ;
						else scroll_close_gesture = 0 ;
					break ;
				case RecyclerView.SCROLL_STATE_IDLE : // Not scrolling
					if(scroll_close_gesture == 2) scroll_close_gesture = 3 ;
						else scroll_close_gesture = 0 ;
			}

			// Wait for the gesture to be finished
			if(newState == RecyclerView.SCROLL_STATE_IDLE)
				{
					// Check if this was a complete closure gesture
					if(scroll_close_gesture == 3)
						{
							// Check if the scrolling is stuck
							if(scroll_last_position == scroll_position)
								{
									// Check if we are listening the scroll of the favorites panel or the app drawer
									if(target == Constants.FAVORITES_PANEL)
										{
											// If the scrolling is stuck on top, close the favorites panel
											if(scroll_position == 0) displayFavorites(false) ;
										}
										else
										{
											// If the scrolling is stuck on bottom/top (based on layout), close the app drawer
											if(reverse_interface)
												{
													if(scroll_position == (applicationsList.getDrawer().size() - 1)) displayDrawer(false) ;
												}
												else
												{
													if(scroll_position == 0) displayDrawer(false) ;
												}
										}
								}

							// Reset the closure gesture tracking
							scroll_close_gesture = 0 ;
						}

					// Update the last position to detect the stuck state
					scroll_last_position = scroll_position ;
				}
		}


		/**
		 * Update the position of the first/last visible item as the user is scrolling.
		 */
		@Override
		public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
		{
			// Let the parent actions be performed
			super.onScrolled(recyclerView, dx, dy) ;

			// Check if we are listening the scroll of the favorites panel or the app drawer
			if(target == Constants.FAVORITES_PANEL)
				{
					// Update the position of the first visible item
					scroll_position = favoritesLayout.findFirstCompletelyVisibleItemPosition() ;
				}
				else
				{
					// Update the position of the last/first visible item (based on layout)
					if(reverse_interface) scroll_position = drawerLayout.findLastCompletelyVisibleItemPosition() ;
						else scroll_position = drawerLayout.findFirstCompletelyVisibleItemPosition() ;
				}
		}
	}
}
