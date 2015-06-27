package com.kabouzeid.gramophone.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.Util;

import static android.support.v7.widget.RecyclerView.OnScrollListener;

public class FastScroller extends FrameLayout {
    private static final int HANDLE_HIDE_DELAY = 1500;
    private static final int HANDLE_ANIMATION_DURATION = 300;

    private View handle;
    private View bar;

    private RecyclerView recyclerView;

    private final HandleHider handleHider = new HandleHider();
    private final ScrollListener scrollListener = new ScrollListener();

    private boolean isHidden;
    private int hideTranslationX;

    private ViewPropertyAnimator currentAnimator = null;

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise(context);
    }

    private void initialise(Context context) {
        hideTranslationX = getContext().getResources().getDimensionPixelSize(R.dimen.scrollbar_width) * (Util.isRTL(context) ? -1 : 1);
        setClipChildren(false);
        inflate(context, R.layout.vertical_recycler_fast_scroller_layout, this);
        handle = findViewById(R.id.scroll_handle);
        bar = findViewById(R.id.scroll_bar);
        handle.setEnabled(true);
        setPressedHandleColor(ThemeSingleton.get().positiveColor);
        setUpBarBackground();
        postDelayed(handleHider, HANDLE_HIDE_DELAY);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            setHandlePosition(event.getY());
            handle.setPressed(true);
            setRecyclerViewPosition(event.getY());
            showIfHidden();
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            handle.setPressed(false);
            scheduleHide();
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        recyclerView.addOnScrollListener(scrollListener);
    }

    private void setRecyclerViewPosition(float y) {
        if (recyclerView != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            float proportion = y / (float) getHeightMinusPadding();
            int targetPos = getValueInRange(0, itemCount - 1, (int) (proportion * (float) itemCount));
            recyclerView.scrollToPosition(targetPos);
        }
    }

    private int getValueInRange(int min, int max, int value) {
        int minimum = Math.max(min, value);
        return Math.min(minimum, max);
    }

    private void setHandlePosition(float y) {
        float position = y / getHeightMinusPadding();
        int handleHeight = handle.getHeight();
        handle.setY(getValueInRange(0, getHeightMinusPadding() - handleHeight, (int) ((getHeightMinusPadding() - handleHeight) * position)));
    }

    private void showImpl() {
        isHidden = false;
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = animate().translationX(0).setDuration(HANDLE_ANIMATION_DURATION);
        currentAnimator.start();
    }

    private void hideImpl() {
        isHidden = true;
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }
        currentAnimator = animate().translationX(hideTranslationX).setDuration(HANDLE_ANIMATION_DURATION);
        currentAnimator.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                currentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                currentAnimator = null;
            }
        });
        currentAnimator.start();
    }

    private class HandleHider implements Runnable {
        @Override
        public void run() {
            hideImpl();
        }
    }

    private void showIfHidden() {
        if (isHidden) {
            getHandler().removeCallbacks(handleHider);
            showImpl();
        }
    }

    private void scheduleHide() {
        getHandler().removeCallbacks(handleHider);
        getHandler().postDelayed(handleHider, HANDLE_HIDE_DELAY);
    }

    private int getHeightMinusPadding() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }

    private float computeHandlePosition() {
        View firstVisibleView = recyclerView.getChildAt(0);
        int firstVisiblePosition = recyclerView.getChildAdapterPosition(firstVisibleView);
        int visibleRange = recyclerView.getChildCount();
        int lastVisiblePosition = firstVisiblePosition + visibleRange;
        int itemCount = recyclerView.getAdapter().getItemCount();
        int position;
        if (firstVisiblePosition == 0) {
            position = 0;
        } else if (lastVisiblePosition == itemCount - 1) {
            position = itemCount - 1;
        } else {
            position = firstVisiblePosition;
        }
        float proportion = (float) position / (float) itemCount;
        return getHeightMinusPadding() * proportion;
    }

    public void updateHandlePosition() {
        setHandlePosition(computeHandlePosition());
    }

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
                    new InsetDrawable(new ColorDrawable(accent), 0, 0, getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0));
            drawable.addState(View.EMPTY_STATE_SET,
                    new InsetDrawable(new ColorDrawable(colorControlNormal), 0, 0, getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0));
        }
        handle.setBackground(drawable);
    }

    private void setUpBarBackground() {
        Drawable drawable;

        int colorControlNormal = Util.resolveColor(getContext(), R.attr.colorControlNormal);

        if (!Util.isRTL(getContext())) {
            drawable = new InsetDrawable(new ColorDrawable(colorControlNormal), getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0, 0, 0);
        } else {
            drawable = new InsetDrawable(new ColorDrawable(colorControlNormal), 0, 0, getResources().getDimensionPixelSize(R.dimen.scrollbar_inset), 0);
        }
        bar.setBackground(drawable);
    }

    private class ScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView rv, int dx, int dy) {
            updateHandlePosition();
            showIfHidden();
            scheduleHide();
        }
    }
}