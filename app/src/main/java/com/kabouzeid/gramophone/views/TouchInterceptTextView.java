package com.kabouzeid.gramophone.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.concurrent.Semaphore;

/**
 * Created by lincoln on 7/16/17.
 */

public class TouchInterceptTextView extends AppCompatTextView {
    private static final int RETRUNCATE_DELAY = 600;

    private static final String TAG = "E/TouchInterceptFL";
    private static final String NULL_VIEWS_EXCEPTION_MESSAGE = "Either textView or scrollView is null. Maybe you " +
            "forgot to set them using setTouchInterceptHorizontalScrollView and setScrollableTextView " +
            "via XML? Did you set it to something null?";
    private static final String NULL_LIST_PARENT = "The ListParent, aka the parent ListView or RecyclerView is null." +
            "It is highly reccomended you set the ListParent either programmatically or via XML" +
            "if you're TouchInterceptFrameLayout is associated with any type of ListParent. If your" +
            "TouchInterceptFrameLayout does not interact with any type of ListParent no need to set it" +
            "and ignore this message.";

    private TouchInterceptFrameLayout touchInterceptFrameLayout;
    private TouchInterceptHorizontalScrollView scrollView;

    private String song;
    private String songTruncated;

    private static boolean truncateText;

    private final Semaphore semaphore = new Semaphore(0);

    public TouchInterceptTextView(Context context) {
        super(context);
        setTag("TITV");
        Log.d("Initiate", "1");

        final TouchInterceptTextView tV = this;
        post(new Runnable() {
            @Override
            public void run() {
                final TouchInterceptHorizontalScrollView sV = getTouchInterceptHorizontalScrollView();
                song = getText().toString();
                if(sV.canScroll()) {
                    System.out.println("Width: " + Integer.toString(sV.getWidth()));
                    setText(getText().toString());
                }else{
                    setTruncateText(song,song,getTouchInterceptFrameLayout(), sV, tV);
                }
            }
        });
    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTag("TITV");
        setLongClickable(true);
        Log.d("Initiate", "2");

        final TouchInterceptTextView tV = this;
        post(new Runnable() {
            @Override
            public void run() {
                final TouchInterceptHorizontalScrollView sV = getTouchInterceptHorizontalScrollView();
                song = getText().toString();
                if(sV.canScroll()) {
                    System.out.println("Width: " + Integer.toString(sV.getWidth()));
                    setText(getText().toString());
                }else{
                    setTruncateText(song,song,getTouchInterceptFrameLayout(), sV, tV);
                }
            }
        });
    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTag("TITV");
        setLongClickable(true);
        Log.d("Initiate", "3");
    }

    public TouchInterceptFrameLayout getTouchInterceptFrameLayout() {
        return (TouchInterceptFrameLayout) getRootView().findViewWithTag("TIFL");
    }

    public TouchInterceptHorizontalScrollView getTouchInterceptHorizontalScrollView() {
        return (TouchInterceptHorizontalScrollView) getParent();
    }

//    @Override
//    public void setText(CharSequence text, BufferType type) {
//        final String content= text.toString();
//        System.out.println("Width: "+Integer.toString(getWidth()));
//        System.out.println("Current text: "+getText().toString());
//        System.out.println("Content: "+content);
//        try {
//            if(getWidth() > 0) {
//                if (!getText().toString().endsWith("\u202F") &&
//                        !content.endsWith("\uFEFF")) {
//                    song = content;
//                    songTruncated = TextUtils.ellipsize(content,
//                            getPaint(),
//                            (float) getTouchInterceptHorizontalScrollView().getWidth(),
//                            TextUtils.TruncateAt.END).toString() + "\u202F";
//                    super.setText(songTruncated, BufferType.NORMAL);
//
//                    System.out.println("Set Song Truncated: "+songTruncated);
//
//                    setTruncateText(song, songTruncated);
//                } else {
//                    super.setText(content, BufferType.NORMAL);
//                }
//            }else{
//                super.setText(content, BufferType.NORMAL);
//                post(new Runnable() {
//                    @Override
//                    public void run() {
//                        song = content;
//                        System.out.println("Reset content: "+ song);
//                        if(getWidth() > 0) setText(content);
//                    }
//                });
//            }
//        } catch (NullPointerException e){
//            e.printStackTrace();
//            super.setText(content, BufferType.NORMAL);
//        }
//        super.setText(text, BufferType.NORMAL);
//    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String content = text.toString();
        song = content;
        final TouchInterceptHorizontalScrollView sV = getTouchInterceptHorizontalScrollView();
        if(sV != null && !content.endsWith("\uFEFF") && sV.canScroll()){
            System.out.println("Width: " + Integer.toString(sV.getWidth()));
                content = TextUtils.ellipsize(content,
                        getPaint(),
                        (float) sV.getWidth(),
                        TextUtils.TruncateAt.END).toString() + "\u202F";
                setTruncateText(song, content, getTouchInterceptFrameLayout(), sV, this);
            }else{
                setTruncateText(content,content, getTouchInterceptFrameLayout(), sV, this);

            }
        super.setText(content, BufferType.NORMAL);
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
    public void setTruncateText(final String s, final String sT,
                                final TouchInterceptFrameLayout fL,
                                final TouchInterceptHorizontalScrollView sV,
                                final TouchInterceptTextView tV){
        try {

            post(new Runnable() {
                @Override
                public void run() {
                    song = s;
                    songTruncated = sT;

                    System.out.println("Song Truncated: "+ sT);

                    if (isTextTruncated(sT)) {

                        Log.d("is Song Truncated","true");

                        if (s.equals(sT)) {
                            sV.setScrollingEnabled(false);

                        } else {
                            sV.setScrollingEnabled(true);

                            sV.setOnEndScrollListener(
                                    new TouchInterceptHorizontalScrollView.OnEndScrollListener() {
                                        @Override
                                        public void onEndScroll() {
                                            reTruncateScrollText(sT,sV,tV);
                                        }
                                    });
                        }
                            initializeListParent(fL, sV);
                    }else{
                        sV.setScrollingEnabled(false);
                    }

                }
            });
        }catch (NullPointerException exception){
            Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
            Log.e("Method: ","setTruncateText()");
            System.out.println(TAG + " TouchInterceptHorizontalScrollView = " + sV.toString());
            System.out.println(TAG + " TouchInterceptTextView = " + this.toString());
            Log.e(TAG, exception.toString());
        }
    }

