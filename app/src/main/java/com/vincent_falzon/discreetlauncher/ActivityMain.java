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
import android.content.DialogInterface ;
import android.content.Intent ;
import android.content.pm.PackageManager ;
import android.content.pm.ResolveInfo ;
import android.os.Bundle ;
import androidx.core.view.GestureDetectorCompat ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import android.view.ContextMenu ;
import android.view.GestureDetector ;
import android.view.MenuInflater ;
import android.view.MenuItem ;
import android.view.MotionEvent ;
import android.view.View ;
import android.widget.LinearLayout ;
import android.widget.Toast ;
import java.text.SimpleDateFormat ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator ;
import java.util.Date ;
import java.util.List ;

/**
 * Main class and home screen activity.
 */
public class ActivityMain extends AppCompatActivity
{
	// Global attributes
	private static ArrayList<Application> global_applicationsList ;
	private static ArrayList<Application> global_favoritesList ;
	private static String global_list_last_update;
	
	// Attributes
	private Intent drawerActivityLauncher ;
	private GestureDetectorCompat detector ;
	private RecyclerAdapter adapter ;
	private LinearLayout favoritesPanel ;
	private InternalFile file ;

	
	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Call the constructor of the parent class
		super.onCreate(savedInstanceState) ;

		// Initializations
		setContentView(R.layout.activity_main) ;
		detector = new GestureDetectorCompat(this, new GestureListener()) ;
		file = new InternalFile(this, "favorites.txt") ;
		registerForContextMenu(findViewById(R.id.access_menu_button)) ;

		// Build the applications lists
		updateApplicationsList() ;
		updateFavoritesList() ;
		
		// Prepare the display of the favorites panel over 4 columns
		RecyclerView recycler = findViewById(R.id.favorites_applications) ;
		adapter = new RecyclerAdapter(this, true) ;
		recycler.setAdapter(adapter) ;
		recycler.setLayoutManager(new GridLayoutManager(this, 4)) ;
		favoritesPanel = findViewById(R.id.favorites_panel) ;
		favoritesPanel.setVisibility(View.GONE) ;
		adapter.notifyDataSetChanged() ;

		// Display a message if the user doesn't have any favorites applications yet
		if(global_favoritesList.size() == 0)
			displayToast(R.string.text_no_favorites_yet, Toast.LENGTH_LONG) ;

