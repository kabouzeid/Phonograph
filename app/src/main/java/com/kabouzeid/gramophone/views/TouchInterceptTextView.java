package com.kabouzeid.gramophone.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

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

    private TouchInterceptFrameLayout touchInterceptFrameLayout;
    private TouchInterceptHorizontalScrollView scrollView;

    private String song;
    private String songTruncated;

    private static boolean truncateText;

    private final Semaphore semaphore = new Semaphore(0);

    public TouchInterceptTextView(Context context) {
        super(context);
        setTag("TITV");
    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTag("TITV");
        setLongClickable(true);
    }

    public TouchInterceptTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTag("TITV");
        setLongClickable(true);
    }

    public TouchInterceptFrameLayout getTouchInterceptFrameLayout() {
        return (TouchInterceptFrameLayout) getRootView().findViewWithTag("TIFL");
    }

    public TouchInterceptHorizontalScrollView getTouchInterceptHorizontalScrollView() {
        return (TouchInterceptHorizontalScrollView) this.getParent();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        String content= text.toString();
        try {
            if (!isTextTruncated(text.toString()) &&
                    !content.endsWith("\uFEFF") &&
                    isTextTruncated(getText().toString())) {
                song = content;
                songTruncated = TextUtils.ellipsize(content,
                        getPaint(),
                        (float) getWidth(),
                        TextUtils.TruncateAt.END).toString() + "\u202F";
                super.setText(songTruncated, BufferType.NORMAL);
                setTruncateText(song);
            }else{
                super.setText(content, BufferType.NORMAL);
                setTruncateText(content);
            }
        } catch (NullPointerException e){
            e.printStackTrace();
            super.setText(content, BufferType.NORMAL);
            setTruncateText(content);
        }
    }

    public void setTruncateText(final String content){
        post(new Runnable() {
            @Override
            public void run() {
                getTouchInterceptFrameLayout().setTruncateText(content);
            }
        });
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
        truncateText = false;
        setText(song);
    }

    /**
     * Retruncates the text with a fancy scroll to beginning animation that takes a set amount of time
     */
    public void reTruncateScrollText() {
        ObjectAnimator.ofInt(getTouchInterceptHorizontalScrollView(), "scrollX", 0).setDuration(RETRUNCATE_DELAY).start();
        getTouchInterceptHorizontalScrollView().slidingPanelSetTouchEnabled(true);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setText(songTruncated);
            }
        }, RETRUNCATE_DELAY);
    }
}
