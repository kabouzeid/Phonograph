package com.poupa.vinylmusicplayer.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class WidthFitSquareLayout extends FrameLayout {

    private boolean forceSquare = true;

    public WidthFitSquareLayout(Context context) {
        super(context);
    }

    public WidthFitSquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidthFitSquareLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WidthFitSquareLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec, forceSquare ? widthMeasureSpec : heightMeasureSpec);
    }

    public void forceSquare(boolean forceSquare) {
        this.forceSquare = forceSquare;
        requestLayout();
    }
}
