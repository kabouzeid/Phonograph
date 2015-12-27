package com.kabouzeid.gramophone.ui.activities.base;

import android.app.ActivityManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */

public abstract class AbsThemeActivity extends AppCompatActivity implements KabViewsDisableAble {
    private int colorPrimary;
    private int colorPrimaryDarker;
    private int colorAccent;
    private boolean darkTheme;
    private boolean coloredNavigationBar;

    @Nullable
    private ActivityManager.TaskDescription taskDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtil.getInstance(this).getGeneralTheme());
        super.onCreate(savedInstanceState);
        setupTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // the handler is necessary to avoid "java.lang.RuntimeException: Performing pause of activity that is not resumed"
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                recreateIfThemeChanged();
            }
        }, 200);
    }

    private void setupTheme() {
        colorPrimary = PreferenceUtil.getInstance(this).getThemeColorPrimary(this);
        colorPrimaryDarker = ColorUtil.shiftColorDown(colorPrimary);
        colorAccent = PreferenceUtil.getInstance(this).getThemeColorAccent(this);
        darkTheme = PreferenceUtil.getInstance(this).getGeneralTheme() == R.style.Theme_MaterialMusic;
        coloredNavigationBar = PreferenceUtil.getInstance(this).shouldUseColoredNavigationBar();

        final ColorStateList accentColorStateList;
        if (colorAccent == Color.WHITE && !darkTheme) {
            accentColorStateList = ColorStateList.valueOf(Color.BLACK);
        } else if (colorAccent == Color.BLACK && darkTheme) {
            accentColorStateList = ColorStateList.valueOf(Color.WHITE);
        } else {
            accentColorStateList = ColorStateList.valueOf(colorAccent);
        }

        ThemeSingleton.get().positiveColor = accentColorStateList;
        ThemeSingleton.get().negativeColor = accentColorStateList;
        ThemeSingleton.get().neutralColor = accentColorStateList;
        ThemeSingleton.get().widgetColor = accentColorStateList.getDefaultColor();
        ThemeSingleton.get().darkTheme = darkTheme;

        if (!overridesTaskColor()) {
            notifyTaskColorChange(getThemeColorPrimary());
        }
    }

    protected void recreateIfThemeChanged() {
        if (didThemeChanged()) {
            recreate();
        }
    }

    private boolean didThemeChanged() {
        return coloredNavigationBar != PreferenceUtil.getInstance(this).shouldUseColoredNavigationBar() ||
                colorPrimary != PreferenceUtil.getInstance(this).getThemeColorPrimary(this) ||
                colorAccent != PreferenceUtil.getInstance(this).getThemeColorAccent(this) ||
                darkTheme != (PreferenceUtil.getInstance(this).getGeneralTheme() == R.style.Theme_MaterialMusic);
    }

    protected void notifyTaskColorChange(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Sets color of entry in the system recents page
            if (taskDescription == null || taskDescription.getPrimaryColor() != color) {
                taskDescription = new ActivityManager.TaskDescription(
                        null,
                        null,
                        ColorUtil.getOpaqueColor(color));
                setTaskDescription(taskDescription);
            }
        }
    }

    public int getThemeColorPrimary() {
        return colorPrimary;
    }

    public int getThemeColorPrimaryDarker() {
        return colorPrimaryDarker;
    }

    public int getThemeColorAccent() {
        return colorAccent;
    }

    public boolean shouldColorNavigationBar() {
        return coloredNavigationBar;
    }

    protected void setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            Util.setAllowDrawUnderStatusBar(getWindow());
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            Util.setStatusBarTranslucent(getWindow());
    }

    protected void setNavigationBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setNavigationBarColor(color);
    }

    /**
     * This will set the color of the view with the id "status_bar" on KitKat and Lollipop.
     * On Lollipop if no such view is found it will set the statusbar color using the native method.
     *
     * @param color the new statusbar color (will be shifted down on Lollipop and above)
     */
    protected final void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
            if (statusBar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBar.setBackgroundColor(ColorUtil.shiftColorDown(color));
                } else {
                    statusBar.setBackgroundColor(color);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ColorUtil.shiftColorDown(color));
            }
        }
    }

    protected final void setNavigationBarThemeColor() {
        setNavigationBarColor(colorPrimary);
    }

    protected final void setStatusBarThemeColor() {
        setStatusBarColor(colorPrimary);
    }

    protected final void resetNavigationBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setNavigationBarColor(ColorUtil.resolveColor(this, android.R.attr.navigationBarColor));
    }

    protected final void resetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setStatusBarColor(ColorUtil.resolveColor(this, android.R.attr.statusBarColor));
    }

    protected boolean overridesTaskColor() {
        return false;
    }
}