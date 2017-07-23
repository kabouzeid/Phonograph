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

    private int scrollViewID;
    private int textViewID;
    private int listParentID;

    private TouchInterceptHorizontalScrollView scrollView;
    private TouchInterceptTextView textView;
    private View listParent;

    private Rect scrollViewRect = new Rect();
    private float startX;

    private boolean isTap;
    private boolean currentlySettingTextHere = false;

    private String song;
    private String songTruncated;

    /**
     * A TextWatcher used to monitor when the contents of this view has changed
     * since if this view is in a list it will be recycled. This ensures instances are current
     * and when the text changes it will be truncated.
     */
    protected TextWatcher truncateTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            if(!currentlySettingTextHere){

                TouchInterceptHorizontalScrollView sV = (TouchInterceptHorizontalScrollView) findViewWithTag("TIHS");
                if (sV != null) scrollView = sV;
                TouchInterceptTextView tV = (TouchInterceptTextView) findViewWithTag("TITV");
                if(tV != null) textView = tV;
                View lP = findViewById(listParentID);
                if(lP != null) listParent = lP;

                //setTruncateText(textView.getText().toString());
            }
            currentlySettingTextHere = false;

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
    };

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
                scrollView = (TouchInterceptHorizontalScrollView) findViewWithTag("TIHS");
                textView = (TouchInterceptTextView) findViewWithTag("TITV");
                View lP = findViewById(listParentID);
                if(lP != null) listParent = lP;

                textView.post(new Runnable() {
                    @Override
                    public void run() {
                        textView.addTextChangedListener(truncateTextWatcher);
                    }
                });
            }
        });

    }

    /**
     * Sets the TouchInterceptHorizontalScrollView contained by this FrameLayout
     * @param sv The HorizontalScrollView containing text that needs to be scrolled
     */
    public void setTouchInterceptHorizontalScrollView(TouchInterceptHorizontalScrollView sv) {
        this.scrollView = sv;
    }

    /**
     * Sets the TextView that is contained within that TouchInterceptHorizontalScrollView.
     * @param tv The TextView that needs to be scrolled (typically song or album title)
     */
    public void setScrollableTextView(TouchInterceptTextView tv) {
        this.textView = tv;
    }

    /**
     * Sets the List Parent programmatically
     * @param lP Must be either a type of RecyclerView or a type of ListView
     */
    public void setListParent(View lP){
        this.listParent = lP;
    }

    public View getListParent(){
        return listParent;
    }

    /**
     * Gets the ListParent (the parent ListView or RecyclerView) that has been
     * set via xml or programmatically and sets a Scroll Listener. When scrolling
     * clicks are cancelled to prevent any interference with scrolling.
     */
    public void initializeListParent(){

        try{
        if(listParent instanceof RecyclerView){
            ((RecyclerView) listParent).addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    scrollView.slidingPanelSetTouchEnabled(true);
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
                    scrollView.slidingPanelSetTouchEnabled(true);
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
        }catch (NullPointerException exception){
            Log.w(TAG, NULL_LIST_PARENT);
            System.out.println(TAG + " listParent = " + listParent.toString());
            Log.w(TAG, exception.toString());
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
    public void setTruncateText(String s, String sT){
        song = s;
        songTruncated = sT;
        try {
            scrollView = (TouchInterceptHorizontalScrollView) findViewWithTag("TIHS");
            textView = (TouchInterceptTextView) findViewWithTag("TITV");
            //runs after scrollview has been drawn
            textView.post(new Runnable() {
                @Override
                public void run() {
                    if (scrollView.canScroll()) {
                        songTruncated = TextUtils.ellipsize(song,
                                textView.getPaint(),
                                (float) scrollView.getWidth(),
                                TextUtils.TruncateAt.END).toString() + "\u202F";

                        if (songTruncated != null && !songTruncated.isEmpty()) {
                            setText(songTruncated);

                            if (songTruncated.equals(song)) {
                                scrollView.setScrollingEnabled(false);

                            } else {
                                scrollView.setScrollingEnabled(true);

                                scrollView.setOnEndScrollListener(
                                        new TouchInterceptHorizontalScrollView.OnEndScrollListener() {
                                            @Override
                                            public void onEndScroll() {
                                                reTruncateScrollText();
                                            }
                                        });
                            }
                            initializeListParent();
                        }else{
                            scrollView.setScrollingEnabled(false);
                        }
                    }else{
                        scrollView.setScrollingEnabled(false);
                    }

                }
            });
        }catch (NullPointerException exception){
            Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
            Log.e("Method: ","setTruncateText()");
            System.out.println(TAG + " TouchInterceptHorizontalScrollView = " + scrollView.toString());
            System.out.println(TAG + " TouchInterceptTextView = " + textView);
            Log.e(TAG, exception.toString());
            }
    }

    /**
     * Used to set the text and indicate that the TextView text is being
     * set in this CustomView class and not somewhere else.
     * @param text The text to set the TextView to
     */
    private void setText(String text){
        currentlySettingTextHere = true;
        textView.setText(text);
    }

    public boolean isTextTruncated(String text) {
        if (text.endsWith("…\u202F")) return true;
        else return false;
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

        if(scrollView.isScrollable()) {

            boolean emptyTruncateText;
            boolean isTextTruncated;

            int x = Math.round(e.getRawX());
            int y = Math.round(e.getRawY());
            try {
                scrollView.getGlobalVisibleRect(scrollViewRect);

                boolean touchedScrollView =
                        x > scrollViewRect.left && x < scrollViewRect.right &&
                                y > scrollViewRect.top && y < scrollViewRect.bottom;

//                if (songTruncated != null){
//                    emptyTruncateText = songTruncated.isEmpty();
//                    isTextTruncated = songTruncated.endsWith("…\u202F");
//                    if(emptyTruncateText) Log.e(TAG, EMPTY_TRUNCATE_STRING);
//                }else{
//                    emptyTruncateText = true;
//                    isTextTruncated = false;
//                    Log.e(TAG, EMPTY_TRUNCATE_STRING);
//                }

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
                                textView.unTruncateText();
                                isTap = false;
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

            } catch (NullPointerException exception) {
                Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
                Log.e("Method: ","onInterceptTouchEvent()");
                System.out.println(TAG + " TouchInterceptHorizontalScrollView = " + scrollView.toString());
                System.out.println(TAG + " TouchInterceptTextView = " + textView);
                Log.e(TAG, exception.toString());
                onTouchEvent(e);
                return false;
            }
        }else{
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

    /**
     * Retruncates the text with a fancy scroll to beginning animation that takes a set amount of time
     */
    public void reTruncateScrollText(){
        ObjectAnimator.ofInt(scrollView, "scrollX",  0).setDuration(RETRUNCATE_DELAY).start();
        scrollView.slidingPanelSetTouchEnabled(true);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setText(songTruncated);
            }
        }, RETRUNCATE_DELAY);
    }
}
