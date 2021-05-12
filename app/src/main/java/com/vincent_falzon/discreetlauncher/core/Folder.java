package com.vincent_falzon.discreetlauncher.core ;

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
import android.content.Intent ;
import android.content.res.Configuration ;
import android.graphics.drawable.Drawable ;
import android.view.Gravity ;
import android.view.LayoutInflater ;
import android.view.MotionEvent ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.LinearLayout ;
import android.widget.PopupWindow ;
import android.widget.TextView ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.ActivityFolders ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.RecyclerAdapter ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator ;

/**
 * Represent a folder and all the applications that this folder contains.
 */
public class Folder extends Application
{
	// Attributes
	private final ArrayList<Application> applications ;
	private PopupWindow popup ;


	/**
	 * Constructor to represent a folder
	 * @param display_name Displayed to the user
	 * @param icon Displayed to the user
	 */
	public Folder(String display_name, Drawable icon)
	{
		super(display_name, Constants.APK_FOLDER + display_name, Constants.APK_FOLDER, icon) ;
		applications = new ArrayList<>() ;
		popup = null ;
	}


	/**
	 * Get the display name of the folder followed by the number of elements inside.
	 * @return Name displayed in the menus
	 */
	public String getDisplayNameWithCount()
	{
		return display_name + " (" + applications.size() + ")" ;
	}


	/**
	 * Set the display name of the folder.
	 * @param new_name New display name
	 */
	public void setDisplayName(String new_name)
	{
		display_name = new_name ;
		name = Constants.APK_FOLDER + display_name ;
	}


	/**
	 * Get the file name of the folder.
	 * @return Name of the file in the internal storage
	 */
	public String getFileName()
	{
		return Constants.FILE_FOLDER_PREFIX + display_name + ".txt" ;
	}


	/**
	 * Get the applications list.
	 * @return List of applications contained in the folder
	 */
	public ArrayList<Application> getApplications()
	{
		return applications ;
	}


	/**
	 * Add an application to the folder if it is not already there.
	 * @param application To add
	 */
	public void addToFolder(Application application)
	{
		if(applications.contains(application)) return ;
		applications.add(application) ;
	}


	/**
	 * Sort the folder content if necessary.
	 */
	public void sortFolder()
	{
		if(applications.size() < 2) return ;
		Collections.sort(applications, new Comparator<Application>()
		{
			@Override
			public int compare(Application application1, Application application2)
			{
				return application1.getDisplayName().compareToIgnoreCase(application2.getDisplayName()) ;
			}
		}) ;
	}


	/**
	 * Display the content of the folder as a popup.
	 * @param parent Element from which the event originates
	 * @return Always <code>true</code>
	 */
	public boolean start(View parent)
	{
		// Initializations
		Context context = parent.getContext() ;
		LayoutInflater inflater = LayoutInflater.from(context) ;

		// Prepare the popup view
		View popupView = inflater.inflate(R.layout.folder_popup, (ViewGroup)null) ;

		// Prepare the folder title
		TextView popupTitle = popupView.findViewById(R.id.popup_title) ;
		popupTitle.setText(getDisplayNameWithCount()) ;
		popupTitle.setOnClickListener(new ManageFoldersAccessor()) ;

		// Prepare the folder content
		RecyclerView popupRecycler = popupView.findViewById(R.id.popup_recycler) ;
		popupRecycler.setAdapter(new RecyclerAdapter(context, applications)) ;
		int orientation = context.getResources().getConfiguration().orientation ;
		popupRecycler.setLayoutManager(new GridLayoutManager(context,
				orientation == Configuration.ORIENTATION_LANDSCAPE ? Constants.COLUMNS_LANDSCAPE : Constants.COLUMNS_PORTRAIT)) ;

		// Create the popup representing the folder
		int popup_height = Math.min(context.getResources().getDisplayMetrics().heightPixels / 2, parent.getRootView().getHeight()) ;
		popup = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, popup_height, true) ;
		popupView.setOnTouchListener(new PopupTouchListener()) ;

		// Display the popup
		popup.showAtLocation(parent, Gravity.CENTER, 0, 0) ;
		return true ;
	}


	/**
	 * Dismiss the popup if it is currently displayed.
	 */
	public void closePopup()
	{
		if(popup != null) popup.dismiss() ;
	}


	/**
	 * Allow to access the activity to manage folders when clicking a view.
	 */
	private class ManageFoldersAccessor implements View.OnClickListener
	{
		/**
		 * Detect a click on a view.
		 * @param view Target element
		 */
		@Override
		public void onClick(View view)
		{
			view.getContext().startActivity(new Intent().setClass(view.getContext(), ActivityFolders.class)) ;
			closePopup() ;
		}
	}


	/**
	 * Dismiss the popup when the user touchs outside of it (needs <code>focusable = true</code>).
	 */
	private class PopupTouchListener implements View.OnTouchListener
	{
		/**
		 * Detect a gesture on a view.
		 * @param view Target element
		 * @param event Details about the gesture
		 * @return <code>true</code> if the event is consumed, <code>false</code> otherwise
		 */
		@Override
		public boolean onTouch(View view, MotionEvent event)
		{
			view.performClick() ;
			closePopup() ;
			return true ;
		}
	}
}
