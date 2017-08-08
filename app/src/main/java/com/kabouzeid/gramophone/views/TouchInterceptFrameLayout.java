package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * @author Lincoln (theduffmaster)
 * 
 * A custom {@link FrameLayout} that intercepts touch events and decides whether to consume them or
 * pass them on to a child {@link TouchInterceptHorizontalScrollView} and its
 * {@link AutoTruncateTextView}.
 *
 * This only needs to be used if the layout containing the {@link TouchInterceptHorizontalScrollView}
 * is clickable.
 */
public class TouchInterceptFrameLayout extends FrameLayout {

    public static final String TAG = TouchInterceptFrameLayout.class.getSimpleName();

    private static final int MAX_CLICK_DISTANCE = 5;

    private TouchInterceptHorizontalScrollView scrollView;

    private Rect scrollViewRect;
    private float startX;
    private boolean isTap;

    public TouchInterceptFrameLayout(@NonNull Context context) {
        this(context, null);
        init();
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scrollViewRect = new Rect();
        setTag(TouchInterceptFrameLayout.TAG);
    }

    /**
     * @return Returns the child {@link TouchInterceptHorizontalScrollView}.
     */
    public TouchInterceptHorizontalScrollView getTouchInterceptHorizontalScrollView() {
        return (TouchInterceptHorizontalScrollView) findViewWithTag(TouchInterceptHorizontalScrollView.TAG);
    }

    /**
     * Intercepts touch events to selectively pass the event on to its child view.
     * It also detects where the touch was placed so that if the touch is not in the scrollview, the
     * touch is not passed to it, avoiding the child view swallowing up the long press. ACTION_MOVE
     * actions are cancelled here and instead passed to the child view.
     *
     * @param e The intercepted touch event.
     * @return True if the MotionEvent will be intercepted (i.e. it will not be passed on to its
     *         child, but rather to the onTouchEvent method of this view).
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int x = Math.round(e.getRawX());
        int y = Math.round(e.getRawY());

        scrollView = getTouchInterceptHorizontalScrollView();
        scrollView.getGlobalVisibleRect(scrollViewRect);

        boolean touchedScrollView =
                x > scrollViewRect.left && x < scrollViewRect.right &&
                y > scrollViewRect.top && y < scrollViewRect.bottom;

        if (scrollView.isScrollable()) {
            switch (e.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (!touchedScrollView) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            scrollView.cancelPendingInputEvents();
                        } else {
                            scrollView.cancelLongPress();
                        }
                        return false;
                    }

                    startX = e.getX();
                    isTap = true;
                    onTouchEvent(e);
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (touchedScrollView) {
                        float distance = Math.abs(e.getX() - startX);

                        // Scrolling the view: cancel event to prevent long press
                        if (distance > MAX_CLICK_DISTANCE) {
                            isTap = false;
                            this.cancelLongPress();
                        }
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (touchedScrollView && isTap) {
                        onTouchEvent(e);
                    }
                    this.requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        }

        if (touchedScrollView) {
            onTouchEvent(e);
        }
        return false;
    }
}
