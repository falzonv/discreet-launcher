package com.vincent_falzon.discreetlauncher.settings ;

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
import android.content.res.TypedArray ;
import android.graphics.Bitmap ;
import android.graphics.Bitmap.Config ;
import android.graphics.Color ;
import android.util.AttributeSet ;
import android.view.View ;
import android.widget.ImageView ;
import android.widget.LinearLayout ;
import androidx.annotation.NonNull ;
import androidx.appcompat.content.res.AppCompatResources ;
import androidx.preference.Preference ;
import androidx.preference.PreferenceViewHolder ;
import com.vincent_falzon.discreetlauncher.R ;
import java.util.Objects ;

/**
 * Allow to display a color picker in the standard Android Preference library.
 *
 * <br/><br/><b>Credit Notice</b><br/>
 * Although they were completely rewritten for Discreet Launcher needs, the "ColorPicker*" classes
 * are largerly based on the ColorPickerPreference library of Daniel Nilsson and Sergey Margaritov.
 */
public class ColorPickerPreference extends Preference implements Preference.OnPreferenceClickListener, ColorPickerDialog.SaveRequestListener
{
	// Attributes
	private final float density ;
	private String default_color ;
	private int current_color ;
	ColorPickerDialog pickerDialog ;


	/**
	 * Constructor.
	 */
	public ColorPickerPreference(Context context, AttributeSet attributes)
	{
		// Let the parent actions be performed
		super(context, attributes) ;

		// Initializations
		density = getContext().getResources().getDisplayMetrics().density ;
		setOnPreferenceClickListener(this) ;
	}


	/**
	 * Called when the initial value of a preference is set.
	 */
	@Override
	protected void onSetInitialValue(Object defaultValue)
	{
		onSaveRequest(getPersistedString((String)defaultValue)) ;
	}


	/**
	 * Retrieve the default value of the preference in its attributes.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray attributes, int index)
	{
		String value = attributes.getString(index) ;
		default_color = (value != null && value.startsWith("#")) ? value : "#FFFFFFFF" ;
		return default_color ;
	}


	/**
	 * Called when a preference is clicked.
	 */
	@Override
	public boolean onPreferenceClick(@NonNull Preference preference)
	{
		// Open the color picked and mark the event as consumed
		pickerDialog = new ColorPickerDialog(getContext(), current_color, default_color, getTitle(), this) ;
		pickerDialog.show() ;
		return true ;
	}


	/**
	 * Called when a new color should be saved in the preferences.
	 */
	@Override
	public void onSaveRequest(String new_color)
	{
		try
		{
			// Try to notify the ChangeListener that a preference will been changed
			Objects.requireNonNull(getOnPreferenceChangeListener()).onPreferenceChange(this, new_color) ;
		}
		catch (NullPointerException ignored) { }

		// Save the new color and update the preview
		if(isPersistent()) persistString(new_color) ;
		current_color = ColorPickerDialog.convertHexadecimalColorToInt(new_color) ;
		notifyChanged() ;
	}


	/**
	 * Called when there is a change in the preference data.
	 */
	@Override
	public void onBindViewHolder(@NonNull PreferenceViewHolder holder)
	{
		// Let the parent actions be performed
		super.onBindViewHolder(holder) ;

		// Retrieve the widget frame allowing to display a custom preview
		LinearLayout widgetFrame = holder.itemView.findViewById(android.R.id.widget_frame) ;
		if(widgetFrame == null) return ;

		// Display the widget frame and adjust its spacing with the right edge of the screen
		widgetFrame.setVisibility(View.VISIBLE) ;
		widgetFrame.setPadding(
				widgetFrame.getPaddingLeft(),
				widgetFrame.getPaddingTop(),
				Math.round(8 * density),
				widgetFrame.getPaddingBottom()) ;

		// Remove any previous preview in the widget frame
		int count = widgetFrame.getChildCount() ;
		if(count > 0) widgetFrame.removeViews(0, count) ;

		// Initialize the Bitmap which will be used to build the preview
		int size = Math.round(32 * density) ;
		Bitmap bitmap = Bitmap.createBitmap(size, size, Config.ARGB_8888) ;
		int width = bitmap.getWidth() ;
		int height = bitmap.getHeight() ;

		// Browse each column of pixel in the bitmap
		int pixel ;
		for(int i = 0 ; i < width ; i++)
		{
			// Browse each pixel of the column
			for(int j = i ; j < height ; j++)
			{
				// Check if the pixel is on a side (border) or the middle (color)
				pixel = (i <= 1 || i >= (width - 2) || j >= (height - 2)) ? Color.GRAY : current_color ;

				// Draw the pixel and its counterpart across the diagonal
				bitmap.setPixel(i, j, pixel) ;
				bitmap.setPixel(j, i, pixel) ;
			}
		}

		// Prepare the preview and add it to the widget frame
		ImageView preview = new ImageView(getContext()) ;
		preview.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.alpha_grid)) ;
		preview.setImageBitmap(bitmap) ;
		widgetFrame.addView(preview) ;
	}
}
