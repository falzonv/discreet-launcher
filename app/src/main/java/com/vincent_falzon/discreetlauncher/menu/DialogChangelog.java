package com.vincent_falzon.discreetlauncher.menu ;

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
import android.content.DialogInterface ;
import android.os.Build ;
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.TextView ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AlertDialog ;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.Utils ;
import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStreamReader ;
import java.util.ArrayList ;

/**
 * Display the Discreet Launcher changelog.
 */
public class DialogChangelog extends AlertDialog
{
	// Constants
	private static final String TAG = "DialogChangelog" ;

	// Attributes
	private final ArrayList<ArrayList<String>> releases ;


	/**
	 * Constructor.
	 */
	@SuppressWarnings({"RedundantCast", "RedundantSuppression"})
	public DialogChangelog(Context context)
	{
		// Let the parent actions be performed
		super(context) ;

		// Load the XML layout
		View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_changelog, (ViewGroup) null) ;
		setView(dialogView) ;

		// Load the changelog files
		releases = new ArrayList<>() ;
		ArrayList<String> changelog_files = new ArrayList<>() ;
		try
		{
			// Select the proper changelog folder and retrieve its content
			String folder = context.getString(R.string.menu_changelog).equals("Journal des changements") ? "changelog-fr" : "changelog-en" ;
			String[] files = context.getAssets().list(folder) ;
			if(files != null)
				{
					// Store the complete path of the files and order them by release number
					for(String file : files) if(file.length() == 5) changelog_files.add(folder + "/" + file) ;
					for(String file : files) if(file.length() == 6) changelog_files.add(folder + "/" + file) ;
				}
		}
		catch(IOException exception)
		{
			// An error happened while listing the folder
			Utils.logError(TAG, exception.getMessage()) ;
		}

		// Display an error message if the changelog folder was not found
		if(changelog_files.size() == 0)
			{
				ArrayList<String> error = new ArrayList<>() ;
				error.add(context.getString(R.string.error_changelog_missing_folder)) ;
				releases.add(error) ;
			}

		// Browse the releases notes starting with the most recent
		for(int i = (changelog_files.size() - 1) ; i >= 0 ; i--)
		{
			// Store the informations of a new release
			String file = changelog_files.get(i) ;
			ArrayList<String> release = new ArrayList<>() ;
			String buffer ;
			try
			{
				// Read the content from the file line by line
				BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open(file))) ;
				while((buffer = reader.readLine()) != null) release.add(buffer) ;
				reader.close() ;
			}
			catch(IOException exception)
			{
				// An error happened while reading the file
				release.add(context.getString(R.string.error_changelog_missing_file, file)) ;
			}
			releases.add(release) ;
		}

		// Display the list of releases
		RecyclerView recycler = dialogView.findViewById(R.id.changelog) ;
		recycler.setLayoutManager(new LinearLayoutManager(context)) ;
		recycler.setAdapter(new RecyclerAdapter()) ;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			recycler.setOverScrollMode(View.OVER_SCROLL_NEVER) ;

		// Add the close button
		setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_close), (DialogInterface.OnClickListener)null) ;
	}


	// ---------------------------------------------------------------------------------------------

	/**
	 * Fill a RecyclerView with the releases.
	 */
	private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ReleaseView>
	{
		/**
		 * Create a ReleaseView to add in the RecyclerView based on an XML layout.
		 */
		@NonNull
		@Override
		public RecyclerAdapter.ReleaseView onCreateViewHolder(ViewGroup parent, int viewType)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext()) ;
			return new RecyclerAdapter.ReleaseView(inflater.inflate(R.layout.view_release, parent, false)) ;
		}


		/**
		 * Write the details of each release in the RecyclerView.
		 */
		@Override
		public void onBindViewHolder(@NonNull final RecyclerAdapter.ReleaseView releaseView, int i)
		{
			// Skip the release if it is empty
			if(releases.get(i).size() == 0) return ;

			// Prepare and display the release title
			releaseView.title.setText(releases.get(i).get(0)) ;

			// Prepare and display the release details
			StringBuilder details = new StringBuilder() ;
			boolean first_line = true ;
			for(String line : releases.get(i))
			{
				// Skip the title line
				if(first_line)
					{
						first_line = false ;
						continue ;
					}

				// Add the release details
				details.append(line).append("\n") ;
			}
			releaseView.details.setText(details) ;
		}


		/**
		 * Return the number of items in the RecyclerView.
		 */
		@Override
		public int getItemCount()
		{
			return releases.size() ;
		}


		// -----------------------------------------------------------------------------------------

		/**
		 * Represent a release item in the RecyclerView.
		 */
		public class ReleaseView extends RecyclerView.ViewHolder
		{
			// Attributes
			private final TextView title ;
			private final TextView details ;

			/**
			 * Constructor.
			 */
			ReleaseView(View view)
			{
				super(view) ;
				title = view.findViewById(R.id.release_title) ;
				details = view.findViewById(R.id.release_details) ;
			}
		}
	}
}
