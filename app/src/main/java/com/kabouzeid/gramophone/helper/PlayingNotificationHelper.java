package com.kabouzeid.gramophone.helper;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.RemoteViews;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MusicControllerActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class PlayingNotificationHelper {

    public static final String TAG = PlayingNotificationHelper.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1337;
    public static final String ACTION_NOTIFICATION_COLOR_PREFERENCE_CHANGED = "com.kabouzeid.gramophone.NOTIFICATION_COLOR_PREFERENCE_CHANGED";
    public static final String EXTRA_NOTIFICATION_COLORED = "com.kabouzeid.gramophone.EXTRA_NOTIFICATION_COLORED";

    private final MusicService service;

    private final NotificationManager notificationManager;
    private Notification notification = null;

    private RemoteViews notificationLayout;
    private RemoteViews notificationLayoutExpanded;

    private Song currentSong;
    private boolean isPlaying;
    private String currentAlbumArtUri;

    private boolean isColored;
    private boolean isReceiverRegistered;
    private boolean isNotificationShown;

    final IntentFilter intentFilter;

    public PlayingNotificationHelper(final MusicService service) {
        this.service = service;
        notificationManager = (NotificationManager) service
                .getSystemService(Context.NOTIFICATION_SERVICE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOTIFICATION_COLOR_PREFERENCE_CHANGED);
    }

    private BroadcastReceiver notificationColorPreferenceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_NOTIFICATION_COLOR_PREFERENCE_CHANGED)) {
                boolean isColored = intent.getBooleanExtra(EXTRA_NOTIFICATION_COLORED, false);
                if (isNotificationShown && PlayingNotificationHelper.this.isColored != isColored) {
                    updateNotification(isColored);
                }
            }
        }
    };

    public void updateNotification() {
        updateNotification(PreferenceUtils.getInstance(service).coloredNotification());
    }

    private void updateNotification(final boolean isColored) {
        Song song = service.getCurrentSong();
        if (song.id == -1) {
            service.stopForeground(true);
            return;
        }
        this.isColored = isColored;
        currentSong = song;
        this.isPlaying = service.isPlaying();
        if (!isReceiverRegistered)
            service.registerReceiver(notificationColorPreferenceChangedReceiver, intentFilter);
        isReceiverRegistered = true;
        isNotificationShown = true;

        notificationLayout = new RemoteViews(service.getPackageName(),
                isColored ? R.layout.notification_controller_colored : R.layout.notification_controller);
        notificationLayoutExpanded = new RemoteViews(service.getPackageName(),
                isColored ? R.layout.notification_controller_big_colored : R.layout.notification_controller_big);

        notification = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(getOpenMusicControllerPendingIntent())
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(notificationLayout)
                .build();

        notification.bigContentView = notificationLayoutExpanded;

        setUpCollapsedLayout();
        setUpExpandedLayout();
        loadAlbumArt();
        setUpPlaybackActions();
        setUpExpandedPlaybackActions();

        service.startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent getOpenMusicControllerPendingIntent() {
        Intent result = new Intent(service, MusicControllerActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(service);
        taskStackBuilder.addParentStack(MusicControllerActivity.class);
        taskStackBuilder.addNextIntent(result);
        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setUpExpandedPlaybackActions() {
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.action_play_pause,
                retrievePlaybackActions(1));

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.action_next,
                retrievePlaybackActions(2));

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.action_prev,
                retrievePlaybackActions(3));

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.action_quit,
                retrievePlaybackActions(4));

        notificationLayoutExpanded.setImageViewResource(R.id.action_play_pause,
                isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);
    }

    private void setUpPlaybackActions() {
        notificationLayout.setOnClickPendingIntent(R.id.action_play_pause,
                retrievePlaybackActions(1));

        notificationLayout.setOnClickPendingIntent(R.id.action_next,
                retrievePlaybackActions(2));

        notificationLayout.setOnClickPendingIntent(R.id.action_prev,
                retrievePlaybackActions(3));

        notificationLayout.setImageViewResource(R.id.action_play_pause,
                isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);
    }

    private PendingIntent retrievePlaybackActions(final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        switch (which) {
            case 1:
                action = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(service, 1, action, 0);
                return pendingIntent;
            case 2:
                action = new Intent(MusicService.ACTION_SKIP);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(service, 2, action, 0);
                return pendingIntent;
            case 3:
                action = new Intent(MusicService.ACTION_REWIND);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(service, 3, action, 0);
                return pendingIntent;
            case 4:
                action = new Intent(MusicService.ACTION_QUIT);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(service, 4, action, 0);
                return pendingIntent;
            default:
                break;
        }
        return null;
    }

    private void setUpCollapsedLayout() {
        if (currentSong != null) {
            notificationLayout.setTextViewText(R.id.title, currentSong.title);
            notificationLayout.setTextViewText(R.id.text, currentSong.artistName);
            notificationLayout.setTextViewText(R.id.text2, currentSong.albumName);
        }
    }

    private void setUpExpandedLayout() {
        if (currentSong != null) {
            notificationLayoutExpanded.setTextViewText(R.id.title, currentSong.title);
            notificationLayoutExpanded.setTextViewText(R.id.text, currentSong.artistName);
            notificationLayoutExpanded.setTextViewText(R.id.text2, currentSong.albumName);
        }
    }

    private void loadAlbumArt() {
        currentAlbumArtUri = MusicUtil.getAlbumArtUri(currentSong.albumId).toString();
        ImageLoader.getInstance().displayImage(currentAlbumArtUri, new NonViewAware(new ImageSize(-1, -1), ViewScaleType.CROP), new SimpleImageLoadingListener() {
            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (currentAlbumArtUri.equals(imageUri))
                    setAlbumArt(loadedImage);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (currentAlbumArtUri.equals(imageUri))
                    setAlbumArt(null);
            }
        });
    }

    private void setAlbumArt(Bitmap albumArt) {
        if (albumArt != null) {
            notificationLayout.setImageViewBitmap(R.id.icon, albumArt);
            notificationLayoutExpanded.setImageViewBitmap(R.id.icon, albumArt);
            if (isColored) {
                int defaultColor = service.getResources().getColor(R.color.default_colored_notification_color);
                int newColor = Palette.from(albumArt).resizeBitmapSize(100).generate().getVibrantColor(defaultColor);
                setBackgroundColor(newColor);
            }
        } else {
            notificationLayout.setImageViewResource(R.id.icon, R.drawable.default_album_art);
            notificationLayoutExpanded.setImageViewResource(R.id.icon, R.drawable.default_album_art);
            if (isColored) {
                int defaultColor = service.getResources().getColor(R.color.default_colored_notification_color);
                setBackgroundColor(defaultColor);
            }
        }

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void setBackgroundColor(int color) {
        notificationLayout.setInt(R.id.root, "setBackgroundColor", color);
        notificationLayoutExpanded.setInt(R.id.root, "setBackgroundColor", color);
    }

    public void killNotification() {
        if (isReceiverRegistered)
            service.unregisterReceiver(notificationColorPreferenceChangedReceiver);
        isReceiverRegistered = false;
        service.stopForeground(true);
        notification = null;
        isNotificationShown = false;
    }

    public void updatePlayState(final boolean isPlaying) {
        this.isPlaying = isPlaying;

        if (notification == null) {
            return;
        }
        if (notificationLayout != null) {
            notificationLayout.setImageViewResource(R.id.action_play_pause,
                    isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);
        }
        if (notificationLayoutExpanded != null) {
            notificationLayoutExpanded.setImageViewResource(R.id.action_play_pause,
                    isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp);
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
