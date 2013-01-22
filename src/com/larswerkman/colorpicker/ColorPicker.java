/*
 * Copyright 2012 Lars Werkman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.larswerkman.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Displays a holo-themed color picker.
 * 
 * <p>
 * Use {@link #getColor()} to retrieve the selected color.
 * </p>
 */
public class ColorPicker extends View {
	/*
	 * Constants used to save/restore the instance state.
	 */
	private static final String STATE_PARENT = "parent";
	private static final String STATE_ANGLE = "angle";

	/**
	 * Colors to construct the color wheel using {@link SweepGradient}.
	 * 
	 * <p>
	 * Note: The algorithm in {@link #normalizeColor(int)} highly depends on
	 * these exact values. Be aware that {@link #setColor(int)} might break if
	 * you change this array.
	 * </p>
	 */
	private static final int[] COLORS = new int[] { Color.DKGRAY, Color.CYAN, };

	/**
	 * {@code Paint} instance used to draw the color wheel.
	 */
	private Paint mColorWheelPaint;

	/**
	 * {@code Paint} instance used to draw the pointer's "halo".
	 */
	private Paint mPointerHaloPaint;

	/**
	 * {@code Paint} instance used to draw the pointer (the selected color).
	 */
	private Paint mPointerColor;

	/**
	 * The stroke width used to paint the color wheel (in pixels).
	 */
	private int mColorWheelStrokeWidth;

	/**
	 * The radius of the pointer (in pixels).
	 */
	private int mPointerRadius;

	/**
	 * The rectangle enclosing the color wheel.
	 */
	private RectF mColorWheelRectangle = new RectF();

	/**
	 * {@code true} if the user clicked on the pointer to start the move mode.
	 * {@code false} once the user stops touching the screen.
	 * 
	 * @see #onTouchEvent(MotionEvent)
	 */
	private boolean mUserIsMovingPointer = false;

	/**
	 * The ARGB value of the currently selected color.
	 */
	private int mColor;

	/**
	 * Number of pixels the origin of this view is moved in X- and Y-direction.
	 * 
	 * <p>
	 * We use the center of this (quadratic) View as origin of our internal
	 * coordinate system. Android uses the upper left corner as origin for the
	 * View-specific coordinate system. So this is the value we use to translate
	 * from one coordinate system to the other.
	 * </p>
	 * 
	 * <p>
	 * Note: (Re)calculated in {@link #onMeasure(int, int)}.
	 * </p>
	 * 
	 * @see #onDraw(Canvas)
	 */
	private float mTranslationOffset;

	/**
	 * Radius of the color wheel in pixels.
	 * 
	 * <p>
	 * Note: (Re)calculated in {@link #onMeasure(int, int)}.
	 * </p>
	 */
	private float mColorWheelRadius;

	/**
	 * The pointer's position expressed as angle (in rad).
	 */
	private float mAngle;
	private Paint textPaint;
	private String text;
	private int conversion = 0;
	private int max = 100;
	private String color_attr;
	private int color;
	private SweepGradient s;
	private Paint mArcColor;
	private String wheel_color_attr, wheel_unactive_color_attr,
			pointer_color_attr, pointer_halo_color_attr, text_color_attr;
	private int wheel_color, unactive_wheel_color, pointer_color,
			pointer_halo_color, text_size, text_color, init_position;

	public ColorPicker(Context context) {
		super(context);
		init(null, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs, 0);
	}

