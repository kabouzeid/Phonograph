package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by lincoln on 7/3/17.
 */

public class TouchInterceptHorizontalScrollView extends HorizontalScrollView {
    public TouchInterceptHorizontalScrollView(Context context) {
        super(context);
    }

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return false;
    }
}
