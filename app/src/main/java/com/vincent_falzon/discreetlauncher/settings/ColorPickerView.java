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
import android.annotation.SuppressLint ;
import android.content.Context ;
import android.graphics.Bitmap ;
import android.graphics.BitmapShader ;
import android.graphics.Canvas ;
import android.graphics.Color ;
import android.graphics.ComposeShader ;
import android.graphics.LinearGradient ;
import android.graphics.Paint ;
import android.graphics.Paint.Style ;
import android.graphics.Point ;
import android.graphics.PorterDuff ;
import android.graphics.RectF ;
import android.graphics.Shader ;
import android.graphics.Shader.TileMode ;
import android.graphics.drawable.Drawable ;
import android.util.AttributeSet ;
import android.view.MotionEvent ;
import android.view.View ;
import androidx.appcompat.content.res.AppCompatResources ;
import com.vincent_falzon.discreetlauncher.R ;

/**
 * Display a color picker with HSV+alpha panels and a preview of the new color.
 *
 * <br/><br/><b>Credit Notice</b><br/>
 * Although they were completely rewritten for Discreet Launcher needs, the "ColorPicker*" classes
 * are largerly based on the ColorPickerPreference library of Daniel Nilsson and Sergey Margaritov.
 */
public class ColorPickerView extends View
{
	// Attributes related to dimensions
	private final float density ;
	private final int view_width ;
	private final int view_height ;
	private final RectF huePanel ;
	private final RectF hueSlider ;
	private final RectF satValPanel ;
	private final RectF alphaPanel ;
	private final RectF alphaSlider ;
	private final RectF colorPreviewPanel ;

	// Attributes related to drawing
	private final Paint huePaint ;
	private final Paint satValPaint ;
	private final Shader valShader ;
	private final Paint alphaPaint ;
	private final Paint colorPreviewPaint ;
	private final Paint colorPreviewText ;
	private final Paint alphaGridPaint ;
	private final Paint sliderPaint ;
	private final Paint pointerPaint ;

	// Other attributes
	private ColorChangeListener changeListener ;
	private final Point gestureStart ;
	private final float[] hsv_color ;
	private int alpha ;


	/**
	 * Notify the dialog that the selected color has changed to update the hexadecimal text.
	 */
	public interface ColorChangeListener
	{
		void onColorChanged(int color) ;
	}


