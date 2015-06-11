package com.kabouzeid.gramophone.ui.activities.base;

import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */

public abstract class ThemeBaseActivity extends AppCompatActivity implements KabViewsDisableAble {
    private int colorPrimary;
    private int colorPrimaryDarker;
    private int colorAccent;

    private ActivityManager.TaskDescription taskDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtils.getInstance(this).getGeneralTheme());
        super.onCreate(savedInstanceState);
        setupTheme();
    }


    private void setupTheme() {
        colorPrimary = PreferenceUtils.getInstance(this).getThemeColorPrimary();
        colorPrimaryDarker = Util.shiftColorDown(colorPrimary);
        colorAccent = PreferenceUtils.getInstance(this).getThemeColorAccent();

        ThemeSingleton.get().positiveColor = colorAccent;
        ThemeSingleton.get().negativeColor = ThemeSingleton.get().positiveColor;
        ThemeSingleton.get().neutralColor = ThemeSingleton.get().positiveColor;
        ThemeSingleton.get().widgetColor = ThemeSingleton.get().positiveColor;
        ThemeSingleton.get().darkTheme = PreferenceUtils.getInstance(this).getGeneralTheme() == R.style.Theme_MaterialMusic;

        if (!overridesTaskColor()) {
            notifyTaskColorChange(PreferenceUtils.getInstance(this).getThemeColorPrimary());
        }
    }

    protected void notifyTaskColorChange(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Sets color of entry in the system recents page
            if (taskDescription == null || taskDescription.getPrimaryColor() != color) {
                taskDescription = new ActivityManager.TaskDescription(
                        null,
                        null,
                        color);
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

    protected void setStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            Util.setAllowDrawUnderStatusBar(getWindow());
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            Util.setStatusBarTranslucent(getWindow(), true);
    }

    protected final void setNavigationBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setNavigationBarColor(Util.shiftColorDown(color));
    }

    protected final void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(Util.shiftColorDown(color));
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
            if (statusBar != null) statusBar.setBackgroundColor(color);
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
            setNavigationBarColor(Util.resolveColor(this, android.R.attr.navigationBarColor));
    }

    protected final void resetStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setStatusBarColor(Util.resolveColor(this, android.R.attr.statusBarColor));
    }

    protected boolean overridesTaskColor() {
        return false;
    }
}