package com.kabouzeid.gramophone.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ListView;
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
    private static final int RETRUNCATE_DELAY = 500;

    private String TAG = "E/TouchInterceptFL";
    private String NULL_VIEWS_EXCEPTION_MESSAGE = "Did you forget to call setViews" + "" +
            "when creating your FrameLayout? Either textView or scrollView is null.";
    private String EMPTY_TRUNCATE_STRING = "songTruncated is empty or null. Did you remember " +
            "to set the song string when setting the song name in your text view?";

    private TouchInterceptHorizontalScrollView scrollView;
    private TextView textView;
    private View listParent;
    private TouchInterceptFrameLayout frameLayout;

    private int scrollViewID;
    private int textViewID;
    private int listParentID;

    private Rect scrollViewRect = new Rect();
    private float startX;

    private boolean isTap;
    private boolean currentlySettingTextHere = false;

    private String songTruncated;
    private String song;

    protected TextWatcher truncateTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            if(!currentlySettingTextHere){

                TouchInterceptHorizontalScrollView sV = (TouchInterceptHorizontalScrollView) findViewById(scrollViewID);
                if (sV != null) scrollView = sV;
                TextView tV = (TextView) findViewById(textViewID);
                if(tV != null) textView = tV;
                View lP = findViewById(listParentID);
                if(lP != null) listParent = lP;

                setTruncateText(textView.getText().toString());
            }
            currentlySettingTextHere = false;

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
    };

    public TouchInterceptFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TouchInterceptFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TouchInterceptFrameLayout, defStyleAttr, 0);
        scrollViewID = a.getResourceId(R.styleable.TouchInterceptFrameLayout_setTouchInterceptHorizontalScrollView, 0);
        textViewID = a.getResourceId(R.styleable.TouchInterceptFrameLayout_setScrollableTextView, 0);
        listParentID = a.getResourceId(R.styleable.TouchInterceptFrameLayout_setListParent, 0);
        frameLayout = this;

        this.post(new Runnable() {
            @Override
            public void run() {
                scrollView = (TouchInterceptHorizontalScrollView) findViewById(scrollViewID);
                textView = (TextView) findViewById(textViewID);
                View lP = findViewById(listParentID);
                if(lP != null) listParent = lP;

                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.addTextChangedListener(truncateTextWatcher);
                        setTruncateText(textView.getText().toString());
                    }
                });
            }
        });

    }

    /**
     * Fetches the HorizontalScrollView contained by this FrameLayout and also gets the
     * TextView that is contained within that HorizontalScrollView.
     * Must be called before setTruncateText.
     * Call this when you are assigning views in your layout.
     * @param sv The HorizontalScrollView containing text that needs to be scrolled
     */
    public void setTouchInterceptHorizontalScrollView(TouchInterceptHorizontalScrollView sv) {
        this.scrollView = sv;
    }

    /**
     * Fetches the HorizontalScrollView contained by this FrameLayout and also gets the
     * TextView that is contained within that HorizontalScrollView.
     * Must be called before setTruncateText.
     * Call this when you are assigning views in your layout.
     * @param tv The TextView that needs to be scrolled (typically song or album title)
     */
    public void setScrollableTextView(TextView tv) {
        this.textView = tv;
    }

    public void setListParent(View lP){
        this.listParent = lP;
    }

    public void initializeListParent(){

        if(listParent instanceof RecyclerView){
            ((RecyclerView) listParent).addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                }

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);

                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        CancelClick();
                    }
                }
            });
        }

        if(listParent instanceof ListView){
            ((ListView) listParent).setOnScrollListener(new AbsListView.OnScrollListener(){
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
                public void onScrollStateChanged(AbsListView view, int newState) {

                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        CancelClick();
                    } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        CancelClick();
                    }
                }
            });
        }
    }

    /**
     * Does exactly what android:ellipsize="end" does, except this works in HorizontalScrollViews.
     * Truncates the string so it doesn't get cuttoff in the HorizontalScrollView
     * and puts an ellipsis at the end of it. Then it sets the TextView with the new Ellipsized value.
     * Must be called after setViews or it will throw a NullPointerException.
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
            textView.post(new Runnable() {
                @Override
                public void run() {
                    song = textView.getText().toString();
                    songTruncated = TextUtils.ellipsize(song,
                            textView.getPaint(),
                            (float) scrollView.getWidth(),
                            TextUtils.TruncateAt.END).toString();

                    if (!songTruncated.isEmpty()) {
                        setText(songTruncated);

                        if(songTruncated.equals(song)) {
                            scrollView.setScrollingEnabled(false);

                        }else{
                            scrollView.setScrollingEnabled(true);

                            scrollView.setOnEndScrollListener(new TouchInterceptHorizontalScrollView.OnEndScrollListener()
                            {
                                @Override
                                public void onEndScroll() {
                                    ReTruncateScrollText();
                                }
                            });
                        }
                        initializeListParent();
                    }
                }
            });
        }catch (NullPointerException exception){
            Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
            Log.e(TAG, exception.toString());
            }
    }

    private void setText(String text){
        currentlySettingTextHere = true;
        textView.setText(text);
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
            if(emptyTruncateText)  Log.e(TAG, EMPTY_TRUNCATE_STRING);

            boolean isTextTruncated = songTruncated.endsWith("…");

            switch (e.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    if (!touchedScrollView){
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
                            if((!emptyTruncateText && isTextTruncated)) setText(song);
                            CancelClick();

                            isTap = false;
                        }
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (touchedScrollView) {
                        if (isTap) onTouchEvent(e);
                    }
                    this.requestDisallowInterceptTouchEvent(false);
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

    /**
     * Cancels any Long Presses and inpending clicks. Used to prevent views from
     * acting on touches while vertically scrolling or any case where the user is not
     * interacting with the item views
     */
    private void CancelClick(){
        this.cancelPendingInputEvents();
        this.cancelLongPress();
        scrollView.cancelLongPress();
        scrollView.cancelPendingInputEvents();
    }

    public void ReTruncateScrollText(){
        ObjectAnimator.ofInt(scrollView, "scrollX",  0).setDuration(RETRUNCATE_DELAY).start();
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setText(songTruncated);
            }
        }, RETRUNCATE_DELAY+100);
    }
}
