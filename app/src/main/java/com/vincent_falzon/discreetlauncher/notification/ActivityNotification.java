package com.vincent_falzon.discreetlauncher.notification ;

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
import android.content.res.Configuration ;
import android.os.Bundle ;
import android.view.View ;
import android.widget.TextView ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.ActivityFavorites ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.RecyclerAdapter ;

/**
 * Activity called when the notification is clicked.
 */
public class ActivityNotification extends AppCompatActivity
{
	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Initializations related to the interface
		setContentView(R.layout.popup) ;

		// Define the title and make it open the favorites manager when clicked
		TextView title = findViewById(R.id.popup_title) ;
		title.setText(R.string.info_favorites_access) ;
		title.setOnClickListener(new PopupClickListener()) ;
		findViewById(R.id.close_popup).setOnClickListener(new PopupClickListener()) ;

		// Display the list of favorites applications
		RecyclerView recycler = findViewById(R.id.popup_recycler) ;
		recycler.setAdapter(new RecyclerAdapter(this, ActivityMain.getApplicationsList().getFavorites())) ;
		boolean is_landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ;
		recycler.setLayoutManager(new GridLayoutManager(this, is_landscape ? Constants.COLUMNS_LANDSCAPE : Constants.COLUMNS_PORTRAIT)) ;
	}


	/**
	 * Listen for a click on the popup.
	 */
	private class PopupClickListener implements View.OnClickListener
	{
		/**
		 * Detect a click on a view.
		 * @param view Target element
		 */
		@Override
		public void onClick(View view)
		{
			// If the title was clicked, open the interface to manage favorites
			if(view.getId() == R.id.popup_title)
				view.getContext().startActivity(new Intent().setClass(view.getContext(), ActivityFavorites.class)) ;

			// Close the popup
			finish() ;
		}
	}
}
