package com.vincent_falzon.discreetlauncher.storage ;

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
import android.net.Uri ;
import android.os.ParcelFileDescriptor ;
import com.vincent_falzon.discreetlauncher.Utils ;
import java.io.BufferedReader ;
import java.io.FileReader ;
import java.io.FileWriter ;
import java.io.IOException ;
import java.util.ArrayList ;

/**
 * Perform actions on an external file selected by the user.
 */
public abstract class ExternalFile
{
	// Constants
	private static final String TAG = "ExternalFile" ;


	/**
	 * Return the file content as an array of lines, or <code>null</code> if an error happened.
	 */
	public static ArrayList<String> readAllLines(Context context, Uri location)
	{
		// Prepare the table used to store the lines
		ArrayList<String> content = new ArrayList<>() ;
		String buffer ;

		try
		{
			// Try to open the file
			ParcelFileDescriptor file = context.getContentResolver().openFileDescriptor(location, "r") ;
			if(file == null) return null ;

			// Read the content from the file line by line
			BufferedReader reader = new BufferedReader(new FileReader(file.getFileDescriptor())) ;
			while((buffer = reader.readLine()) != null) content.add(buffer) ;
			reader.close() ;
			file.close() ;
		}
		catch(IOException exception)
		{
			// An error happened while reading the file
			Utils.logError(TAG, exception.getMessage()) ;
			return null ;
		}

		// Return the content of the file
		return content ;
	}


	/**
	 * Write an array of lines in the external file (created if not existing).
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public static boolean writeAllLines(Context context, Uri location, ArrayList<String> content)
	{
		try
		{
			// Try to open the file
			ParcelFileDescriptor file = context.getContentResolver().openFileDescriptor(location, "w") ;
			if(file == null) return false ;

			// Write all lines in the file
			FileWriter writer = new FileWriter(file.getFileDescriptor()) ;
			for(String line : content)
			{
				writer.write(line);
				writer.write(System.lineSeparator()) ;
			}
			writer.close() ;
			file.close() ;
			return true ;
		}
		catch(IOException exception)
		{
			// An error happened while writing the line
			Utils.logError(TAG, exception.getMessage()) ;
			return false ;
		}
	}
}
