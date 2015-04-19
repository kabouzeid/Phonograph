package com.kabouzeid.gramophone.ui.activities.base;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * @author Aidan Follestad (afollestad)
 */

/**
 * READ!
 * <p/>
 * Instructions:
 * <p/>
 * KitKat or Lollipop solid statusBar with the right color (primaryDark):
 * - shouldColorStatusBar return true OR return false and call setStatusBarColor() in the activity with a custom color
 * - setStatusBarTranslucent(!Util.hasLollipopSDK())
 * <p/>
 * KitKat or Lollipop translucent statusBar (not the color is too dark on Lollipop and KitKat only does fading but MUCH better performance the setStatusBarColor in onScrollCallback)
 * - shouldColorStatusBar return false DO NOT return true and do not call setStatusBarColor() in this case at all here
 * - setStatusBarTranslucent(true)
 * - use a view below the statusBar to color it
 */

public abstract class ThemeBaseActivity extends ActionBarActivity implements KabViewsDisableAble {

//    private boolean mLastDarkTheme;
//    private int mLastPrimary;
//    private int mLastAccent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(PreferenceUtils.getInstance(this).getGeneralTheme());
        super.onCreate(savedInstanceState);
        setupTheme();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupTheme() {
        // Apply colors to system UI if necessary
        setShouldColorNavBar(shouldColorNavBar());
        setShouldColorStatusBar(shouldColorStatusBar());

        // Persist current values so the Activity knows if they change
//        mLastDarkTheme = PreferenceUtils.getInstance(this).getGeneralTheme() == 1;
//        mLastPrimary = PreferenceUtils.getInstance(this).getThemeColorPrimary();
//        mLastAccent = PreferenceUtils.getInstance(this).getThemeColorAccent();

        // Accent colors in dialogs, and any dynamic views that pull from this singleton
        ThemeSingleton.get().positiveColor = PreferenceUtils.getInstance(this).getThemeColorAccent();
        ThemeSingleton.get().negativeColor = ThemeSingleton.get().positiveColor;
        ThemeSingleton.get().neutralColor = ThemeSingleton.get().positiveColor;
        ThemeSingleton.get().widgetColor = ThemeSingleton.get().positiveColor;
        // Dark theme
        ThemeSingleton.get().darkTheme = PreferenceUtils.getInstance(this).getGeneralTheme() == R.style.Theme_MaterialMusic;

        if (!overrideTaskColor()) {
            notifyTaskColorChange(PreferenceUtils.getInstance(this).getThemeColorPrimary());
        }
    }

    protected boolean overrideTaskColor() {
        return false;
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

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mLastDarkTheme != (PreferenceUtils.getInstance(this).getGeneralTheme() == 1) ||
//                mLastPrimary != PreferenceUtils.getInstance(this).getThemeColorPrimary() ||
//                mLastAccent != PreferenceUtils.getInstance(this).getThemeColorAccent()) {
//            // Theme colors changed, recreate the Activity
//            recreate();
//        }
//    }

    protected void setStatusBarTranslucent(boolean statusBarTranslucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow(), statusBarTranslucent);
        }
    }

    protected abstract boolean shouldColorStatusBar();

    protected abstract boolean shouldColorNavBar();

    protected final void setStatusBarColor(int color, boolean forceSystemBarTint) {
        if (!forceSystemBarTint && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(color);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected final void setShouldColorNavBar(boolean shouldColorNavBar) {
        if (Util.hasLollipopSDK()) {
            if (shouldColorNavBar) {
                final int primaryDark = PreferenceUtils.getInstance(this).getThemeColorPrimaryDarker();
                getWindow().setNavigationBarColor(primaryDark);
            } else {
                getWindow().setNavigationBarColor(Color.BLACK);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected final void setShouldColorStatusBar(boolean shouldColorStatusBar) {
        if (shouldColorStatusBar) {
            final int primaryDark = PreferenceUtils.getInstance(this).getThemeColorPrimaryDarker();
            setStatusBarColor(primaryDark, false);
        } else {
            if (Util.hasLollipopSDK()) {
                getWindow().setStatusBarColor(Util.resolveColor(this, android.R.attr.statusBarColor));
            } else {
                SystemBarTintManager tintManager = new SystemBarTintManager(this);
                tintManager.setStatusBarTintEnabled(false);
            }
        }
    }
}