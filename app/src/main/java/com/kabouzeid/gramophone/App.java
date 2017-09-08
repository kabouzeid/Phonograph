package com.kabouzeid.gramophone;

import android.app.Application;
import android.os.Build;

import com.kabouzeid.gramophone.appshortcuts.DynamicShortcutManager;

public class App extends Application {

    public static final String TAG = App.class.getSimpleName();

    private static App app;

    private Billing billing;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        CrashLogger.init(this);

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }

        billing = new Billing(this);
    }

    public static boolean isProFlavor() {
        return BuildConfig.FLAVOR.equals("pro");
    }

    public static boolean isProEnabled() {
        return BuildConfig.DEBUG || app.billing.isPurchased();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        billing.release();
    }
}
