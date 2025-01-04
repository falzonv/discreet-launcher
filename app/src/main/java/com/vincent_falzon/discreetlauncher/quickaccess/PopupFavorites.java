package com.vincent_falzon.discreetlauncher.quickaccess ;

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
import android.content.Context ;
import android.content.Intent ;
import android.os.Build ;
import android.os.Bundle ;
import android.view.View ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.menu.ActivityFavorites ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.FlexibleGridLayout ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.RecyclerAdapter ;

/**
 * Display a popup containing the favorites.
 */
public class PopupFavorites extends AppCompatActivity
{
	/**
	 * Constructor.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Initializations related to the interface
		setContentView(R.layout.view_popup) ;

		// Listen for clicks on the popup header
		findViewById(R.id.popup_header).setOnClickListener(view -> {
				// Open the interface to manage favorites and close the popup
				view.getContext().startActivity(new Intent().setClass(view.getContext(), ActivityFavorites.class)) ;
				finish() ;
			}) ;

		// Display the list of favorites applications
		RecyclerView recycler = findViewById(R.id.popup_recycler) ;
		recycler.setAdapter(new RecyclerAdapter(this, ActivityMain.getApplicationsList().getFavorites(), Constants.FAVORITES_PANEL)) ;
		recycler.setLayoutManager(new FlexibleGridLayout(this, ActivityMain.getApplicationWidth())) ;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			recycler.setOverScrollMode(View.OVER_SCROLL_NEVER) ;
	}


	/**
	 * Provide the Intent to use to display the favorites popup over other apps.
	 */
	public static Intent getIntent(Context context)
	{
		Intent intent = new Intent(Intent.ACTION_MAIN) ;
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP) ;
		intent.setClassName(context.getPackageName(), context.getPackageName() + ".quickaccess.PopupFavorites") ;
		return intent ;
	}
}
