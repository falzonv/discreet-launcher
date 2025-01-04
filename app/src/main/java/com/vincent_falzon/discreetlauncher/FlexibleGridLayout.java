package com.vincent_falzon.discreetlauncher ;

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
import androidx.recyclerview.widget.GridLayoutManager ;
import androidx.recyclerview.widget.RecyclerView ;

/**
 * Provide a GridLayoutManager with a dynamic width adapted to the parent available width.
 */
public class FlexibleGridLayout extends GridLayoutManager
{
	// Attributes
	private final int item_width ;


	/**
	 * Constructor with an item width given in pixels.
	 */
	public FlexibleGridLayout(Context context, int item_width)
	{
		// Create the GridLayout starting with one column
		super(context, 1, RecyclerView.VERTICAL, false) ;

		// Retrieve the width of an item in pixels
		this.item_width = item_width ;
	}


	/**
	 * Called to lay out the elements of the RecyclerView.
	 */
	@Override
	public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state)
	{
		// Update the number of columns according to the available width
		setSpanCount(Math.max(1, getWidth() / item_width)) ;

		// Let the parent actions be performed
		super.onLayoutChildren(recycler, state) ;
	}
}
