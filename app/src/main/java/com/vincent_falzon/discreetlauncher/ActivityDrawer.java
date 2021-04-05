package com.vincent_falzon.discreetlauncher;

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
import android.content.Intent ;
import android.content.IntentFilter ;
import android.os.Build ;
import android.os.Bundle ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import android.widget.TextView ;

/**
 * Applications drawer activity.
 */
public class ActivityDrawer extends AppCompatActivity
{
	// Attributes
	private GridLayoutManager layoutManager ;
	private RecyclerAdapter adapter ;
	private static boolean adapter_update_needed ;
	private EventsReceiver applicationsListUpdater ;
	private EventsReceiver legacyShortcutsCreator ;
	private int position ;
	private int last_position ;


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
		setContentView(R.layout.activity_drawer) ;
		RecyclerView recycler = findViewById(R.id.applications_list) ;
		layoutManager = new GridLayoutManager(this, 4) ;

		// Indicate the last time the applications list was updated
		TextView lastUpdateDateTime = findViewById(R.id.last_update_datetime) ;
		lastUpdateDateTime.setText(getString(R.string.text_applications_list_last_update, ActivityMain.getApplicationsList().getLastUpdate())) ;

		// Display the applications list
		adapter = new RecyclerAdapter(false) ;
		recycler.setAdapter(adapter) ;
		recycler.setLayoutManager(layoutManager) ;
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

		// Follow the scrolling position to detect when it is stuck on top
		position = 0 ;
		last_position = -1 ;
		recycler.addOnScrollListener(new ScrollListener()) ;
	}


	/**
	 * Inform the activity that an update of the RecyclerView is needed.
	 */
	static void setAdapterUpdateNeeded()
	{
		adapter_update_needed = true ;
	}


	/**
	 * Detect a scrolling action on a RecyclerView.
	 */
	class ScrollListener extends RecyclerView.OnScrollListener
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
					if((position == 0) && (last_position == 0)) finish() ;

					// Update the last position to detect the stuck state
					last_position = position ;
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
			position = layoutManager.findFirstCompletelyVisibleItemPosition() ;
		}
	}


	/**
	 * Perform actions when the user come back to the drawer.
	 */
	@Override
	public void onResume()
	{
		super.onResume() ;

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
		super.onDestroy() ;
		if(applicationsListUpdater != null) unregisterReceiver(applicationsListUpdater) ;
		if(legacyShortcutsCreator != null) unregisterReceiver(legacyShortcutsCreator) ;
	}
}
