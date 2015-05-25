package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SquareFitImageView extends ImageView {

    public SquareFitImageView(Context context) {
        super(context);
    }

    public SquareFitImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareFitImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int newWidth = Math.min(widthMeasureSpec, heightMeasureSpec);
        //noinspection SuspiciousNameCombination
        super.onMeasure(newWidth, newWidth);
    }

}