	public ColorPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs, defStyle);
	}

	private void init(AttributeSet attrs, int defStyle) {
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.ColorPicker, defStyle, 0);

		initAttributes(a);

		a.recycle();
		mAngle = (float) (-Math.PI / 2);

		mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mColorWheelPaint.setShader(s);
		mColorWheelPaint.setColor(unactive_wheel_color);
		mColorWheelPaint.setStyle(Paint.Style.STROKE);
		mColorWheelPaint.setStrokeWidth(mColorWheelStrokeWidth);

		mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerHaloPaint.setColor(pointer_halo_color);
		mPointerHaloPaint.setStrokeWidth(mPointerRadius + 10);
		// mPointerHaloPaint.setAlpha(150);

		textPaint = new Paint();
		textPaint.setColor(text_color);
		textPaint.setStyle(Style.FILL_AND_STROKE);
		// canvas.drawPaint(textPaint);
		textPaint.setTextSize(text_size);

		mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPointerColor.setStrokeWidth(mPointerRadius);
		text = conversion + "";
		// mPointerColor.setColor(calculateColor(mAngle));
		mPointerColor.setColor(pointer_color);

		mArcColor = new Paint(Paint.ANTI_ALIAS_FLAG);
		mArcColor.setColor(wheel_color);
		mArcColor.setStyle(Paint.Style.STROKE);
		mArcColor.setStrokeWidth(mColorWheelStrokeWidth);
	}

	private void initAttributes(TypedArray a) {
		mColorWheelStrokeWidth = a.getInteger(
				R.styleable.ColorPicker_wheel_size, 16);
		mPointerRadius = a.getInteger(R.styleable.ColorPicker_pointer_size, 48);
		max = a.getInteger(R.styleable.ColorPicker_max, 100);
		init_position = a.getInteger(R.styleable.ColorPicker_init_position, 0);

		color_attr = a.getString(R.styleable.ColorPicker_color);
		wheel_color_attr = a
				.getString(R.styleable.ColorPicker_wheel_active_color);
		wheel_unactive_color_attr = a
				.getString(R.styleable.ColorPicker_wheel_unactive_color);
		pointer_color_attr = a.getString(R.styleable.ColorPicker_pointer_color);
		pointer_halo_color_attr = a
				.getString(R.styleable.ColorPicker_pointer_halo_color);

		text_color_attr = a.getString(R.styleable.ColorPicker_text_color);

		text_size = a.getInteger(R.styleable.ColorPicker_text_size, 95);

		if (color_attr != null) {
			try {
				color = Color.parseColor(color_attr);
			} catch (IllegalArgumentException e) {
				color = Color.CYAN;
			}
			color = Color.parseColor(color_attr);
		} else {
			color = Color.CYAN;
		}

		if (wheel_color_attr != null) {
			try {
				wheel_color = Color.parseColor(wheel_color_attr);
			} catch (IllegalArgumentException e) {
				wheel_color = Color.DKGRAY;
			}

		} else {
			wheel_color = Color.DKGRAY;
		}
		if (wheel_unactive_color_attr != null) {
			try {
				unactive_wheel_color = Color
						.parseColor(wheel_unactive_color_attr);
			} catch (IllegalArgumentException e) {
				unactive_wheel_color = Color.CYAN;
			}

		} else {
			unactive_wheel_color = Color.CYAN;
		}

		if (pointer_color_attr != null) {
			try {
				pointer_color = Color.parseColor(pointer_color_attr);
			} catch (IllegalArgumentException e) {
				pointer_color = Color.CYAN;
			}

		} else {
			pointer_color = Color.CYAN;
		}

		if (pointer_halo_color_attr != null) {
			try {
				pointer_halo_color = Color.parseColor(pointer_halo_color_attr);
			} catch (IllegalArgumentException e) {
				pointer_halo_color = Color.CYAN;
			}

		} else {
			pointer_halo_color = Color.DKGRAY;
		}

		if (text_color_attr != null) {
			try {
				text_color = Color.parseColor(text_color_attr);
			} catch (IllegalArgumentException e) {
				text_color = Color.CYAN;
			}
		} else {
			text_color = Color.CYAN;
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// All of our positions are using our internal coordinate system.
		// Instead of translating
		// them we let Canvas do the work for us.
		canvas.translate(mTranslationOffset, mTranslationOffset);

		// Draw the color wheel.
		canvas.drawOval(mColorWheelRectangle, mColorWheelPaint);

		canvas.drawArc(mColorWheelRectangle, 270,
				calculateRadiansFromAngle(mAngle), false, mArcColor);

		float[] pointerPosition = calculatePointerPosition(mAngle);

		// Draw the pointer's "halo"
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				mPointerRadius, mPointerHaloPaint);

		// Draw the pointer (the currently selected color) slightly smaller on
		// top.
		canvas.drawCircle(pointerPosition[0], pointerPosition[1],
				(float) (mPointerRadius / 1.2), mPointerColor);
		text = String.valueOf(calculateTextFromAngle(mAngle));
		canvas.drawText(text,
				(mColorWheelRectangle.centerX())
						- (textPaint.measureText(text) / 2),
				mColorWheelRectangle.centerY() + (textPaint.getTextSize() / 2),
				textPaint);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = getDefaultSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
		int min = Math.min(width, height);
		setMeasuredDimension(min, min);

		mTranslationOffset = min * 0.5f;
		mColorWheelRadius = mTranslationOffset - mPointerRadius;

		mColorWheelRectangle.set(-mColorWheelRadius, -mColorWheelRadius,
				mColorWheelRadius, mColorWheelRadius);
	}

	private int ave(int s, int d, float p) {
		return s + java.lang.Math.round(p * (d - s));
	}

	/**
	 * Calculate the color using the supplied angle.
	 * 
	 * @param angle
	 *            The selected color's position expressed as angle (in rad).
	 * 
	 * @return The ARGB value of the color on the color wheel at the specified
	 *         angle.
	 */
	private int calculateColor(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}

		if (unit <= 0) {
			// conversion = COLORS[0];
			return COLORS[0];
		}
		if (unit >= 1) {
			// conversion = COLORS[COLORS.length - 1];
			return COLORS[COLORS.length - 1];
		}

		float p = unit * (COLORS.length - 1);
		float q = unit * max;
		int j = (int) q;
		// conversion -= j;
		int i = (int) p;
		p -= i;

		int c0 = COLORS[i];
		int c1 = COLORS[i + 1];
		int a = ave(Color.alpha(c0), Color.alpha(c1), p);
		int r = ave(Color.red(c0), Color.red(c1), p);
		int g = ave(Color.green(c0), Color.green(c1), p);
		int b = ave(Color.blue(c0), Color.blue(c1), p);

		mColor = Color.argb(a, r, g, b);

		return Color.argb(a, r, g, b);
	}

	private int calculateTextFromAngle(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}
		conversion = (int) ((unit * max) - ((max / 4) * 3));
		if (conversion < 0)
			conversion += max;
		return conversion;
	}

	private int calculateRadiansFromAngle(float angle) {
		float unit = (float) (angle / (2 * Math.PI));
		if (unit < 0) {
			unit += 1;
		}
		int radians = (int) ((unit * 360) - ((360 / 4) * 3));
		if (radians < 0)
			radians += 360;
		return radians;
	}

	/**
	 * Get the selected value
	 * 
	 * @return the value between 0 and max
	 */
	public int getValue() {
		return conversion;
	}

	/**
	 * Get the currently selected color.
	 * 
	 * @return The ARGB value of the currently selected color.
	 */
	public int getColor() {
		return mColor;
	}

	/**
	 * Set the color to be highlighted by the pointer.
	 * 
	 * @param color
	 *            The RGB value of the color to highlight. If this is not a
	 *            color displayed on the color wheel a very simple algorithm is
	 *            used to map it to the color wheel. The resulting color often
	 *            won't look close to the original color. This is especially
	 *            true for shades of grey. You have been warned!
	 */
	public void setColor(int color) {
		mAngle = colorToAngle(color);
		mPointerColor.setColor(calculateColor(mAngle));
		invalidate();
	}

	/**
	 * Convert a color to an angle.
	 * 
	 * @param color
	 *            The RGB value of the color to "find" on the color wheel.
	 *            {@link #normalizeColor(int)} will be used to map this color to
	 *            one on the color wheel if necessary.
	 * 
	 * @return The angle (in rad) the "normalized" color is displayed on the
	 *         color wheel.
	 */
	private float colorToAngle(int color) {
		int[] colorInfo = normalizeColor(color);
		int normColor = colorInfo[0];
		int colorMask = colorInfo[1];
		int shiftValue = colorInfo[2];

		int anchorColor = (normColor & ~colorMask);

		// Find the "anchor" color in the COLORS array
		for (int i = 0; i < COLORS.length - 1; i++) {
			if (COLORS[i] == anchorColor) {
				int nextValue = COLORS[i + 1];

				double value;
				double decimals = ((normColor >> shiftValue) & 0xFF) / 255D;

				// Find out if the gradient our color belongs to goes from the
				// element just found to
				// the next element in the array.
				if ((nextValue & colorMask) != (anchorColor & colorMask)) {
					// Compute value depending of the gradient direction
					if (nextValue < anchorColor) {
						value = i + 1 - decimals;
					} else {
						value = i + decimals;
					}
				} else {
					// It's a gradient from this element to the previous element
					// in the array.

					// Wrap to the end of the array if the "anchor" color is the
					// first element.
					int index = (i == 0) ? COLORS.length - 1 : i;
					int prevValue = COLORS[index - 1];

					// Compute value depending of the gradient direction
					if (prevValue < anchorColor) {
						value = index - 1 + decimals;
					} else {
						value = index - decimals;
					}
				}

				// Calculate the angle in rad (from -PI to PI)
				float angle = (float) (2 * Math.PI * value / (COLORS.length - 1));
				if (angle > Math.PI) {
					angle -= 2 * Math.PI;
				}

				return angle;
			}
		}

		// This shouldn't happen
		return 0;
	}

	/**
	 * "Normalize" the supplied color.
	 * 
	 * <p>
	 * This will set the lowest value of R,G,B to 0, the highest to 255, and
	 * will keep the middle value.<br>
	 * For values close to those on the color wheel this will result in close
	 * matches. For other values, especially shades of grey this will produce
	 * funny results.
	 * </p>
	 * 
	 * @param color
	 *            The color to "normalize".
	 * 
	 * @return An {@code int} array with the following contents:
	 *         <ol>
	 *         <li>The ARGB value of the "normalized" color.</li>
	 *         <li>A mask with all bits {@code 0} but those for the byte
	 *         representing the "middle value" that remains unchanged in the
	 *         "normalized" color.</li>
	 *         <li>The number of bits the "normalized" color has to be shifted
	 *         to the right so the "middle value" is in the lower 8 bits.</li>
	 *         </ol>
	 */
	private int[] normalizeColor(int color) {
		int red = Color.red(color);
		int green = Color.green(color);
		int blue = Color.blue(color);

		int newRed = red;
		int newGreen = green;
		int newBlue = blue;

		int maskRed = 0;
		int maskGreen = 0;
		int maskBlue = 0;
		int shiftValue;

		if (red < green && red < blue) {
			// Red is the smallest component
			newRed = 0;
			if (green > blue) {
				// Green is the largest component
				shiftValue = 0;
				maskBlue = 0xFF;
				newGreen = 0xFF;
			} else {
				// We make blue the largest component
				shiftValue = 8;
				maskGreen = 0xFF;
				newBlue = 0xFF;
			}
		} else if (green < red && green < blue) {
			// Green is the smallest component
			newGreen = 0;
			if (red > blue) {
				// Red is the largest component
				shiftValue = 0;
				maskBlue = 0xFF;
				newRed = 0xFF;
			} else {
				// We make blue the largest component
				shiftValue = 16;
				maskRed = 0xFF;
				newBlue = 0xFF;
			}
		} else {
			// We make blue the smallest component
			newBlue = 0;
			if (red > green) {
				// Red is the largest component
				shiftValue = 8;
				maskGreen = 0xFF;
				newRed = 0xFF;
			} else {
				// We make green the largest component
				shiftValue = 16;
				maskRed = 0xFF;
				newGreen = 0xFF;
			}
		}

		int normColor = Color.argb(255, newRed, newGreen, newBlue);
		int colorMask = Color.argb(0, maskRed, maskGreen, maskBlue);

		return new int[] { normColor, colorMask, shiftValue };
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Convert coordinates to our internal coordinate system
		float x = event.getX() - mTranslationOffset;
		float y = event.getY() - mTranslationOffset;

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Check whether the user pressed on (or near) the pointer
			float[] pointerPosition = calculatePointerPosition(mAngle);
			if (x >= (pointerPosition[0] - 48)
					&& x <= (pointerPosition[0] + 48)
					&& y >= (pointerPosition[1] - 48)
					&& y <= (pointerPosition[1] + 48)) {
				mUserIsMovingPointer = true;
				invalidate();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (mUserIsMovingPointer) {
				mAngle = (float) java.lang.Math.atan2(y, x);
				// mPointerColor.setColor(calculateColor(mAngle));

				// textPaint.setColor(calculateColor(mAngle));
				// text = conversion + "";

				invalidate();
			}
			break;
		case MotionEvent.ACTION_UP:
			mUserIsMovingPointer = false;
			break;
		}
		return true;
	}

	/**
	 * Calculate the pointer's coordinates on the color wheel using the supplied
	 * angle.
	 * 
	 * @param angle
	 *            The position of the pointer expressed as angle (in rad).
	 * 
	 * @return The coordinates of the pointer's center in our internal
	 *         coordinate system.
	 */
	private float[] calculatePointerPosition(float angle) {
		float x = (float) (mColorWheelRadius * Math.cos(angle));
		float y = (float) (mColorWheelRadius * Math.sin(angle));

		return new float[] { x, y };
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		Bundle state = new Bundle();
		state.putParcelable(STATE_PARENT, superState);
		state.putFloat(STATE_ANGLE, mAngle);

		return state;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;

		Parcelable superState = savedState.getParcelable(STATE_PARENT);
		super.onRestoreInstanceState(superState);

		mAngle = savedState.getFloat(STATE_ANGLE);
		mPointerColor.setColor(calculateColor(mAngle));
	}
}
