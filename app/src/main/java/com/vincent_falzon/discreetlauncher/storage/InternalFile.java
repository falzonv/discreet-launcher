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
import java.io.File ;
import java.io.FilenameFilter ;
import static com.vincent_falzon.discreetlauncher.ActivityMain.getInternalFolder ;

/**
 * Manage the storage of an internal file.
 */
public class InternalFile
{
	// Attributes
	final File file ;


	/**
	 * Constructor.
	 */
	InternalFile(String filename)
	{
		file = new File(getInternalFolder(), filename) ;
	}


	/**
	 * Check if the internal file exists on the system.
	 */
	public boolean exists()
	{
		return file.exists() ;
	}


	/**
	 * Try to remove the internal file (considered as successful if not existing).
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public boolean remove()
	{
		if(!exists()) return true ;
		return file.delete() ;
	}


	/**
	 * Return the name of the internal file without the path.
	 */
	public String getName()
	{
		return file.getName() ;
	}


	/**
	 * Try to rename the internal file.
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	public boolean rename(String new_filename)
	{
		return file.renameTo(new File(getInternalFolder(), new_filename)) ;
	}


	/**
	 * Search internal files starting with a prefix (returns <code>null</code> if none was found).
	 */
	public static String[] searchFilesStartingWith(Context context, final String prefix)
	{
		FilenameFilter filter = (directory, name) -> name.startsWith(prefix) ;
		return context.getFilesDir().list(filter) ;
	}
}
