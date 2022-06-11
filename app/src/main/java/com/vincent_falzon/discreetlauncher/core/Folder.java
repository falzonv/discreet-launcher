package com.vincent_falzon.discreetlauncher.core ;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2022 Vincent Falzon

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
import android.content.SharedPreferences ;
import android.graphics.Color ;
import android.graphics.drawable.ColorDrawable ;
import android.graphics.drawable.Drawable ;
import android.view.Gravity ;
import android.view.LayoutInflater ;
import android.view.MotionEvent ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.LinearLayout ;
import android.widget.PopupWindow ;
import android.widget.TextView ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.Utils ;
import com.vincent_falzon.discreetlauncher.menu.ActivityFolders ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.FlexibleGridLayout ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.RecyclerAdapter ;
import java.util.ArrayList ;
import java.util.Collections ;
import java.util.Comparator ;
import static com.vincent_falzon.discreetlauncher.ActivityMain.getApplicationWidth ;

/**
 * Represent a folder and all the applications it contains.
 */
public class Folder extends Application
{
	// Attributes
	private final ArrayList<Application> applications ;
	private PopupWindow popup ;
	private int color ;


	/**
	 * Constructor.
	 */
	public Folder(String display_name, Drawable icon, int color)
	{
		super(display_name, Constants.APK_FOLDER + display_name, Constants.APK_FOLDER, icon,  null) ;
		applications = new ArrayList<>() ;
		popup = null ;
		this.color = color ;
	}


	/**
	 * Return the display name of the folder followed by the number of elements inside.
	 */
	public String getDisplayNameWithCount()
	{
		return display_name + " (" + applications.size() + ")" ;
	}


	/**
	 * Set the display name of the folder.
	 */
	@Override
	public void setDisplayName(String new_name)
	{
		display_name = new_name ;
		name = Constants.APK_FOLDER + display_name ;
		component_info = "{" + apk + "/" + name + "}" ;
	}


	/**
	 * Return the file name of the folder.
	 */
	public String getFileName()
	{
		return Constants.FILE_FOLDER_PREFIX + display_name + ".txt" ;
	}


	/**
	 * Return the list of applications that this folder contains.
	 */
	public ArrayList<Application> getApplications()
	{
		return applications ;
	}


	/**
	 * Set the color to use for the folder icon.
	 */
	public void setColor(int new_color)
	{
		color = new_color ;
	}


	/**
	 * Return the color to use for the folder icon.
	 */
	public int getColor()
	{
		return color ;
	}


	/**
	 * Add an application to the folder if it is not already there.
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
	 */
	@SuppressWarnings({"RedundantCast", "RedundantSuppression"})
	public boolean start(View parent)
	{
		// Inflate the popup view from its XML layout
		Context context = parent.getContext() ;
		View popupView = LayoutInflater.from(context).inflate(R.layout.view_popup, (ViewGroup)null) ;

		// Prepare the folder header
		TextView popupTitle = popupView.findViewById(R.id.popup_title) ;
		popupTitle.setText(getDisplayNameWithCount()) ;
		popupTitle.setTextColor(color) ;
		popupView.findViewById(R.id.popup_header).setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View view)
				{
					// Open the interface to manage folders and close the popup
					showSettings(view.getContext()) ;
					closePopup() ;
				}
			}) ;

		// Prepare the folder content
		RecyclerView popupRecycler = popupView.findViewById(R.id.popup_recycler) ;
		RecyclerAdapter recyclerAdapter = new RecyclerAdapter(context, applications, Constants.FOLDER) ;
		popupRecycler.setAdapter(recyclerAdapter) ;
		popupRecycler.setLayoutManager(new FlexibleGridLayout(context, getApplicationWidth())) ;

		// Retrieve the app drawer colors
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context) ;
		int text_color = Utils.getColor(settings, Constants.TEXT_COLOR_DRAWER, Constants.COLOR_FOR_TEXT_ON_OVERLAY) ;
		int background_color = Utils.getColor(settings, Constants.BACKGROUND_COLOR_DRAWER, Constants.COLOR_FOR_OVERLAY) ;

		// Lighten the folder background color for better contrast
		float[] background_hsv = new float[3] ;
		Color.colorToHSV(background_color, background_hsv) ;
		background_hsv[2] += (background_hsv[2] <= 0.5) ? 0.2 : 0.1 ;
		int folder_background_color = Color.HSVToColor(235, background_hsv) ;

		// Set the folder colors
		popupView.findViewById(R.id.popup_header).setBackgroundColor(folder_background_color) ;
		popupView.findViewById(R.id.popup_line1).setBackgroundColor(color) ;
		popupRecycler.setBackgroundColor(folder_background_color) ;
		recyclerAdapter.setTextColor(text_color) ;

		// Create the popup representing the folder
		int popup_height = context.getResources().getDisplayMetrics().heightPixels / 2 ;
		popup = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, popup_height, true) ;
		popupView.setOnTouchListener(new PopupTouchListener()) ;

		// Fix popup not closing on press back with API 21
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)) ;

		// Display the popup
		popup.showAtLocation(parent, Gravity.CENTER, 0, 0) ;
		return true ;
	}


	/**
	 * Open the interface to manage folders.
	 */
	@Override
	public void showSettings(Context context)
	{
		context.startActivity(new Intent().setClass(context, ActivityFolders.class)) ;
	}


	/**
	 * Dismiss the popup if it is currently displayed.
	 */
	public void closePopup()
	{
		if(popup != null) popup.dismiss() ;
	}


	// ---------------------------------------------------------------------------------------------

	/**
	 * Dismiss the popup when the user touchs outside of it (needs <code>focusable = true</code>).
	 */
	private class PopupTouchListener implements View.OnTouchListener
	{
		/**
		 * Called when a view is touched.
		 */
		@Override
		public boolean onTouch(View view, MotionEvent event)
		{
			// Close the popup and mark the event as consumed
			view.performClick() ;
			closePopup() ;
			return true ;
		}
	}
}
