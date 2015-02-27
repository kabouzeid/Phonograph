package com.kabouzeid.materialmusic;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.kabouzeid.materialmusic.helper.MusicPlayerRemote;
import com.kabouzeid.materialmusic.misc.AppKeys;
import com.kabouzeid.materialmusic.util.ImageLoaderUtil;

import io.fabric.sdk.android.Fabric;

/**
 * Created by karim on 25.11.14.
 */
public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    private MusicPlayerRemote playerRemote;
    private int appTheme;
    private SharedPreferences defaultSharedPreferences;
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        ImageLoaderUtil.initImageLoader(this);
    }

    public MusicPlayerRemote getMusicPlayerRemote() {
        if (playerRemote == null) {
            playerRemote = new MusicPlayerRemote(this);
            playerRemote.restorePreviousState();
        }
        return playerRemote;
    }

    public int getAppTheme() {
        if (appTheme == 0) {
            appTheme = getDefaultSharedPreferences().getInt(AppKeys.SP_THEME, R.style.Theme_MaterialMusic);
        }
        return appTheme;
    }

    public SharedPreferences getDefaultSharedPreferences() {
        if (defaultSharedPreferences == null) {
            defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return defaultSharedPreferences;
    }

    public void setAppTheme(int appTheme) {
        this.appTheme = appTheme;
        defaultSharedPreferences.edit().putInt(AppKeys.SP_THEME, appTheme).apply();
    }

    public boolean isTablet() {
        return getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public boolean isInPortraitMode() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public void addToRequestQueue(Request request) {
        request.setTag(TAG);
        getRequestQueue().add(request);
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        return requestQueue;
    }
}
