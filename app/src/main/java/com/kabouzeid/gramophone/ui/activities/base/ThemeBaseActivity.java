package com.kabouzeid.gramophone.ui.activities.base;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.ThemeSingleton;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.ColorChooserDialog;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.readystatesoftware.systembartint.SystemBarTintManager;

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */

public abstract class ThemeBaseActivity extends AppCompatActivity implements KabViewsDisableAble {
    private final boolean statusBarTranslucent = shouldSetStatusBarTranslucent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setStatusBarTranslucent(statusBarTranslucent);
        setTheme(PreferenceUtils.getInstance(this).getGeneralTheme());
        super.onCreate(savedInstanceState);
        setupTheme();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupTheme() {
        // Apply colors to system UI if necessary
        setShouldColorNavBar(shouldColorNavBar());
        setShouldColorStatusBar(shouldColorStatusBar());

        // Accent colors in dialogs, and any dynamic views that pull from this singleton
        ThemeSingleton.get().positiveColor = PreferenceUtils.getInstance(this).getThemeColorAccent();
        ThemeSingleton.get().negativeColor = ThemeSingleton.get().positiveColor;
        ThemeSingleton.get().neutralColor = ThemeSingleton.get().positiveColor;
        ThemeSingleton.get().widgetColor = ThemeSingleton.get().positiveColor;
        // Dark theme
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

    private void setStatusBarTranslucent(boolean statusBarTranslucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow(), statusBarTranslucent);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected final void setNavigationBarColor(int color) {
        if (Util.isAtLeastLollipop())
            getWindow().setNavigationBarColor(ColorChooserDialog.shiftColorDown(color));

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected final void setStatusBarColor(int color) {
        if (!statusBarTranslucent && Util.isAtLeastLollipop()) {
            getWindow().setStatusBarColor(color);
        } else {
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(color);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected final void setShouldColorNavBar(boolean shouldColorNavBar) {
        if (Util.isAtLeastLollipop()) {
            if (shouldColorNavBar) {
                setNavigationBarColor(PreferenceUtils.getInstance(this).getThemeColorPrimary());
            } else {
                getWindow().setNavigationBarColor(Util.resolveColor(this, android.R.attr.navigationBarColor));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected final void setShouldColorStatusBar(boolean shouldColorStatusBar) {
        if (shouldColorStatusBar) {
            final int primary = PreferenceUtils.getInstance(this).getThemeColorPrimary();
            setStatusBarColor(primary);
        } else {
            if (Util.isAtLeastLollipop()) {
                getWindow().setStatusBarColor(Util.resolveColor(this, android.R.attr.statusBarColor));
            } else {
                SystemBarTintManager tintManager = new SystemBarTintManager(this);
                tintManager.setStatusBarTintEnabled(false);
            }
        }
    }

    protected abstract boolean shouldColorStatusBar();

    protected abstract boolean shouldColorNavBar();

    protected abstract boolean shouldSetStatusBarTranslucent();

    protected boolean overridesTaskColor() {
        return false;
    }
}