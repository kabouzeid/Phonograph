package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

/**
 * Created by lincoln on 7/3/17.
 */

/**
 * A custom HorizontalScrollView that is only useful as the child of a TouchInterceptFrameLayout.
 * This allows for the TouchInterceptFrameLayout to disable and enable scrolling in addition to
 * being able to know when a user is and is not interacting with the scrolling view.
 */
public class TouchInterceptHorizontalScrollView extends HorizontalScrollView {

    /**
     * Listens for when a user has stopped interacting with the scroll view
     */
    public interface OnEndScrollListener {
        // Triggered when a user has stopped interacting with the scroll view
        void onEndScroll();
    }

    private long lastScrollUpdate = -1;

    private boolean cancel;

    //The delay before triggering onEndScroll()
    private int delay = 1000;

    private class ScrollStateHandler implements Runnable {
        //Runs when the user has not touched the scroll view for 1 second
        @Override
        public void run() {
            if(!cancel) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastScrollUpdate) > delay) {
                    lastScrollUpdate = -1;
                    if (onEndScrollListener != null) {
                        onEndScrollListener.onEndScroll();
                    }
                } else {
                    postDelayed(this, delay);
                }
            }
        }
    }

    private OnEndScrollListener onEndScrollListener;


    public TouchInterceptHorizontalScrollView(Context context) {
        super(context);
    }

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // "true" if we can scroll (not locked)
    // "false" if we cannot scroll (locked)
    private boolean mScrollable = true;

    /**
     * Disables and enables the ScrollView
     * @param enabled set to "true" to enable, "false" to disable
     */
    public void setScrollingEnabled(boolean enabled) {
        mScrollable = enabled;
    }

    /**
     * Returns whether the ScrollView is enabled or disabled
     * @return Returns "true" if enabled, "false" if disabled
     */
    public boolean isScrollable() {
        return mScrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cancel = true;
                // If we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(ev);
                // Only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point
            case MotionEvent.ACTION_UP:
                cancel = false;
                // The user is done interacting with the scroll view
                postDelayed(new ScrollStateHandler(), delay);
                lastScrollUpdate = System.currentTimeMillis();
            default:
                return super.onTouchEvent(ev);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't do anything with intercepted touch events if
        // we are not scrollable
        if (!mScrollable) return false;
        else return super.onInterceptTouchEvent(ev);
    }

    /**
     * Fetches the OnEndScrollListener
     * @return Returns the OnEndScrollListener
     */
    public OnEndScrollListener getOnEndScrollListener() {
        return onEndScrollListener;
    }

    /**
     * Sets an OnEndScrollListener. Only one can be set at a time.
     * @param mOnEndScrollListener The OnEndScrollListener to be set
     */
    public void setOnEndScrollListener(OnEndScrollListener mOnEndScrollListener) {
        this.onEndScrollListener = mOnEndScrollListener;
    }


}
