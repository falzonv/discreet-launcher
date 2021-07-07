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
import android.os.Bundle ;
import android.text.Editable ;
import android.text.TextWatcher ;
import android.view.KeyEvent ;
import android.view.View ;
import android.view.inputmethod.EditorInfo ;
import android.widget.EditText ;
import android.widget.TextView ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.recyclerview.widget.RecyclerView ;

/**
 * Activity called when the notification is clicked.
 */
public class ActivitySearch extends AppCompatActivity
{
	// Attributes
	private SearchAdapter adapter ;


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
		findViewById(R.id.popup_title).setVisibility(View.GONE) ;
		findViewById(R.id.close_popup).setOnClickListener(new PopupClickListener()) ;

		// Prepare the search bar
		EditText searchBar = findViewById(R.id.search_bar) ;
		searchBar.setVisibility(View.VISIBLE) ;
		searchBar.addTextChangedListener(new TextChangeListener()) ;
		searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
				{
					// Perform an action when the user presses "Enter"
					if(actionId == EditorInfo.IME_ACTION_DONE)
						{
							// If there is only one application remaining, start it
							if(adapter.getItemCount() == 1)
								{
									adapter.getFirstItem().start(view) ;
									return true ;
								}
						}
					return false ;
				}
			}) ;

		// Prepare the recycler with all applications (without folders)
		RecyclerView recycler = findViewById(R.id.popup_recycler) ;
		adapter = new SearchAdapter(this, ActivityMain.getApplicationsList().getApplications(false)) ;
		recycler.setAdapter(adapter) ;
		recycler.setLayoutManager(new FlexibleGridLayout(this, ActivityMain.getApplicationWidth())) ;
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
			// Close the popup
			finish() ;
		}
	}


	/**
	 * Called when the text of an EditText is changed by the user.
	 */
	private class TextChangeListener implements TextWatcher
	{
		// Needed to implement TextWatcher
		@Override
		public void beforeTextChanged(CharSequence text, int start, int count, int after){ }


		/**
		 * Called when the text has just been changed.
		 * @param text New text
		 * @param start Place of the replacement
		 * @param before Previous text length
		 * @param count Number of new characters
		 */
		@Override
		public void onTextChanged(CharSequence text, int start, int before, int count)
		{
			// Update the display of the RecyclerView
			adapter.getFilter().filter(text) ;
		}

		// Needed to implement TextWatcher
		@Override
		public void afterTextChanged(Editable text){ }
	}
}
