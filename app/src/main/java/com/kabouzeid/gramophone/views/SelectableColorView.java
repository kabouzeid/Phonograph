package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.gramophone.util.ColorUtil;


public class SelectableColorView extends FrameLayout {

    private boolean selected;

    private final int borderWidthExtraSmall;
    private final int borderWidthSmall;
    private final int borderWidthLarge;

    private Paint outerPaint;
    private Paint gapPaint;
    private Paint innerPaint;

    public SelectableColorView(Context context) {
        this(context, null, 0);
    }

    public SelectableColorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectableColorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources r = getResources();
        borderWidthExtraSmall = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
        borderWidthSmall = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
        borderWidthLarge = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());

        gapPaint = new Paint();
        gapPaint.setAntiAlias(true);
        gapPaint.setColor(ThemeSingleton.get().darkTheme ? Color.parseColor("#444444") : Color.WHITE);

        innerPaint = new Paint();
        innerPaint.setAntiAlias(true);

        outerPaint = new Paint();
        outerPaint.setAntiAlias(true);

        setWillNotDraw(false);
    }

    private void updateColor(@ColorInt int color) {
        innerPaint.setColor(color);
        outerPaint.setColor(ColorUtil.shiftColorDown(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int[][] states = new int[][]{
                    new int[]{android.R.attr.state_pressed}
            };
            int[] colors = new int[]{createSelectedColor(color)};
            ColorStateList rippleColors = new ColorStateList(states, colors);
            Drawable mask = new ShapeDrawable(new OvalShape());
            RippleDrawable rippleDrawable = new RippleDrawable(rippleColors, null, mask);

            setForeground(rippleDrawable);
        } else {
            ShapeDrawable pressedDrawable = new ShapeDrawable(new OvalShape());
            pressedDrawable.getPaint().setColor(createSelectedColor(color));
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressedDrawable);

            setForeground(stateListDrawable);
        }
    }

    @ColorInt
    private static int createSelectedColor(int color) {
        if (ColorUtil.useDarkTextColorOnBackground(color)) {
            return ColorUtil.shiftColor(color, 0.8f);
        } else {
            return ColorUtil.shiftColor(color, 1.2f);
        }
    }

    public void setColor(@ColorInt int color) {
        updateColor(color);
        requestLayout();
        invalidate();
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
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
            if (heightMode == MeasureSpec.AT_MOST)
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final int outerRadius = getMeasuredWidth() / 2;
        if (selected) {
            final int whiteRadius = outerRadius - borderWidthLarge;
            final int innerRadius = whiteRadius - borderWidthSmall;
            canvas.drawCircle(getMeasuredWidth() / 2,
                    getMeasuredHeight() / 2,
                    outerRadius,
                    outerPaint);
            canvas.drawCircle(getMeasuredWidth() / 2,
                    getMeasuredHeight() / 2,
                    whiteRadius,
                    gapPaint);
            canvas.drawCircle(getMeasuredWidth() / 2,
                    getMeasuredHeight() / 2,
                    innerRadius,
                    innerPaint);
        } else {
            final int innerRadius = outerRadius - borderWidthExtraSmall;
            canvas.drawCircle(getMeasuredWidth() / 2,
                    getMeasuredHeight() / 2,
                    outerRadius,
                    outerPaint);
            canvas.drawCircle(getMeasuredWidth() / 2,
                    getMeasuredHeight() / 2,
                    innerRadius,
                    innerPaint);
        }
    }
}
