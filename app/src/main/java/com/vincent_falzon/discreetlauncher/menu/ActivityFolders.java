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
import android.annotation.SuppressLint ;
import android.app.Activity ;
import android.content.Context ;
import android.content.SharedPreferences ;
import android.graphics.drawable.Drawable ;
import android.os.Build ;
import android.os.Bundle ;
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import android.view.inputmethod.EditorInfo ;
import android.view.inputmethod.InputMethodManager ;
import android.widget.EditText ;
import android.widget.ImageButton ;
import android.widget.TextView ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.appcompat.content.res.AppCompatResources ;
import androidx.core.content.ContextCompat ;
import androidx.preference.PreferenceManager ;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.Utils ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.core.FolderIcon ;
import com.vincent_falzon.discreetlauncher.settings.ColorPickerDialog ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Allow to manage folders.
 */
public class ActivityFolders extends AppCompatActivity implements View.OnClickListener
{
	// Attributes
	private FoldersListAdapter adapter ;
	private ArrayList<Folder> folders ;


	/**
	 * Constructor.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState);

		// Interface initializations
		setContentView(R.layout.activity_folders) ;
		findViewById(R.id.new_folder_button).setOnClickListener(this) ;
		folders = ActivityMain.getApplicationsList().getFolders() ;

		// Prepare and diplay the list of folders
		RecyclerView foldersList = findViewById(R.id.folders_list) ;
		foldersList.setLayoutManager(new LinearLayoutManager(this)) ;
		adapter = new FoldersListAdapter(folders) ;
		foldersList.setAdapter(adapter) ;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			foldersList.setOverScrollMode(View.OVER_SCROLL_NEVER) ;
	}


	/**
	 * Called when an element is clicked.
	 */
	@SuppressLint("NotifyDataSetChanged")
	public void onClick(View view)
	{
		// Check if the button to add a new folder has a been clicked
		if(view.getId() == R.id.new_folder_button)
			{
				// Retrieve the new folder name and check that it is not empty
				String new_folder_name = ((EditText)findViewById(R.id.new_folder_name)).getText().toString() ;
				if(new_folder_name.isEmpty())
					{
						// Display an error message and quit
						Utils.displayLongToast(this, getString(R.string.error_folder_empty_name)) ;
						return ;
					}

				// Hide the keyboard and check if the folder already exists
				((InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0) ;
				InternalFileTXT file = new InternalFileTXT(Constants.FILE_FOLDER_PREFIX + new_folder_name + ".txt") ;
				if(file.exists())
					{
						// Display an error message and quit
						Utils.displayLongToast(this, getString(R.string.error_folder_already_exists)) ;
						return ;
					}

				// Prepare the folder icon
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this) ;
				int icon_size = Utils.getIconSize(this, settings) ;
				Drawable baseIcon = AppCompatResources.getDrawable(this, R.drawable.icon_folder) ;
				Drawable icon = new FolderIcon(baseIcon, icon_size, 0, ContextCompat.getColor(this, R.color.for_icon_added_in_drawer), false) ;
				icon.setBounds(0, 0, icon_size, icon_size) ;

				// Create the folder and update the list
				file.writeLine("") ;
				folders.add(new Folder(new_folder_name, icon, ContextCompat.getColor(this, R.color.for_icon_added_in_drawer))) ;
				ActivityMain.updateList(this) ;
				adapter.notifyDataSetChanged() ;
			}
	}


	// ---------------------------------------------------------------------------------------------

	/**
	 * Fill a RecyclerView with the folders.
	 */
	private static class FoldersListAdapter extends RecyclerView.Adapter<FoldersListAdapter.FolderView>
	{
		// Attributes
		private final ArrayList<Folder> folders ;


		/**
		 * Constructor to fill a RecyclerView with the folders list.
		 */
		FoldersListAdapter(ArrayList<Folder> folders)
		{
			this.folders = folders ;
		}


