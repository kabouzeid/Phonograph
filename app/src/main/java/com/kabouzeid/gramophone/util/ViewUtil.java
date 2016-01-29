package com.kabouzeid.gramophone.util;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ViewUtil {

    public final static int PHONOGRAPH_ANIM_TIME = 1000;

    public static Animator createBackgroundColorTransition(final View v, final int startColor, final int endColor) {
        return createColorAnimator(v, "backgroundColor", startColor, endColor);
    }

    public static Animator createTextColorTransition(final TextView v, final int startColor, final int endColor) {
        return createColorAnimator(v, "textColor", startColor, endColor);
    }

    private static Animator createColorAnimator(Object target, String propertyName, int startColor, int endColor) {
        ObjectAnimator animator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator = ObjectAnimator.ofArgb(target, propertyName, startColor, endColor);
        } else {
            animator = ObjectAnimator.ofInt(target, propertyName, startColor, endColor);
            animator.setEvaluator(new ArgbEvaluator());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        }
        animator.setDuration(PHONOGRAPH_ANIM_TIME);
        return animator;
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (ViewCompat.getTranslationX(v) + 0.5f);
        final int ty = (int) (ViewCompat.getTranslationY(v) + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }
}