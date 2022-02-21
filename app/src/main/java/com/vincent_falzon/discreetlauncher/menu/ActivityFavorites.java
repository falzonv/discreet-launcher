package com.vincent_falzon.discreetlauncher.menu ;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2022 Vincent Falzon

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
import android.content.DialogInterface ;
import android.graphics.drawable.Drawable ;
import android.os.Bundle ;
import android.view.LayoutInflater ;
import android.view.MotionEvent ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.TextView ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.recyclerview.widget.ItemTouchHelper ;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.util.ArrayList ;
import java.util.Collections ;

/**
 * Allow to manage favorites.
 */
public class ActivityFavorites extends AppCompatActivity implements View.OnClickListener
{
	// Attributes
	private ArrayList<Application> favorites ;
	private final RecyclerAdapter adapter = new RecyclerAdapter() ;
	private ItemTouchHelper touchManager ;
	private int icon_size ;


	/**
	 * Constructor.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// Let the parent actions be performed
		super.onCreate(savedInstanceState) ;

		// Initializations
		setContentView(R.layout.activity_favorites) ;
		icon_size = Math.round(32 * getResources().getDisplayMetrics().density) ;
		favorites = ActivityMain.getApplicationsList().getFavorites() ;
		findViewById(R.id.select_favorites_button).setOnClickListener(this) ;

		// Display the sortable list of favorites
		RecyclerView recycler = findViewById(R.id.favorites_list) ;
		recycler.setLayoutManager(new LinearLayoutManager(this)) ;
		recycler.setAdapter(adapter) ;

		// Listen for sorting actions on the list
		touchManager = new ItemTouchHelper(new TouchCallback()) ;
		touchManager.attachToRecyclerView(recycler) ;
	}


	/**
	 * Called when an element is clicked.
	 */
	public void onClick(View view)
	{
		// Do not continue if something else than the favorites selection button has been clicked
		if(view.getId() != R.id.select_favorites_button) return ;

		// Prepare the list of applications
		final ArrayList<Application> applications = new ArrayList<>(ActivityMain.getApplicationsList().getFavorites()) ;
		ArrayList<Application> allApplications = ActivityMain.getApplicationsList().getApplications(true) ;
		for(Application application : allApplications)
			if(!applications.contains(application)) applications.add(application) ;

		// List the names of all applications
		CharSequence[] app_names = new CharSequence[applications.size()] ;
		int i = 0 ;
		for(Application application : applications)
		{
			if(application instanceof Folder) app_names[i] = ((Folder)application).getDisplayNameWithCount() ;
				else app_names[i] = application.getDisplayName() ;
			i++ ;
		}

		// Retrieve the currently selected applications
		final InternalFileTXT file = new InternalFileTXT(Constants.FILE_FAVORITES) ;
		final boolean[] selected = new boolean[app_names.length] ;
		if(file.exists())
				for(i = 0 ; i < app_names.length ; i++)
					selected[i] = file.isLineExisting(applications.get(i).getComponentInfo()) ;
			else for(i = 0 ; i < app_names.length ; i++) selected[i] = false ;

		// Prepare and display the selection dialog
		AlertDialog.Builder dialog = new AlertDialog.Builder(this) ;
		dialog.setTitle(R.string.button_select_favorites) ;
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
						// Remove the current file
						if(!file.remove()) return ;

						// Write the new selected applications to the file
						for(i = 0 ; i < selected.length ; i++)
							if(selected[i]) file.writeLine(applications.get(i).getComponentInfo()) ;

						// Update the favorite applications list
						ActivityMain.updateFavorites(null) ;
						adapter.notifyDataSetChanged() ;
					}
				}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.show() ;
	}


	/**
	 * Perform actions when the user leaves the activity.
	 */
	@Override
	public void onPause()
	{
		// Let the parent actions be performed
		super.onPause() ;

		// Write the last favorites order in the file
		InternalFileTXT file = new InternalFileTXT(Constants.FILE_FAVORITES) ;
		if(file.remove())
			for(Application application : favorites) file.writeLine(application.getComponentInfo()) ;

		// Update the favorite applications list
		ActivityMain.updateFavorites(this) ;
	}


	/**
	 * Fill a RecyclerView with the favorites.
	 */
	private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.FavoriteView>
	{
		/**
		 * Create a FavoriteView to add in the RecyclerView based on an XML layout.
		 */
		@NonNull
		@Override
		public FavoriteView onCreateViewHolder(ViewGroup parent, int viewType)
		{
			LayoutInflater inflater = LayoutInflater.from(parent.getContext()) ;
			return new FavoriteView(inflater.inflate(R.layout.view_favorite, parent, false)) ;
		}


		/**
		 * Write the name of each favorite in the RecyclerView and listen for dragging action.
		 */
		@Override
		public void onBindViewHolder(@NonNull final FavoriteView favoriteView, int i)
		{
			// Display the name of the favorite
			if(favorites.get(i) instanceof Folder)
					favoriteView.name.setText(((Folder)favorites.get(i)).getDisplayNameWithCount()) ;
				else favoriteView.name.setText(favorites.get(i).getDisplayName()) ;

			// Display the icon of the favorite
			Drawable.ConstantState iconState = favorites.get(i).getIcon().getConstantState() ;
			if(iconState != null)
				{
					// Create a copy to not downsize the real favorite icon
					Drawable icon = iconState.newDrawable(getResources()).mutate() ;
					icon.setBounds(0, 0, icon_size, icon_size) ;
					favoriteView.name.setCompoundDrawables(icon, null, null, null) ;
				}

			// Listen for dragging action on the view of this favorite
			favoriteView.itemView.setOnTouchListener(new View.OnTouchListener()
			{
				@Override
				public boolean onTouch(View view, MotionEvent event)
				{
					view.performClick() ;
					if(event.getAction() == MotionEvent.ACTION_DOWN) touchManager.startDrag(favoriteView) ;
					return false ;
				}
			}) ;
		}


		/**
		 * Return the number of items in the RecyclerView.
		 */
		@Override
		public int getItemCount()
		{
			return favorites.size() ;
		}


		/**
		 * Represent a favorite item in the RecyclerView.
		 */
		public class FavoriteView extends RecyclerView.ViewHolder
		{
			// Attributes
			private final TextView name ;

			/**
			 * Constructor.
			 */
			FavoriteView(View view)
			{
				super(view) ;
				name = view.findViewById(R.id.favorite_item) ;
			}
		}
	}


	/**
	 * Called when a touch is detected on an element.
	 */
	public class TouchCallback extends ItemTouchHelper.Callback
	{
		/**
		 * Define the recognized touch gestures (only up and down drags are allowed).
		 */
		@Override
		public int getMovementFlags(@NonNull RecyclerView recycler, @NonNull RecyclerView.ViewHolder viewHolder)
		{
			return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) ;
		}


		/**
		 * Called when a ViewHolder is moved from a source over a target.
		 */
		@Override
		public boolean onMove(@NonNull RecyclerView recycler, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target)
		{
			// Retrieve the starting and ending positions of the dragging action
			int start_position = source.getBindingAdapterPosition() ;
			int end_position = target.getBindingAdapterPosition() ;

			// Perform the move and inform the adapter
			Collections.swap(favorites, start_position, end_position) ;
			adapter.notifyItemMoved(start_position, end_position) ;
			return true ;
		}


		// Needed to extend ItemTouchHelper.Callback
		@Override
		public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
	}
}