		/**
		 * Create a FolderView to add in the RecyclerView based on an XML layout.
		 */
		@NonNull
		@Override
		public FolderView onCreateViewHolder(ViewGroup parent, int viewType)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext()) ;
			View view = inflater.inflate(R.layout.view_folder_editor, parent, false) ;
			return new FolderView(view) ;
		}


		/**
		 * Write the name of each folder in the RecyclerView.
		 */
		@Override
		public void onBindViewHolder(FolderView folderView, int i)
		{
			folderView.name.setText(folders.get(i).getDisplayNameWithCount()) ;
			folderView.color.setImageDrawable(folders.get(i).getIcon()) ;
		}


		/**
		 * Return the number of items in the RecyclerView.
		 */
		@Override
		public int getItemCount()
		{
			return folders.size() ;
		}


		// -----------------------------------------------------------------------------------------

		/**
		 * Represent a folder item in the RecyclerView.
		 */
		public class FolderView extends RecyclerView.ViewHolder implements View.OnClickListener
		{
			// Attributes
			private final TextView name ;
			private final ImageButton color ;


			/**
			 * Constructor.
			 */
			FolderView(View view)
			{
				// Let the parent actions be performed
				super(view) ;

				// Initializations
				name = view.findViewById(R.id.edit_folder_name) ;
				color = view.findViewById(R.id.edit_folder_color) ;

				// Listen for clicks on elements
				name.setOnClickListener(this) ;
				color.setOnClickListener(this) ;
				view.findViewById(R.id.edit_folder_content).setOnClickListener(this) ;
				view.findViewById(R.id.remove_folder).setOnClickListener(this) ;
			}


			/**
			 * Called when an element is clicked.
			 */
			@SuppressLint("NotifyDataSetChanged")
			@Override
			public void onClick(View view)
			{
				// Initializations
				final Context context = view.getContext() ;
				final Folder folder = folders.get(getBindingAdapterPosition()) ;
				final InternalFileTXT file = new InternalFileTXT(folder.getFileName()) ;
				AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
				dialog.setNegativeButton(R.string.button_cancel, null) ;

				// Identify which element has been clicked
				int selection = view.getId() ;
				if(selection == R.id.edit_folder_color)
					{
						ColorPickerDialog colorDialog = new ColorPickerDialog(context,
								folder.getColor(),
								ColorPickerDialog.convertIntColorToHexadecimal(ContextCompat.getColor(context, R.color.for_icon_added_in_drawer), true),
								folder.getDisplayName(),
								color -> {
									// Remove the previous color from the mapping file
									InternalFileTXT folders_colors = new InternalFileTXT(Constants.FILE_FOLDERS_COLORS) ;
									folders_colors.removeLine(folder.getFileName() + Constants.SEPARATOR) ;

									// Registrer the new color and update the applications list
									folders_colors.writeLine(folder.getFileName() + Constants.SEPARATOR + color) ;
									ActivityMain.updateList(context) ;
									notifyDataSetChanged() ;

									// Update the preview in the folders organizer
									folder.setColor(ColorPickerDialog.convertHexadecimalColorToInt(color)) ;
									ArrayList<Folder> all_folders = ActivityMain.getApplicationsList().getFolders() ;
									for(Folder the_folder : all_folders)
										if(the_folder.getComponentInfo().equals(folder.getComponentInfo()))
											folder.setIcon(the_folder.getIcon()) ;
								}) ;
						colorDialog.show() ;
					}
					else if(selection == R.id.edit_folder_name)
					{
						// Ask the user for the new name
						final EditText newFolderName = new EditText(context) ;
						newFolderName.setText(folder.getDisplayName()) ;
						newFolderName.setSingleLine() ;
						newFolderName.setImeOptions(EditorInfo.IME_ACTION_DONE) ;
						dialog.setTitle(R.string.hint_rename_folder) ;
						dialog.setView(newFolderName) ;
						dialog.setPositiveButton(R.string.button_apply, (dialogInterface, which) -> {
									// Check if the new name is empty or already exists
									String new_folder_name = newFolderName.getText().toString() ;
									if(new_folder_name.isEmpty())
										{
											// Display an error message and quit
											Utils.displayLongToast(context, context.getString(R.string.error_folder_empty_name)) ;
											return ;
										}
									String new_filename = Constants.FILE_FOLDER_PREFIX + new_folder_name + ".txt" ;
									if(new InternalFileTXT(new_filename).exists())
										{
											// Display an error message and quit
											Utils.displayLongToast(context, context.getString(R.string.error_folder_already_exists)) ;
											return ;
										}

									// Rename the folder file
									String current_filename = folder.getFileName() ;
									if(file.rename(Constants.FILE_FOLDER_PREFIX + new_folder_name + ".txt"))
										{
											// If it exists, browse the file mapping folders and colors
											InternalFileTXT folders_colors = new InternalFileTXT(Constants.FILE_FOLDERS_COLORS) ;
											ArrayList<String> file_content = folders_colors.readAllLines() ;
											if(file_content != null)
												{
													// If a mapping already exists, reproduce it with the new name
													for(String line : file_content)
														if(line.startsWith(current_filename))
															{
																String color = line.replace(current_filename + Constants.SEPARATOR, "") ;
																folders_colors.writeLine(new_filename + Constants.SEPARATOR + color) ;
															}

													// Remove the mapping with the previous name
													folders_colors.removeLine(current_filename) ;
												}

											// Update the favorites if necessary
											InternalFileTXT favorites = new InternalFileTXT(Constants.FILE_FAVORITES) ;
											boolean was_in_favorites = favorites.removeLine(folder.getComponentInfo()) ;
											folder.setDisplayName(new_folder_name) ;
											if(was_in_favorites) favorites.writeLine(folder.getComponentInfo()) ;

											// Update the applications list
											ActivityMain.updateList(context) ;
											notifyDataSetChanged() ;
										}
										else Utils.displayLongToast(context, context.getString(R.string.error_folder_rename)) ;
								}) ;
						dialog.show() ;
					}
					else if(selection == R.id.edit_folder_content)
					{
						// List the names of all applications
						final ArrayList<Application> applications = new ArrayList<>() ;
						applications.addAll(folder.getApplications()) ;
						applications.addAll(ActivityMain.getApplicationsList().getApplicationsNotInFolders()) ;
						CharSequence[] app_names = new CharSequence[applications.size()] ;
						int i = 0 ;
						for(Application application : applications)
						{
							if(application instanceof Folder) app_names[i] = ((Folder)application).getDisplayNameWithCount() ;
								else app_names[i] = application.getDisplayName() ;
							i++ ;
						}

						// Retrieve the current folder applications
						final boolean[] selected = new boolean[app_names.length] ;
						if(file.exists())
								for(i = 0 ; i < app_names.length ; i++)
									selected[i] = file.isLineExisting(applications.get(i).getComponentInfo()) ;
							else for(i = 0 ; i < app_names.length ; i++) selected[i] = false ;

						// Let the user select the folder content
						dialog.setMultiChoiceItems(app_names, selected, (dialogInterface, position, checked) -> { }) ;
						dialog.setPositiveButton(R.string.button_apply, (dialogInterface, which) -> {
									// Remove the current folder file
									if(!file.remove()) return ;

									// Add the selected applications in the folder
									for(int j = 0 ; j < selected.length ; j++)
										if(selected[j]) file.writeLine(applications.get(j).getComponentInfo()) ;

									// Recreate the empty file if the removed line was the single one
									if(!file.exists()) file.writeLine("") ;

									// Update the display in the activity
									folder.getApplications().clear() ;
									for(String component_info : file.readAllLines())
									{
										// Search the internal name in the applications list
										for(Application application : applications)
											if(application.getComponentInfo().equals(component_info))
												{
													// Add the application in the folder
													folder.addToFolder(application) ;
													break ;
												}
									}

									// Update the applications list
									ActivityMain.updateList(context) ;
									notifyDataSetChanged() ;
								}) ;
						dialog.show() ;
					}
					else if(selection == R.id.remove_folder)
					{
						// Ask confirmation before removing the folder
						dialog.setMessage(R.string.hint_remove_folder) ;
						dialog.setPositiveButton(R.string.button_remove_folder, (dialogInterface, which) -> {
									// Remove any color affected to this folder from the mapping file
									new InternalFileTXT(Constants.FILE_FOLDERS_COLORS).removeLine(folder.getFileName() + Constants.SEPARATOR) ;

									// Remove the folder file and update the applications list
									if(!file.remove()) return ;
									folders.remove(getBindingAdapterPosition()) ;
									ActivityMain.updateList(context) ;
									notifyDataSetChanged() ;
								}) ;
						dialog.show() ;
					}
			}
		}
	}
}
