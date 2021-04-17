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
import android.content.pm.PackageManager ;
import android.content.res.Resources ;
import android.graphics.drawable.Drawable ;
import androidx.core.content.res.ResourcesCompat ;
import org.xmlpull.v1.XmlPullParser ;
import org.xmlpull.v1.XmlPullParserException ;
import java.io.IOException ;

/**
 * Provide icon pack support.
 */
class IconPack
{
	// Attributes
	private final String pack_name;
	private Resources pack_resources;
	private int appfilter_id;


	/**
	 * Constructor.
	 * @param name Icon pack APK name
	 */
	IconPack(String name)
	{
		pack_name = name ;
		pack_resources = null ;
		appfilter_id = 0 ;
	}


	/**
	 * Try to load the icon pack resources.
	 * @param context To get the package manager
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	boolean loadResources(Context context)
	{
		try
		{
			// Try to load the icon pack resources
			PackageManager apkManager = context.getPackageManager() ;
			pack_resources = apkManager.getResourcesForApplication(pack_name) ;
			return true ;
		}
		catch(PackageManager.NameNotFoundException e)
		{
			return false ;
		}
	}


	/**
	 * Try to find the ID of the appfilter.xml file in the selected icon pack
	 * @return <code>true</code> if successful, <code>false</code> otherwise
	 */
	boolean findAppfilterID()
	{
		// Check if the resources are loaded
		if(pack_resources == null) return false ;

		// Try to get the appfilter.xml file
		appfilter_id = pack_resources.getIdentifier("appfilter", "xml", pack_name) ;
		return (appfilter_id > 0) ;
	}


	/**
	 * Search the icon of an application in the pack.
	 * @param apk Package name of the application
	 * @param name Internal name of the application
	 * @return An icon or <code>null</code> if it cannot be retrieved
	 */
	Drawable searchIcon(String apk, String name)
	{
		// Check if appfilter.xml exists in the icon pack
		if(appfilter_id <= 0) return null ;

		// Initializations (XML file need to be reloaded for each icon)
		XmlPullParser appfilter = pack_resources.getXml(appfilter_id) ;
		String component_info = "ComponentInfo{" + apk + "/" + name ;
		int i, j ;

		try
		{
			// Browse the appfilter.xml file
			int event = appfilter.getEventType() ;
			while(event != XmlPullParser.END_DOCUMENT)
			{
				// Search only the <item ...> tags
				if((event == XmlPullParser.START_TAG) && appfilter.getName().equals("item"))
					{
						// Browse up to the "component" attribute
						for(i = 0; i < appfilter.getAttributeCount() ; i++)
						{
							if(appfilter.getAttributeName(i).equals("component"))
								{
									// Check if this is the searched package
									if(appfilter.getAttributeValue(i).startsWith(component_info))
										{
											// Get the icon name in the pack
											String icon_name = "";
											for(j = 0; j < appfilter.getAttributeCount() ; j++)
												if(appfilter.getAttributeName(j).equals("drawable"))
													icon_name = appfilter.getAttributeValue(j) ;

											// Try to load the icon from the pack
											int icon_id = pack_resources.getIdentifier(icon_name, "drawable", pack_name) ;
											if(icon_id > 0) return ResourcesCompat.getDrawable(pack_resources, icon_id, null) ;

											// No icon to load
											return null ;
										}

									// Move to the next <item ...> tag
									break ;
								}
						}
					}
				event = appfilter.next() ;
			}

			// Package not found in the icon pack
			return null ;
		}
		catch(XmlPullParserException | IOException e)
		{
			// An error happened during the parsing
			return null ;
		}
	}
}
