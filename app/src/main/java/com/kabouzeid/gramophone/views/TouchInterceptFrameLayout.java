package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.kabouzeid.gramophone.R;

/**
 * @author Lincoln (theduffmaster)
 * 
 * A custom FrameLayout view that intercepts touch events and decides whether to consume them or
 * pass on the touch events to a TouchInterceptHorizontalScrollview which contains a TextView.
 * In order for this to work properly the child views,
 * a TouchInterceptHorizontalScrollView and a TextView, must be set via xml using the
 * setTouchInterceptHorizontalScrollView and setScrollableTextView xml attributes. If this view is ever
 * scrolled or interacts with a ListParent that is a ListView or a RecyclerView, then that ListParent
 * must be set programmatically or via the designated XML attribute.
 */
public class TouchInterceptFrameLayout extends FrameLayout {

    private static final int MAX_CLICK_DISTANCE = 5;
    private static final int RETRUNCATE_DELAY = 600;

    private static final String TAG = "E/TouchInterceptFL";
    private static final String XML_VIEW_IDS_NOT_SET = "It appears as if the IDs for the TouchInterceptHorizontalScrollView and its" +
            "child scrollable TextView have not been set. If you have not already, you must set "  +
            "them using setTouchInterceptHorizontalScrollView and setScrollableTextView via XML";
    private static final String NULL_VIEWS_EXCEPTION_MESSAGE = "Either textView or scrollView is null. Maybe you " +
            "forgot to set them using setTouchInterceptHorizontalScrollView and setScrollableTextView " +
            "via XML? Did you set it to something null?";
    private static final String NULL_LIST_PARENT = "The ListParent, aka the parent ListView or RecyclerView is null." +
            "It is highly reccomended you set the ListParent either programmatically or via XML" +
            "if you're TouchInterceptFrameLayout is associated with any type of ListParent. If your" +
            "TouchInterceptFrameLayout does not interact with any type of ListParent no need to set it" +
            "and ignore this message.";
    private static final String EMPTY_TRUNCATE_STRING = "songTruncated is empty or null. Did you remember " +
            "to set the song string when setting the song name in your text view?";

    private int listParentID;

    private TouchInterceptHorizontalScrollView scrollView;
    private View listParent;

    private Rect scrollViewRect = new Rect();
    private float startX;

    private boolean isTap;

    public TouchInterceptFrameLayout(@NonNull Context context) {
        this(context, null);
        setTag("TIFL");
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        setTag("TIFL");
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTag("TIFL");

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TouchInterceptFrameLayout, defStyleAttr, 0);

        listParentID = a.getResourceId(R.styleable.TouchInterceptFrameLayout_setListParent, 0);

        this.post(new Runnable() {
            @Override
            public void run() {
                View lP = findViewById(listParentID);
                if (lP != null) listParent = lP;
            }
        });

    }

    /**
     * Sets the List Parent programmatically
     * @param lP Must be either a type of RecyclerView or a type of ListView
     */
    public void setListParent(View lP){
        this.listParent = lP;
    }

    public View getListParent(){
        if(listParent == null)
            return findViewById(listParentID);
        else return listParent;
    }

    public TouchInterceptHorizontalScrollView getTouchInterceptHorizontalScrollView() {
        return (TouchInterceptHorizontalScrollView) findViewWithTag("TIHS");
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

        scrollView = (TouchInterceptHorizontalScrollView) findViewWithTag("TIHS");

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
