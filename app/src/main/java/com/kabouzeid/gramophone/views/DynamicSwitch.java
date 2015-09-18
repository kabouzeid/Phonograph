package com.kabouzeid.gramophone.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.internal.widget.ThemeUtils;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.gramophone.R;

import static android.support.v7.internal.widget.ThemeUtils.getThemeAttrColorStateList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class DynamicSwitch extends SwitchCompat {
    static final int[] DISABLED_STATE_SET = new int[]{-android.R.attr.state_enabled};
    static final int[] CHECKED_STATE_SET = new int[]{android.R.attr.state_checked};
    static final int[] EMPTY_STATE_SET = new int[0];

    public DynamicSwitch(@NonNull Context context) {
        super(context);
        init();
    }

    public DynamicSwitch(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DynamicSwitch(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        final int color = ThemeSingleton.get().positiveColor.getDefaultColor();
        setTint(this, color);
    }

    public static void setTint(@NonNull SwitchCompat switchCompat, int color) {
        ColorStateList trackColorSl = createSwitchTrackColorStateList(switchCompat.getContext(), color);
        ColorStateList thumbColorSl = createSwitchThumbColorStateList(switchCompat.getContext(), color);

        Drawable thumbDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(switchCompat.getContext(), R.drawable.abc_switch_thumb_material));
        DrawableCompat.setTintList(thumbDrawable, thumbColorSl);
        DrawableCompat.setTintMode(thumbDrawable, PorterDuff.Mode.MULTIPLY);
        switchCompat.setThumbDrawable(thumbDrawable);

        Drawable trackDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(switchCompat.getContext(), R.drawable.abc_switch_track_mtrl_alpha));
        DrawableCompat.setTintList(trackDrawable, trackColorSl);
        switchCompat.setTrackDrawable(trackDrawable);
    }

    @NonNull
    private static ColorStateList createSwitchTrackColorStateList(@NonNull Context context, int colorActivated) {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];
        int i = 0;

        // Disabled state
        states[i] = DISABLED_STATE_SET;
        colors[i] = getColorWithMultipliedAlpha(ThemeUtils.getThemeAttrColor(context, android.R.attr.colorForeground), 0.1f);
        i++;

        states[i] = CHECKED_STATE_SET;
        colors[i] = getColorWithMultipliedAlpha(colorActivated, 0.3f);
        i++;

        // Default enabled state
        states[i] = EMPTY_STATE_SET;
        colors[i] = getColorWithMultipliedAlpha(ThemeUtils.getThemeAttrColor(context, android.R.attr.colorForeground), 0.3f);

        return new ColorStateList(states, colors);
    }

    @NonNull
    private static ColorStateList createSwitchThumbColorStateList(@NonNull Context context, int colorActivated) {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];
        int i = 0;

        final ColorStateList thumbColor = getThemeAttrColorStateList(context,
                android.support.v7.appcompat.R.attr.colorSwitchThumbNormal);

        if (thumbColor != null && thumbColor.isStateful()) {
            // If colorSwitchThumbNormal is a valid ColorStateList, extract the default and
            // disabled colors from it

            // Disabled state
            states[i] = DISABLED_STATE_SET;
            colors[i] = thumbColor.getColorForState(states[i], 0);
            i++;

            states[i] = CHECKED_STATE_SET;
            colors[i] = colorActivated;
            i++;

            // Default enabled state
            states[i] = EMPTY_STATE_SET;
            colors[i] = thumbColor.getDefaultColor();
        } else {
            // Else we'll use an approximation using the default disabled alpha

            // Disabled state
            states[i] = DISABLED_STATE_SET;
            colors[i] = ThemeUtils.getDisabledThemeAttrColor(context, android.support.v7.appcompat.R.attr.colorSwitchThumbNormal);
            i++;

            states[i] = CHECKED_STATE_SET;
            colors[i] = colorActivated;
            i++;

            // Default enabled state
            states[i] = EMPTY_STATE_SET;
            colors[i] = ThemeUtils.getThemeAttrColor(context, android.support.v7.appcompat.R.attr.colorSwitchThumbNormal);
        }

        return new ColorStateList(states, colors);
    }

    private static int getColorWithMultipliedAlpha(int color, float alpha) {
        final int originalAlpha = Color.alpha(color);
        return ColorUtils.setAlphaComponent(color, Math.round(originalAlpha * alpha));
    }
}