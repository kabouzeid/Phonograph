package com.kabouzeid.gramophone.ui.activities.base;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.BuildConfig;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.model.UIPreferenceChangedEvent;
import com.squareup.otto.Subscribe;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsBaseActivity extends AbsThemeActivity implements KabViewsDisableAble {

    private boolean areViewsEnabled;
    private final Object uiPreferenceChangeListener = new Object() {
        @Subscribe
        public void onUIPreferenceChangedEvent(@NonNull UIPreferenceChangedEvent event) {
            AbsBaseActivity.this.onUIPreferenceChangedEvent(event);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (!BuildConfig.DEBUG) Crashlytics.setString("Current activity", getTag());
        super.onCreate(savedInstanceState);
        try {
            App.bus.register(uiPreferenceChangeListener);
        } catch (Exception ignored) {
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @NonNull
    protected abstract String getTag();

    @Override
    protected void onResume() {
        super.onResume();
        enableViews();
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

    protected void onUIPreferenceChangedEvent(@NonNull UIPreferenceChangedEvent event) {
        switch (event.getAction()) {
            case UIPreferenceChangedEvent.THEME_CHANGED:
                recreate();
                break;
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
