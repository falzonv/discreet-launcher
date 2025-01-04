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

// Imports
import android.content.ActivityNotFoundException ;
import android.content.BroadcastReceiver ;
import android.content.Context ;
import android.content.Intent ;
import android.content.IntentFilter ;
import android.content.SharedPreferences ;
import android.content.res.Resources ;
import android.graphics.Canvas ;
import android.graphics.Paint ;
import android.graphics.Rect ;
import android.graphics.Typeface ;
import android.text.TextPaint ;
import android.util.AttributeSet ;
import android.view.MotionEvent ;
import android.view.View ;
import androidx.annotation.NonNull ;
import androidx.preference.PreferenceManager ;
import java.text.DateFormat ;
import java.text.SimpleDateFormat ;
import java.util.Calendar ;

/**
 * Represent the clock on the home screen.
 */
public class ViewClock extends View implements View.OnTouchListener
{
	// Constants
	private static final String TAG = "ViewClock" ;

	// Attributes
	private final SharedPreferences settings ;
	private final TextPaint textClock ;
	private final Paint analogClock ;
	private final float padding ;
	private final float[] time_text_sizes ;
	private final float clock_tick_length ;
	private final float clock_stroke_width_circle ;
	private final float clock_stroke_width_tick ;
	private final float clock_shadow_radius ;
	private final float screen_width ;
	private final Rect rect_date ;
	private final Rect rect_time ;
	private boolean clock_disabled ;


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
		padding = resources.getDimension(R.dimen.spacing_large) ;
		time_text_sizes = new float[3] ;
		time_text_sizes[0] = resources.getDimension(R.dimen.text_clock_small) ;
		time_text_sizes[1] = resources.getDimension(R.dimen.text_clock_medium) ;
		time_text_sizes[2] = resources.getDimension(R.dimen.text_clock_large) ;
		clock_stroke_width_circle = resources.getDimension(R.dimen.spacing_very_small) ;
		clock_stroke_width_tick = resources.getDimension(R.dimen.spacing_small) * 0.8f ;
		clock_tick_length = resources.getDimension(R.dimen.spacing_small) + clock_stroke_width_tick ;
		clock_shadow_radius = resources.getDimension(R.dimen.spacing_normal) ;
		screen_width = Math.min(resources.getDisplayMetrics().widthPixels, resources.getDisplayMetrics().heightPixels) ;

		// Prepare the TextPaint used to draw the time and date
		textClock = new TextPaint() ;
		textClock.setAntiAlias(true) ;
		textClock.setTypeface(Typeface.SANS_SERIF) ;

		// Prepare the Paint used to draw the analog clock, its ticks and its hands
		analogClock = new Paint() ;
		analogClock.setAntiAlias(true) ;

		// Listen for clock changes (no start/stop mechanism as this view visibility changes often)
		MinuteListener minuteListener = new MinuteListener() ;
		context.registerReceiver(minuteListener, new IntentFilter(Intent.ACTION_TIME_TICK)) ;

