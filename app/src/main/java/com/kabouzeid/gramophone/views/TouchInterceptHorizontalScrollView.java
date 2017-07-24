package com.kabouzeid.gramophone.views;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.kabouzeid.gramophone.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

/**
 * Created by lincoln on 7/3/17.
 */

/**
 * A custom HorizontalScrollView that is only useful as the child of a TouchInterceptFrameLayout.
 * This allows for the TouchInterceptFrameLayout to disable and enable scrolling in addition to
 * being able to know when a user is and is not interacting with the scrolling view.
 */
public class TouchInterceptHorizontalScrollView extends HorizontalScrollView {

    //The delay before triggering onEndScroll()
    public static final int ON_END_SCROLL_DELAY = 1000;

    private long lastScrollUpdate = -1;

    private boolean mIsFling;

    private boolean cancel;
    private boolean cancelCheck;
    private boolean unTruncate;
    private boolean touched;

    private OnEndScrollListener onEndScrollListener;

    private SlidingUpPanelLayout queue;

    /**
     * Listens for when a user has stopped interacting with the scroll view
     */
    public interface OnEndScrollListener {
        // Triggered when a user has stopped interacting with the scroll view
        void onEndScroll();
    }

    private class ScrollStateHandler implements Runnable {
        //Runs when the user has not touched the scroll view for 1 second
        @Override
        public void run() {
            if(!cancel) {
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

    public TouchInterceptHorizontalScrollView(Context context) {
        super(context);
        setTag("TIHS");
        setHorizontalScrollBarEnabled(false);
    }

    public TouchInterceptHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTag("TIHS");
        setHorizontalScrollBarEnabled(false);
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

    /**
     * @return Returns true if this ScrollView can be scrolled
     */
    public boolean canScroll() {
        if(canScrollHorizontally(1) || canScrollHorizontally(-1)){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getAction()) {

            case MotionEvent.ACTION_DOWN:
                touched = true;
                cancel = true;
                // If we can scroll pass the event to the superclass
                if (mScrollable) return super.onTouchEvent(e);
                // Only continue to handle the touch event if scrolling enabled
                return mScrollable; // mScrollable is always false at this point

            case MotionEvent.ACTION_MOVE:
                if(unTruncate = true) {
                    getTouchInterceptTextView().unTruncateText();
                    unTruncate = false;
                }

            case MotionEvent.ACTION_UP:
                slidingPanelSetTouchEnabled(true);
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


        if(e.getAction() == MotionEvent.ACTION_DOWN) slidingPanelSetTouchEnabled(true);


        int x = Math.round(e.getRawX());
        int y = Math.round(e.getRawY());

        Rect scrollViewRect = new Rect();

        getGlobalVisibleRect(scrollViewRect);

        boolean touchedScrollView =
                x > scrollViewRect.left && x < scrollViewRect.right &&
                        y > scrollViewRect.top && y < scrollViewRect.bottom;

        if(!touchedScrollView){
            return false;
        }

        // Don't do anything with intercepted touch events if
        // not scrollable
        if(!mScrollable){
            onTouchEvent(e);
            return false;
        }
        else
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
     * @param mOnEndScrollListener The OnEndScrollListener to be set
     */
    public void setOnEndScrollListener(OnEndScrollListener mOnEndScrollListener) {
        this.onEndScrollListener = mOnEndScrollListener;
    }

    @Override
    public void fling(int velocityY) {
        super.fling(velocityY);
        mIsFling = true;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        if(touched | mIsFling) slidingPanelSetTouchEnabled(false);
        CancelClick();

        if(cancelCheck) cancel = true;

        if (mIsFling) {
            if (Math.abs(x - oldX) < 2 || x >= getMeasuredWidth() || x == 0) {
                slidingPanelSetTouchEnabled(true);
                touched = false;
                // The user is done interacting with the scroll view
                cancel = false;
                postDelayed(new ScrollStateHandler(), ON_END_SCROLL_DELAY);
                lastScrollUpdate = System.currentTimeMillis();
                mIsFling = false;
                cancelCheck = false;
                unTruncate = true;
            }
        }
    }

    /**
     * Enables and disables Sliding Panel dragging for the playing queue sliding panel
     * @param enable Set true to enable dragging, false to disable
     */
    public void slidingPanelSetTouchEnabled(boolean enable){
        queue = (SlidingUpPanelLayout) ((Activity)getContext()).findViewById(R.id.player_sliding_layout);
        if(queue != null) queue.setTouchEnabled(enable);
    }

    /**
     * Cancels any Long Presses and inpending clicks. Used to prevent views from
     * stealing touches while the user is scrolling something.
     */
    public void CancelClick(){
        getRootView().cancelLongPress();
        getRootView().cancelPendingInputEvents();
        this.cancelLongPress();
        this.cancelPendingInputEvents();
    }

     public TouchInterceptFrameLayout getTouchInterceptFrameLayout() {
        return (TouchInterceptFrameLayout) getRootView().findViewWithTag("TIFL");
    }

    public TouchInterceptTextView getTouchInterceptTextView(){
        return (TouchInterceptTextView) ((ViewGroup)this).getChildAt(0);
    }
}