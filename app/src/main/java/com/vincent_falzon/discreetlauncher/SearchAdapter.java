package com.vincent_falzon.discreetlauncher ;

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
import android.content.Context ;
import android.widget.Filter ;
import android.widget.Filterable ;
import com.vincent_falzon.discreetlauncher.core.Application ;
import java.text.Collator ;
import java.util.ArrayList ;

/**
 * Fill a RecyclerView with a list of applications filtered with a search result.
 */
public class SearchAdapter extends RecyclerAdapter implements Filterable
{
	// Attributes
	private final ArrayList<Application> initialApplicationsList ;
	private final Collator collator ;


	/**
	 * Constructor to fill a RecyclerView with the applications list.
	 * @param context To get the settings
	 * @param applicationsList Applications to display in the recycler
	 */
	public SearchAdapter(Context context, ArrayList<Application> applicationsList)
	{
		// Let the parent actions be performed
		super(context, applicationsList) ;

		// Initializations
		initialApplicationsList = applicationsList ;
		collator = Collator.getInstance() ;
		collator.setStrength(Collator.PRIMARY) ;
	}


	/**
	 * Create the filter which will be used to search in the list.
	 * @return Filter used to match the search
	 */
	@Override
	public Filter getFilter()
	{
		return new Filter()
		{
			/**
			 * Define the results after the filter is applied.
			 * @param filter Search pattern
			 * @return Results after filtering
			 */
			@Override
			protected FilterResults performFiltering(CharSequence filter)
			{
				// Check if there is a search pattern
				String search = filter.toString() ;
				if(search.isEmpty()) applicationsList = initialApplicationsList ;
					else
					{
						// Filter the results based on the search pattern
						applicationsList = new ArrayList<>() ;
						int search_length = search.length() ;
						for(Application application : initialApplicationsList)
							if(searchIncludingVariants(search, search_length, application.getDisplayName()))
								applicationsList.add(application) ;
					}

				// Prepare the filter results
				FilterResults filterResults = new FilterResults() ;
				filterResults.values = applicationsList ;
				return filterResults ;
			}


			/**
			 * Display the search results.
			 * @param filter Search pattern
			 * @param results Results after filtering
			 */
			@Override
			protected void publishResults(CharSequence filter, FilterResults results)
			{
				//noinspection unchecked
				applicationsList = (ArrayList<Application>)results.values ;
				notifyDataSetChanged() ;
			}
		} ;
	}


	/**
	 * Provide the first item currently displayed in the adapter.
	 * @return First item in the list, or <code>null</code> if the list is empty
	 */
	public Application getFirstItem()
	{
		if(getItemCount() < 1) return null ;
		return applicationsList.get(0) ;
	}


	/**
	 * Check if a text contains the searched sequence ignoring case and accents.
	 * @param search Sequence to search
	 * @param search_length For optimization in loops
	 * @param text Tested text
	 * @return <code>true</code> if the sequence is in the text, <code>false</code> otherwise
	 */
	private boolean searchIncludingVariants(String search, int search_length, String text)
	{
		// Do not continue if the searched sequence is longer than the text itself
		int text_length = text.length() ;
		if(search_length > text_length) return false ;

		// Search the sequence at all possible positions in the text
		for(int i = 0 ; i <= (text_length - search_length) ; i++)
			if(collator.compare(text.substring(i, i + search_length), search) == 0)
				return true ;

		// The searched sequence was not found
		return false ;
	}
}
