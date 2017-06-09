package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

/**
 * Created by lincoln on 6/8/17.
 */

/**
 * A custom FrameLayout view that intercepts touch events and decides whether to consume them or
 * pass on the touch events to it's children
 */
public class CustomFrameLayout extends FrameLayout {

    private HorizontalScrollView scrollView;

    public CustomFrameLayout(@NonNull Context context) {
        super(context);
    }

    public CustomFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void getScrollView(HorizontalScrollView view){
        scrollView = view;
    }

    /**
     * This intercepts the touch event and, by returning false and onTouchEvent(), passes the touchevent to both itself and it's
     * child views (by calling TouchEvent it passes it to itself). It also detects where the touch was placed
     * so that if the touch is not in the scrollview the touch is not passed to the HorizontalScrollView, avoiding
     * the child view swallowing up the long click. False is passed to still allow MenuItemClick to happen.
     * However, if the action is ACTION_MOVE it cancels the touch event in itself and
     * only gives it to it's children, which, in this case is a HorizontalScrollView.
     * @param e the intercepted touch event
     * @return If this function returns true, the MotionEvent will be intercepted,
     * meaning it will be not be passed on to the child, but rather to the onTouchEvent of this View.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                int x = Math.round(e.getRawX());
                int y = Math.round(e.getRawY());
                Rect scrollViewLocation = new Rect();
                scrollView.getGlobalVisibleRect(scrollViewLocation);
                if (!(x > scrollViewLocation.left && x < scrollViewLocation.right
                        && y > scrollViewLocation.top && y < scrollViewLocation.bottom)) {
                        return false;
                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                MotionEvent eUp = e;
                eUp.setAction(MotionEvent.ACTION_CANCEL);
                onTouchEvent(eUp);
            }
        }
        onTouchEvent(e);
        return false;
    }
}
