package com.fekozma.wallpaperchanger.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularTimeRangeView extends View {

    private Paint circlePaint;
    private Paint arcPaint;
    private Paint handlePaint;
    private Paint textPaint;
    private Paint linePaint;

    private float centerX, centerY;
    private float radius;
    private float handleRadius = 30f;

    private int startHour = 0;
    private int startMinute = 0;
    private int endHour = 0;
    private int endMinute = 0;

    private boolean isDraggingStart = false;
    private boolean isDraggingEnd = false;

    private OnTimeRangeChangeListener listener;

    public interface OnTimeRangeChangeListener {
        void onTimeRangeChanged(int startHour, int startMinute, int endHour, int endMinute);
    }

    public CircularTimeRangeView(Context context) {
        super(context);
        init();
    }

    public CircularTimeRangeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularTimeRangeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#404040"));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(40f);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setColor(Color.parseColor("#4CAF50"));
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(40f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handlePaint.setColor(Color.parseColor("#2196F3"));
        handlePaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#808080"));
        linePaint.setStrokeWidth(2f);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - 80f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw hour markers (including half-hours)
        for (int i = 0; i < 48; i++) {
            float angle = (i * 7.5f - 90f);
            float angleRad = (float) Math.toRadians(angle);
            
            // Different lengths for full hours vs half hours
            boolean isFullHour = (i % 2 == 0);
            float innerRadius = isFullHour ? radius - 25f : radius - 15f;
            float outerRadius = isFullHour ? radius + 25f : radius + 15f;

            float startX = centerX + innerRadius * (float) Math.cos(angleRad);
            float startY = centerY + innerRadius * (float) Math.sin(angleRad);
            float endX = centerX + outerRadius * (float) Math.cos(angleRad);
            float endY = centerY + outerRadius * (float) Math.sin(angleRad);

            canvas.drawLine(startX, startY, endX, endY, linePaint);

            // Draw hour labels (only for full hours at 0, 3, 6, 9, 12, 15, 18, 21)
            if (isFullHour && (i / 2) % 3 == 0) {
                float textRadius = radius + 60f;
                float textX = centerX + textRadius * (float) Math.cos(angleRad);
                float textY = centerY + textRadius * (float) Math.sin(angleRad);
                canvas.drawText(String.valueOf(i / 2), textX, textY + 15f, textPaint);
            }
        }

        // Draw base circle
        RectF oval = new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // Draw arc for selected time range
        float startPosition = startHour * 2 + (startMinute == 30 ? 1 : 0);
        float endPosition = endHour * 2 + (endMinute == 30 ? 1 : 0);
        
        float startAngle = startPosition * 7.5f - 90f;
        float sweepAngle;

        if (endPosition >= startPosition) {
            sweepAngle = (endPosition - startPosition) * 7.5f;
        } else {
            // Wrapping around midnight
            sweepAngle = (48 - startPosition + endPosition) * 7.5f;
        }

        if (sweepAngle > 0) {
            canvas.drawArc(oval, startAngle, sweepAngle, false, arcPaint);
        }

        // Draw start handle
        float startAngleRad = (float) Math.toRadians(startAngle);
        float startHandleX = centerX + radius * (float) Math.cos(startAngleRad);
        float startHandleY = centerY + radius * (float) Math.sin(startAngleRad);
        handlePaint.setColor(Color.parseColor("#FF5722"));
        canvas.drawCircle(startHandleX, startHandleY, handleRadius, handlePaint);

        // Draw end handle
        float endAngle = endPosition * 7.5f - 90f;
        float endAngleRad = (float) Math.toRadians(endAngle);
        float endHandleX = centerX + radius * (float) Math.cos(endAngleRad);
        float endHandleY = centerY + radius * (float) Math.sin(endAngleRad);
        handlePaint.setColor(Color.parseColor("#2196F3"));
        canvas.drawCircle(endHandleX, endHandleY, handleRadius, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if touching start handle
                float startPosition = startHour * 2 + (startMinute == 30 ? 1 : 0);
                float startAngle = startPosition * 7.5f - 90f;
                float startAngleRad = (float) Math.toRadians(startAngle);
                float startHandleX = centerX + radius * (float) Math.cos(startAngleRad);
                float startHandleY = centerY + radius * (float) Math.sin(startAngleRad);
                float distToStart = (float) Math.sqrt(Math.pow(touchX - startHandleX, 2) + Math.pow(touchY - startHandleY, 2));

                // Check if touching end handle
                float endPosition = endHour * 2 + (endMinute == 30 ? 1 : 0);
                float endAngle = endPosition * 7.5f - 90f;
                float endAngleRad = (float) Math.toRadians(endAngle);
                float endHandleX = centerX + radius * (float) Math.cos(endAngleRad);
                float endHandleY = centerY + radius * (float) Math.sin(endAngleRad);
                float distToEnd = (float) Math.sqrt(Math.pow(touchX - endHandleX, 2) + Math.pow(touchY - endHandleY, 2));

                if (distToStart < handleRadius + 20f) {
                    isDraggingStart = true;
                    return true;
                } else if (distToEnd < handleRadius + 20f) {
                    isDraggingEnd = true;
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDraggingStart || isDraggingEnd) {
                    float dx = touchX - centerX;
                    float dy = touchY - centerY;
                    float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
                    angle = (angle + 90f + 360f) % 360f;

                    int position = Math.round(angle / 7.5f) % 48;
                    int hour = position / 2;
                    int minute = (position % 2) * 30;

                    if (isDraggingStart) {
                        startHour = hour;
                        startMinute = minute;
                    } else {
                        endHour = hour;
                        endMinute = minute;
                    }

                    if (listener != null) {
                        listener.onTimeRangeChanged(startHour, startMinute, endHour, endMinute);
                    }

                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDraggingStart = false;
                isDraggingEnd = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    public void setStartTime(int hour, int minute) {
        this.startHour = hour % 24;
        this.startMinute = (minute >= 30) ? 30 : 0;
        invalidate();
    }

    public void setEndTime(int hour, int minute) {
        this.endHour = hour % 24;
        this.endMinute = (minute >= 30) ? 30 : 0;
        invalidate();
    }

    public int getStartHour() {
        return startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public int getEndHour() {
        return endHour;
    }

    public int getEndMinute() {
        return endMinute;
    }

    public void setOnTimeRangeChangeListener(OnTimeRangeChangeListener listener) {
        this.listener = listener;
    }
}
