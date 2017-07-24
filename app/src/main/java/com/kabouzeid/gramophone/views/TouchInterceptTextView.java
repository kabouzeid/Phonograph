package com.kabouzeid.gramophone.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

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

    private int textBoundsWidth;

    public TouchInterceptTextView(Context context) {
        super(context);
        setTag("TITV");
        setLongClickable(true);
        setClickable(true);
        setSingleLine();

    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTag("TITV");
        setLongClickable(true);
        setClickable(true);
        setSingleLine();

    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TouchInterceptFrameLayout getTouchInterceptFrameLayout() {
        return (TouchInterceptFrameLayout) getRootView().findViewWithTag("TIFL");
    }

    public TouchInterceptHorizontalScrollView getTouchInterceptHorizontalScrollView() {
        return (TouchInterceptHorizontalScrollView) getParent();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        textBoundsWidth = MeasureSpec.getSize(widthMeasureSpec);

        String currentText = getText().toString();
        Boolean isUntruncatedSong = currentText.endsWith("\uFEFF");

        if(!currentText.endsWith("\u202F") &&
                !isUntruncatedSong) song = currentText;

        if(!isUntruncatedSong &&
                (getWidth() == 0 | textBoundsWidth < getPaint().measureText(currentText)) ) {
            String truncatedText = TextUtils.ellipsize(currentText,
                    getPaint(),
                    (float) textBoundsWidth,
                    TextUtils.TruncateAt.END).toString() + "\u202F";
            setText(truncatedText);
            setTruncateText(song,truncatedText);
        }else{
            setText(currentText);
            setTruncateText(song,currentText);
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
    public void setTruncateText(final String s, final String sT){

        try {
            post(new Runnable() {
                @Override
                public void run() {
                    if(!s.endsWith("\u202F")) song = s;
                    songTruncated = sT;

                    final TouchInterceptHorizontalScrollView sV = getTouchInterceptHorizontalScrollView();

                    if (isTextTruncated(sT)) {

                        if (s == sT && !sT.endsWith("\uFEFF")) {
                            sV.setScrollingEnabled(false);

                        } else {
                            sV.setScrollingEnabled(true);

                            sV.setOnEndScrollListener(
                                    new TouchInterceptHorizontalScrollView.OnEndScrollListener() {
                                        @Override
                                        public void onEndScroll() {
                                            reTruncateScrollText(sT, sV, TouchInterceptTextView.this);
                                        }
                                    });
                        }

                    }else{
                        if(!sT.endsWith("\uFEFF")) sV.setScrollingEnabled(false);
                    }

                }
            });
        }catch (NullPointerException exception){
            Log.e(TAG, NULL_VIEWS_EXCEPTION_MESSAGE);
            Log.e("Method: ","setTruncateText()");
            System.out.println(TAG + " TouchInterceptHorizontalScrollView = " + getTouchInterceptHorizontalScrollView().toString());
            System.out.println(TAG + " TouchInterceptTextView = " + this.toString());
            Log.e(TAG, exception.toString());
        }
    }

    public boolean isTextTruncated(String text) {
        if (text.endsWith("…\u202F")) return true;
        else return false;
    }

    public void unTruncateText(){
        String untrunucatedText = song + "\uFEFF";
        setText(untrunucatedText);
    }

    public String getSongTruncated(){
        return this.songTruncated;
    }

    public String getUntruncatedSong(){
        return this.song;
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
