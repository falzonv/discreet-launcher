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

	// Constants related to applications
	public static final String APK_SEARCH = "discreetlauncher.search" ;
	public static final String APK_FOLDER = "discreetlauncher.folder" ;
	public static final String APK_SHORTCUT = "discreetlauncher.shortcut" ;
	public static final String APK_SHORTCUT_LEGACY = "discreetlauncher.shortcut_legacy" ;
	public static final String SHORTCUT_SEPARATOR = "--SHORT--CUT--" ;

	// Constants representing settings keys
	public static final String NONE = "none" ;
	public static final String NOTIFICATION = "notification" ;
	public static final String APPLICATION_THEME = "application_theme" ;
	public static final String BACKGROUND_COLOR = "background_color" ;
	public static final String TRANSPARENT_STATUS_BAR = "transparent_status_bar" ;
	public static final String HIDE_MENU_BUTTON = "hide_menu_button" ;
	public static final String CLOCK_FORMAT = "clock_format" ;
	public static final String ICON_PACK = "icon_pack" ;
	public static final String HIDE_APP_NAMES = "hide_app_names" ;
	public static final String FORCE_PORTRAIT = "force_portrait" ;
	public static final String IMMERSIVE_MODE = "immersive_mode" ;
	public static final String REVERSE_INTERFACE = "reverse_interface" ;
	public static final String TOUCH_TARGETS = "touch_targets" ;

	// To remove after 31/07/2021 (setting migrated to internal file)
	public static final String HIDDEN_APPLICATIONS = "hidden_applications" ;
	public static final String NOTIFICATION_SEPARATOR = "_discreet_" ;

	// To remove after 30/09/2021 (setting merged with clock format)
	public static final String DISPLAY_CLOCK = "display_clock" ;
}
