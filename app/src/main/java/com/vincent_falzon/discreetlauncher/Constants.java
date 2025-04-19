package com.vincent_falzon.discreetlauncher ;

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

/**
 * Provide all the constants of the application.
 */
public interface Constants
{
	// Constants related to internal files
	String FILE_FAVORITES = "favorites.txt" ;
	String FILE_FOLDER_PREFIX = "folder_" ;
	String FILE_FOLDERS_COLORS = "folders_colors.txt" ;
	String FILE_HIDDEN = "hidden.txt" ;
	String FILE_RENAME_APPS = "rename_apps.txt" ;
	String FILE_SHORTCUTS = "shortcuts.txt" ;
	String FILE_SHORTCUTS_LEGACY = "shortcuts_legacy.txt" ;
	String FILE_ICON_SHORTCUT_PREFIX = "icon_shortcut_" ;

	// Constants related to applications
	String APK_SEARCH = "discreetlauncher.search" ;
	String APK_FOLDER = "discreetlauncher.folder" ;
	String APK_SHORTCUT = "discreetlauncher.shortcut" ;
	String APK_SHORTCUT_LEGACY = "discreetlauncher.shortcut_legacy" ;
	String SEPARATOR = "--SEPARATOR--" ;
	String SHORTCUT_SEPARATOR = "--SHORT--CUT--" ;
	int FAVORITES_PANEL = 0 ;
	int APP_DRAWER = 1 ;
	int FOLDER = 2 ;
	int SEARCH = 3 ;

	// Constants representing settings keys
	String NONE = "none" ;
	String APPLICATION_THEME = "application_theme" ;
	String TRANSPARENT_STATUS_BAR = "transparent_status_bar" ;
	String DARK_STATUS_BAR_ICONS = "dark_status_bar_icons" ;
	String ICON_SIZE_DP = "icon_size_dp" ;
	String HIDE_APP_NAMES = "hide_app_names" ;
	String HIDE_FOLDER_NAMES = "hide_folder_names" ;
	String REMOVE_PADDING = "remove_padding" ;
	String BACKGROUND_COLOR_FAVORITES = "background_color_favorites" ;
	String TEXT_COLOR_FAVORITES = "text_color_favorites" ;
	String BACKGROUND_COLOR_DRAWER = "background_color_drawer" ;
	String TEXT_COLOR_DRAWER = "text_color_drawer" ;
	String BACKGROUND_COLOR_FOLDERS = "background_color_folders" ;
	String TEXT_COLOR_FOLDERS = "text_color_folders" ;
	String CLOCK_FORMAT = "clock_format" ;
	String CLOCK_COLOR = "clock_color" ;
	String CLOCK_SHADOW_COLOR = "clock_shadow_color" ;
	String CLOCK_POSITION = "clock_position" ;
	String CLOCK_SIZE = "clock_size" ;
	String ICON_PACK = "icon_pack" ;
	String ICON_PACK_SECONDARY = "icon_pack_secondary" ;
	String ICON_COLOR_FILTER = "icon_color_filter" ;
	String NOTIFICATION = "notification" ;
	String FORCED_ORIENTATION = "forced_orientation" ;
	String ALWAYS_SHOW_FAVORITES = "always_show_favorites" ;
	String REVERSE_INTERFACE = "reverse_interface" ;
	String IMMERSIVE_MODE = "immersive_mode" ;
	String TOUCH_TARGETS = "touch_targets" ;
	String INTERACTIVE_CLOCK = "interactive_clock" ;
	String CLOCK_APP = "clock_app" ;
	String HIDE_MENU_BUTTON = "hide_menu_button" ;
	String DISABLE_APP_DRAWER = "disable_app_drawer" ;
	String DOUBLE_TAP = "double_tap" ;
	String SWIPE_LEFTWARDS = "swipe_leftwards" ;
	String SWIPE_RIGHTWARDS = "swipe_rightwards" ;

	// Constants represetings fallback colors
	String COLOR_FOR_OVERLAY = "#66000000" ;
	String COLOR_FOR_DENSE_OVERLAY = "#EB333333" ;
	String COLOR_FOR_TEXT_ON_OVERLAY = "#FFFFFFFF" ;
	String COLOR_TRANSPARENT = "#00000000" ;

	// --- Constants representing old settings keys for compatibility ---
	// Removed in v3.1.0 - 23/04/2021 (migrated to internal file hidden.txt)
	String OLD_HIDDEN_APPLICATIONS = "hidden_applications" ;
	String OLD_HIDDEN_APPS_SEPARATOR = "_discreet_" ;
	// Removed in v4.0.0 - 05/06/2021 (merged with CLOCK_FORMAT)
	String OLD_DISPLAY_CLOCK = "display_clock" ;
	// Removed in v5.2.0 - 13/11/2021 (merged with FORCED_ORIENTATION)
	String OLD_FORCE_PORTRAIT = "force_portrait" ;
	// Removed in v5.2.0 - 13/11/2021 (splitted between favorites and app drawer)
	String OLD_BACKGROUND_COLOR = "background_color" ;
	// Removed in v7.6.0 - 24/08/2024 (setting converted from seekbar to list)
	String OLD_ICON_SIZE = "icon_size" ;
}
