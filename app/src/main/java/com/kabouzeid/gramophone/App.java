package com.kabouzeid.gramophone;

import android.app.Application;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import io.fabric.sdk.android.Fabric;

/**
 * Created by karim on 25.11.14.
 */
public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    public static Bus bus = new Bus(ThreadEnforcer.MAIN);

    private int appTheme;
    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        MusicPlayerRemote.init(this);
    }

    public int getAppTheme() {
        if (appTheme == 0) {
            appTheme = PreferenceManager.getDefaultSharedPreferences(this).getInt(AppKeys.SP_THEME, R.style.Theme_MaterialMusic);
        }
        return appTheme;
    }

    public void setAppTheme(int appTheme) {
        this.appTheme = appTheme;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(AppKeys.SP_THEME, appTheme).apply();
    }

    public boolean isTablet() {
        return getResources().getConfiguration().smallestScreenWidthDp >= 600;
    }

    public boolean isInPortraitMode() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public void addToVolleyRequestQueue(Request request) {
        request.setTag(TAG);
        getVolleyRequestQueue().add(request);
    }

    public RequestQueue getVolleyRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(this);
        }
        return requestQueue;
    }
}
