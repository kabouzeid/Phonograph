package com.poupa.vinylmusicplayer.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class HeightFitSquareLayout extends FrameLayout {

    private boolean forceSquare = true;

    public HeightFitSquareLayout(Context context) {
        super(context);
    }

    public HeightFitSquareLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeightFitSquareLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeightFitSquareLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(forceSquare ? heightMeasureSpec : widthMeasureSpec, heightMeasureSpec);
    }

    public void forceSquare(boolean forceSquare) {
        this.forceSquare = forceSquare;
        requestLayout();
    }
}
