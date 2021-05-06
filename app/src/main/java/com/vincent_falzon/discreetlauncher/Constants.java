package com.vincent_falzon.discreetlauncher ;

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

/**
 * Provide all the constants of the application.
 */
public abstract class Constants
{
	// Constants related to internal files
	public static final String FILE_FAVORITES = "favorites.txt" ;
	public static final String FILE_FOLDER_PREFIX = "folder_" ;
	public static final String FILE_HIDDEN = "hidden.txt" ;
	public static final String FILE_SHORTCUTS = "shortcuts.txt" ;
	public static final String FILE_SHORTCUTS_LEGACY = "shortcuts_legacy.txt" ;
	public static final String FILE_ICON_SHORTCUT_PREFIX = "icon_shortcut_" ;
	public static final String FILE_ICON_FOLDER_PREFIX = "icon_folder_" ;

	// Constants related to applications
	public static final String APK_FOLDER = "discreetlauncher.folder" ;
	public static final String APK_SHORTCUT = "discreetlauncher.shortcut" ;
	public static final String APK_SHORTCUT_LEGACY = "discreetlauncher.shortcut_legacy" ;
	public static final String SHORTCUT_SEPARATOR = "--SHORT--CUT--" ;

	// Constants related to display
	public static final int COLUMNS_PORTRAIT = 4 ;
	public static final int COLUMNS_LANDSCAPE = 5 ;

	// Constants representing settings keys
	public static final String DISPLAY_CLOCK = "display_clock" ;
	public static final String CLOCK_FORMAT = "clock_format" ;
	public static final String TRANSPARENT_STATUS_BAR = "transparent_status_bar" ;
	public static final String FORCE_PORTRAIT = "force_portrait" ;
	public static final String IMMERSIVE_MODE = "immersive_mode" ;
	public static final String HIDE_APP_NAMES = "hide_app_names" ;
	public static final String ICON_PACK = "icon_pack" ;
	public static final String HIDDEN_APPLICATIONS = "hidden_applications" ;
	public static final String NOTIFICATION = "notification" ;

	// Constants related to settings without being settings keys
	public static final String NONE = "none" ;
	public static final String NOTIFICATION_SEPARATOR = "_discreet_" ;
}
