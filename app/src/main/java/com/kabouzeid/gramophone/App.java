package com.kabouzeid.gramophone;

import android.app.Application;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import org.jaudiotagger.tag.TagOptionSingleton;

import io.fabric.sdk.android.Fabric;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends Application {
    public static final String TAG = App.class.getSimpleName();
    public static final Bus bus = new Bus(ThreadEnforcer.MAIN);

    private RequestQueue requestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        if (!BuildConfig.DEBUG) Fabric.with(this, new Crashlytics());

        MusicPlayerRemote.startAndBindService(this);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);
        L.writeLogs(false); // turns off UILs annoying LogCat output

        TagOptionSingleton.getInstance().isAndroid();
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
