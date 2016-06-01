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

package com.jesusm.holocircleseekbar.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.jesus.lib.R;

public class HoloCircleSeekBar extends View {

    private static final String STATE_PARENT = "parent";
    private static final String STATE_ANGLE = "angle";
    private static final int TEXT_SIZE_DEFAULT_VALUE = 25;
    private static final int END_WHEEL_DEFAULT_VALUE = 360;
    public static final int COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE = 16;
    public static final float POINTER_RADIUS_DEF_VALUE = 8;
    public static final int MAX_POINT_DEF_VALUE = 100;
    public static final int START_ANGLE_DEF_VALUE = 0;

    private OnCircleSeekBarChangeListener mOnCircleSeekBarChangeListener;

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
    private float mPointerRadius;

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
     * Number of pixels the origin of this view is moved in X- and Y-direction.
     * <p>
     * <p>
     * We use the center of this (quadratic) View as origin of our internal
     * coordinate system. Android uses the upper left corner as origin for the
     * View-specific coordinate system. So this is the value we use to translate
     * from one coordinate system to the other.
     * </p>
     * <p>
     * <p>
     * Note: (Re)calculated in {@link #onMeasure(int, int)}.
     * </p>
     *
     * @see #onDraw(Canvas)
     */
    private float mTranslationOffset;

    /**
     * Radius of the color wheel in pixels.
     * <p>
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
    private int max = 100;
    private SweepGradient s;
    private Paint mArcColor;
    private int wheel_color, unactive_wheel_color, pointer_color, pointer_halo_color, text_size, text_color;
    private int init_position = -1;
    private boolean block_end = false;
    private float lastX;
    private int last_radians = 0;
    private boolean block_start = false;

    private int arc_finish_radians = 360;
    private int start_arc = 270;

    private float[] pointerPosition;
    private RectF mColorCenterHaloRectangle = new RectF();
    private int end_wheel;

    private boolean show_text = true;
    private Rect bounds = new Rect();

    public HoloCircleSeekBar(Context context) {
        super(context);
        init(null, 0);
    }

    public HoloCircleSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HoloCircleSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        final TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.HoloCircleSeekBar, defStyle, 0);

        initAttributes(a);

        a.recycle();
        // mAngle = (float) (-Math.PI / 2);

        mColorWheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorWheelPaint.setShader(s);
        mColorWheelPaint.setColor(unactive_wheel_color);
        mColorWheelPaint.setStyle(Style.STROKE);
        mColorWheelPaint.setStrokeWidth(mColorWheelStrokeWidth);

        Paint mColorCenterHalo = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorCenterHalo.setColor(Color.CYAN);
        mColorCenterHalo.setAlpha(0xCC);
        // mColorCenterHalo.setStyle(Paint.Style.STROKE);
        // mColorCenterHalo.setStrokeWidth(mColorCenterHaloRectangle.width() /
        // 2);

        mPointerHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerHaloPaint.setColor(pointer_halo_color);
        mPointerHaloPaint.setStrokeWidth(mPointerRadius + 10);
        // mPointerHaloPaint.setAlpha(150);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
        textPaint.setColor(text_color);
        textPaint.setStyle(Style.FILL_AND_STROKE);
        textPaint.setTextAlign(Align.LEFT);
        // canvas.drawPaint(textPaint);
        textPaint.setTextSize(text_size);

        mPointerColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPointerColor.setStrokeWidth(mPointerRadius);

        // mPointerColor.setColor(calculateColor(mAngle));
        mPointerColor.setColor(pointer_color);

        mArcColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mArcColor.setColor(wheel_color);
        mArcColor.setStyle(Style.STROKE);
        mArcColor.setStrokeWidth(mColorWheelStrokeWidth);

        Paint mCircleTextColor = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCircleTextColor.setColor(Color.WHITE);
        mCircleTextColor.setStyle(Style.FILL);

        arc_finish_radians = (int) calculateAngleFromText(init_position) - 90;

        if (arc_finish_radians > end_wheel)
            arc_finish_radians = end_wheel;
        mAngle = calculateAngleFromRadians(arc_finish_radians > end_wheel ? end_wheel
                : arc_finish_radians);
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));

        invalidate();
    }

    private void setTextFromAngle(int angleValue) {
        this.text = String.valueOf(angleValue);
    }

    private void initAttributes(TypedArray a) {
        mColorWheelStrokeWidth = a.getInteger(
                R.styleable.HoloCircleSeekBar_wheel_size, COLOR_WHEEL_STROKE_WIDTH_DEF_VALUE);
        mPointerRadius = a.getDimension(
                R.styleable.HoloCircleSeekBar_pointer_size, POINTER_RADIUS_DEF_VALUE);
        max = a.getInteger(R.styleable.HoloCircleSeekBar_max, MAX_POINT_DEF_VALUE);

        String wheel_color_attr = a
                .getString(R.styleable.HoloCircleSeekBar_wheel_active_color);
        String wheel_unactive_color_attr = a
                .getString(R.styleable.HoloCircleSeekBar_wheel_unactive_color);
        String pointer_color_attr = a
                .getString(R.styleable.HoloCircleSeekBar_pointer_color);
        String pointer_halo_color_attr = a
                .getString(R.styleable.HoloCircleSeekBar_pointer_halo_color);

        String text_color_attr = a.getString(R.styleable.HoloCircleSeekBar_text_color);

        text_size = a.getDimensionPixelSize(R.styleable.HoloCircleSeekBar_text_size, TEXT_SIZE_DEFAULT_VALUE);

        init_position = a.getInteger(R.styleable.HoloCircleSeekBar_init_position, 0);

        start_arc = a.getInteger(R.styleable.HoloCircleSeekBar_start_angle, START_ANGLE_DEF_VALUE);
        end_wheel = a.getInteger(R.styleable.HoloCircleSeekBar_end_angle, END_WHEEL_DEFAULT_VALUE);

        show_text = a.getBoolean(R.styleable.HoloCircleSeekBar_show_text, true);

        last_radians = end_wheel;

        if (init_position < start_arc)
            init_position = calculateTextFromStartAngle(start_arc);

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
        canvas.drawArc(mColorWheelRectangle, start_arc + 270, end_wheel
                - (start_arc), false, mColorWheelPaint);

        canvas.drawArc(mColorWheelRectangle, start_arc + 270,
                (arc_finish_radians) > (end_wheel) ? end_wheel - (start_arc)
                        : arc_finish_radians - start_arc, false, mArcColor);

        // Draw the pointer's "halo"
        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                mPointerRadius, mPointerHaloPaint);

        // Draw the pointer (the currently selected color) slightly smaller on
        // top.
        canvas.drawCircle(pointerPosition[0], pointerPosition[1],
                (float) (mPointerRadius / 1.2), mPointerColor);
        textPaint.getTextBounds(text, 0, text.length(), bounds);
        // canvas.drawCircle(mColorWheelRectangle.centerX(),
        // mColorWheelRectangle.centerY(), (bounds.width() / 2) + 5,
        // mCircleTextColor);
        if (show_text)
            canvas.drawText(
                    text,
                    (mColorWheelRectangle.centerX())
                            - (textPaint.measureText(text) / 2),
                    mColorWheelRectangle.centerY() + bounds.height() / 2,
                    textPaint);

        // last_radians = calculateRadiansFromAngle(mAngle);

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

        mColorCenterHaloRectangle.set(-mColorWheelRadius / 2,
                -mColorWheelRadius / 2, mColorWheelRadius / 2,
                mColorWheelRadius / 2);

        updatePointerPosition();

    }

    private int calculateValueFromAngle(float angle) {
        float m = angle - start_arc;

        float f = (end_wheel - start_arc) / m;

        return (int) (max / f);
    }

    private int calculateTextFromStartAngle(float angle) {
        float f = (end_wheel - start_arc) / angle;

        return (int) (max / f);
    }

    private double calculateAngleFromText(int position) {
        if (position == 0 || position >= max)
            return (float) 90;

        double f = (double) max / (double) position;

        double f_r = 360 / f;

        return f_r + 90;
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

    private float calculateAngleFromRadians(int radians) {
        return (float) (((radians + 270) * (2 * Math.PI)) / 360);
    }

    /**
     * Get the selected value
     *
     * @return the value between 0 and max
     */
    public int getValue() {
        return Integer.valueOf(text);
    }

    public void setMax(int max) {
        this.max = max;
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
        updatePointerPosition();
        invalidate();
    }

    public void setValue(float newValue) {
        if (newValue == 0) {
            arc_finish_radians = start_arc;
        } else if (newValue == this.max) {
            arc_finish_radians = end_wheel;
        } else {
            float newAngle = (float) (360.0 * (newValue / max));
            arc_finish_radians = (int) calculateAngleFromRadians(calculateRadiansFromAngle(newAngle)) + 1;
        }

        mAngle = calculateAngleFromRadians(arc_finish_radians);
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
        updatePointerPosition();
        invalidate();
    }

    private void updatePointerPosition() {
        pointerPosition = calculatePointerPosition(mAngle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Convert coordinates to our internal coordinate system
        float x = event.getX() - mTranslationOffset;
        float y = event.getY() - mTranslationOffset;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check whether the user pressed on (or near) the pointer
                mAngle = (float) Math.atan2(y, x);

                block_end = false;
                block_start = false;
                mUserIsMovingPointer = true;

                arc_finish_radians = calculateRadiansFromAngle(mAngle);

                if (arc_finish_radians > end_wheel) {
                    arc_finish_radians = end_wheel;
                    block_end = true;
                }

                if (!block_end) {
                    setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
                    updatePointerPosition();
                    invalidate();
                }
                if (mOnCircleSeekBarChangeListener != null) {
                    mOnCircleSeekBarChangeListener.onStartTrackingTouch(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mUserIsMovingPointer) {
                    mAngle = (float) Math.atan2(y, x);

                    int radians = calculateRadiansFromAngle(mAngle);

                    if (last_radians > radians && radians < (360 / 6) && x > lastX
                            && last_radians > (360 / 6)) {

                        if (!block_end && !block_start)
                            block_end = true;
                        // if (block_start)
                        // block_start = false;
                    } else if (last_radians >= start_arc
                            && last_radians <= (360 / 4) && radians <= (360 - 1)
                            && radians >= ((360 / 4) * 3) && x < lastX) {
                        if (!block_start && !block_end)
                            block_start = true;
                        // if (block_end)
                        // block_end = false;

                    } else if (radians >= end_wheel && !block_start
                            && last_radians < radians) {
                        block_end = true;
                    } else if (radians < end_wheel && block_end
                            && last_radians > end_wheel) {
                        block_end = false;
                    } else if (radians < start_arc && last_radians > radians
                            && !block_end) {
                        block_start = true;
                    } else if (block_start && last_radians < radians
                            && radians > start_arc && radians < end_wheel) {
                        block_start = false;
                    }

                    if (block_end) {
                        arc_finish_radians = end_wheel - 1;
                        setTextFromAngle(max);
                        mAngle = calculateAngleFromRadians(arc_finish_radians);
                        updatePointerPosition();
                    } else if (block_start) {
                        arc_finish_radians = start_arc;
                        mAngle = calculateAngleFromRadians(arc_finish_radians);
                        setTextFromAngle(0);
                        updatePointerPosition();
                    } else {
                        arc_finish_radians = calculateRadiansFromAngle(mAngle);
                        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
                        updatePointerPosition();
                    }
                    invalidate();
                    if (mOnCircleSeekBarChangeListener != null)
                        mOnCircleSeekBarChangeListener.onProgressChanged(this,
                                Integer.parseInt(text), true);

                    last_radians = radians;

                }
                break;
            case MotionEvent.ACTION_UP:
                mUserIsMovingPointer = false;
                if (mOnCircleSeekBarChangeListener != null) {
                    mOnCircleSeekBarChangeListener.onStopTrackingTouch(this);
                }
                break;
        }
        // Fix scrolling
        if (event.getAction() == MotionEvent.ACTION_MOVE && getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        lastX = x;

        return true;
    }

    /**
     * Calculate the pointer's coordinates on the color wheel using the supplied
     * angle.
     *
     * @param angle The position of the pointer expressed as angle (in rad).
     * @return The coordinates of the pointer's center in our internal
     * coordinate system.
     */
    private float[] calculatePointerPosition(float angle) {
        // if (calculateRadiansFromAngle(angle) > end_wheel)
        // angle = calculateAngleFromRadians(end_wheel);
        float x = (float) (mColorWheelRadius * Math.cos(angle));
        float y = (float) (mColorWheelRadius * Math.sin(angle));

        return new float[]{x, y};
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
        arc_finish_radians = calculateRadiansFromAngle(mAngle);
        setTextFromAngle(calculateValueFromAngle(arc_finish_radians));
        updatePointerPosition();
    }

    public void setInitPosition(int init) {
        init_position = init;
        setTextFromAngle(init_position);
        mAngle = calculateAngleFromRadians(init_position);
        arc_finish_radians = calculateRadiansFromAngle(mAngle);
        updatePointerPosition();
        invalidate();
    }

    public void setOnSeekBarChangeListener(OnCircleSeekBarChangeListener l) {
        mOnCircleSeekBarChangeListener = l;
    }

    public int getMaxValue() {
        return max;
    }

    public interface OnCircleSeekBarChangeListener {

        void onProgressChanged(HoloCircleSeekBar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(HoloCircleSeekBar seekBar);

        void onStopTrackingTouch(HoloCircleSeekBar seekBar);

    }

}
