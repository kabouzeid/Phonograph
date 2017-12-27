package com.poupa.vinylmusicplayer.misc;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Aidan Follestad (afollestad)
 */
public class LagTracker {

    private static LagTracker mSingleton;
    private static Map<String, Long> mMap;
    private boolean mEnabled = true;

    private LagTracker() {
        mMap = new HashMap<>();
    }

    public static LagTracker get() {
        if (mSingleton == null)
            mSingleton = new LagTracker();
        return mSingleton;
    }

    public LagTracker enable() {
        mEnabled = true;
        return this;
    }

    public LagTracker disable() {
        mEnabled = false;
        return this;
    }

    public void start(String key) {
        final long start = System.nanoTime();
        if (!mEnabled) {
            if (!mMap.isEmpty())
                mMap.clear();
            return;
        }
        mMap.put(key, start);
    }

    public void end(String key) {
        final long end = System.nanoTime();
        if (!mEnabled) {
            if (!mMap.isEmpty())
                mMap.clear();
            return;
        }
        if (!mMap.containsKey(key))
            throw new IllegalStateException("No start time found for " + key);
        long start = mMap.get(key);
        long diff = end - start;
        print(key, diff);
        mMap.remove(key);
    }

    private void print(String key, long diff) {
        long ms = TimeUnit.NANOSECONDS.toMillis(diff);
        long s = TimeUnit.NANOSECONDS.toSeconds(diff);
        Log.d("LagTracker", "[" + key + " completed in]: " + diff + " ns (" + ms + "ms, " + s + "s)");
    }
}