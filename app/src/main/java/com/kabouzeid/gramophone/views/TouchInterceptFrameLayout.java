package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;

/**
 * @author Lincoln (theduffmaster)
 * 
 * A custom FrameLayout view that intercepts touch events and decides whether to consume them or
 * pass on the touch events to its children.
 */
public class TouchInterceptFrameLayout extends FrameLayout {

    private static final int MAX_CLICK_DISTANCE = 5;
    private static final int MAX_VERTICAL_DISTANCE = 5;

    private Context c;

    private String TAG;
    private String NULL_VIEWS_EXCEPTION_MESSAGE;
    
    private HorizontalScrollView scrollView;
    private TextView textView;
    private Rect scrollViewRect = new Rect();
    private float startX;
    private float startY;
    private boolean isTap;
    private String songTruncated;
    private String song;

    public TouchInterceptFrameLayout(@NonNull Context context) {
        super(context);
        c= context;
        TAG = c.getResources().getString(R.string.TOUCH_INTERCEPT_FRAME_LAYOUT_TAG);
        NULL_VIEWS_EXCEPTION_MESSAGE = c.getResources().getString(R.string.NULL_VIEWS_EXCEPTION_MESSAGE);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        c= context;
        TAG = c.getResources().getString(R.string.TOUCH_INTERCEPT_FRAME_LAYOUT_TAG);
        NULL_VIEWS_EXCEPTION_MESSAGE = c.getResources().getString(R.string.NULL_VIEWS_EXCEPTION_MESSAGE);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        c= context;
        TAG = c.getResources().getString(R.string.TOUCH_INTERCEPT_FRAME_LAYOUT_TAG);
        NULL_VIEWS_EXCEPTION_MESSAGE = c.getResources().getString(R.string.NULL_VIEWS_EXCEPTION_MESSAGE);
    }

    /**
     * Fetches the HorizontalScrollView contained by this FrameLayout and also gets the
     * TextView that is contained within that HorizontalScrollView.
     * Must be called before setTruncateText.
     * Call this when you are assigning views in your layout.
     * @param sv The HorizontalScrollView containing text that needs to be scrolled
     * @param tv The TextView that needs to be scrolled (typically song or album title)
     */
    public void setScrollViews(HorizontalScrollView sv, TextView tv) {
        scrollView = sv;
        textView = tv;
    }

    /**
     * Does exactly what android:ellipsize="end" does, except this works in HorizontalScrollViews.
     * Truncates the string so it doesn't get cuttoff in the HorizontalScrollView
     * and puts an ellipsis at the end of it. Then it sets the TextView with the new Ellipsized value.
     * Must be called after setScrollViews or it will throw a NullPointerException.
     * Call this when setting the song title during view creation.
     *
     * If this is never called then the text will never be truncated and will remain
     * cut off, still allowing the HorizontalScrollingView to scroll.
     * @param s The string (song title or album title typically) contained by the text view.
     */
    public void setTruncateText(String s){
        song = s;
        try {
            //runs after scrollview has been drawn
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    String sT = TextUtils.ellipsize(song,
                            textView.getPaint(),
                            (float) scrollView.getWidth(),
                            TextUtils.TruncateAt.END).toString();
                    songTruncated = sT;
                    if (!sT.isEmpty()) {
                        textView.setText(sT);
                    }
                }
            });
        }catch (NullPointerException exception){
            Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
            Log.e(TAG, exception.toString());
            }
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
        try {
            scrollView.getGlobalVisibleRect(scrollViewRect);

            boolean touchedScrollView =
                    x > scrollViewRect.left && x < scrollViewRect.right &&
                            y > scrollViewRect.top && y < scrollViewRect.bottom;

            boolean emptyTruncateText = songTruncated.isEmpty();
            boolean isTextTruncated = songTruncated.endsWith("…");

            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!touchedScrollView || (!emptyTruncateText && !isTextTruncated)){
                        scrollView.cancelPendingInputEvents();
                        return false;
                    }

                    startX = e.getX();
                    startY = e.getY();
                    isTap = true;
                    onTouchEvent(e);

                    break;

                case MotionEvent.ACTION_MOVE:
                    float distanceY = Math.abs(e.getY() - startY);
                    if (touchedScrollView) {
                        float distance = Math.abs(e.getX() - startX);

                        // Scrolling the view: cancel event to prevent long press
                        if (distance > MAX_CLICK_DISTANCE) {
                            if (!emptyTruncateText){
                                textView.setText(song);
                            }else{
                                Log.e("E/TouchInterceptFL","songTruncated is empty or null. Did you remember " +
                                        "to set the song string when setting the song name in your text view?");
                            }
                            cancelPendingInputEvents();
                            cancelLongPress();
                            scrollView.cancelLongPress();
                            scrollView.cancelPendingInputEvents();
                            isTap = false;
                        }
                    }
                    // Scrolling vertically: cancel horizontal scrolling events
                    if (distanceY > MAX_VERTICAL_DISTANCE) {
                        cancelLongPress();
                        cancelPendingInputEvents();
                        scrollView.cancelLongPress();
                        scrollView.cancelPendingInputEvents();
                        isTap = false;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (touchedScrollView) {
                        if (isTap) onTouchEvent(e);
                        //uncomment if you want text to retrunucate
                        //if ((!emptyTruncateText && !isTextTruncated)) textView.setText(songTruncated);
                    }
                    break;
            }
            return false;

        }catch (NullPointerException exception) {
            Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
            Log.e(TAG, exception.toString());
            onTouchEvent(e);
            return false;
        }
    }

}
