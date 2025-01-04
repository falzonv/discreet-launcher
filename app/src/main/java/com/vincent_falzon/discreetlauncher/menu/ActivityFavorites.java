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
import android.graphics.drawable.Drawable ;
import android.os.Build ;
import android.os.Bundle ;
import android.view.LayoutInflater ;
import android.view.MotionEvent ;
import android.view.View ;
import android.view.ViewGroup ;
import android.widget.ImageView ;
import android.widget.TextView ;
import androidx.annotation.NonNull ;
import androidx.appcompat.app.AlertDialog ;
import androidx.appcompat.app.AppCompatActivity ;
import androidx.appcompat.content.res.AppCompatResources ;
import androidx.recyclerview.widget.ItemTouchHelper ;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import com.vincent_falzon.discreetlauncher.core.Folder ;
import com.vincent_falzon.discreetlauncher.storage.InternalFileTXT ;
import java.text.Collator ;
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
	private Drawable folder_icon ;
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
		folder_icon = AppCompatResources.getDrawable(this, R.drawable.icon_folder) ;
		icon_size = Math.round(32 * getResources().getDisplayMetrics().density) ;
		favorites = ActivityMain.getApplicationsList().getFavorites() ;
		findViewById(R.id.button_select_favorites).setOnClickListener(this) ;
		findViewById(R.id.button_sort_alphabetically).setOnClickListener(this) ;

		// Display the sortable list of favorites
		RecyclerView recycler = findViewById(R.id.favorites_list) ;
		recycler.setLayoutManager(new LinearLayoutManager(this)) ;
		recycler.setAdapter(adapter) ;
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			recycler.setOverScrollMode(View.OVER_SCROLL_NEVER) ;

		// Listen for sorting actions on the list
		touchManager = new ItemTouchHelper(new TouchCallback()) ;
		touchManager.attachToRecyclerView(recycler) ;
	}


	/**
	 * Called when an element is clicked.
	 */
	@Override
	public void onClick(View view)
	{
		// Check which element was clicked and perform the related actions
		int selection = view.getId() ;
		if(selection == R.id.button_select_favorites) selectFavorites() ;
			else if(selection == R.id.button_sort_alphabetically) sortFavoritesAlphabetically() ;
	}


	/**
	 * Display a dialog allowing to add and remove multiple favorites at the same time.
	 */
	@SuppressLint("NotifyDataSetChanged")
	private void selectFavorites()
	{
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
		dialog.setMultiChoiceItems(app_names, selected, (dialogInterface, position, checked) -> { }) ;
		dialog.setPositiveButton(R.string.button_apply, (dialogInterface, position) -> {
				// Replace the internal file content with the new selected applications
				if(!file.remove()) return ;
				for(position = 0 ; position < selected.length ; position++)
					if(selected[position]) file.writeLine(applications.get(position).getComponentInfo()) ;

				// Update the list of favorites
				ActivityMain.updateFavorites(null) ;
				adapter.notifyDataSetChanged() ;
			}) ;
		dialog.setNegativeButton(R.string.button_cancel, null) ;
		dialog.show() ;
	}


	/**
	 * Re-order all the current favorites from A to Z.
	 */
	@SuppressLint("NotifyDataSetChanged")
	private void sortFavoritesAlphabetically()
	{
		// Do not continue if there are no favorites to sort
		if(favorites.size() < 2) return ;

		// Ask confirmation before sorting the favorites
		AlertDialog.Builder dialog = new AlertDialog.Builder(this) ;
		dialog.setMessage(R.string.warning_sort_favorites_alphabetically) ;
		dialog.setPositiveButton(R.string.button_sort, (dialogInterface, which) -> {
				// Sort the favorites in alphabetical order based on display name
				Collator collator = Collator.getInstance() ;
				collator.setStrength(Collator.PRIMARY) ;
				Collections.sort(favorites, (application1, application2) -> collator.compare(application1.getDisplayName(), application2.getDisplayName())) ;

				// Update the list of favorites
				adapter.notifyDataSetChanged() ;
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


	// ---------------------------------------------------------------------------------------------

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

			// Retrieve the icon of the favorite or use the folder icon instead
			Drawable.ConstantState iconState = favorites.get(i).getIcon().getConstantState() ;
			if(iconState == null) iconState = folder_icon.getConstantState() ;

			// Create a copy to not downsize the real favorite icon, then resize and display it
			if(iconState != null)
				{
					Drawable icon = iconState.newDrawable(getResources()).mutate() ;
					icon.setBounds(0, 0, icon_size, icon_size) ;
					favoriteView.name.setCompoundDrawables(icon, null, null, null) ;
				}

			// Listen for dragging action on the view of this favorite
			favoriteView.itemView.setOnTouchListener((view, event) ->
				{
					view.performClick() ;
					if(event.getAction() == MotionEvent.ACTION_DOWN) touchManager.startDrag(favoriteView) ;
					return false ;
				}) ;

			// Prepare the button to move a favorite before the previous one
			if(i > 0)
				{
					favoriteView.moveBefore.setVisibility(View.VISIBLE) ;
					favoriteView.moveBefore.setContentDescription(getString(R.string.favorite_move_before, favorites.get(i - 1).getDisplayName())) ;
				}
				else favoriteView.moveBefore.setVisibility(View.INVISIBLE) ;

			// Prepare the button to move a favorite after the next one
			if(i < (favorites.size() - 1))
				{
					favoriteView.moveAfter.setVisibility(View.VISIBLE) ;
					favoriteView.moveAfter.setContentDescription(getString(R.string.favorite_move_after, favorites.get(i + 1).getDisplayName())) ;
				}
				else favoriteView.moveAfter.setVisibility(View.INVISIBLE) ;
		}


		/**
		 * Return the number of items in the RecyclerView.
		 */
		@Override
		public int getItemCount()
		{
			return favorites.size() ;
		}


		// -----------------------------------------------------------------------------------------

		/**
		 * Represent a favorite item in the RecyclerView.
		 */
		public class FavoriteView extends RecyclerView.ViewHolder implements View.OnClickListener
		{
			// Attributes
			private final TextView name ;
			private final ImageView moveBefore ;
			private final ImageView moveAfter ;

			/**
			 * Constructor.
			 */
			FavoriteView(View view)
			{
				// Let the parent actions be performed
				super(view) ;

				// Initializations
				name = view.findViewById(R.id.favorite_name) ;
				moveBefore = view.findViewById(R.id.favorite_move_before) ;
				moveAfter = view.findViewById(R.id.favorite_move_after) ;

				// Listen for clicks on the buttons
				moveBefore.setOnClickListener(this) ;
				moveAfter.setOnClickListener(this) ;
			}


			/**
			 * Called when an element is pressed.
			 */
			public void onClick(View view)
			{
				// Check the clicked button and perform the move if possible
				int position = getBindingAdapterPosition() ;
				if((view.getId() == R.id.favorite_move_before) && (position > 0))
					{
						// Move the clicked element before the previous one
						Collections.swap(favorites, position, position - 1) ;
						adapter.notifyItemMoved(position, position - 1) ;
						adapter.notifyItemChanged(position - 1) ;
						adapter.notifyItemChanged(position) ;
					}
					else if((view.getId() == R.id.favorite_move_after) && (position < (favorites.size() - 1)))
					{
						// Move the clicked element after the next one
						Collections.swap(favorites, position, position + 1) ;
						adapter.notifyItemMoved(position, position + 1) ;
						adapter.notifyItemChanged(position) ;
						adapter.notifyItemChanged(position + 1) ;
					}
			}
		}
	}


	// ---------------------------------------------------------------------------------------------

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
			adapter.notifyItemChanged(start_position) ;
			adapter.notifyItemChanged(end_position) ;
			return true ;
		}


		// Needed to extend ItemTouchHelper.Callback
		@Override
		public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { }
	}
}
