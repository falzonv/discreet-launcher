package com.vincent_falzon.discreetlauncher.core ;

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
import android.app.Activity ;
import android.content.Context ;
import android.content.SharedPreferences ;
import android.graphics.Color ;
import android.graphics.drawable.ColorDrawable ;
import android.graphics.drawable.Drawable ;
import android.os.Build ;
import android.text.Editable ;
import android.text.TextWatcher ;
import android.view.Gravity ;
import android.view.LayoutInflater ;
import android.view.MotionEvent ;
import android.view.View ;
import android.view.ViewGroup ;
import android.view.inputmethod.EditorInfo ;
import android.view.inputmethod.InputMethod ;
import android.view.inputmethod.InputMethodManager ;
import android.widget.EditText ;
import android.widget.LinearLayout ;
import android.widget.PopupWindow ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.FlexibleGridLayout ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.SearchAdapter ;
import com.vincent_falzon.discreetlauncher.Utils ;
import java.util.ArrayList ;

/**
 * Represent the Search application.
 */
public class Search extends Application
{
	// Attributes
	private PopupWindow popup ;
	private SearchAdapter adapter ;


	/**
	 * Constructor.
	 */
	public Search(String display_name, Drawable icon)
	{
		super(display_name, Constants.APK_SEARCH, Constants.APK_SEARCH, icon, null) ;
		popup = null ;
	}


	/**
	 * Display the Search application as a popup.
	 */
	@SuppressWarnings({"RedundantCast", "RedundantSuppression"})
	public boolean start(View parent)
	{
		// Initializations
		Context context = parent.getContext() ;
		LayoutInflater inflater = LayoutInflater.from(context) ;

		// Prepare the popup view
		View popupView = inflater.inflate(R.layout.view_popup, (ViewGroup)null) ;
		popupView.findViewById(R.id.popup_header).setVisibility(View.INVISIBLE) ;
		popupView.findViewById(R.id.popup_line1).setVisibility(View.INVISIBLE) ;
		popupView.findViewById(R.id.popup_line2).setVisibility(View.VISIBLE) ;

		// Prepare the search bar
		EditText searchBar = popupView.findViewById(R.id.search_bar) ;
		searchBar.setVisibility(View.VISIBLE) ;
		searchBar.addTextChangedListener(new TextChangeListener()) ;
		searchBar.setOnEditorActionListener((view, actionId, event) -> {
				// Perform an action when the user presses "Enter"
				if(actionId == EditorInfo.IME_ACTION_SEARCH)
					{
						// Launch the first app displayed in the results
						adapter.launchFirstApp(view) ;
						return true ;
					}
				return false ;
			}) ;

		// Retrieve all the applications without folders and the Search
		ArrayList<Application> applications = ActivityMain.getApplicationsList().getApplications(false) ;
		Application search = null ;
		for(Application application : applications)
			if(application instanceof Search) search = application ;
		if(search != null) applications.remove(search) ;

		// Prepare the popup content
		RecyclerView recycler = popupView.findViewById(R.id.popup_recycler) ;
		adapter = new SearchAdapter(context, applications) ;
		recycler.setAdapter(adapter) ;
		recycler.setLayoutManager(new FlexibleGridLayout(context, ActivityMain.getApplicationWidth())) ;
		recycler.setMinimumHeight(0) ;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			recycler.setOverScrollMode(View.OVER_SCROLL_NEVER) ;

		// Retrieve the app drawer colors
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context) ;
		int text_color = Utils.getColor(settings, Constants.TEXT_COLOR_DRAWER, Constants.COLOR_FOR_TEXT_ON_OVERLAY) ;
		int background_color = Utils.getColor(settings, Constants.BACKGROUND_COLOR_DRAWER, Constants.COLOR_FOR_OVERLAY) ;

		// Lighten the search background color for better contrast
		float[] background_hsv = new float[3] ;
		Color.colorToHSV(background_color, background_hsv) ;
		background_hsv[2] += (background_hsv[2] <= 0.5) ? 0.2 : 0.1 ;
		int search_background_color = Color.HSVToColor(235, background_hsv) ;

		// Set the search colors
		recycler.setBackgroundColor(search_background_color) ;
		adapter.setTextColor(text_color) ;

		// Create the popup representing the Search application
		popup = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true) ;
		popupView.setOnTouchListener(new PopupTouchListener()) ;

		// Fix popup not closing on press back with API 21
		popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)) ;

		// Display the popup and the keyboard
		popup.showAtLocation(parent, Gravity.BOTTOM, 0, 0) ;
		((InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE)).toggleSoftInputFromWindow(parent.getWindowToken(), InputMethod.SHOW_EXPLICIT, 0) ;
		searchBar.requestFocus() ;
		return true ;
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
		 * Called when an element is touched.
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


	// ---------------------------------------------------------------------------------------------

	/**
	 * Called when the text of an EditText is changed by the user.
	 */
	private class TextChangeListener implements TextWatcher
	{
		// Needed to implement TextWatcher
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after){ }


		/**
		 * Called when the text has just been changed.
		 */
		@Override
		public void onTextChanged(CharSequence text, int start, int before, int count)
		{
			// Update the display of the RecyclerView
			adapter.getFilter().filter(text) ;
		}

		// Needed to implement TextWatcher
		@Override
		public void afterTextChanged(Editable s){ }
	}
}
