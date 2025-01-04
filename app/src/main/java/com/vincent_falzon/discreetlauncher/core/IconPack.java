package com.vincent_falzon.discreetlauncher.core ;

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
import android.annotation.SuppressLint ;
import android.content.Context ;
import android.content.SharedPreferences ;
import android.content.pm.PackageManager ;
import android.content.res.Resources ;
import android.graphics.drawable.Drawable ;
import androidx.core.content.res.ResourcesCompat ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.ActivityMain ;
import com.vincent_falzon.discreetlauncher.Constants ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.Utils ;
import org.xmlpull.v1.XmlPullParser ;
import org.xmlpull.v1.XmlPullParserException ;
import java.io.IOException ;

/**
 * Provide icon pack support.
 */
class IconPack
{
	// Constants
	private static final String TAG = "IconPack" ;

	// Attributes
	private final String pack_name ;
	private Resources pack_resources ;
	private int appfilter_id ;


	/**
	 * Constructor.
	 */
	@SuppressLint("DiscouragedApi")
	IconPack(Context context, String setting_key)
	{
		// Check if an icon pack is selected
		appfilter_id = 0 ;
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context) ;
		pack_name = settings.getString(setting_key, Constants.NONE) ;
		if(pack_name.equals(Constants.NONE)) return ;

		try
		{
			// Try to load the icon pack resources
			PackageManager apkManager = context.getPackageManager() ;
			pack_resources = apkManager.getResourcesForApplication(pack_name) ;
		}
		catch(PackageManager.NameNotFoundException exception)
		{
			// Display an error message and set the icon pack to none
			Utils.displayLongToast(context, context.getString(R.string.error_app_not_found, pack_name)) ;
			Utils.logInfo(TAG, pack_name + " not found, reset of \"" + setting_key + "\"") ;
			ActivityMain.setSkipListUpdate(true) ;
			SharedPreferences.Editor editor = settings.edit() ;
			editor.putString(setting_key, Constants.NONE).apply() ;
			ActivityMain.setSkipListUpdate(false) ;
			return ;
		}

		// Try to get the appfilter.xml file (display an error message if not successful)
		appfilter_id = pack_resources.getIdentifier("appfilter", "xml", pack_name) ;
		if(appfilter_id <= 0) appfilter_id = pack_resources.getIdentifier("appfilter", "raw", pack_name) ;
		if(appfilter_id <= 0) Utils.displayLongToast(context, context.getString(R.string.error_icon_pack_appfilter_not_found, pack_name)) ;
	}


	/**
	 * Search the icon of an application in the pack (returns <code>null</code> if not found).
	 */
	@SuppressLint("DiscouragedApi")
	Drawable searchIcon(String apk, String name)
	{
		// Do not continue if no icon pack is loaded
		if(appfilter_id <= 0) return null ;

		// Initializations (the XML file need to be reloaded for each icon)
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
						for(i = 0 ; i < appfilter.getAttributeCount() ; i++)
						{
							if(appfilter.getAttributeName(i).equals("component"))
								{
									// Check if this is the searched package
									if(appfilter.getAttributeValue(i).startsWith(component_info))
										{
											// Get the icon name in the pack
											String icon_name = "" ;
											for(j = 0 ; j < appfilter.getAttributeCount() ; j++)
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
		catch(XmlPullParserException | IOException exception)
		{
			// An error happened during the parsing
			Utils.logError(TAG, exception.getMessage()) ;
			return null ;
		}
	}
}
