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

// Imports
import android.content.Context ;
import android.content.SharedPreferences ;
import android.content.res.Resources ;
import android.graphics.Canvas ;
import android.graphics.Paint ;
import android.graphics.Rect ;
import android.graphics.Typeface ;
import android.text.TextPaint ;
import android.util.AttributeSet ;
import android.view.View ;
import androidx.preference.PreferenceManager ;
import com.vincent_falzon.discreetlauncher.settings.ActivitySettingsAppearance ;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;

/**
 * Represent the clock on the home screen.
 */
public class ViewClock extends View
{
	// Attributes
	private final SharedPreferences settings ;
	private final TextPaint textClock ;
	private final Paint analogClock ;
	private final float vertical_padding ;
	private final float time_text_size ;
	private final float clock_radius ;
	private final float clock_tick_length ;
	private final float clock_stroke_width_circle ;
	private final float clock_stroke_width_tick ;
	private final Rect rect ;


	/**
	 * Constructor.
	 */
	public ViewClock(Context context)
	{
		this(context, null, 0) ;
	}


	/**
	 * Constructor with custom attributes.
	 */
	public ViewClock(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0) ;
	}


	/**
	 * Constructor with custom attributes and custom style.
	 */
	public ViewClock(Context context, AttributeSet attrs, int defStyleAttr)
	{
		// Let the parent actions be performed
        super(context, attrs, defStyleAttr) ;

		// Retrieve the settings
		settings = PreferenceManager.getDefaultSharedPreferences(getContext()) ;

		// Retrieve interface dimensions
		Resources resources = getResources() ;
		vertical_padding = resources.getDimension(R.dimen.spacing_large) ;
		time_text_size = resources.getDimension(R.dimen.text_size_huge) ;
		clock_radius = resources.getDimension(R.dimen.spacing_huge) ;
		clock_stroke_width_circle = resources.getDimension(R.dimen.spacing_very_small) ;
		clock_stroke_width_tick = resources.getDimension(R.dimen.spacing_small) * 0.8f ;
		clock_tick_length = resources.getDimension(R.dimen.spacing_small) + clock_stroke_width_tick ;

		// Prepare the TextPaint used to draw the time and date
		textClock = new TextPaint() ;
		textClock.setAntiAlias(true) ;
		textClock.setTypeface(Typeface.SANS_SERIF) ;

		// Prepare the Paint used to draw the analog clock, its ticks and its hands
		analogClock = new Paint() ;
		analogClock.setAntiAlias(true) ;

		// Other initializations
		rect = new Rect() ;
	}


	/**
	 * Called when the view must be (re)drawn.
	 */
	@Override
    protected void onDraw(Canvas canvas)
	{
		// Let the parent actions be performed
		super.onDraw(canvas) ;

		// Do not continue if the clock is not enabled
		String clock_format = settings.getString(Constants.CLOCK_FORMAT, Constants.NONE) ;
		if((clock_format == null) || clock_format.equals(Constants.NONE)) return ;

		// Initializations
		float center_x = getWidth() / 2f ;
		int clock_color = ActivitySettingsAppearance.getColor(settings, Constants.CLOCK_COLOR, getResources().getColor(R.color.for_text_on_overlay)) ;
		textClock.setColor(clock_color) ;
		analogClock.setColor(clock_color) ;

		// Retrieve the current date and time
		Calendar current_time = Calendar.getInstance() ;
		int hour12 = current_time.get(Calendar.HOUR) ;
		int hour24 = current_time.get(Calendar.HOUR_OF_DAY) ;
		int minute = current_time.get(Calendar.MINUTE) ;

		// Retrieve the clock position
		String clock_position = settings.getString(Constants.CLOCK_POSITION, Constants.CLOCK_POSITION_DEFAULT) ;
		if(clock_position == null) clock_position = Constants.CLOCK_POSITION_DEFAULT ;

		// Draw the selected clock
		float offset_y ;
		if(clock_format.equals("analog"))
			{
				// Compute the required offset to perform the vertical alignement
				if(clock_position.equals("middle")) offset_y = getHeight() / 2f - clock_radius - vertical_padding ;
					else if(clock_position.equals("bottom")) offset_y = getHeight() - 2 * clock_radius - 2 * vertical_padding ;
					else offset_y = 0 ;

				// Compute values related to the clock
				float clock_center = vertical_padding + clock_radius + offset_y ;
				float length_minute_hand = clock_radius * 0.82f ;
				float length_hour_hand = length_minute_hand * 0.65f ;
				float one_tour_by_12 = 2 * (float)Math.PI / 12 ;

				// Draw the clock circle and ticks
				analogClock.setStyle(Paint.Style.STROKE) ;
				analogClock.setStrokeWidth(clock_stroke_width_circle) ;
				canvas.drawCircle(center_x, clock_center, clock_radius, analogClock) ;
				analogClock.setStrokeWidth(clock_stroke_width_tick) ;
				for(int i = 0 ; i < 12 ; i++)
				{
					float hour_tick = i * one_tour_by_12 ;
					canvas.drawLine(center_x + (float)Math.sin(hour_tick) * (clock_radius - clock_tick_length), clock_center + (float)Math.cos(hour_tick) * (clock_radius - clock_tick_length),
							center_x + (float)Math.sin(hour_tick) * clock_radius, clock_center + (float)Math.cos(hour_tick) * clock_radius, analogClock) ;
				}

				// Draw the clock hands
				float hour_in_rad = (float)Math.PI - (hour12 + minute / 60f) * one_tour_by_12 ;
				float minute_in_rad = (float)Math.PI - minute * one_tour_by_12 / 5 ;
				canvas.drawLine(center_x, clock_center, center_x + (float)Math.sin(hour_in_rad) * length_hour_hand, clock_center + (float)Math.cos(hour_in_rad) * length_hour_hand, analogClock) ;
				canvas.drawLine(center_x, clock_center, center_x + (float)Math.sin(minute_in_rad) * length_minute_hand, clock_center + (float)Math.cos(minute_in_rad) * length_minute_hand, analogClock) ;
				analogClock.setStyle(Paint.Style.FILL) ;
				canvas.drawCircle(center_x, clock_center, clock_radius * 0.05f, analogClock) ;
			}
			else if(clock_format.equals("datetime"))
			{
				// Prepare the date and time texts
				String date_text = SimpleDateFormat.getDateInstance(DateFormat.LONG).format(current_time.getTime()) ;
				String time_text = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(current_time.getTime()) ;

				// Compute the date and time dimensions
				textClock.setTextSize(time_text_size) ;
				textClock.getTextBounds(time_text, 0, time_text.length(), rect) ;
				float time_text_height = rect.height() ;
				float time_text_center = center_x - rect.width() / 2f ;
				textClock.setTextSize(time_text_size * 0.5f) ;
				textClock.getTextBounds(date_text, 0, date_text.length(), rect) ;
				float date_text_height = rect.height() ;
				float date_text_center = center_x - rect.width() / 2f ;

				// Define the related offset
				if(clock_position.equals("middle")) offset_y = getHeight() / 2f + (time_text_height - 0.5f * vertical_padding - date_text_height) / 2 ;
					else if(clock_position.equals("bottom")) offset_y = getHeight() - 1.5f * vertical_padding - date_text_height ;
					else offset_y = vertical_padding + time_text_height ;

				// Draw the time text
				textClock.setTextSize(time_text_size) ;
				canvas.drawText(time_text, time_text_center, offset_y, textClock) ;

				// Draw the date text
				textClock.setTextSize(time_text_size * 0.5f) ;
				canvas.drawText(date_text, date_text_center, offset_y + 0.5f * vertical_padding + date_text_height, textClock) ;
			}
			else
			{
				// Prepare the time text according to the selected format
				String time_text ;
				if(clock_format.startsWith("h:")) time_text = hour12 + ":" ;
					else time_text = ((hour24 < 10) ? "0" : "") + hour24 + ":" ;
				time_text += ((minute < 10) ? "0" : "") + minute ;
				if(clock_format.equals("h:mm a"))
					time_text += (current_time.get(Calendar.AM_PM) == Calendar.AM) ? " AM" : " PM" ;

				// Draw the time text
				textClock.setTextSize(time_text_size) ;
				textClock.getTextBounds(time_text, 0, time_text.length(), rect) ;
				if(clock_position.equals("middle")) offset_y = getHeight() / 2f + rect.height() / 2f ;
					else if(clock_position.equals("bottom")) offset_y = getHeight() - vertical_padding ;
					else offset_y = vertical_padding + rect.height() ;
				canvas.drawText(time_text, center_x - rect.width() / 2f, offset_y, textClock) ;
			}
    }
}
