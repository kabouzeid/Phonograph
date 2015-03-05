package com.kabouzeid.gramophone.helper;

/**
 * Created by karim on 27.12.14.
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MusicControllerActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PlayingNotificationHelper {
    public static final String TAG = PlayingNotificationHelper.class.getSimpleName();
    public static final int NOTIFICATION_ID = 1337;

    private final MusicService service;

    private final NotificationManager notificationManager;
    private Notification notification = null;

    private RemoteViews notificationLayout;
    private RemoteViews notificationLayoutExpanded;

    public PlayingNotificationHelper(final MusicService service) {
        this.service = service;
        notificationManager = (NotificationManager) service
                .getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void buildNotification(Song song, final boolean isPlaying) {
        notificationLayout = new RemoteViews(service.getPackageName(),
                R.layout.notification_playing);
        notificationLayoutExpanded = new RemoteViews(service.getPackageName(),
                R.layout.notification_playing_expanded);

        setUpCollapsedLayout(song);
        setUpExpandedLayout(song);
        setUpPlaybackActions(isPlaying);
        setUpExpandedPlaybackActions(isPlaying);

        notification = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(getOpenMusicControllerPendingIntent())
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(notificationLayout)
                .build();
        notification.bigContentView = notificationLayoutExpanded;

        service.startForeground(NOTIFICATION_ID, notification);
    }

    private PendingIntent getOpenMusicControllerPendingIntent() {
        Intent result = new Intent(service, MusicControllerActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(service);
        taskStackBuilder.addParentStack(MusicControllerActivity.class);
        taskStackBuilder.addNextIntent(result);
        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setUpExpandedPlaybackActions(boolean isPlaying) {
        notificationLayoutExpanded.setOnClickPendingIntent(R.id.button_toggle_play_pause,
                retrievePlaybackActions(1));

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.button_next,
                retrievePlaybackActions(2));

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.button_prev,
                retrievePlaybackActions(3));

        notificationLayoutExpanded.setOnClickPendingIntent(R.id.button_quit,
                retrievePlaybackActions(4));

        notificationLayoutExpanded.setImageViewResource(R.id.button_toggle_play_pause,
                isPlaying ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp);
    }

    private void setUpPlaybackActions(boolean isPlaying) {
        notificationLayout.setOnClickPendingIntent(R.id.button_toggle_play_pause,
                retrievePlaybackActions(1));

        notificationLayout.setOnClickPendingIntent(R.id.button_next,
                retrievePlaybackActions(2));

        notificationLayout.setOnClickPendingIntent(R.id.button_quit,
                retrievePlaybackActions(4));

        notificationLayout.setImageViewResource(R.id.button_toggle_play_pause,
                isPlaying ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp);
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

    private void setUpCollapsedLayout(Song song) {
        loadAlbumArt(notificationLayout, MusicUtil.getAlbumArtUri(song.albumId).toString());
        notificationLayout.setTextViewText(R.id.song_title, song.title);
        notificationLayout.setTextViewText(R.id.song_artist, song.title);
    }

    private static void loadAlbumArt(RemoteViews notificationView, String albumArtUri) {
        Bitmap albumArtBitmap = ImageLoader.getInstance().loadImageSync(albumArtUri);
        if (albumArtBitmap == null) {
            notificationView.setImageViewResource(R.id.album_art, R.drawable.default_album_art);
        } else {
            notificationView.setImageViewBitmap(R.id.album_art, albumArtBitmap);
        }
    }

    private void setUpExpandedLayout(Song song) {
        loadAlbumArt(notificationLayoutExpanded, MusicUtil.getAlbumArtUri(song.albumId).toString());
        notificationLayoutExpanded.setTextViewText(R.id.song_title, song.title);
        notificationLayoutExpanded.setTextViewText(R.id.song_artist, song.artistName);
        notificationLayoutExpanded.setTextViewText(R.id.album_title, song.albumName);
    }

    public void killNotification() {
        service.stopForeground(true);
        notification = null;
    }

    public void updatePlayState(final boolean isPlaying) {
        if (notification == null || notificationManager == null) {
            return;
        }
        if (notificationLayout != null) {
            notificationLayout.setImageViewResource(R.id.button_toggle_play_pause,
                    isPlaying ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp);
        }
        if (notificationLayoutExpanded != null) {
            notificationLayoutExpanded.setImageViewResource(R.id.button_toggle_play_pause,
                    isPlaying ? R.drawable.ic_pause_white_48dp : R.drawable.ic_play_arrow_white_48dp);
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
