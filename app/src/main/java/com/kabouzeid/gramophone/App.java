package com.kabouzeid.gramophone;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.kabouzeid.gramophone.imageloader.PhonographImageDownloader;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.L;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import io.fabric.sdk.android.Fabric;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends Application {
    public static final String TAG = App.class.getSimpleName();

    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (!BuildConfig.DEBUG) Fabric.with(this, new Crashlytics());

        refWatcher = LeakCanary.install(this);

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new PhonographImageDownloader(this))
                .memoryCacheSizePercentage(30)
                .build();
        ImageLoader.getInstance().init(config);
        L.writeLogs(false); // turns off UILs annoying LogCat output
    }
}
