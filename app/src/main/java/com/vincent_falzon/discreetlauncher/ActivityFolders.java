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
import android.os.Bundle ;
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.TextView ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import java.util.ArrayList ;

/**
 * Allow to import and export settings, favorites applications and shortcuts.
 */
public class ActivityFolders extends AppCompatActivity
{
	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState);

		// Initializations
		setContentView(R.layout.activity_folders);
		ArrayList<Folder> folders = ActivityMain.getApplicationsList().getFolders() ;

		// Load the folders list
		RecyclerView foldersList = findViewById(R.id.folders_list) ;
		foldersList.setLayoutManager(new LinearLayoutManager(this)) ;
		foldersList.setAdapter(new FoldersListAdapter(folders)) ;
	}


	/**
	 * Fill a RecyclerView with the folders.
	 */
	private static class FoldersListAdapter extends RecyclerView.Adapter<FoldersListAdapter.FolderView>
	{
		// Attributes
		private final ArrayList<Folder> folders ;


		/**
		 * Constructor to fill a RecyclerView with the folders list.
		 * @param folders Folders to display in the recycler
		 */
		FoldersListAdapter(ArrayList<Folder> folders)
		{
			this.folders = folders ;
		}


		/**
		 * Create a FolderView to add in the RecyclerView based on an XML layout.
		 * @param parent To get the context
		 * @param viewType Not used (herited)
		 * @return Created FolderView
		 */
		@NonNull
		@Override
		public FolderView onCreateViewHolder(ViewGroup parent, int viewType)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext()) ;
			View view = inflater.inflate(R.layout.view_folder, parent, false) ;
			return new FolderView(view) ;
		}


		/**
		 * Write the name of each folder in the RecyclerView
		 * @param folderView Current folder
		 * @param i For incrementation
		 */
		@Override
		public void onBindViewHolder(FolderView folderView, int i)
		{
			folderView.name.setText(folders.get(i).getDisplayName()) ;
		}


		/**
		 * Return the number of items in the RecyclerView.
		 * @return Number of items
		 */
		@Override
		public int getItemCount()
		{
			return folders.size() ;
		}


		/**
		 * Represent a folder item in the RecyclerView.
		 */
		public static class FolderView extends RecyclerView.ViewHolder implements View.OnClickListener
		{
			// Attributes
			private final TextView name ;


			/**
			 * Constructor.
			 * @param view To get the context
			 */
			FolderView(View view)
			{
				// Let the parent actions be performed
				super(view) ;

				// Listen for clicks on elements
				name = view.findViewById(R.id.edit_folder_name) ;
				name.setOnClickListener(this) ;
				view.findViewById(R.id.edit_folder_content).setOnClickListener(this) ;
				view.findViewById(R.id.remove_folder).setOnClickListener(this) ;
			}


			/**
			 * Perform an action when an element is clicked.
			 * @param view Target element
			 */
			@Override
			public void onClick(View view)
			{
				// Identify which element has been clicked
				Context context = view.getContext() ;
				int selection = view.getId() ;
				if(selection == R.id.edit_folder_name)
					{
						// Propose to edit the folder name
						ShowDialog.toast(context, R.string.folders_create_new) ;
					}
					else if(selection == R.id.edit_folder_content)
					{
						// Propose to edit the folder content
						ShowDialog.toast(context, R.string.button_settings) ;
					}
					else if(selection == R.id.remove_folder)
					{
						// Propose to remove the folder
						ShowDialog.toast(context, R.string.button_remove) ;
					}
			}
		}
	}
}