		// Prepare the Drawer activity launcher
		drawerActivityLauncher = new Intent() ;
		drawerActivityLauncher.setClass(this, ActivityDrawer.class) ;
	}
	
	
	/**
	 * Build the applications list and sort them in alphabetic order.
	 */
	private void updateApplicationsList()
	{
		// Initializations
		PackageManager apkManager = getPackageManager() ;
		if(global_applicationsList == null) global_applicationsList = new ArrayList<>() ;
			else global_applicationsList.clear() ;
			
		// Retrieve the list of applications that can be launched by the user
		Intent intent = new Intent(Intent.ACTION_MAIN) ;
		intent.addCategory(Intent.CATEGORY_LAUNCHER) ;
		List<ResolveInfo> apkManagerList = apkManager.queryIntentActivities(intent, 0) ;

		// Defin the icons size in pixels
		int icon_size_px = Math.round(48 * getResources().getDisplayMetrics().density) ;

		// Browse the APK manager list and store the data of each application in the main list
		for(ResolveInfo entry:apkManagerList)
		{
			Application application = new Application(
					entry.loadLabel(apkManager).toString(),
					entry.activityInfo.packageName,
					entry.loadIcon(apkManager)) ;
			application.getIcon().setBounds(0, 0, icon_size_px, icon_size_px) ;
			global_applicationsList.add(application) ;
		}

		// Sort applications in alphabetic order
		Collections.sort(global_applicationsList, new Comparator<Application>()
		{
			@Override
			public int compare(Application application1, Application application2)
			{
				return application1.getName().compareToIgnoreCase(application2.getName()) ;
			}
		}) ;

		// Save the last update timestamp
		global_list_last_update = SimpleDateFormat.getDateTimeInstance().format(new Date()) ;
	}


	/**
	 * Built the favorites applications list.
	 */
	private void updateFavoritesList()
	{
		// Initializations
		if(global_favoritesList == null) global_favoritesList = new ArrayList<>() ;
			else global_favoritesList.clear() ;

		// Check if the favorites file exists
		if(file.isExisting())
			{
				// Retrieve and browse the APK identifiers of all favorites applications
				for(String nom : file.readAllLines())
				{
					// Search the APK identifier in the applications list
					for(Application application : global_applicationsList)
						if(application.getApk().equals(nom))
						{
							// Add the application to the favorites and move to the next APK
							global_favoritesList.add(application) ;
							break ;
						}
				}
			}
	}


	/**
	 * Return the timestamp of the last time the applications list was updated.
	 * @return Date and time in text format
	 */
	public static String getListLastUpdate()
	{
		return global_list_last_update ;
	}


	/**
	 * Return the list of applications.
	 * @return Displayed in the Drawer activity
	 */
	public static ArrayList<Application> getApplicationsList()
	{
		return global_applicationsList ;
	}


	/**
	 * Return the list of favorites applications.
	 * @return Displayed in the favorites panel
	 */
	public static ArrayList<Application> getFavoritesList()
	{
		return global_favoritesList ;
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
				updateApplicationsList() ;
				displayToast(R.string.text_applications_list_refreshed) ;
				return true ;
			}
			else if(selection == R.id.menu_action_manage_favorites)
			{
				// Display a menu to select the favorites applications
				displayManageFavoritesDialog() ;
				return true ;
			}
			else if(selection == R.id.menu_action_about)
			{
				// Display the About information
				displayAboutDialog() ;
				return true ;
			}
		return false ;
	}


	/**
	 * Display a Toast with a custom message and a custom duration.
	 * @param message In R.string format
	 * @param length Toast.LENGTH_SHORT or Toast.LENGTH_LONG
	 */
	private void displayToast(int message, int length)
	{
		Toast.makeText(getApplicationContext(), message, length).show() ;
	}


	/**
	 * Display a Toast with a custom message and a short duration.
	 * @param message In R.string format
	 */
	private void displayToast(int message)
	{
		displayToast(message, Toast.LENGTH_SHORT) ;
	}


	/**
	 * Prepare and display the favorites applications management dialog.
	 */
	private void displayManageFavoritesDialog()
	{
		// List the names of all applications
		CharSequence[] app_names = new CharSequence[global_applicationsList.size()] ;
		int i = 0 ;
		for(Application application : global_applicationsList)
		{
			app_names[i] = application.getName() ;
			i++ ;
		}

		// Retrieve the current favorites applications
		final boolean[] selected = new boolean[app_names.length] ;
		if(file.isExisting())
			for(i = 0 ; i < app_names.length ; i++)
				selected[i] = file.isLineExisting(global_applicationsList.get(i).getApk()) ;
		else for(i = 0 ; i < app_names.length ; i++)
			selected[i] = false ;

		// Prepare and display the selection dialog
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
								displayToast(R.string.error_remove_favorites) ;
								return ;
							}

						// Write the new favorites list
						for(i = 0 ; i < selected.length ; i++)
						{
							if(selected[i])
								if(!file.writeLine(global_applicationsList.get(i).getApk()))
									{
										String message = getString(R.string.error_add_new_favorite, global_applicationsList.get(i).getApk()) ;
										Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show() ;
										return ;
									}
						}

						// Update the favorites panel and inform the user
						updateFavoritesList() ;
						adapter.notifyDataSetChanged() ;
						displayToast(R.string.text_favorites_saved) ;
					}
				}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.show() ;
	}


	/**
	 * Prepare and display the About dialog.
	 */
	private void displayAboutDialog()
	{
		AlertDialog.Builder dialog = new AlertDialog.Builder(this) ;
		dialog.setTitle(R.string.button_about) ;
		dialog.setView(R.layout.about) ;
		dialog.setNeutralButton(R.string.button_close, null) ;
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
							}
							else
							{
								// Open the applications drawer
								startActivity(drawerActivityLauncher) ;
							}
					}
					else
					{
						// Going down, open the favorites panel
						favoritesPanel.setVisibility(View.VISIBLE) ;
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
}
