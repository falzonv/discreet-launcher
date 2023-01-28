package com.vincent_falzon.discreetlauncher.core ;

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
import android.graphics.Bitmap ;
import android.graphics.Canvas ;
import android.graphics.ColorFilter ;
import android.graphics.Paint ;
import android.graphics.PixelFormat ;
import android.graphics.PorterDuff ;
import android.graphics.PorterDuffColorFilter ;
import android.graphics.drawable.Drawable ;
import androidx.appcompat.content.res.AppCompatResources ;
import com.vincent_falzon.discreetlauncher.R ;

/**
 * Create a colored folder icon containing the number of elements inside the folder.
 */
public class FolderIcon extends Drawable
{
	// Attribute
	private final String number ;
	private final Bitmap icon ;
	private final Paint paint ;
	private final int icon_size ;


	/**
	 * Constructor.
	 */
	public FolderIcon(Context context, int icon_size_pixels, int applications_number, int color)
	{
		// Retrieve the folder icon
		icon_size = icon_size_pixels ;
		Drawable folderIcon = AppCompatResources.getDrawable(context, R.drawable.icon_folder) ;
		if(folderIcon != null)
			{
				// Convert the folder icon into a Bitmap of the correct size
				Bitmap convertedIcon = Bitmap.createBitmap(folderIcon.getIntrinsicWidth(), folderIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888) ;
				folderIcon.setBounds(0, 0, icon_size, icon_size) ;
				folderIcon.draw(new Canvas(convertedIcon)) ;

				// Get an editable copy of the Bitmap and change its color according to settings
				icon = convertedIcon.copy(Bitmap.Config.ARGB_8888, true) ;
				Paint iconPaint = new Paint() ;
				iconPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)) ;
				new Canvas(icon).drawBitmap(icon, 0, 0, iconPaint) ;
			}
			else icon = null ;

		// Retrieve the number to write and define its settings
		this.number = "" + applications_number ;
		paint = new Paint() ;
		paint.setAntiAlias(true) ;
		paint.setTextSize(icon_size / 3f) ;
		paint.setColor(color) ;
		paint.setTextAlign(Paint.Align.CENTER) ;
	}


	/**
	 * Draw the folder icon with the number of applications inside.
	 */
	@Override
	public void draw(Canvas canvas)
	{
		canvas.drawBitmap(icon, 0, 0, paint) ;
		canvas.drawText(number, icon_size * 0.5f, icon_size * 0.875f, paint) ;
	}


	// Needed to extend Drawable.
	@Override
	public void setAlpha(int alpha)
	{
		paint.setAlpha(alpha) ;
	}


	// Needed to extend Drawable.
	@Override
	public void setColorFilter(ColorFilter colorFilter)
	{
		paint.setColorFilter(colorFilter) ;
	}


	// Needed to extend Drawable
	@Override
	public int getOpacity()
	{
		return PixelFormat.TRANSPARENT ;
	}
}
