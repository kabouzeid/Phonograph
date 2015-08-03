package com.kabouzeid.gramophone.ui.activities.base;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;

import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsBaseActivity extends AbsThemeActivity implements KabViewsDisableAble {

    private boolean areViewsEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableViews();
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
            showOverflowMenu();
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    protected void showOverflowMenu() {

    }

    /**
     * Should be overwritten and re enable all {@link android.view.View} to ensure they are accessible again
     * <p/>
     * This is necessary because of a bug with the shared element transition
     */
    @Override
    public void enableViews() {
        areViewsEnabled = true;
    }

    /**
     * Should be overwritten and disable all views that start a new activity on click to prevent opening an activity multiple times
     * <p/>
     * This is necessary because of a bug with the shared element transition
     */
    @Override
    public void disableViews() {
        areViewsEnabled = false;
    }

    @Override
    public boolean areViewsEnabled() {
        return areViewsEnabled;
    }
}
