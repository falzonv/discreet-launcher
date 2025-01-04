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
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.Utils ;
import java.io.BufferedReader ;
import java.io.FileReader ;
import java.io.FileWriter ;
import java.io.IOException ;
import java.util.ArrayList ;

/**
 * Manage the storage of an internal TXT file.
 */
public class InternalFileTXT extends InternalFile
{
	// Constants
	private static final String TAG = "InternalFileTXT" ;


	/**
	 * Constructor (the given filename should include the extension).
	 */
	public InternalFileTXT(String filename)
	{
		super(filename) ;
	}


	/**
	 * Return the file content as an array of lines, or <code>null</code> if an error happened.
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
	 * Check if the given line exists in the file.
	 */
	public boolean isLineExisting(String searched_line)
	{
		if(!exists()) return false ;
		return readAllLines().contains(searched_line) ;
	}


	/**
	 * Write a line followed by <code>\n</code> at the end of the file (created if not existing).
	 */
	public void writeLine(String added_line)
	{
		try
		{
			// Write the line at the end of the file followed by a new line character
			FileWriter writer = new FileWriter(file, true) ;
			writer.write(added_line) ;
			writer.write(System.lineSeparator()) ;
			writer.close() ;
		}
		catch(IOException exception)
		{
			// An error happened while writing the line
			Utils.logError(TAG, exception.getMessage()) ;
		}
	}


	/**
	 * Search and remove lines starting with the provided pattern.
	 * @return <code>true</code> if at least one line was removed, <code>false</code> otherwise
	 */
	public boolean removeLine(String to_remove)
	{
		// Keep a copy of the file content and remove it
		ArrayList<String> content = readAllLines() ;
		if(content == null) return false ;
		if(!remove()) return false ;

		// Write back the content of the file except the line to remove
		boolean result = false ;
		for(String line : content)
		{
			if(line.startsWith(to_remove)) result = true ;
				else writeLine(line) ;
		}

		// Recreate the empty file if the removed line was the single one
		if(!exists()) writeLine("") ;
		return result ;
	}


	/**
	 * Return an array of lines where each line starts with the filename.
	 */
	public ArrayList<String> prepareForExport()
	{
		// Return the content of the file or indicate that it does not exist
		ArrayList<String> content = new ArrayList<>() ;
		if(!exists()) content.add(file.getName() + ": " + Constants.NONE) ;
			else for(String line : readAllLines()) content.add(file.getName() + ": " + line) ;
		return content ;
	}
}
