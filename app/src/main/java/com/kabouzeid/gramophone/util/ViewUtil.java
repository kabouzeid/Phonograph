package com.kabouzeid.gramophone.util;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.gramophone.R;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ViewUtil {

    public final static int PHONOGRAPH_ANIM_TIME = 1000;

    public static Animator createBackgroundColorTransition(final View v, @ColorInt final int startColor, @ColorInt final int endColor) {
        return createColorAnimator(v, "backgroundColor", startColor, endColor);
    }

    public static Animator createTextColorTransition(final TextView v, @ColorInt final int startColor, @ColorInt final int endColor) {
        return createColorAnimator(v, "textColor", startColor, endColor);
    }

    private static Animator createColorAnimator(Object target, String propertyName, @ColorInt int startColor, @ColorInt int endColor) {
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

    public static Drawable createSelectorDrawable(Context context, @ColorInt int color) {
        final StateListDrawable baseSelector = new StateListDrawable();
        baseSelector.addState(new int[]{android.R.attr.state_activated}, new ColorDrawable(color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(color), baseSelector, new ColorDrawable(Color.WHITE));
        }

        baseSelector.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        baseSelector.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(color));
        return baseSelector;
    }

    public static boolean hitTest(View v, int x, int y) {
        final int tx = (int) (v.getTranslationX() + 0.5f);
        final int ty = (int) (v.getTranslationY() + 0.5f);
        final int left = v.getLeft() + tx;
        final int right = v.getRight() + tx;
        final int top = v.getTop() + ty;
        final int bottom = v.getBottom() + ty;

        return (x >= left) && (x <= right) && (y >= top) && (y <= bottom);
    }

    public static void setUpFastScrollRecyclerViewColor(Context context, FastScrollRecyclerView recyclerView, int accentColor) {
        recyclerView.setPopupBgColor(accentColor);
        recyclerView.setPopupTextColor(MaterialValueHelper.getPrimaryTextColor(context, ColorUtil.isColorLight(accentColor)));
        recyclerView.setThumbColor(accentColor);
        recyclerView.setTrackColor(ColorUtil.withAlpha(ATHUtil.resolveColor(context, R.attr.colorControlNormal), 0.12f));
    }

    public static float convertDpToPixel(float dp, Resources resources) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * metrics.density;
    }

    public static float convertPixelsToDp(float px, Resources resources) {
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / metrics.density;
    }
}
