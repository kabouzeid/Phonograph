package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * @author Lincoln (theduffmaster)
 *
 * A custom {@link HorizontalScrollView} that is only useful as the child of a
 * {@link TouchInterceptFrameLayout}. Allows for the layout to disable and enable scrolling in
 * addition to being able to know when a user is and is not interacting with the scrolling view.
 *
 * Must have a {@link AutoTruncateTextView} as its only child.
 */
public class TouchInterceptHorizontalScrollView extends HorizontalScrollView {

    public static final String TAG = TouchInterceptHorizontalScrollView.class.getSimpleName();

    /** Delay before triggering {@link OnEndScrollListener#onEndScroll} */
    private static final int ON_END_SCROLL_DELAY = 1000;

    private static final int MAX_CLICK_DISTANCE = 5;

    private float startX;
    private long lastScrollUpdate = -1;
    private boolean scrollable;
    private boolean isFling;
    private Rect scrollViewRect;
    private OnEndScrollListener onEndScrollListener;

    // Whether user is interacting with this again and to cancel text retruncate
    private boolean cancel;
    private boolean cancelCheck;

    // ID of the active pointer
    private int activePointerId;

    // Whether to untruncate the text in the TouchInterceptTextView
    private boolean untruncate;

    public TouchInterceptHorizontalScrollView(Context context) {
        super(context);
        init();
    }

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        lastScrollUpdate = -1;
        scrollable = true;
        scrollViewRect = new Rect();
        setLongClickable(false);
        setTag(TouchInterceptHorizontalScrollView.TAG);
        setHorizontalScrollBarEnabled(false);
    }

    public TouchInterceptFrameLayout getTouchInterceptFrameLayout() {
        return (TouchInterceptFrameLayout) getRootView().findViewWithTag(TouchInterceptFrameLayout.TAG);
    }

    /**
     * @return Returns the child {@link AutoTruncateTextView}.
     */
    public AutoTruncateTextView getTouchInterceptTextView() {
        return (AutoTruncateTextView) this.getChildAt(0);
    }

    /**
     * @return Returns the set {@link OnEndScrollListener}.
     */
    public OnEndScrollListener getOnEndScrollListener() {
        return onEndScrollListener;
    }

    /**
     * Disables and enables scrolling.
     *
     * @param scrollable Whether the view should be scrollable.
     */
    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    /**
     * Returns whether the view is scrollable.
     *
     * @return Whether the view is scrollable.
     */
    public boolean isScrollable() {
        return scrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancel = true;
                startX = e.getX();

                // If we can scroll, pass the event to the superclass
                if (scrollable) {
                    return super.onTouchEvent(e);
                }

                // Don't continue to handle the touch event if scrolling is disabled
                return false;

            case MotionEvent.ACTION_MOVE:
                float distance = Math.abs(e.getX() - startX);

                // Currently scrolling, so untruncate text
                if (untruncate && distance > MAX_CLICK_DISTANCE) {
                    getTouchInterceptTextView().untruncateText();
                    untruncate = false;
                }

            case MotionEvent.ACTION_UP:
                // User is done interacting with the scroll view
                cancel = false;
                postDelayed(new ScrollStateHandler(), ON_END_SCROLL_DELAY);
                lastScrollUpdate = System.currentTimeMillis();
                untruncate = true;

            default:
                return super.onTouchEvent(e);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        int x = Math.round(e.getRawX());
        int y = Math.round(e.getRawY());

        // Check to see if it's a valid pointerID.
        // If it's invalid, a long click is triggered. This stops that.
        switch (e.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = e.getPointerId(0);
                break;

            case MotionEvent.ACTION_MOVE:
                if (e.findPointerIndex(activePointerId) == -1) {
                    cancelLongClick();
                }
                break;
        }

        getGlobalVisibleRect(scrollViewRect);

        boolean touchedScrollView =
                x > scrollViewRect.left && x < scrollViewRect.right &&
                y > scrollViewRect.top && y < scrollViewRect.bottom;

        if (!touchedScrollView) {
            return false;
        }

        // Don't do anything with intercepted touch events if not scrollable
        if (!scrollable) {
            onTouchEvent(e);
            return false;
        }

        return super.onInterceptTouchEvent(e);
    }

    /**
     * Sets the {@link OnEndScrollListener}.
     *
     * @param onEndScrollListener The listener to be set.
     */
    public void setOnEndScrollListener(OnEndScrollListener onEndScrollListener) {
        this.onEndScrollListener = onEndScrollListener;
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);
        isFling = true;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        cancelLongClick();

        if (cancelCheck) {
            cancel = true;
        }

        if (isFling && (Math.abs(x - oldX) < 2 || x >= getMeasuredWidth() || x == 0)) {
            // User is done interacting with the scroll view
            cancel = false;
            postDelayed(new ScrollStateHandler(), ON_END_SCROLL_DELAY);
            lastScrollUpdate = System.currentTimeMillis();
            isFling = false;
            cancelCheck = false;
            untruncate = true;
        }
    }

    /**
     * Cancels any long presses. Used to prevent views from stealing touches while the user is
     * scrolling something.
     */
    private void cancelLongClick() {
        getRootView().cancelLongPress();
        this.cancelLongPress();
    }

    interface OnEndScrollListener {
        /**
         * Triggered when a user has stopped interacting with the
         * {@link TouchInterceptHorizontalScrollView}.
         */
        void onEndScroll();
    }

    private class ScrollStateHandler implements Runnable {
        @Override
        public void run() {
            if (!cancel) {
                // Hasn't been touched for some time
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastScrollUpdate) > ON_END_SCROLL_DELAY) {
                    lastScrollUpdate = -1;
                    if (onEndScrollListener != null) {
                        cancelCheck = true;
                        onEndScrollListener.onEndScroll();
                    }
                } else {
                    postDelayed(this, ON_END_SCROLL_DELAY);
                }
            }
        }
    }
}
