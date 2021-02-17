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
import android.content.pm.PackageManager ;
import androidx.annotation.NonNull ;
import androidx.recyclerview.widget.RecyclerView ;
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.TextView ;
import java.util.ArrayList ;

/**
 * Fill a RecyclerView with the applications list (complete or favorites).
 */
class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ApplicationView>
{
	// Attributes
	private final PackageManager apkManager ;
	private final ArrayList<Application> applicationsList ;


	/**
	 * Constructor to fill a RecyclerView with the applications list.
	 * @param context To get the APK manager
	 * @param favorites Full list or favorites only
	 */
	RecyclerAdapter(Context context, boolean favorites)
	{
		apkManager = context.getPackageManager() ;
		if(favorites) applicationsList = ActivityMain.getFavoritesList() ;
			else applicationsList = ActivityMain.getApplicationsList() ;
	}


	/**
	 * Create an ApplicationView to add in the RecyclerView based on an XML layout.
	 * @param parent To get the context
	 * @param viewType Not used (herited)
	 * @return Created ApplicationView
	 */
	@NonNull
	@Override
	public ApplicationView onCreateViewHolder(ViewGroup parent, int viewType)
	{
		LayoutInflater inflater = LayoutInflater.from(parent.getContext()) ;
		View view = inflater.inflate(R.layout.recycler_element, parent, false) ;
		return new ApplicationView(view) ;
	}


	/**
	 * Write the metadata (name, icon) of each application in the RecyclerView
	 * @param appView Current application
	 * @param i For incrementation
	 */
	@Override
	public void onBindViewHolder(ApplicationView appView, int i)
	{
		appView.name.setText(applicationsList.get(i).getName()) ;
		appView.name.setCompoundDrawables(null, applicationsList.get(i).getIcon(), null, null) ;
	}


	/**
	 * Return the number of items in the RecyclerView.
	 * @return Number of items
	 */
	@Override
	public int getItemCount()
	{
		return applicationsList.size() ;
	}


	/**
	 * Represent a clickable application item in the RecyclerView.
	 */
	public class ApplicationView extends RecyclerView.ViewHolder implements View.OnClickListener
	{
		// Attributes
		private final TextView name;


		/**
		 * Constructor.
		 * @param view To get the context
		 */
		ApplicationView(View view)
		{
			// Call the constructor of the parent class
			super(view) ;

			// Listen for a click on the application
			name = view.findViewById(R.id.application_item) ;
			view.setOnClickListener(this) ;
		}


		/**
		 * Start the application when it is clicked.
		 * @param view To get the context
		 */
		@Override
		public void onClick(View view)
		{
			// Retrieve the APK identifier and start the application
			String apk = applicationsList.get(getAdapterPosition()).getApk() ;
			view.getContext().startActivity(apkManager.getLaunchIntentForPackage(apk)) ;
		}
	}
}
