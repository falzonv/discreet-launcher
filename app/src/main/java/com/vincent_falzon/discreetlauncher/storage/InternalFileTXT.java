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
import java.io.BufferedReader ;
import java.io.FileReader ;
import java.io.FileWriter ;
import java.util.ArrayList ;

/**
 * Manage the storage of an internal TXT file.
 */
public class InternalFileTXT extends InternalFile
{
	/**
	 * Constructor to create or open an internal TXT file.
	 * @param context To get the folder path
	 * @param filename Name of the file including the extension
	 */
	public InternalFileTXT(Context context, String filename)
	{
		super(context, filename) ;
	}


	/**
	 * Read the internal file line by line and store the result in an array of lines.
	 * @return Content of the file or <code>null</code> if an error happened
	 */
	public ArrayList<String> readAllLines()
	{
		// Check if the file exists
		if(!exists()) return null ;

		// Prepare the table used to store the lines
		ArrayList<String> content = new ArrayList<>() ;
		String buffer ;

		try
		{
			// Read the content from the file line by line
			BufferedReader reader = new BufferedReader(new FileReader(file)) ;
			while((buffer = reader.readLine()) != null) content.add(buffer) ;
			reader.close() ;
		}
		catch (Exception e)
		{
			// An error happened while reading the file
			return null ;
		}

		// Return the content of the file
		return content ;
	}


	/**
	 * Check if a specific line exists in the file.
	 * @param search Text of the line
	 * @return <code>true</code> if it exists, <code>false</code> otherwis
	 */
	public boolean isLineExisting(String search)
	{
		return readAllLines().contains(search) ;
	}


	/**
	 * Write a new line at the end of the file (create it if not existing yet).
	 * After the writing, a new line character is added.
	 * @param added_line To write in the file
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public boolean writeLine(String added_line)
	{
		try
		{
			// Write the line at the end of the file followed by a new line character
			FileWriter writer = new FileWriter(file, true) ;
			writer.write(added_line) ;
			writer.write(System.lineSeparator()) ;
			writer.close() ;
			return true ;
		}
		catch (Exception e)
		{
			// An error happened while writing the line
			return false ;
		}
	}
}
