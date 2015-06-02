package com.kabouzeid.gramophone.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.Util;

/**
 * Defines a basic widget that will allow for fast scrolling a RecyclerView using the basic paradigm of
 * a handle and a bar.
 */
public class FastScroller extends FrameLayout {

    /**
     * The long bar along which a handle travels
     */
    protected final View mBar;
    /**
     * The handle that signifies the user's progress in the list
     */
    protected final View mHandle;
    protected RecyclerView.OnScrollListener mOnScrollListener;
    protected OnTouchListener mOnTouchListener;
    private RecyclerView mRecyclerView;
    private AnimatorSet mAnimator;
    private boolean animatingIn;
    private final Runnable mHide;
    private final int mMinScrollHandleHeight;
    private final int mHiddenTranslationX;

    public FastScroller(Context context) {
        this(context, null, 0);
    }

    public FastScroller(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.vertical_recycler_fast_scroller_layout, this);

        mBar = findViewById(R.id.scroll_bar);
        mHandle = findViewById(R.id.scroll_handle);
        mMinScrollHandleHeight = getResources().getDimensionPixelSize(R.dimen.min_scrollhandle_height);

        mHiddenTranslationX = (Util.isRTL(getContext()) ? -1 : 1) * getResources().getDimensionPixelSize(R.dimen.scrollbar_width);
        mHide = new Runnable() {
            @Override
            public void run() {
                if (!mHandle.isPressed()) {
                    if (mAnimator != null && mAnimator.isStarted()) {
                        mAnimator.cancel();
                    }
                    mAnimator = new AnimatorSet();
                    ObjectAnimator animator2 = ObjectAnimator.ofFloat(FastScroller.this, View.TRANSLATION_X,
                            mHiddenTranslationX);
                    animator2.setInterpolator(new FastOutLinearInInterpolator());
                    animator2.setDuration(150);
                    mHandle.setEnabled(false);
                    mAnimator.play(animator2);
                    mAnimator.start();
                }
            }
        };

        mHandle.setOnTouchListener(new OnTouchListener() {
            private float mInitialBarHeight;
            private float mLastPressedYAdjustedToInitial;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mOnTouchListener.onTouch(v, event);
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    mHandle.setPressed(true);

                    mInitialBarHeight = mBar.getHeight();
                    mLastPressedYAdjustedToInitial = event.getY() + mHandle.getY() + mBar.getY();
                } else if (event.getActionMasked() == MotionEvent.ACTION_MOVE) {
                    float newHandlePressedY = event.getY() + mHandle.getY() + mBar.getY();
                    int barHeight = mBar.getHeight();
                    float newHandlePressedYAdjustedToInitial =
                            newHandlePressedY + (mInitialBarHeight - barHeight);

                    float deltaPressedYFromLastAdjustedToInitial =
                            newHandlePressedYAdjustedToInitial - mLastPressedYAdjustedToInitial;

                    int dY = (int) ((deltaPressedYFromLastAdjustedToInitial / mInitialBarHeight) *
                            (mRecyclerView.computeVerticalScrollRange()));

                    updateRvScroll(dY);

                    mLastPressedYAdjustedToInitial = newHandlePressedYAdjustedToInitial;
                } else if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                    mLastPressedYAdjustedToInitial = -1;

                    mHandle.setPressed(false);
                    postAutoHide();
                }

                return true;
            }
        });

        setTranslationX(mHiddenTranslationX);

        //Default selected handle color
        setPressedHandleColor(Color.BLACK);
        setUpBarBackground();
    }

    /**
     * Provides the ability to programmatically set the color of the fast scroller's handle
     */
    public void setPressedHandleColor(int accent) {
        StateListDrawable drawable = new StateListDrawable();

        int colorControlNormal = Util.resolveColor(getContext(), R.attr.colorControlNormal);

        if (!Util.isRTL(getContext())) {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                    new InsetDrawable(new ColorDrawable(accent), getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0, 0, 0));
            drawable.addState(View.EMPTY_STATE_SET,
                    new InsetDrawable(new ColorDrawable(colorControlNormal), getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0, 0, 0));
        } else {
            drawable.addState(View.PRESSED_ENABLED_STATE_SET,
                    new InsetDrawable(new ColorDrawable(accent), 0, getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0, 0));
            drawable.addState(View.EMPTY_STATE_SET,
                    new InsetDrawable(new ColorDrawable(colorControlNormal), 0, getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0, 0));
        }
        mHandle.setBackground(drawable);
    }

    private void setUpBarBackground() {
        Drawable drawable;

        int colorControlNormal = Util.resolveColor(getContext(), R.attr.colorControlNormal);

        if (!Util.isRTL(getContext())) {
            drawable = new InsetDrawable(new ColorDrawable(colorControlNormal), getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0, 0, 0);
        } else {
            drawable = new InsetDrawable(new ColorDrawable(colorControlNormal), 0, getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0, 0);
        }
        mBar.setBackground(drawable);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        initRecyclerViewOnScrollListener();
    }

    public void initRecyclerViewOnScrollListener() {
        mOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                requestLayout();

                mHandle.setEnabled(true);
                if (!animatingIn && getTranslationX() != 0) {
                    if (mAnimator != null && mAnimator.isStarted()) {
                        mAnimator.cancel();
                    }
                    mAnimator = new AnimatorSet();
                    ObjectAnimator animator = ObjectAnimator.ofFloat(FastScroller.this, View.TRANSLATION_X, 0);
                    animator.setInterpolator(new LinearOutSlowInInterpolator());
                    animator.setDuration(100);
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            animatingIn = false;
                        }
                    });
                    animatingIn = true;
                    mAnimator.play(animator);
                    mAnimator.start();
                }
                postAutoHide();
            }
        };
        mRecyclerView.addOnScrollListener(mOnScrollListener);
    }

    public void setOnHandleTouchListener(OnTouchListener listener) {
        mOnTouchListener = listener;
    }

    private void postAutoHide() {
        if (mRecyclerView != null) {
            mRecyclerView.removeCallbacks(mHide);
            mRecyclerView.postDelayed(mHide, 1500);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        int scrollOffset = mRecyclerView.computeVerticalScrollOffset();
        int verticalScrollRange = mRecyclerView.computeVerticalScrollRange()
                + mRecyclerView.getPaddingBottom();

        int barHeight = mBar.getHeight();
        float ratio = (float) scrollOffset / (verticalScrollRange - barHeight);

        int calculatedHandleHeight = (int) ((float) barHeight / verticalScrollRange * barHeight);
        if (calculatedHandleHeight < mMinScrollHandleHeight) {
            calculatedHandleHeight = mMinScrollHandleHeight;
        }

        if (calculatedHandleHeight >= barHeight) {
            setTranslationX(mHiddenTranslationX);
            return;
        }

        float y = ratio * (barHeight - calculatedHandleHeight);

        mHandle.layout(mHandle.getLeft(), (int) y, mHandle.getRight(), (int) y + calculatedHandleHeight);
    }

    public void updateRvScroll(int dY) {
        if (mRecyclerView != null && mHandle != null) {
            try {
                mRecyclerView.scrollBy(0, dY);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }
}