		// Other initializations
		rect_date = new Rect() ;
		rect_time = new Rect() ;
		setOnTouchListener(this) ;
	}


	/**
	 * Called when the view must be (re)drawn.
	 */
	@Override
	protected void onDraw(@NonNull Canvas canvas)
	{
		// Let the parent actions be performed
		super.onDraw(canvas) ;

		// Do not continue if the clock is not enabled
		String clock_format = settings.getString(Constants.CLOCK_FORMAT, Constants.NONE) ;
		clock_disabled = clock_format.equals(Constants.NONE) ;
		if(clock_disabled) return ;

		// Initializations
		Utils.logInfo(TAG, "updating the clock") ;
		int view_height = getHeight() ;
		int view_width = getWidth() ;
		float offset_x ;
		float offset_y ;
		rect_date.setEmpty() ;
		rect_time.setEmpty() ;

		// Retrieve the clock colors, position and size
		int clock_color = Utils.getColor(settings, Constants.CLOCK_COLOR, Constants.COLOR_FOR_TEXT_ON_OVERLAY) ;
		int shadow_color = Utils.getColor(settings, Constants.CLOCK_SHADOW_COLOR, Constants.COLOR_FOR_OVERLAY) ;
		String clock_position = settings.getString(Constants.CLOCK_POSITION, "middle") ;
		String clock_size = settings.getString(Constants.CLOCK_SIZE, "medium") ;

		// Retrieve the current date and time
		Calendar current_time = Calendar.getInstance() ;
		int hour12 = current_time.get(Calendar.HOUR) ;
		int hour24 = current_time.get(Calendar.HOUR_OF_DAY) ;
		int minute = current_time.get(Calendar.MINUTE) ;

		// Check if the clock should be analog or text-based
		if(clock_format.equals("analog"))
			{
				// Adjust the clock radius to the selected clock size
				float clock_radius ;
				if(clock_size.equals("small")) clock_radius = screen_width * 0.10f ;
					else if(clock_size.equals("large")) clock_radius = screen_width * 0.30f ;
					else clock_radius = screen_width * 0.20f ;

				// Compute the required offset to perform the vertical alignement
				if(clock_position.startsWith("middle")) offset_y = view_height / 2f - clock_radius - padding ;
					else if(clock_position.startsWith("bottom")) offset_y = view_height - 2 * clock_radius - 2 * padding ;
					else offset_y = 0 ;

				// Compute the required offset to perfrom the horizontal alignement
				if(clock_position.endsWith("left")) offset_x = 0 ;
					else if(clock_position.endsWith("right")) offset_x = view_width - 2 * clock_radius - 2 * padding ;
					else offset_x = view_width / 2f - clock_radius - padding ;

				// Compute values related to the clock
				float center_x = padding + clock_radius + offset_x ;
				float center_y = padding + clock_radius + offset_y ;
				float length_minute_hand = clock_radius * 0.82f ;
				float length_hour_hand = length_minute_hand * 0.65f ;
				float one_tour_by_12 = 2 * (float)Math.PI / 12 ;

				// Draw the clock circle and ticks
				analogClock.setColor(clock_color) ;
				analogClock.setStyle(Paint.Style.STROKE) ;
				analogClock.setStrokeWidth(clock_stroke_width_circle) ;
				canvas.drawCircle(center_x, center_y, clock_radius, analogClock) ;
				analogClock.setStrokeWidth(clock_stroke_width_tick) ;
				for(int i = 0 ; i < 12 ; i++)
				{
					float hour_tick = i * one_tour_by_12 ;
					canvas.drawLine(center_x + (float)Math.sin(hour_tick) * (clock_radius - clock_tick_length), center_y + (float)Math.cos(hour_tick) * (clock_radius - clock_tick_length),
							center_x + (float)Math.sin(hour_tick) * clock_radius, center_y + (float)Math.cos(hour_tick) * clock_radius, analogClock) ;
				}

				// Draw the clock hands
				float hour_in_rad = (float)Math.PI - (hour12 + minute / 60f) * one_tour_by_12 ;
				float minute_in_rad = (float)Math.PI - minute * one_tour_by_12 / 5 ;
				canvas.drawLine(center_x, center_y, center_x + (float)Math.sin(hour_in_rad) * length_hour_hand, center_y + (float)Math.cos(hour_in_rad) * length_hour_hand, analogClock) ;
				canvas.drawLine(center_x, center_y, center_x + (float)Math.sin(minute_in_rad) * length_minute_hand, center_y + (float)Math.cos(minute_in_rad) * length_minute_hand, analogClock) ;
				analogClock.setStyle(Paint.Style.FILL) ;
				canvas.drawCircle(center_x, center_y, clock_radius * 0.05f, analogClock) ;

				// Define the clock touch area
				rect_time.set(Math.round(center_x - clock_radius), Math.round(center_y - clock_radius), Math.round(center_x + clock_radius), Math.round(center_y + clock_radius)) ;
				return ;
			}

		// Prepare the text colors
		textClock.setColor(clock_color) ;
		textClock.setShadowLayer(clock_shadow_radius, 0, 0, shadow_color) ;

		// Adjust the text size to the selected clock size
		float time_text_size ;
		if(clock_size.equals("small")) time_text_size = time_text_sizes[0] ;
			else if(clock_size.equals("large")) time_text_size = time_text_sizes[2] ;
			else time_text_size = time_text_sizes[1] ;

		// Check the text format to use
		if(clock_format.startsWith("datetime"))
			{
				// Prepare the date and time texts
				String date_text ;
				if(clock_format.endsWith("short")) date_text = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT).format(current_time.getTime()) ;
					else date_text = SimpleDateFormat.getDateInstance(DateFormat.FULL).format(current_time.getTime()) ;
				String time_text = SimpleDateFormat.getTimeInstance(DateFormat.SHORT).format(current_time.getTime()) ;

				// Compute the time text dimensions
				textClock.setTextSize(time_text_size) ;
				textClock.getTextBounds(time_text, 0, time_text.length(), rect_time) ;

				// Compute the date text dimensions, making it fit in the screen width
				float date_text_size_factor = 0.4f ;
				textClock.setFakeBoldText(true) ;
				textClock.setTextSize(time_text_size * date_text_size_factor) ;
				textClock.getTextBounds(date_text, 0, date_text.length(), rect_date) ;
				while((rect_date.width() > (view_width - 2 * padding)) && (date_text_size_factor > 0))
				{
					// Progressively lower the size of the date text size
					date_text_size_factor -= 0.01f ;
					textClock.setTextSize(time_text_size * date_text_size_factor) ;
					textClock.getTextBounds(date_text, 0, date_text.length(), rect_date) ;
				}

				// Define the vertical offset
				if(clock_position.startsWith("middle")) offset_y = view_height / 2f + (rect_time.height() - 0.5f * padding - rect_date.height()) / 2 ;
					else if(clock_position.startsWith("bottom")) offset_y = view_height - 1.5f * padding - rect_date.height() ;
					else offset_y = padding + rect_time.height() ;

				// Define the horizontal offset of the time text
				if(clock_position.endsWith("left")) offset_x = padding ;
					else if(clock_position.endsWith("right")) offset_x = view_width - rect_time.width() - padding ;
					else offset_x = view_width / 2f - rect_time.width() / 2f ;

				// Draw the time text
				textClock.setFakeBoldText(false) ;
				textClock.setTextSize(time_text_size) ;
				canvas.drawText(time_text, offset_x - rect_time.left, offset_y, textClock) ;
				rect_time.offset(Math.round(offset_x), Math.round(offset_y)) ;

				// Define the horizontal offset of the date text
				if(clock_position.endsWith("left")) offset_x = padding ;
					else if(clock_position.endsWith("right")) offset_x = view_width - rect_date.width() - padding ;
					else offset_x = view_width / 2f - rect_date.width() / 2f ;

				// Draw the date text
				textClock.setFakeBoldText(true) ;
				textClock.setTextSize(time_text_size * date_text_size_factor) ;
				canvas.drawText(date_text, offset_x - rect_date.left, offset_y + 0.5f * padding + rect_date.height(), textClock) ;
				rect_date.offset(Math.round(offset_x), Math.round(offset_y + 0.5f * padding + rect_date.height())) ;
			}
			else if(clock_format.startsWith("date"))
			{
				// Prepare the date text
				String date_text ;
				if(clock_format.endsWith("short")) date_text = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT).format(current_time.getTime()) ;
					else date_text = SimpleDateFormat.getDateInstance(DateFormat.FULL).format(current_time.getTime()) ;

				// Compute the date text dimensions, making it fit in the screen width
				float date_text_size_factor = 0.6f ;
				textClock.setFakeBoldText(true) ;
				textClock.setTextSize(time_text_size * date_text_size_factor) ;
				textClock.getTextBounds(date_text, 0, date_text.length(), rect_date) ;
				while((rect_date.width() > (view_width - 2 * padding)) && (date_text_size_factor > 0))
				{
					// Progressively lower the size of the date text size
					date_text_size_factor -= 0.01f ;
					textClock.setTextSize(time_text_size * date_text_size_factor) ;
					textClock.getTextBounds(date_text, 0, date_text.length(), rect_date) ;
				}

				// Define the vertical offset
				if(clock_position.startsWith("middle")) offset_y = view_height / 2f + rect_date.height() / 2f ;
					else if(clock_position.startsWith("bottom")) offset_y = view_height - padding ;
					else offset_y = padding + rect_date.height() ;

				// Define the horizontal offset of the date text
				if(clock_position.endsWith("left")) offset_x = padding ;
					else if(clock_position.endsWith("right")) offset_x = view_width - rect_date.width() - padding ;
					else offset_x = view_width / 2f - rect_date.width() / 2f ;

				// Draw the date text
				textClock.setFakeBoldText(true) ;
				textClock.setTextSize(time_text_size * date_text_size_factor) ;
				canvas.drawText(date_text, offset_x - rect_date.left, offset_y, textClock) ;
				rect_date.offset(Math.round(offset_x), Math.round(offset_y)) ;
			}
			else
			{
				// Prepare the time text according to the selected format
				String time_text ;
				if(clock_format.startsWith("h:"))
					{
						if(hour12 == 0) time_text = "12:" ;
							else time_text = hour12 + ":" ;
					}
					else time_text = ((hour24 < 10) ? "0" : "") + hour24 + ":" ;
				time_text += ((minute < 10) ? "0" : "") + minute ;
				if(clock_format.equals("h:mm a"))
					time_text += (current_time.get(Calendar.AM_PM) == Calendar.AM) ? " AM" : " PM" ;

				// Define the vertical offset
				textClock.setTextSize(time_text_size) ;
				textClock.getTextBounds(time_text, 0, time_text.length(), rect_time) ;
				if(clock_position.startsWith("middle")) offset_y = view_height / 2f + rect_time.height() / 2f ;
					else if(clock_position.startsWith("bottom")) offset_y = view_height - padding ;
					else offset_y = padding + rect_time.height() ;

				// Define the horizontal offset
				if(clock_position.endsWith("left")) offset_x = padding ;
					else if(clock_position.endsWith("right")) offset_x = view_width - rect_time.width() - padding ;
					else offset_x = view_width / 2f - rect_time.width() / 2f ;

				// Draw the time text
				canvas.drawText(time_text, offset_x - rect_time.left, offset_y, textClock) ;
				rect_time.offset(Math.round(offset_x), Math.round(offset_y)) ;
			}
	}


	/**
	 * Called when an element is touched.
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		// Do not continue if the clock is not enabled
		if(clock_disabled) return false ;

		// Do not continue if the interactive clock setting is not enabled
		if(!settings.getBoolean(Constants.INTERACTIVE_CLOCK, false)) return false ;

		// Retrieve the event coordinates
		int x = Math.round(event.getX()) ;
		int y = Math.round(event.getY()) ;

		// If the touch starts and ends in the time area, try to start the selected clock app
		if(rect_time.contains(x, y))
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN) return true ;
				if(event.getAction() == MotionEvent.ACTION_UP)
					return Utils.searchAndStartApplication(this, settings, Constants.CLOCK_APP) ;
			}

		// If the touch starts and ends in the time area, try to start the default calendar
		if(rect_date.contains(x, y))
			{
				if(event.getAction() == MotionEvent.ACTION_DOWN) return true ;
				if(event.getAction() == MotionEvent.ACTION_UP)
					{
						try
						{
							Intent calendarIntent = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR) ;
							calendarIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
							getContext().startActivity(calendarIntent) ;
							return true ;
						}
						catch(ActivityNotFoundException|NullPointerException exception)
						{
							Utils.displayLongToast(getContext(), getContext().getString(R.string.error_app_not_found, "{calendar}")) ;
							return false ;
						}
					}
			}

		// Do not handle touchs outside the date/time areas
		return false ;
	}



	// ---------------------------------------------------------------------------------------------

	/**
	 * Listen for every minute on the system clock.
	 */
	private class MinuteListener extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			// Check if a minute change just happened
			if((intent != null) && Intent.ACTION_TIME_TICK.equals(intent.getAction()))
				invalidate() ;
		}
	}
}
