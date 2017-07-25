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
 * @author Created by lincoln on 7/16/17.
 *
 * TextView that automatically does exactly what android:ellipsize="end" does, except this works in a TouchInterceptHorizontalScrollViews.
 * Truncates the string so it doesn't get cuttoff in the TouchInterceptHorizontalScrollView
 * and puts an ellipsis at the end of it.
 * Must be used within a TouchInterceptHorizontalScrollview or it won't work
 */

public class TouchInterceptTextView extends AppCompatTextView {
    private static final int RETRUNCATE_DELAY = 600;

    //Tag used so other views can find this one
    private static final String touchInterceptTextViewTag = "TITV";

    private static final String TAG = "E/TouchInterceptFL";
    private static final String NULL_VIEWS_EXCEPTION_MESSAGE = "Either textView or scrollView is null. Maybe you " +
            "forgot to set them using setTouchInterceptHorizontalScrollView and setScrollableTextView " +
            "via XML? Did you set it to something null?";

    private String title;
    private String titleTruncated;

    public TouchInterceptTextView(Context context) {
        super(context);
        setTag(touchInterceptTextViewTag);
        //Have to set this enorder to enable long clicking when touching the text
        setLongClickable(true);
        //Blocks clicks from passing through this view
        setClickable(true);
        //Can't use maxlines, have to use this. Typical Android BS
        setSingleLine();
    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTag(touchInterceptTextViewTag);
        //Have to set this enorder to enable long clicking when touching the text
        setLongClickable(true);
        //Blocks clicks from passing through this view
        setClickable(true);
        //Can't use maxlines, have to use this. Typical Android BS
        setSingleLine();
    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @return Returns the TouchInterceptFrameLayout in this layout
     */
    public TouchInterceptFrameLayout getTouchInterceptFrameLayout() {
        return (TouchInterceptFrameLayout) getRootView().findViewWithTag("TIFL");
    }

    /**
     * @return Returns the parent TouchInterceptHorizontalScrollview
     */
    public TouchInterceptHorizontalScrollView getTouchInterceptHorizontalScrollView() {
        return (TouchInterceptHorizontalScrollView) getParent();
    }

    /**
     * The text undergoes truncation here. onMeasure is immediately called after setText
     * and has a reference to the parent bounds. The parents bounds are used for setting the
     * length of the truncate text ensuring that the text does not get cut off
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int textBoundsWidth = MeasureSpec.getSize(widthMeasureSpec);
        String currentText = getText().toString();
        Boolean isUntruncatedSong = currentText.endsWith("\uFEFF");

        if(!currentText.endsWith("\u202F") &&
                !isUntruncatedSong) title = currentText;

        if(!isUntruncatedSong &&
                (getWidth() == 0 | textBoundsWidth < getPaint().measureText(currentText)) ) {

            /**
             * Does exactly what android:ellipsize="end" does, except this works in HorizontalScrollViews.
             * Truncates the string so it doesn't get cuttoff in the HorizontalScrollView
             * and puts an ellipsis at the end of it. Then it sets the TextView with the new Ellipsized value.
             */
            String truncatedText = TextUtils.ellipsize(currentText,
                    getPaint(),
                    (float) textBoundsWidth,
                    TextUtils.TruncateAt.END).toString()

                    //The \u202F charachter is an invisible charachter used as a marker for whether
                    //a string has undergone truncation or not
                    + "\u202F";

            setText(truncatedText);
            initiateTruncateText(title,truncatedText);
        }else{
            setText(currentText);
            initiateTruncateText(title,currentText);
        }

    }

    /**
     * Takes the string that's undergone truncation and based on whether it's been truncated or not
     * set whether it should be scrollable or not and what to do when the user finishes scrolling
     * @param s The string before truncation
     * @param sT The string after truncation
     */
    public void initiateTruncateText(final String s, final String sT){

        try {
            post(new Runnable() {
                @Override
                public void run() {
                    //The \u202F charachter is an invisible charachter used as a marker for whether
                    //a string has undergone truncation or not
                    if(!s.endsWith("\u202F")) title = s;
                    titleTruncated = sT;

                    final TouchInterceptHorizontalScrollView sV = getTouchInterceptHorizontalScrollView();

                    if (isTextTruncated(sT)) {

                        if (s.equals(sT) && !sT.endsWith("\uFEFF")) {
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
            Log.e("Method: ","initiateTruncateText()");
            System.out.println(TAG + " TouchInterceptHorizontalScrollView = " + getTouchInterceptHorizontalScrollView().toString());
            System.out.println(TAG + " TouchInterceptTextView = " + this.toString());
            Log.e(TAG, exception.toString());
        }
    }

    /**
     * @param text The string to check
     * @return Returns whether the text has been truncated or not
     */
    public boolean isTextTruncated(String text) {
        return text.endsWith("…\u202F");
    }

    /**
     * Untruncates the text in this textview and sets it
     */
    public void unTruncateText(){
        //The uEFF unicode charachter is an invisible charachter used as a marker for whether
        //a string is the untruncated song to be set
        String untrunucatedText = title + "\uFEFF";
        setText(untrunucatedText);
    }

    /**
     * @return Returns the text in this textview truncated
     */
    public String getTruncatedTitle(){
        return this.titleTruncated;
    }

    /**
     * @return Returns the text in this textview untruncated
     */
    public String getUntruncatedTitle(){
        return this.title;
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
}
