package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class WidthFitSquareImageView extends ImageView {

    public WidthFitSquareImageView(Context context) {
        super(context);
    }

    public WidthFitSquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidthFitSquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //noinspection SuspiciousNameCombination
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
