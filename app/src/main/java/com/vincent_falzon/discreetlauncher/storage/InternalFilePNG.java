package com.vincent_falzon.discreetlauncher.storage ;

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
	 * @param context To get the folder path
	 * @param filename Name of the file including the extension
	 */
	public InternalFilePNG(Context context, String filename)
	{
		super(context, filename) ;
	}


	/**
	 * Write a Bitmap image to the internal file.
	 * @param bitmap The image to write
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public boolean writeToFile(Bitmap bitmap)
	{
		// Do not continue if the bitmap is empty
		if(bitmap == null) return false ;

		try
		{
			// Write the Bitmap in the file
			FileOutputStream output_file = new FileOutputStream(file) ;
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, output_file) ;
			output_file.close() ;
			return true ;
		}
		catch(IOException e)
		{
			// An error happened
			return false ;
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
	 * Convert the content of the internal file to a Base64 String.
	 * @return A Base64 String or <code>null</code> if an error happened
	 */
	public String convertFileToString()
	{
		// Try to decode the Bitmap
		Bitmap bitmap = readFromFile() ;
		if(bitmap == null) return null ;

		// Encode the Bitmap as a Base64 String
		ByteArrayOutputStream byteArray = new ByteArrayOutputStream() ;
		bitmap.compress(Bitmap.CompressFormat.PNG,100, byteArray) ;
		return Base64.encodeToString(byteArray.toByteArray(), Base64.NO_WRAP) ;
	}


	/**
	 * Decode a Base64 String Bitmap and write it to the internal file.
	 * @param data A Bitmap encoded as a Base64 String
	 */
	public void convertStringToFile(String data)
	{
		byte[] bitmap_bytes = Base64.decode(data, Base64.NO_WRAP) ;
		writeToFile(BitmapFactory.decodeByteArray(bitmap_bytes, 0, bitmap_bytes.length)) ;
	}
}
