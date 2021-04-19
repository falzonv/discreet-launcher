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
	public static final String FAVORITES_FILE = "favorites.txt" ;
	public static final String SHORTCUTS_FILE = "shortcuts.txt" ;
	public static final String SHORTCUTS_LEGACY_FILE = "shortcuts_legacy.txt" ;
	public static final String SHORTCUT_ICON_PREFIX = "icon_shortcut_" ;

	// Constants related to shortcuts
	public static final String APK_SHORTCUT = "discreetlauncher.shortcut" ;
	public static final String APK_SHORTCUT_LEGACY = "discreetlauncher.shortcut_legacy" ;
	public static final String SHORTCUT_SEPARATOR = "--SHORT--CUT--" ;

	// Constants related to folders
	public static final String APK_FOLDER = "discreetlauncher.folder" ;

	// Constants related to display
	public static final int COLUMNS_PORTRAIT = 4 ;
	public static final int COLUMNS_LANDSCAPE = 5 ;

	// Constants representing settings keys
	public static final String DISPLAY_CLOCK = "display_clock" ;
	public static final String TRANSPARENT_STATUS_BAR = "transparent_status_bar" ;
	public static final String FORCE_PORTRAIT = "force_portrait" ;
	public static final String ICON_PACK = "icon_pack" ;
	public static final String HIDDEN_APPLICATIONS = "hidden_applications" ;
	public static final String DISPLAY_NOTIFICATION = "display_notification" ;
	public static final String NOTIFICATION_TEXT = "notification_text" ;
	public static final String HIDE_ON_LOCK_SCREEN = "hide_on_lock_screen" ;
	public static final String NOTIFICATION_APP = "notification_app" ;

	// Constants related to settings without being settings keys
	public static final String NONE = "none" ;
	public static final String NOTIFICATION_SEPARATOR = "_discreet_" ;
}
