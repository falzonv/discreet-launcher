package com.vincent_falzon.discreetlauncher.core ;

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
import android.graphics.Bitmap ;
import android.graphics.Canvas ;
import android.graphics.ColorFilter ;
import android.graphics.Paint ;
import android.graphics.PixelFormat ;
import android.graphics.drawable.Drawable ;
import androidx.core.content.res.ResourcesCompat ;
import com.vincent_falzon.discreetlauncher.R ;

/**
 * Create a folder icon containing the number of elements inside the folders.
 */
public class FolderIcon extends Drawable
{
	// Attribute
	private final Bitmap icon ;
	private final String number ;
	private final Paint paint ;
	private final float density ;


	/**
	 * Constructor.
	 * @param context To load the folder icon
	 * @param applications_number To display in the icon
	 */
	public FolderIcon(Context context, int applications_number)
	{
		// Retrieve the folder icon and convert it into a bitmap
		density = context.getResources().getDisplayMetrics().density ;
		int icon_size = Math.round(48 * density) ;
		Drawable folderIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.icon_folder, null) ;
		if(folderIcon != null)
			{
				icon = Bitmap.createBitmap(folderIcon.getIntrinsicWidth(), folderIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888) ;
				folderIcon.setBounds(0, 0, icon_size, icon_size) ;
				folderIcon.draw(new Canvas(icon)) ;
			}
			else icon = null ;

		// Retrieve the number to write and define its settings
		this.number = "" + applications_number ;
		paint = new Paint() ;
		paint.setAntiAlias(true) ;
		paint.setTextSize(16 * density) ;
		paint.setColor(context.getResources().getColor(R.color.white)) ;
		paint.setTextAlign(Paint.Align.CENTER) ;
	}


	/**
	 * Draw the folder icon with the number of applications inside.
	 * @param canvas Where to draw
	 */
	@Override
	public void draw(Canvas canvas)
	{
		canvas.drawBitmap(icon, 0, 0, paint);
		canvas.drawText(number, 24 * density, 42 * density, paint);
	}


	/**
	 * Needed to extend Drawable.
	 * @param alpha 0 is transparent and 255 is opaque
	 */
	@Override
	public void setAlpha(int alpha)
	{
		paint.setAlpha(alpha) ;
	}


	/**
	 * Needed to extend Drawable.
	 * @param colorFilter Color filter to apply, <code>null</code> to remove
	 */
	@Override
	public void setColorFilter(ColorFilter colorFilter)
	{
		paint.setColorFilter(colorFilter) ;
	}


	/**
	 * Needed to extend Drawable.
	 * @return Opacity class to use
	 */
	@Override
	public int getOpacity()
	{
		return PixelFormat.TRANSPARENT ;
	}
}
