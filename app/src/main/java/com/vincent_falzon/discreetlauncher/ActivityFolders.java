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
import android.app.Activity ;
import android.content.Context ;
import android.content.DialogInterface ;
import android.graphics.Bitmap ;
import android.graphics.Canvas ;
import android.graphics.drawable.Drawable ;
import android.os.Bundle ;
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import android.view.inputmethod.InputMethodManager ;
import android.widget.EditText ;
import android.widget.ImageView ;
import android.widget.TextView ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.core.content.res.ResourcesCompat ;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.storage.InternalFilePNG ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;

/**
 * Allow to import and export settings, favorites applications and shortcuts.
 */
public class ActivityFolders extends AppCompatActivity implements View.OnClickListener
{
	// Attributes
	private FoldersListAdapter adapter ;
	private ArrayList<Folder> folders ;


	/**
	 * Constructor.
	 * @param savedInstanceState To retrieve the context
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
	}


	/**
	 * Perform an action when an element is clicked.
	 * @param view Target element
	 */
	public void onClick(View view)
	{
		// Identify which element has been clicked
		if(view.getId() == R.id.new_folder_button)
			{
				// Retrieve the new folder name and check that it is not empty
				String new_folder_name = ((EditText)findViewById(R.id.new_folder_name)).getText().toString() ;
				if(new_folder_name.isEmpty()) return ;

				// Hide the keyboard and check if the folder already exists
				((InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0) ;
				InternalFileTXT file = new InternalFileTXT(Constants.FILE_FOLDER_PREFIX + new_folder_name + ".txt") ;
				if(file.exists())
					{
						// Display an error message and quit
						ShowDialog.toastLong(this, getString(R.string.error_folder_already_exists)) ;
						return ;
					}

				// Use the notification icon as folder icon
				Drawable icon = ResourcesCompat.getDrawable(getResources(), R.drawable.notification_icon, null) ;
				int icon_size = Math.round(48 * getResources().getDisplayMetrics().density) ;
				if(icon != null) icon.setBounds(0, 0, icon_size, icon_size) ;

				// Create the folder and update the list
				file.writeLine("") ;
				folders.add(new Folder(new_folder_name, icon)) ;
				ActivityMain.updateList(this) ;
				adapter.notifyDataSetChanged() ;
			}
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
			View view = inflater.inflate(R.layout.folder_view, parent, false) ;
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
			folderView.icon.setImageDrawable(folders.get(i).getIcon()) ;
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
		public class FolderView extends RecyclerView.ViewHolder implements View.OnClickListener
		{
			// Attributes
			private final TextView name ;
			private final ImageView icon ;


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
				icon = view.findViewById(R.id.edit_folder_icon) ;
				icon.setOnClickListener(this) ;
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
				// Initializations
				final Context context = view.getContext() ;
				final Folder folder = folders.get(getBindingAdapterPosition()) ;
				final InternalFileTXT file = new InternalFileTXT(folder.getFileName()) ;
				AlertDialog.Builder dialog = new AlertDialog.Builder(context) ;
				dialog.setNegativeButton(R.string.button_cancel, null) ;

				// Identify which element has been clicked
				int selection = view.getId() ;
				if(selection == R.id.edit_folder_icon)
					{
						// Get the list of applications
						final ArrayList<Application> applications = ActivityMain.getApplicationsList().getApplications(false) ;
						CharSequence[] app_names = new CharSequence[applications.size() + 1] ;
						app_names[0] = context.getString(R.string.dialog_default_folder_icon) ;
						int i = 1 ;
						for(Application application : applications)
						{
							app_names[i] = application.getDisplayName() ;
							i++ ;
						}

						// Display the list of applications for user selection
						final int[] selected_application = { 0 } ;
						selected_application[0] = -1 ;
						dialog.setTitle(R.string.dialog_edit_icon) ;
						dialog.setSingleChoiceItems(app_names, -1,
								new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										selected_application[0] = which ;
									}
								}) ;
						dialog.setPositiveButton(R.string.button_apply,
								new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										// Check if an icon has been selected
										if(selected_application[0] < 0) return ;

										// Retrieve the new icon to use for the folder
										Drawable icon ;
										String folder_name = folder.getName().replace(Constants.APK_FOLDER, "") ;
										if(selected_application[0] == 0)
											{
												// Use the notification icon as folder icon
												icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.notification_icon, null) ;
												int icon_size = Math.round(48 * context.getResources().getDisplayMetrics().density) ;
												if(icon != null) icon.setBounds(0, 0, icon_size, icon_size) ;
												new InternalFilePNG(Constants.FILE_ICON_FOLDER_PREFIX + folder_name + ".png").remove() ;
											}
											else
											{
												// Retrieve the icon from the selected application
												icon = applications.get(selected_application[0] - 1).getIcon() ;

												// Save the icon as a file for later display
												Bitmap bitmapIcon = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888) ;
												icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight()) ;
												icon.draw(new Canvas(bitmapIcon)) ;
												new InternalFilePNG(Constants.FILE_ICON_FOLDER_PREFIX + folder_name + ".png").writeToFile(bitmapIcon) ;
											}

