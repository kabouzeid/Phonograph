package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * @author Lincoln (theduffmaster)
 *
 * A custom HorizontalScrollView that is only useful as the child of a TouchInterceptFrameLayout.
 * This allows for the TouchInterceptFrameLayout to disable and enable scrolling in addition to
 * being able to know when a user is and is not interacting with the scrolling view.
 *
 * Must have a TouchInterceptTextView as it's child. It can only have one child.
 */
public class TouchInterceptHorizontalScrollView extends HorizontalScrollView {

    public static final String TAG = TouchInterceptHorizontalScrollView.class.getSimpleName();

    // The delay before triggering onEndScroll()
    public static final int ON_END_SCROLL_DELAY = 1000;
    private static final int MAX_CLICK_DISTANCE = 5;

    private float startX;
    private Rect scrollViewRect = new Rect();

    private long lastScrollUpdate = -1;

    private boolean isFling;

    // Whether user is interacting with this again and to cancel text retruncate
    private boolean cancel;
    private boolean cancelCheck;

    // Whether to untruncate the text in the TouchInterceptTextView
    private boolean unTruncate;

    // Whether this was touched
    private boolean touched;

    private OnEndScrollListener onEndScrollListener;

    private SlidingUpPanelLayout slidingPanel;

    private boolean mScrollable = true;

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
        setTag(TouchInterceptHorizontalScrollView.TAG);
        setHorizontalScrollBarEnabled(false);
    }

    public TouchInterceptFrameLayout getTouchInterceptFrameLayout() {
        return (TouchInterceptFrameLayout) getRootView().findViewWithTag(TouchInterceptFrameLayout.TAG);
    }

    /**
     * @return Returns the child TouchInterceptTextView
     */
    public TouchInterceptTextView getTouchInterceptTextView() {
        return (TouchInterceptTextView) this.getChildAt(0);
    }

    /**
     * Disables and enables the ScrollView
     *
     * @param scrollable set to "true" to enable, "false" to disable
     */
    public void setScrollable(boolean scrollable) {
        mScrollable = scrollable;
    }

    /**
     * Returns whether the ScrollView is enabled or disabled
     *
     * @return Returns "true" if enabled, "false" if disabled
     */
    public boolean isScrollable() {
        return mScrollable;
    }

    /**
     * @return Returns true if this ScrollView can be scrolled
     */
    public boolean canScroll() {
        return (canScrollHorizontally(1) || canScrollHorizontally(-1));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touched = true;
                cancel = true;
                startX = e.getX();

                // If we can scroll pass the event to the superclass
                if (mScrollable) {
                    return super.onTouchEvent(e);
                }

                // Only continue to handle the touch event if scrolling enabled
                return false;

            case MotionEvent.ACTION_MOVE:
                float distance = Math.abs(e.getX() - startX);

                // Currently scrolling so untruncate text
                if (unTruncate && distance > MAX_CLICK_DISTANCE) {
                    getTouchInterceptTextView().unTruncateText();
                    unTruncate = false;
                }

            case MotionEvent.ACTION_UP:
                touched = false;
                // The user is done interacting with the scroll view
                cancel = false;
                postDelayed(new ScrollStateHandler(), ON_END_SCROLL_DELAY);
                lastScrollUpdate = System.currentTimeMillis();
                unTruncate = true;

            default:
                return super.onTouchEvent(e);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {

        int x = Math.round(e.getRawX());
        int y = Math.round(e.getRawY());

        scrollViewRect = new Rect();

        getGlobalVisibleRect(scrollViewRect);

        boolean touchedScrollView =
                x > scrollViewRect.left && x < scrollViewRect.right &&
                y > scrollViewRect.top && y < scrollViewRect.bottom;

        if (!touchedScrollView) {
            return false;
        }

        // Don't do anything with intercepted touch events if not scrollable
        if (!mScrollable) {
            onTouchEvent(e);
            return false;
        }

        return super.onInterceptTouchEvent(e);
    }

    /**
     * @return Returns the set OnEndScrollListener
     */
    public OnEndScrollListener getOnEndScrollListener() {
        return onEndScrollListener;
    }

    /**
     * Sets an OnEndScrollListener. Only one can be set at a time.
     *
     * @param mOnEndScrollListener The OnEndScrollListener to be set
     */
    public void setOnEndScrollListener(OnEndScrollListener mOnEndScrollListener) {
        this.onEndScrollListener = mOnEndScrollListener;
    }

    @Override
    public void fling(int velocityX) {
        super.fling(velocityX);
        isFling = true;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        CancelClick();

        if (cancelCheck) {
            cancel = true;
        }

        if (isFling && (Math.abs(x - oldX) < 2 || x >= getMeasuredWidth() || x == 0)) {
            touched = false;
            // The user is done interacting with the scroll view
            cancel = false;
            postDelayed(new ScrollStateHandler(), ON_END_SCROLL_DELAY);
            lastScrollUpdate = System.currentTimeMillis();
            isFling = false;
            cancelCheck = false;
            unTruncate = true;
        }
    }

    /**
     * Cancels any Long Presses and inpending clicks. Used to prevent views from
     * stealing touches while the user is scrolling something.
     */
    public void CancelClick() {
        getRootView().cancelLongPress();
        this.cancelLongPress();
    }

    /**
     * Listens for when a user has stopped interacting with the scroll view
     */
    public interface OnEndScrollListener {
        // Triggered when a user has stopped interacting with the scroll view
        void onEndScroll();
    }

    private class ScrollStateHandler implements Runnable {
        // Runs when the user has not touched the scroll view for 1 second
        @Override
        public void run() {
            if (!cancel) {
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