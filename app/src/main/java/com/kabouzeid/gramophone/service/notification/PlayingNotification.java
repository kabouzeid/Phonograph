package com.kabouzeid.gramophone.service.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.ui.activities.SleepTimerActivity;
import com.kabouzeid.gramophone.util.SleepTimerUtil;

import static android.content.Context.NOTIFICATION_SERVICE;

public abstract class PlayingNotification {

    private static final int NOTIFICATION_ID = 1;
    static final String NOTIFICATION_CHANNEL_ID = "playing_notification";

    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 0;

    private int notifyMode = NOTIFY_MODE_BACKGROUND;

    private NotificationManager notificationManager;
    protected MusicService service;
    boolean stopped;

    public synchronized void init(MusicService service) {
        this.service = service;
        notificationManager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    public abstract void update();

    public synchronized void stop() {
        stopped = true;
        service.stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    PendingIntent clickAction() {
        Intent intent = new Intent(service, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(service, 0, intent, 0);
    }

    PendingIntent playbackAction(final String action) {
        Intent intent = new Intent(service, MusicService.class).setAction(action);
        return PendingIntent.getService(service, 0, intent, 0);
    }

    PendingIntent deleteAction() {
        return PendingIntent.getService(service, 0, SleepTimerUtil.getTimerAction(service), 0);
    }

    PendingIntent sleepTimerAction() {
        return PendingIntent.getActivity(service, 0, new Intent(service, SleepTimerActivity.class), 0);
    }

    void updateNotifyModeAndPostNotification(Notification notification) {
        int newNotifyMode;
        if (service.isPlaying()) {
            newNotifyMode = NOTIFY_MODE_FOREGROUND;
        } else {
            newNotifyMode = NOTIFY_MODE_BACKGROUND;
        }

        if (notifyMode != newNotifyMode && newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            service.stopForeground(false);
        }

        if (newNotifyMode == NOTIFY_MODE_FOREGROUND) {
            service.startForeground(NOTIFICATION_ID, notification);
        } else if (newNotifyMode == NOTIFY_MODE_BACKGROUND) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }

        notifyMode = newNotifyMode;
    }

    @RequiresApi(26)
    private void createNotificationChannel() {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID);
        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, service.getString(R.string.playing_notification_name), NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(service.getString(R.string.playing_notification_description));
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
