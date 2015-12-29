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
    private int theme;
    private boolean coloredNavigationBar;

    @Nullable
    private ActivityManager.TaskDescription taskDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        theme = PreferenceUtil.getInstance(this).getGeneralTheme();
        setTheme(theme);
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
        boolean dark = theme != R.style.Theme_Phonograph_Light;

        colorPrimary = PreferenceUtil.getInstance(this).getThemeColorPrimary(this);
        colorPrimaryDarker = ColorUtil.shiftColorDown(colorPrimary);
        colorAccent = PreferenceUtil.getInstance(this).getThemeColorAccent(this);
        coloredNavigationBar = PreferenceUtil.getInstance(this).shouldUseColoredNavigationBar();

        final ColorStateList accentColorStateList;
        if (colorAccent == Color.WHITE && !dark) {
            accentColorStateList = ColorStateList.valueOf(Color.BLACK);
        } else if (colorAccent == Color.BLACK && dark) {
            accentColorStateList = ColorStateList.valueOf(Color.WHITE);
        } else {
            accentColorStateList = ColorStateList.valueOf(colorAccent);
        }

        ThemeSingleton.get().positiveColor = accentColorStateList;
        ThemeSingleton.get().negativeColor = accentColorStateList;
        ThemeSingleton.get().neutralColor = accentColorStateList;
        ThemeSingleton.get().widgetColor = accentColorStateList.getDefaultColor();
        ThemeSingleton.get().darkTheme = dark;


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
                theme != PreferenceUtil.getInstance(this).getGeneralTheme();
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

    protected void setUseDarkStatusBarIcons(boolean useDarkIcons) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int systemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            if (useDarkIcons) {
                getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
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