package com.kabouzeid.gramophone.ui.activities.base;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.graphics.BitmapFactory;
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
        final int primaryDark = PreferenceUtils.getInstance(this).getThemeColorPrimaryDarker();
        if (Util.hasLollipopSDK()) {
            if (shouldColorNavBar())
                getWindow().setNavigationBarColor(primaryDark);
        }

        setStatusBarColor(primaryDark, false);

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Sets color of entry in the system recents page
            ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher),
                    PreferenceUtils.getInstance(this).getThemeColorPrimary());
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

    protected void setUpTranslucence(boolean statusBarTranslucent, boolean navigationBarTranslucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (translucentStatusBarOnLollipop() || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Util.setStatusBarTranslucent(getWindow(), statusBarTranslucent);
            }
            if (Util.isInPortraitMode(this) || Util.isTablet(this)) {
                Util.setNavBarTranslucent(getWindow(), navigationBarTranslucent);
            } else {
                Util.setNavBarTranslucent(getWindow(), false);
            }
        }
    }

    protected boolean translucentStatusBarOnLollipop() {
        return false;
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
}