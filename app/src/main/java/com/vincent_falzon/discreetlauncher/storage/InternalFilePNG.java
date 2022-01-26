package com.vincent_falzon.discreetlauncher.storage ;

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
import android.graphics.BitmapFactory ;
import android.graphics.drawable.BitmapDrawable ;
import android.graphics.drawable.Drawable ;
import android.util.Base64 ;
import java.io.ByteArrayOutputStream ;
import java.io.FileOutputStream ;
import java.io.IOException ;

/**
 * Manage the storage of an internal PNG file.
 */
public class InternalFilePNG extends InternalFile
{
	/**
	 * Constructor to create or open an internal PNG file.
	 * @param filename Name of the file including the extension
	 */
	public InternalFilePNG(String filename)
	{
		super(filename) ;
	}


	/**
	 * Write a Bitmap image to the internal file.
	 * @param bitmap The image to write
	 */
	public void writeToFile(Bitmap bitmap)
	{
		// Do not continue if the bitmap is empty
		if(bitmap == null) return;

		try
		{
			// Write the Bitmap in the file
			FileOutputStream output_file = new FileOutputStream(file) ;
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, output_file) ;
			output_file.close() ;
		}
		catch(IOException e)
		{
			// An error happened
		}
	}


	/**
	 * Return the content of the internal file as a Bitmap.
	 * @return Content of the file or <code>null</code> if an error happened
	 */
	public Bitmap readFromFile()
	{
		if(!exists()) return null ;
		return BitmapFactory.decodeFile(file.getAbsolutePath(), new BitmapFactory.Options()) ;
	}


	/**
	 * Convert a Bitmap to a Drawable.
	 * @param context To get the resources for metrics
	 * @param bitmap The Bitmap to convert
	 * @return A Drawable or <code>null</code> if an error happened
	 */
	public Drawable convertBitmapToDrawable(Context context, Bitmap bitmap)
	{
		if(bitmap == null) return null ;
		return new BitmapDrawable(context.getResources(), bitmap) ;
	}


	/**
	 * Prepare the internal file for inclusion in an export file.
	 * @return The filename followed by a Base64 String or an empty string if an error happened
	 */
	public String prepareForExport()
	{
		// Try to decode the Bitmap
		Bitmap bitmap = readFromFile() ;
		if(bitmap == null) return "" ;

		// Encode the Bitmap as a Base64 String and return the result
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream() ;
		bitmap.compress(Bitmap.CompressFormat.PNG,100, byteArray) ;
		return file.getName() + ": " + Base64.encodeToString(byteArray.toByteArray(), Base64.NO_WRAP) ;
	}


	/**
	 * Decode the line representing a Bitmap in an import file and write it to the internal file.
	 * @param line Read from a previously exported file
	 */
	public void loadFromImport(String line)
	{
		// Extract and decode the Base64 String representing the Bitmap
		String data = line.replace(file.getName() + ": ", "") ;
		byte[] bitmap_bytes = Base64.decode(data, Base64.NO_WRAP) ;

		// Create the internal file from the decoded data
		writeToFile(BitmapFactory.decodeByteArray(bitmap_bytes, 0, bitmap_bytes.length)) ;
	}
}
