package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * @author Lincoln (theduffmaster)
 * 
 * A custom FrameLayout view that intercepts touch events and decides whether to consume them or
 * pass on the touch events to a TouchInterceptHorizontalScrollview which contains a TouchInterceptTextView.
 * This only needs to be used if the layout that the TouchHorizontalScrollView and the TouchInterceptTextView
 * are in is clickable in any way.
 */
public class TouchInterceptFrameLayout extends FrameLayout {

    private static final int MAX_CLICK_DISTANCE = 5;

    //Tag used so other views can find this one
    private static final String touchInterceptFrameLayoutViewTag = "TIFL";

    private static final String touchInterceptHorizontalScrollViewTag = "TIHS";

    private static final String TAG = "E/TouchInterceptFL";
    private static final String NULL_VIEWS_EXCEPTION_MESSAGE = "Either textView or scrollView is null. Maybe you " +
            "forgot to set them using setTouchInterceptHorizontalScrollView and setScrollableTextView " +
            "via XML? Did you set it to something null?";

    private TouchInterceptHorizontalScrollView scrollView;

    private Rect scrollViewRect = new Rect();
    private float startX;

    private boolean isTap;

    public TouchInterceptFrameLayout(@NonNull Context context) {
        this(context, null);
        setTag(touchInterceptFrameLayoutViewTag);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        setTag(touchInterceptFrameLayoutViewTag);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    /**
     * @return Returns the TouchInterceptHorizontalScrollview in this layout
     */
    public TouchInterceptHorizontalScrollView getTouchInterceptHorizontalScrollView() {
        return (TouchInterceptHorizontalScrollView) findViewWithTag(touchInterceptHorizontalScrollViewTag);
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

        scrollView = getTouchInterceptHorizontalScrollView();

            try {

                scrollView.getGlobalVisibleRect(scrollViewRect);

                boolean touchedScrollView =
                        x > scrollViewRect.left && x < scrollViewRect.right &&
                                y > scrollViewRect.top && y < scrollViewRect.bottom;

            if(scrollView.isScrollable()) {

                switch (e.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        scrollView.slidingPanelSetTouchEnabled(true);
                        if (!touchedScrollView) {
                            scrollView.cancelPendingInputEvents();
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
                                CancelClick();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        scrollView.slidingPanelSetTouchEnabled(true);
                        if (touchedScrollView) {
                            if (isTap) onTouchEvent(e);
                        }
                        this.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;

            }else{
                if(touchedScrollView) onTouchEvent(e);
                return false;
            }

            } catch (NullPointerException exception) {
                Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
                Log.e("Method: ","onInterceptTouchEvent()");
                System.out.println(TAG + " TouchInterceptHorizontalScrollView = " + findViewWithTag("TIHS").toString());
                System.out.println(TAG + " TouchInterceptTextView = " + findViewWithTag("TITV").toString());
                Log.e(TAG, exception.toString());
                onTouchEvent(e);
                return false;
            }
    }

    /**
     *Cancels any Long Presses and inpending clicks. Used to prevent views from
     * stealing touches while the user is scrolling something.
     */
    private void CancelClick(){
        this.cancelPendingInputEvents();
        this.cancelLongPress();
        scrollView.cancelLongPress();
        scrollView.cancelPendingInputEvents();
    }
}
