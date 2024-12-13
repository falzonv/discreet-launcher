package com.vincent_falzon.discreetlauncher.core ;

// License
/*

	This file is part of Discreet Launcher.

	Copyright (C) 2019-2024 Vincent Falzon

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
import android.graphics.Bitmap ;
import android.graphics.Canvas ;
import android.graphics.Color ;
import android.graphics.ColorFilter ;
import android.graphics.Paint ;
import android.graphics.PixelFormat ;
import android.graphics.PointF ;
import android.graphics.PorterDuff ;
import android.graphics.PorterDuffColorFilter ;
import android.graphics.drawable.Drawable ;
import androidx.annotation.NonNull ;

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
	private final PointF text_location ;


	/**
	 * Constructor.
	 */
	public FolderIcon(Drawable baseIcon, int icon_size_pixels, int applications_number, int icon_color, int text_color)
	{
		// Prepare the base icon on which the number of apps will be written
		icon_size = icon_size_pixels ;
		if(baseIcon != null)
			{
				// Convert the base icon into a Bitmap of the correct size
				Bitmap convertedIcon = Bitmap.createBitmap(baseIcon.getIntrinsicWidth(), baseIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888) ;
				baseIcon.setBounds(0, 0, icon_size, icon_size) ;
				baseIcon.draw(new Canvas(convertedIcon)) ;

				// Get an editable copy of the Bitmap and, if it is the Discreet Launcher folder icon, change its color according to settings
				icon = convertedIcon.copy(Bitmap.Config.ARGB_8888, true) ;
				Paint iconPaint = new Paint() ;
				if(icon_color != Color.TRANSPARENT)
					iconPaint.setColorFilter(new PorterDuffColorFilter(icon_color, PorterDuff.Mode.SRC_IN)) ;
				new Canvas(icon).drawBitmap(icon, 0, 0, iconPaint) ;
			}
			else icon = null ;

		// Retrieve the number to write, if any, and define its settings
		this.number = String.valueOf(applications_number) ;
		paint = new Paint() ;
		if(applications_number >= 0)
			{
				paint.setAntiAlias(true) ;
				paint.setTextSize(icon_size / 3f) ;
				paint.setColor(text_color) ;
				paint.setTextAlign(Paint.Align.CENTER) ;
			}

		// Center the text when another icon than the Discreet Launcher folder icon is used
		if(icon_color == Color.TRANSPARENT) text_location = new PointF(icon_size * 0.5f, icon_size * 0.5f - paint.ascent() * 0.5f) ;
			else text_location = new PointF(icon_size * 0.5f, icon_size * 0.875f) ;
	}


	/**
	 * Draw the folder icon with the number of applications inside.
	 */
	@Override
	public void draw(@NonNull Canvas canvas)
	{
		if(icon != null) canvas.drawBitmap(icon, 0, 0, paint) ;
		if(!number.equals("-1")) canvas.drawText(number, text_location.x, text_location.y, paint) ;
	}


	// Needed to extend Drawable
	@Override
	public void setAlpha(int alpha)
	{
		paint.setAlpha(alpha) ;
	}


	// Needed to extend Drawable
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
