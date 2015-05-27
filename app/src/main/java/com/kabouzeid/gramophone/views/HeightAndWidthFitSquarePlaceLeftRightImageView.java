package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class HeightAndWidthFitSquarePlaceLeftRightImageView extends ImageView {

    public HeightAndWidthFitSquarePlaceLeftRightImageView(Context context) {
        super(context);
    }

    public HeightAndWidthFitSquarePlaceLeftRightImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeightAndWidthFitSquarePlaceLeftRightImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int small = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(small, small);
    }

}
