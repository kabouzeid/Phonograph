package com.kabouzeid.gramophone.util;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import com.kabouzeid.gramophone.service.MusicService;


public class SleepTimerUtil {

    public static PendingIntent createTimer(Context context) {
        return makeTimerIntent(context, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public static Intent getTimerAction(Context context) {
        return new Intent(context, MusicService.class).setAction(MusicService.ACTION_QUIT);
    }

    /**
     * Returns the current sleep timer, if any.
     * @param context the context.
     * @return the current timer. Returns {@code null} if the sleep timer is not running.
     */
    public static PendingIntent getCurrentTimer(Context context) {
        return makeTimerIntent(context, PendingIntent.FLAG_NO_CREATE);
    }

    public static boolean isTimerRunning(Context context) {
        PendingIntent running = getCurrentTimer(context);

        if (running != null) {
            // AlarmManager does not seem to cancel intents after the alarm went off. We must
            // therefore check  if it expired to determine whether the sleep timer is actually
            // running
            return PreferenceUtil.getInstance(context).getNextSleepTimerElapsedRealTime() - SystemClock.elapsedRealtime() > 0;
        }

        return false;
    }

    private static PendingIntent makeTimerIntent(Context context, int flag) {
        return PendingIntent.getService(context, 110110110, getTimerAction(context), flag);
    }
}