    /**
     * Gets the ListParent (the parent ListView or RecyclerView) that has been
     * set via xml or programmatically and sets a Scroll Listener. When scrolling
     * clicks are cancelled to prevent any interference with scrolling.
     */
    public void initializeListParent(final TouchInterceptFrameLayout fL, final TouchInterceptHorizontalScrollView sV){

        final View listParent = fL.getListParent();

        try{
            if(listParent instanceof RecyclerView){
                ((RecyclerView) listParent).addOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                        sV.slidingPanelSetTouchEnabled(true);
                    }

                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        super.onScrollStateChanged(recyclerView, newState);

                        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                            CancelClick(fL, sV);
                        }
                    }
                });
            }

            if(listParent instanceof ListView){
                ((ListView) listParent).setOnScrollListener(new AbsListView.OnScrollListener(){
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                        sV.slidingPanelSetTouchEnabled(true);
                    }
                    public void onScrollStateChanged(AbsListView view, int newState) {

                        if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                            CancelClick(fL, sV);
                        } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                            CancelClick(fL, sV);
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
     * Sets the TouchIntercept frame layout that is the RootView of the layout.
     * Must be a TouchInterceptFrameLayout
     *
     * @param fL The FrameLayout to be set.
     */
    public void setTouchInterceptFrameLayout(TouchInterceptFrameLayout fL) {
        this.touchInterceptFrameLayout = fL;
    }

    /**
     * Sets the TouchInterceptHorizontalScrollView contained by this FrameLayout
     *
     * @param sv The HorizontalScrollView containing text that needs to be scrolled
     */
    public void setTouchInterceptHorizontalScrollView(TouchInterceptHorizontalScrollView sv) {
        this.scrollView = sv;
    }

    public boolean isTextTruncated(String text) {
        if (text.endsWith("…\u202F")) return true;
        else return false;
    }

    public void unTruncateText(){
        setText(song + "\uFEFF");
    }

    /**
     * Retruncates the text with a fancy scroll to beginning animation that takes a set amount of time
     */
    public void reTruncateScrollText(final String truncatedString,
                                     final TouchInterceptHorizontalScrollView sV,
    final TouchInterceptTextView tV) {
        ObjectAnimator.ofInt(sV, "scrollX", 0).setDuration(RETRUNCATE_DELAY).start();
        sV.slidingPanelSetTouchEnabled(true);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tV.setText(truncatedString);
            }
        }, RETRUNCATE_DELAY);
    }

    /**
     *Cancels any Long Presses and inpending clicks. Used to prevent views from
     * stealing touches while the user is scrolling something.
     */
    private void CancelClick(TouchInterceptFrameLayout fL, TouchInterceptHorizontalScrollView sV){
        fL.cancelPendingInputEvents();
        fL.cancelLongPress();
        sV.cancelLongPress();
        sV.cancelPendingInputEvents();
    }
}
