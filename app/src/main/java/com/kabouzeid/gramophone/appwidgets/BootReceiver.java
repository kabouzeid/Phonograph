package com.kabouzeid.gramophone.appwidgets;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.kabouzeid.gramophone.service.MusicService;

/**
 * @author Eugene Cheung (arkon)
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        // Start music service if there are any existing widgets
        if (widgetManager.getAppWidgetIds(new ComponentName(context, AppWidgetBig.class)).length > 0 ||
                widgetManager.getAppWidgetIds(new ComponentName(context, AppWidgetClassic.class)).length > 0 ||
                widgetManager.getAppWidgetIds(new ComponentName(context, AppWidgetSmall.class)).length > 0) {
            final Intent serviceIntent = new Intent(context, MusicService.class);
            context.startService(serviceIntent);
        }
    }
}
