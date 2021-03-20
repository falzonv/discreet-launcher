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
import android.content.DialogInterface ;
import android.content.Intent ;
import android.content.pm.PackageManager ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AlertDialog ;
import androidx.recyclerview.widget.RecyclerView ;
import android.net.Uri ;
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
		appView.name.setText(applicationsList.get(i).getDisplayName()) ;
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
	public class ApplicationView extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
	{
		// Attributes
		private final TextView name ;


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
			view.setOnLongClickListener(this) ;
		}


		/**
		 * Start the application when it is clicked.
		 * @param view To get the context
		 */
		@Override
		public void onClick(View view)
		{
			int i = getAdapterPosition() ;
			startApplication(view.getContext(), applicationsList.get(i).getName(), applicationsList.get(i).getApk()) ;
		}


		/**
		 * When the application is long clicked, propose to open its system settings.
		 * @param view To get the context
		 * @return always <code>true</code> as we consume the long click event
		 */
		@Override
		public boolean onLongClick(final View view)
		{
			// Retrieve the package name of the application
			int i = getAdapterPosition() ;
			final String name = applicationsList.get(i).getName() ;
			final String apk = applicationsList.get(i).getApk() ;

			// Prepare and display the selection dialog
			final Context context = view.getContext() ;
			AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
			dialog.setMessage(context.getString(R.string.text_open_or_settings, applicationsList.get(i).getDisplayName())) ;
			dialog.setPositiveButton(R.string.button_settings,
					new DialogInterface.OnClickListener()
					{
						// Save the new list of favorites applications
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							// Open the application system settings
							Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS) ;
							intent.setData(Uri.parse("package:" + apk)) ;
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
							context.startActivity(intent) ;
						}
					}) ;
			dialog.setNeutralButton(R.string.button_open,
					new DialogInterface.OnClickListener()
					{
						// Save the new list of favorites applications
						@Override
						public void onClick(DialogInterface dialogInterface, int i)
						{
							// Start the application
							startApplication(context, name, apk) ;
						}
					}) ;
			dialog.show() ;
			return true ;
		}


		/**
		 * Start an application after checking that it is still available.
		 * @param context To get the context
		 * @param name Internal name of the application
		 * @param apk Package name of the application
		 */
		private void startApplication(Context context, String name, String apk)
		{
			// Check if the application still exists (not uninstalled or disabled)
			Intent package_intent = apkManager.getLaunchIntentForPackage(apk) ;
			if(package_intent == null)
				{
					// Display an error message and quit
					AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
					dialog.setMessage(context.getString(R.string.error_application_not_found, apk)) ;
					dialog.setNeutralButton(R.string.button_close, null) ;
					dialog.show() ;
					return ;
				}

			// Try to launch the specific intent of the application
			Intent activity_intent = new Intent(Intent.ACTION_MAIN) ;
			activity_intent.addCategory(Intent.CATEGORY_LAUNCHER) ;
			activity_intent.setClassName(apk, name) ;
			activity_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
			if(activity_intent.resolveActivity(apkManager) != null)
				{
					context.startActivity(activity_intent) ;
					return ;
				}

			// If it was not found, launch the default intent of the package
			package_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
			context.startActivity(package_intent) ;
		}
	}
}
