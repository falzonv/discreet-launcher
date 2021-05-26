package com.vincent_falzon.discreetlauncher.settings ;

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
import android.graphics.Color ;
import android.graphics.PixelFormat ;
import android.text.InputType ;
import android.view.KeyEvent ;
import android.view.LayoutInflater ;
import android.view.View ;
import android.view.ViewGroup ;
import android.view.inputmethod.EditorInfo ;
import android.view.inputmethod.InputMethodManager ;
import android.widget.EditText ;
import android.widget.TextView ;
import androidx.appcompat.app.AppCompatDialog ;
import com.vincent_falzon.discreetlauncher.R ;
import com.vincent_falzon.discreetlauncher.ShowDialog ;

/**
 * Display a color picker dialog.
 *
 * <br/><br/><b>Credit Notice</b><br/>
 * Although they were completely rewritten for Discreet Launcher needs, the "ColorPicker*" classes
 * are largerly based on the ColorPickerPreference library of Daniel Nilsson and Sergey Margaritov.
 */
public class ColorPickerDialog extends AppCompatDialog implements ColorPickerView.ColorChangeListener, View.OnClickListener
{
	// Attributes
    private final ColorPickerView colorPicker ;
    private final EditText newColorHexadecimal ;
	private final String default_color ;
    private final SaveRequestListener saveListener;


	/**
	 * Notify the preference that the selected color need to be saved.
	 */
	public interface SaveRequestListener
	{
        void onSaveRequest(String color) ;
    }


	/**
	 * Constructor.
	 * @param context Provided by a preference
	 * @param initial_color First color to show on the picker
	 * @param default_color Color selected by the reset button
	 * @param title Title of the dialog
	 * @param listener To notify when the selected color need to be saved
	 */
    public ColorPickerDialog(Context context, int initial_color, String default_color, CharSequence title, SaveRequestListener listener)
	{
		// Let the parent actions be performed
        super(context) ;

		// Make sure to use the RGBA_8888 pixel format
		if(getWindow() != null) getWindow().setFormat(PixelFormat.RGBA_8888) ;

		// Load the color picker layout
		LayoutInflater inflater = LayoutInflater.from(context) ;
		final View dialogView = inflater.inflate(R.layout.color_picker, (ViewGroup)null) ;
		setContentView(dialogView) ;

		// Set the dialog title
		setTitle(title) ;

		// Retrieve the interface elements
		colorPicker = dialogView.findViewById(R.id.color_picker_view) ;
		newColorHexadecimal = dialogView.findViewById(R.id.new_color_hexadecimal) ;

		// Prepare the hexadecimal value field
		newColorHexadecimal.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) ;
		newColorHexadecimal.setOnEditorActionListener(new TextView.OnEditorActionListener()
			{
				@Override
				public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
				{
					if(actionId == EditorInfo.IME_ACTION_DONE)
						{
							// Hide the keyboard
							Context context = view.getContext() ;
							((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(view.getWindowToken(), 0) ;

							try
							{
								// Try to read a color from the hexadecimal string
								int new_color = convertHexadecimalColorToInt(newColorHexadecimal.getText().toString()) ;
								colorPicker.setColor(new_color) ;
							}
							catch (IllegalArgumentException e)
							{
								ShowDialog.toastLong(context, context.getString(R.string.error_invalid_color_format)) ;
							}
							return true ;
						}
					return false ;
				}
			});

		// Start to listen for clicks on buttons and color changes
		dialogView.findViewById(R.id.color_reset_button).setOnClickListener(this) ;
		dialogView.findViewById(R.id.color_validate_button).setOnClickListener(this) ;
		dialogView.findViewById(R.id.color_cancel_button).setOnClickListener(this) ;

		// Initialize the color picker
		this.default_color = default_color ;
		colorPicker.setOnColorChangedListener(this) ;
		colorPicker.setColor(initial_color) ;

		// Register the listener to notify when the selected color need to be saved
		saveListener = listener ;
    }


	/**
	 * Update the hexadecimal value when the selected color changes.
	 * @param color New color
	 */
	@Override
    public void onColorChanged(int color)
	{
		newColorHexadecimal.setText(convertIntColorToHexadecimal(color, false).toUpperCase()) ;
    }


	/**
	 * Detect a click on a button.
	 * @param view Element clicked
	 */
    @Override
    public void onClick(View view)
	{
		// Identify which element has been clicked
		int selection = view.getId() ;

		// Check if the user asked to reset the default color
		if(selection == R.id.color_reset_button)
			{
				// Reset the color picker and keep it displayed
				colorPicker.setColor(convertHexadecimalColorToInt(default_color)) ;
				return ;
			}

		// Save the new color if it was asked
		if(selection == R.id.color_validate_button)
        	saveListener.onSaveRequest(convertIntColorToHexadecimal(colorPicker.getColor(), true).toUpperCase()) ;

		// Dismiss the color picker
        dismiss() ;
    }


	/**
	 * Convert an "int" color to its hexadecimal value with alpha.
	 * @param color The "int" color to convert
	 * @param with_alpha <code>true</code> to include alpha, <code>false</code> otherwise
	 * @return Hexadecimal value with alpha (#AARRGGBB)
	 */
	private String convertIntColorToHexadecimal(int color, boolean with_alpha)
	{
		// Convert the color to its hexadecimal value with alpha
		String alpha = Integer.toHexString(Color.alpha(color)) ;
		String red = Integer.toHexString(Color.red(color)) ;
		String green = Integer.toHexString(Color.green(color)) ;
		String blue = Integer.toHexString(Color.blue(color)) ;

		// Add padding with "0" if needed
		if(alpha.length() == 1) alpha = "0" + alpha ;
		if(red.length() == 1) red = "0" + red ;
		if(green.length() == 1) green = "0" + green ;
		if(blue.length() == 1) blue = "0" + blue ;

		// Return the result
		if(with_alpha) return "#" + alpha + red + green + blue ;
		else return "#" + red + green + blue ;
	}


	/**
	 * Read an hexadecimal color string and try to convert it to an "int" color.
	 * @param hexadecimal Format #RRGGBB or #AARRGGBB (the "#" is added if needed)
	 * @return The color in "int" format
	 * @throws IllegalArgumentException If the string format is not valid
	 */
	public static int convertHexadecimalColorToInt(String hexadecimal) throws IllegalArgumentException
	{
		if(!hexadecimal.startsWith("#")) hexadecimal = "#" + hexadecimal ;
		return Color.parseColor(hexadecimal) ;
	}
}