										// Update the applications list
										folder.setIcon(icon) ;
										ActivityMain.updateList(context) ;
										notifyDataSetChanged() ;
									}
								}) ;
						dialog.show() ;
					}
					else if(selection == R.id.edit_folder_name)
					{
						// Ask the user for the new name
						final EditText newFolderName = new EditText(context) ;
						final String current_folder_name = folder.getName().replace(Constants.APK_FOLDER, "") ;
						newFolderName.setText(current_folder_name) ;
						dialog.setTitle(R.string.dialog_edit_name) ;
						dialog.setView(newFolderName) ;
						dialog.setPositiveButton(R.string.button_apply,
								new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										// Check if the new name is empty or already exists
										String new_folder_name = newFolderName.getText().toString() ;
										if(new_folder_name.isEmpty())
											{
												// Display an error message and quit
												ShowDialog.toastLong(context, context.getString(R.string.error_folder_empty_name)) ;
												return ;
											}
										if(new InternalFileTXT(Constants.FILE_FOLDER_PREFIX + new_folder_name + ".txt").exists())
											{
												// Display an error message and quit
												ShowDialog.toastLong(context, context.getString(R.string.error_folder_already_exists)) ;
												return ;
											}

										// Rename the folder file
										if(file.rename(Constants.FILE_FOLDER_PREFIX + new_folder_name + ".txt"))
											{
												// Update the favorites if necessary
												InternalFileTXT favorites = new InternalFileTXT(Constants.FILE_FAVORITES) ;
												boolean was_in_favorites = favorites.removeLine(folder.getName()) ;
												folder.setDisplayName(new_folder_name) ;
												if(was_in_favorites) favorites.writeLine(folder.getName()) ;

												// Update the icon filename if necessary
												InternalFilePNG icon_file = new InternalFilePNG(Constants.FILE_ICON_FOLDER_PREFIX + current_folder_name + ".png") ;
												if(icon_file.exists()) icon_file.rename(Constants.FILE_ICON_FOLDER_PREFIX + new_folder_name + ".png") ;

												// Update the applications list
												ActivityMain.updateList(context) ;
												notifyDataSetChanged() ;
											}
											else ShowDialog.toastLong(context, context.getString(R.string.error_rename_file, file.getName())) ;
									}
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
							app_names[i] = application.getDisplayName() ;
							i++ ;
						}

						// Retrieve the current favorites applications
						final boolean[] selected = new boolean[app_names.length] ;
						if(file.exists()) for(i = 0 ; i < app_names.length ; i++) selected[i] = file.isLineExisting(applications.get(i).getName()) ;
							else for(i = 0 ; i < app_names.length ; i++) selected[i] = false ;

						// Let the user select the folder content
						dialog.setMultiChoiceItems(app_names, selected,
								new DialogInterface.OnMultiChoiceClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i, boolean b) { }
								}) ;
						dialog.setPositiveButton(R.string.button_apply,
								new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										// Remove the current folder file
										if(!file.remove())
											{
												ShowDialog.toastLong(context, context.getString(R.string.error_remove_file, file.getName())) ;
												return ;
											}

										// Write the new folder file
										for(i = 0 ; i < selected.length ; i++)
										{
											// Add selected applications to the folder
											if(selected[i])
												if(!file.writeLine(applications.get(i).getName()))
													{
														ShowDialog.toastLong(context, context.getString(R.string.error_favorite, applications.get(i).getDisplayName())) ;
														return ;
													}
										}

										// Update the display in the activity
										folder.getApplications().clear() ;
										for(String name : file.readAllLines())
										{
											// Search the internal name in the applications list
											for(Application application : applications)
												if(application.getName().equals(name))
													{
														// Add the application in the folder
														folder.addToFolder(application) ;
														break ;
													}
										}

										// Update the applications list
										ActivityMain.updateList(context) ;
										notifyDataSetChanged() ;
									}
								}) ;
						dialog.show() ;
					}
					else if(selection == R.id.remove_folder)
					{
						// Ask confirmation before removing the folder
						dialog.setMessage(R.string.folders_remove_help) ;
						dialog.setPositiveButton(R.string.button_remove,
								new DialogInterface.OnClickListener()
								{
									// Save the new list of favorites applications
									@Override
									public void onClick(DialogInterface dialogInterface, int i)
									{
										// Remove the folder file and update the applications list
										String folder_name = folder.getName().replace(Constants.APK_FOLDER, "") ;
										new InternalFilePNG(Constants.FILE_ICON_FOLDER_PREFIX + folder_name + ".png").remove() ;
										if(!file.remove())
											{
												ShowDialog.toastLong(context, context.getString(R.string.error_remove_file, file.getName())) ;
												return ;
											}
										folders.remove(getBindingAdapterPosition()) ;
										ActivityMain.updateList(context) ;
										notifyDataSetChanged() ;
									}
								}) ;
						dialog.show() ;
					}
			}
		}
	}
}