	/**
	 * Constructor.
	 */
	public ColorPickerView(Context context, AttributeSet attributes)
	{
		// Let the parent actions be performed
		super(context, attributes) ;

		// Retrieve the density
		density = getContext().getResources().getDisplayMetrics().density ;

		// Initializations
		alpha = 0x66 ;
		hsv_color = new float[]{0, 0, 0} ;
		gestureStart = new Point() ;

		// Define elements size (dp * density)
		view_width = Math.round(270 * density) ;
		view_height = Math.round(270 * density) ;

		int panel_space = Math.round(10 * density) ;
		int panel_height = Math.round(30 * density) ;

		// Define the global dimensions of the color picker
		float padding = 8 * density ;
		RectF colorPickerRectangle = new RectF(0, 0, view_width, view_height) ;
		colorPickerRectangle.inset(padding, padding) ;

		// Define the panels dimensions
		huePanel = new RectF(colorPickerRectangle) ;
		huePanel.bottom = huePanel.top + panel_height ;

		// Define the saturation/value panel dimensions
		satValPanel = new RectF(colorPickerRectangle) ;
		satValPanel.top = colorPickerRectangle.top + huePanel.height() + panel_space ;
		satValPanel.bottom = satValPanel.top + (3 * panel_height) ;

		// Define the alpha panel dimensions
		alphaPanel = new RectF(colorPickerRectangle) ;
		alphaPanel.top = satValPanel.bottom + panel_space ;
		alphaPanel.bottom = alphaPanel.top + panel_height ;

		// Define the color preview dimensions
		colorPreviewPanel = new RectF(colorPickerRectangle) ;
		colorPreviewPanel.top = alphaPanel.bottom + (2 * panel_space) ;
		colorPreviewPanel.bottom = colorPreviewPanel.top + (2 * panel_height) ;

		// Define the hue and alpha sliders dimensions
		float slider_offset = 2 * density ;
		hueSlider = new RectF() ;
		hueSlider.top = huePanel.top - slider_offset ;
		hueSlider.bottom = huePanel.bottom + slider_offset ;
		alphaSlider = new RectF() ;
		alphaSlider.top = alphaPanel.top - slider_offset ;
		alphaSlider.bottom = alphaPanel.bottom + slider_offset ;

		// Define the hue color scale
		int[] hue_scale = new int[360] ;
		for(int i = 0 ; i < hue_scale.length ; i++)
			hue_scale[i] = Color.HSVToColor(new float[]{i, 1, 1}) ;

		// Prepare the hue panel gradient
		huePaint = new Paint() ;
		huePaint.setShader(new LinearGradient(
				huePanel.left, huePanel.top, huePanel.right, huePanel.top,
				hue_scale, null, TileMode.CLAMP)) ;

		// Prepare the saturation gradient
		valShader = new LinearGradient(
				satValPanel.left, satValPanel.top, satValPanel.left, satValPanel.bottom,
				0xFFFFFFFF, 0xFF000000, TileMode.CLAMP) ;

		// Define settings of the saturation/value pointer
		pointerPaint = new Paint() ;
		pointerPaint.setStyle(Style.STROKE) ;
		pointerPaint.setStrokeWidth(2 * density) ;
		pointerPaint.setColor(0xFF000000);
		pointerPaint.setShadowLayer(2 * density, 0, 0, 0xFFFFFFFF) ;
		pointerPaint.setAntiAlias(true) ;

		// Define settings of the hue and alpha sliders
		sliderPaint = new Paint() ;
		sliderPaint.setColor(0xFF1C1C1C) ;
		sliderPaint.setStyle(Style.STROKE) ;
		sliderPaint.setStrokeWidth(2 * density) ;
		sliderPaint.setAntiAlias(true) ;

		// Initializations related to the saturation/value, hue and alpha panels
		satValPaint = new Paint() ;
		alphaPaint = new Paint() ;

		// Try to load the alpha grid from resources
		Drawable alphaGrid = AppCompatResources.getDrawable(getContext(), R.drawable.alpha_grid) ;
		if(alphaGrid != null)
			{
				// Convert the alpha grid to a Bitmap
				Bitmap gridBitmap = Bitmap.createBitmap(alphaGrid.getIntrinsicWidth(), alphaGrid.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
				alphaGrid.setBounds(0, 0, alphaGrid.getIntrinsicWidth(), alphaGrid.getIntrinsicHeight()) ;
				alphaGrid.draw(new Canvas(gridBitmap)) ;

				// Repeat the pattern to form a scalable grid
				Shader shader = new BitmapShader(gridBitmap, Shader.TileMode.REPEAT,Shader.TileMode.REPEAT) ;
				alphaGridPaint = new Paint() ;
				alphaGridPaint.setShader(shader);
			}
			else alphaGridPaint = null ;

		// Prepare the preview area
		colorPreviewPaint = new Paint() ;
		colorPreviewText = new Paint() ;
		colorPreviewText.setAntiAlias(true) ;
		colorPreviewText.setTextSize(16 * density) ;
		colorPreviewText.setColor(Color.WHITE) ;
	}


	/**
	 * Draw the color picker interface and its sliders positionned onto the selected color.
	 */
	@Override
	protected void onDraw(Canvas canvas)
	{
		// Update the hue slider position to match the selected color
		float hue_position = huePanel.left + (hsv_color[0] * huePanel.width() / 360) ;
		hueSlider.left = hue_position - 2 * density ;
		hueSlider.right = hue_position + 2 * density ;

		// Retrieve the saturation/value pointer position matching the selected color
		float saturation_position = satValPanel.left + hsv_color[1] * satValPanel.width() ;
		float value_position = satValPanel.top + (1 - hsv_color[2]) * satValPanel.height() ;

		// Update the alpha slider position to match the selected color
		float alpha_position = alphaPanel.left + (alpha * alphaPanel.width() / 0xff) ;
		alphaSlider.left = alpha_position - 2 * density ;
		alphaSlider.right = alpha_position + 2 * density ;

		// Prepare the saturation/value panel gradients (must be done dynamically)
		@SuppressLint("DrawAllocation") Shader satShader = new LinearGradient(
				satValPanel.left, satValPanel.top, satValPanel.right, satValPanel.top,
				0xFFFFFFFF, Color.HSVToColor(new float[]{hsv_color[0], 1, 1}), TileMode.CLAMP) ;
		@SuppressLint("DrawAllocation") Shader satValShader = new ComposeShader(valShader, satShader, PorterDuff.Mode.MULTIPLY) ;
		satValPaint.setShader(satValShader) ;

		// Prepare the alpha panel gradient (must be done dynamically)
		@SuppressLint("DrawAllocation") Shader alphaShader = new LinearGradient(
				alphaPanel.left, alphaPanel.top, alphaPanel.right, alphaPanel.top,
				Color.HSVToColor(0, hsv_color), Color.HSVToColor(hsv_color), TileMode.CLAMP) ;
		alphaPaint.setShader(alphaShader) ;

		// Draw the hue panel and its slider
		canvas.drawRect(huePanel, huePaint) ;
		canvas.drawRoundRect(hueSlider, 2, 2, sliderPaint) ;

		// Draw the saturation/value panel and its pointer
		canvas.drawRect(satValPanel, satValPaint) ;
		canvas.drawCircle(saturation_position, value_position, 5 * density, pointerPaint) ;

		// Draw the alpha panel and its slider
		if(alphaGridPaint != null) canvas.drawRect(alphaPanel, alphaGridPaint) ;
		canvas.drawRect(alphaPanel, alphaPaint) ;
		canvas.drawRoundRect(alphaSlider, 2, 2, sliderPaint) ;

		// Draw the color preview
		if(alphaGridPaint != null) canvas.drawRect(colorPreviewPanel, alphaGridPaint) ;
		colorPreviewPaint.setColor(Color.HSVToColor(alpha, hsv_color)) ;
		canvas.drawRect(colorPreviewPanel, colorPreviewPaint) ;
		canvas.drawText(getContext().getString(R.string.color_picker_preview),
				colorPreviewPanel.left + 2 * density,
				colorPreviewPanel.top + 16 * density,
				colorPreviewText) ;
	}


	/**
	 * Called when the color picker view is touched.
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		// Check the type of touch
		switch(event.getAction())
		{
			case MotionEvent.ACTION_DOWN :
				// Note where the gesture started
				gestureStart.x = (int)event.getX() ;
				gestureStart.y = (int)event.getY() ;
			case MotionEvent.ACTION_MOVE :
				// Check if the selected color needs to be udpated
				if(updateColorFollowingGesture(event))
					{
						// Update the selected color and mark the event as consumed
						if(changeListener != null)
							changeListener.onColorChanged(Color.HSVToColor(alpha, hsv_color)) ;
						invalidate() ;
						return true ;
					}
				break ;
		}
		return super.onTouchEvent(event) ;
	}


	/**
	 * Update the selected color when a gesture has been detected on a panel.
	 * @return <code>true</code> if an update of the picker is needed, <code>false</code> otherwise
	 */
	private boolean updateColorFollowingGesture(MotionEvent event)
	{
		// Check where the gesture started
		if(huePanel.contains(gestureStart.x, gestureStart.y))
			{
				// Get the new position of the hue slider adjusted to the panel limits
				float hue_position = event.getX() ;
				if(hue_position < huePanel.left) hue_position = 0 ;
					else if(hue_position > huePanel.right) hue_position = huePanel.width() ;
					else hue_position = hue_position - huePanel.left ;

				// Update the hue of the currently selected color
				hsv_color[0] = hue_position * 360 / huePanel.width() ;
			}
			else if(satValPanel.contains(gestureStart.x, gestureStart.y))
			{
				// Get the new position of the saturation/value pointer adjusted to the panel limits
				float saturation_position = event.getX() ;
				float value_position = event.getY() ;
				if (saturation_position < satValPanel.left) saturation_position = 0 ;
					else if (saturation_position > satValPanel.right) saturation_position = satValPanel.width() ;
					else saturation_position = saturation_position - satValPanel.left ;
				if (value_position < satValPanel.top) value_position = 0 ;
					else if (value_position > satValPanel.bottom) value_position = satValPanel.height() ;
					else value_position = value_position - satValPanel.top ;

				// Update the saturation and value of the currently selected color
				hsv_color[1] = 1 / satValPanel.width() * saturation_position ;
				hsv_color[2] = 1 - (1 / satValPanel.height() * value_position) ;
			}
			else if(alphaPanel.contains(gestureStart.x, gestureStart.y))
			{
				// Get the new position of the alpha slider adjusted to the panel limits
				float alpha_position = event.getX() ;
				if(alpha_position < alphaPanel.left) alpha_position = 0 ;
					else if(alpha_position > alphaPanel.right) alpha_position = alphaPanel.width() ;
					else alpha_position = alpha_position - alphaPanel.left ;

				// Update the alpha of the currently selected color
				alpha = Math.round(alpha_position * 0xFF / alphaPanel.width()) ;
			}
			else
			{
				// Ignore gestures outside the panels
				return false ;
			}

		// Notify that the display should be updated (selected color has changed)
		return true ;
	}


	/**
	 * Force the color picker dimensions to prevent size issues (ex: view much too high).
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(view_width, view_height) ;
	}


	/**
	 * Set the listener that should be notified when the selected color changes.
	 */
	public void setOnColorChangedListener(ColorChangeListener listener)
	{
		changeListener = listener ;
	}


	/**
	 * Return the color currently selected with the color picker.
	 */
	public int getColor()
	{
		return Color.HSVToColor(alpha, hsv_color) ;
	}


	/**
	 * Select a specific color on the color picker, keeping or not the alpha channel.
	 */
	public void setColor(int new_color, boolean keep_alpha)
	{
		// Update the alpha, hue and saturation/value to the new color
		if(!keep_alpha) alpha = Color.alpha(new_color) ;
		Color.colorToHSV(new_color, hsv_color) ;

		// If requested, notify that the color was changed
		if(changeListener != null)
			changeListener.onColorChanged(Color.HSVToColor(alpha, hsv_color)) ;

		// Refresh the view
		invalidate() ;
	}
}
