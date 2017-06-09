package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
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
public class TouchInterceptFrameLayout extends FrameLayout {

    private Context c;
    private HorizontalScrollView scrollView;
    private static final int MAX_CLICK_DURATION = 200;

    public TouchInterceptFrameLayout (@NonNull Context context) {
        super(context);
        c = context;
    }

    public TouchInterceptFrameLayout (@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        c = context;
    }

    public TouchInterceptFrameLayout (@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        c = context;
    }

    public void setScrollView(HorizontalScrollView view){
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
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        Log.d("Touch Event Intercepted",e.toString());
        int x = Math.round(e.getRawX());
        int y = Math.round(e.getRawY());
        Rect scrollViewLocation = new Rect();
        scrollView.getGlobalVisibleRect(scrollViewLocation);
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                Log.d("ACTION_DOWN?","True");
                Log.d("Event X",Integer.toString(x));
                Log.d("Event Y",Integer.toString(y));
                Log.d("View Left",Integer.toString(scrollViewLocation.left));
                Log.d("View Right",Integer.toString(scrollViewLocation.right));
                Log.d("View Top",Integer.toString(scrollViewLocation.top));
                Log.d("View Bottom",Integer.toString(scrollViewLocation.bottom));
                if (!(x > scrollViewLocation.left && x < scrollViewLocation.right
                        && y > scrollViewLocation.top && y < scrollViewLocation.bottom)) {
                    Log.d("Outside Scrollview","True");
                        return false;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
//                long clickDuration = e.getEventTime() - e.getDownTime();
//                if(clickDuration < MAX_CLICK_DURATION) {
//                    if ((x > scrollViewLocation.left && x < scrollViewLocation.right
//                            && y > scrollViewLocation.top && y < scrollViewLocation.bottom)) {
//                        Log.d("Outside Scrollview","True");
//
//                        return false;
//                    }
//                }
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.d("ACTION MOVE","True");
                if ((x > scrollViewLocation.left && x < scrollViewLocation.right
                        && y > scrollViewLocation.top && y < scrollViewLocation.bottom)) {
                    Log.d("Outside Scrollview","True");

                    return false;
                }
                MotionEvent eCancel = e;
                eCancel.setAction(MotionEvent.ACTION_CANCEL);
                onTouchEvent(eCancel);
            }
        }
        Log.d("InterceptTouch Finished","True");
        onTouchEvent(e);
        return false;
    }
}
