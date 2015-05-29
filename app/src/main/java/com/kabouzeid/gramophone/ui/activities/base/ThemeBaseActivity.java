package com.kabouzeid.gramophone.ui.activities.base;

import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */

public abstract class ThemeBaseActivity extends AppCompatActivity implements KabViewsDisableAble {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtils.getInstance(this).getGeneralTheme());
        super.onCreate(savedInstanceState);
        setupTheme();
    }


    private void setupTheme() {
        ThemeSingleton.get().positiveColor = PreferenceUtils.getInstance(this).getThemeColorAccent();
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
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher),
                    color);
            setTaskDescription(td);
        }
    }

    protected void setStatusBarTranslucent(boolean statusBarTranslucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            Util.setStatusBarTranslucent(getWindow(), statusBarTranslucent);
    }

    protected final void setNavigationBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setNavigationBarColor(color);
    }

    protected final void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(color);
    }

    protected final void setNavigationBarThemeColor() {
        setNavigationBarColor(PreferenceUtils.getInstance(this).getThemeColorPrimaryDarker());
    }

    protected final void setStatusBarThemeColor() {
        setStatusBarColor(PreferenceUtils.getInstance(this).getThemeColorPrimaryDarker());
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