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
 * @author Lincoln (theduffmaster)
 * 
 * A custom FrameLayout view that intercepts touch events and decides whether to consume them or
 * pass on the touch events to its children.
 */
public class TouchInterceptFrameLayout extends FrameLayout {

    private static final int MAX_CLICK_DISTANCE = 10;

    private HorizontalScrollView mScrollView;
    private Rect scrollViewRect = new Rect();
    private float startX;
    private boolean isTap;

    public TouchInterceptFrameLayout(@NonNull Context context) {
        super(context);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollView(HorizontalScrollView view) {
        mScrollView = view;
    }

    /**
     * This intercepts the touch event and, by returning false and onTouchEvent(), passes the touchevent
     * to both itself and its child views (by calling TouchEvent it passes it to itself).
     * It also detects where the touch was placed so that if the touch is not in the scrollview, the
     * touch is not passed to the HorizontalScrollView, avoiding the child view swallowing up the long
     * click. False is passed to still allow MenuItemClick to happen.
     * However, if the action is ACTION_MOVE, it cancels the touch event in itself and
     * only gives it to its children, which, in this case is a HorizontalScrollView.
     *
     * @param e the intercepted touch event
     * @return If this function returns true, the MotionEvent will be intercepted,
     * meaning it will be not be passed on to the child, but rather to the onTouchEvent of this View.
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int x = Math.round(e.getRawX());
        int y = Math.round(e.getRawY());
        mScrollView.getGlobalVisibleRect(scrollViewRect);

        boolean touchedScrollView =
                x > scrollViewRect.left && x < scrollViewRect.right &&
                        y > scrollViewRect.top && y < scrollViewRect.bottom;

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!touchedScrollView) {
                    return false;
                }
                startX = e.getX();
                onTouchEvent(e);
                isTap = true;

                break;

            case MotionEvent.ACTION_MOVE:
                if (touchedScrollView) {
                    float distance = Math.abs(e.getX() - startX);

                    // Scrolling the view: cancel event to prevent long press
                    if (distance > MAX_CLICK_DISTANCE) {
                        MotionEvent eCancel = e;
                        eCancel.setAction(MotionEvent.ACTION_CANCEL);
                        onTouchEvent(eCancel);
                        isTap = false;
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if (touchedScrollView && isTap) {
                    onTouchEvent(e);
                }
                break;
        }

        return false;
    }
}
