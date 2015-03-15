package com.kabouzeid.gramophone.util;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by karim on 06.12.14.
 */
public class ViewUtil {
    public final static int DEFAULT_COLOR_ANIMATION_DURATION = 1000;

    public static void disableViews(ViewGroup layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                disableViews((ViewGroup) child);
            } else {
                child.setEnabled(false);
            }
        }
    }

    public static void enableViews(ViewGroup layout) {
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                enableViews((ViewGroup) child);
            } else {
                child.setEnabled(true);
            }
        }
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void animateViewColor(final View v, final int startColor, final int endColor) {
        animateViewColor(v, startColor, endColor, DEFAULT_COLOR_ANIMATION_DURATION);
    }

    public static void animateViewColor(final View v, final int startColor, final int endColor, final int duration) {
        ObjectAnimator animator = ObjectAnimator.ofObject(v, "backgroundColor",
                new ArgbEvaluator(), startColor, endColor);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        }
        animator.setDuration(duration);
        animator.start();
    }

    public static void setBackgroundAlpha(View view, float alpha, int baseColor) {
        int a = Math.min(255, Math.max(0, (int) (alpha * 255))) << 24;
        int rgb = 0x00ffffff & baseColor;
        view.setBackgroundColor(a + rgb);
    }

    public static void addOnGlobalLayoutListener(final View view, final Runnable runnable) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                runnable.run();
            }
        });
    }

    public static void animateTextViewMaxLines(TextView text, int maxLines) {
        try {
            ObjectAnimator animation = ObjectAnimator.ofInt(text, "maxLines", maxLines);
            animation.setInterpolator(new AccelerateInterpolator());
            animation.setDuration(200);
            animation.start();
        } catch (Exception e) {
            // Some devices crash at runtime when using the ObjectAnimator
            text.setMaxLines(maxLines);
        }
    }
}
