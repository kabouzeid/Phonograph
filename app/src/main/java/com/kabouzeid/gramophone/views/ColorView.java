package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.ColorUtil;

public class ColorView extends FrameLayout {

    private final Paint paint;
    private final Paint paintBorder;
    private final int borderWidth;

    public ColorView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ColorView(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        borderWidth = getResources().getDimensionPixelSize(R.dimen.color_view_border);

        paint = new Paint();
        paint.setAntiAlias(true);

        paintBorder = new Paint();
        paintBorder.setAntiAlias(true);

        setWillNotDraw(false);
    }

    @Override
    public void setBackgroundColor(int color) {
        paint.setColor(color);
        paintBorder.setColor(ColorUtil.shiftColorDown(color));
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            //noinspection SuspiciousNameCombination
            int height = width;
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int canvasSize = canvas.getWidth();
        if (canvas.getHeight() < canvasSize)
            canvasSize = canvas.getHeight();

        int circleCenter = (canvasSize - (borderWidth * 2)) / 2;
        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, ((canvasSize - (borderWidth * 2)) / 2) + borderWidth - 4.0f, paintBorder);
        canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, ((canvasSize - (borderWidth * 2)) / 2) - 4.0f, paint);
    }
}