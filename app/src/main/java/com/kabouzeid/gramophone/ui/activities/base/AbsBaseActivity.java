package com.kabouzeid.gramophone.ui.activities.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.crashlytics.android.Crashlytics;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.kabouzeid.gramophone.util.Util;
import com.squareup.otto.Subscribe;

/**
 * Created by karim on 20.01.15.
 */
public abstract class AbsBaseActivity extends ActionBarActivity implements KabViewsDisableAble {
    private App app;
    private boolean areViewsEnabled;
    private Object uiPreferenceChangeListener = new Object() {
        @Subscribe
        public void onUIPreferenceChangedEvent(UIPreferenceChangedEvent event) {
            AbsBaseActivity.this.onUIPreferenceChangedEvent(event);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Crashlytics.setString(AppKeys.CL_CURRENT_ACTIVITY, getTag());
        setTheme(PreferenceUtils.getInstance(this).getGeneralTheme());
        super.onCreate(savedInstanceState);
        try {
            App.bus.register(uiPreferenceChangeListener);
        } catch (Exception ignored) {
        }
    }

    protected App getApp() {
        if (app == null) {
            app = (App) getApplicationContext();
        }
        return app;
    }

    public abstract String getTag();

    @Override
    protected void onResume() {
        super.onResume();
        enableViews();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onUIPreferenceChangedEvent(UIPreferenceChangedEvent event) {
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.THEME_CHANGED:
                recreate();
                break;
        }
    }

    @Override
    public void enableViews() {
        areViewsEnabled = true;
    }

    @Override
    public void disableViews() {
        areViewsEnabled = false;
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }

    protected void setUpTranslucence(boolean statusBarTranslucent, boolean navigationBarTranslucent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow(), statusBarTranslucent);
            if (Util.isInPortraitMode(this) || Util.isTablet(this)) {
                Util.setNavBarTranslucent(getWindow(), navigationBarTranslucent);
            } else {
                Util.setNavBarTranslucent(getWindow(), false);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            App.bus.unregister(uiPreferenceChangeListener);
        } catch (Exception ignored) {
        }
    }
}
