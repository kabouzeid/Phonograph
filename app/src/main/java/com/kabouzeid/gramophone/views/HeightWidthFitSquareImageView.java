package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class HeightWidthFitSquareImageView extends ImageView {

    public HeightWidthFitSquareImageView(Context context) {
        super(context);
    }

    public HeightWidthFitSquareImageView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeightWidthFitSquareImageView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int small = Math.min(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(small, small);
    }

}
