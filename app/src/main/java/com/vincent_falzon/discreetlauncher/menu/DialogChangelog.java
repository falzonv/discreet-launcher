package com.vincent_falzon.discreetlauncher.menu ;

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
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.TextView ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AlertDialog ;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.R ;
import java.io.BufferedReader ;
import java.io.IOException ;
import java.io.InputStreamReader ;
import java.util.ArrayList ;

/**
 * Display the Discreet Launcher changelog.
 */
public class DialogChangelog extends AlertDialog
{
	// Attributes
	private final ArrayList<ArrayList<String>> releases ;


	/**
	 * Constructor.
	 * @param context Provided by a preference
	 *
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
			// Retrieve the content of the folder
			String folder = context.getString(R.string.changelog_folder) ;
			String[] files = context.getAssets().list(folder) ;
			if(files != null)
				{
					// Store the complete path of the files and order them by release number
					for(String file : files) if(file.length() == 5) changelog_files.add(folder + "/" + file) ;
					for(String file : files) if(file.length() == 6) changelog_files.add(folder + "/" + file) ;
				}
		}
		catch(IOException e)
		{
			// An error happened while listing the folder
			ArrayList<String> error = new ArrayList<>() ;
			error.add(context.getString(R.string.error_changelog_folder)) ;
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
			catch (Exception e)
			{
				// An error happened while reading the file
				ArrayList<String> error = new ArrayList<>() ;
				error.add(context.getString(R.string.error_changelog_file, file)) ;
				releases.add(error) ;
			}
			releases.add(release) ;
		}

		// Display the list of releases
		RecyclerView recycler = dialogView.findViewById(R.id.changelog) ;
		recycler.setLayoutManager(new LinearLayoutManager(context)) ;
		recycler.setAdapter(new RecyclerAdapter()) ;

		// Add the close button
		setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.button_close), (DialogInterface.OnClickListener)null) ;
    }


	/**
	 * Fill a RecyclerView with the releases.
	 */
	private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ReleaseView>
	{
		/**
		 * Create a ReleaseView to add in the RecyclerView based on an XML layout.
		 * @param parent To get the context
		 * @param viewType Not used (herited)
		 * @return Created ReleaseView
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
		 * @param releaseView Current release
		 * @param i For incrementation
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
		 * @return Number of items
		 */
		@Override
		public int getItemCount()
		{
			return releases.size() ;
		}


		/**
		 * Represent a favorite item in the RecyclerView.
		 */
		public class ReleaseView extends RecyclerView.ViewHolder
		{
			// Attributes
			private final TextView title ;
			private final TextView details ;

			/**
			 * Constructor.
			 * @param view Element
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
