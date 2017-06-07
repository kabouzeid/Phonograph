package com.kabouzeid.gramophone.service.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.glide.SongGlideRequest;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.kabouzeid.gramophone.service.MusicService.ACTION_REWIND;
import static com.kabouzeid.gramophone.service.MusicService.ACTION_SKIP;
import static com.kabouzeid.gramophone.service.MusicService.ACTION_TOGGLE_PAUSE;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public class PlayingNotificationImpl24 implements PlayingNotification {
    private static final int NOTIFY_MODE_FOREGROUND = 1;
    private static final int NOTIFY_MODE_BACKGROUND = 0;

    private MusicService service;

    private NotificationManager notificationManager;

    private int notifyMode = NOTIFY_MODE_BACKGROUND;

    private boolean stopped;

    @Override
    public synchronized void init(MusicService service) {
        this.service = service;
        notificationManager = (NotificationManager) service.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = service.getCurrentSong();

        final String albumName = song.albumName;
        final String artistName = song.artistName;
        final boolean isPlaying = service.isPlaying();
        final String text = TextUtils.isEmpty(albumName)
                ? artistName : artistName + " - " + albumName;

        final int playButtonResId = isPlaying
                ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;

        Intent action = new Intent(service, MainActivity.class);
        action.putExtra(MusicService.OPEN_NOW_PLAYING, true);
        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);

        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(MusicService.ACTION_QUIT);
        intent.setComponent(serviceName);
        final PendingIntent deleteIntent = PendingIntent.getService(service, 0, intent, 0);

        final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SongGlideRequest.Builder.from(Glide.with(service), song)
                        .checkIgnoreMediaStore(service)
                        .generatePalette(service).build()
                        .into(new SimpleTarget<BitmapPaletteWrapper>(bigNotificationImageSize, bigNotificationImageSize) {
                            @Override
                            public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                                Palette palette = resource.getPalette();
                                update(resource.getBitmap(), palette.getVibrantColor(palette.getMutedColor(Color.TRANSPARENT)));
                            }

                            @Override
                            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                                update(null, Color.TRANSPARENT);
                            }

                            void update(Bitmap bitmap, int color) {
                                if (bitmap == null)
                                    bitmap = BitmapFactory.decodeResource(service.getResources(), R.drawable.default_album_art);
                                NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                                        service.getString(R.string.action_play_pause),
                                        retrievePlaybackAction(ACTION_TOGGLE_PAUSE));
                                NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp,
                                        service.getString(R.string.action_previous),
                                        retrievePlaybackAction(ACTION_REWIND));
                                NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp,
                                        service.getString(R.string.action_next),
                                        retrievePlaybackAction(ACTION_SKIP));
                                NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(service)
                                        .setSmallIcon(R.drawable.ic_notification)
                                        .setLargeIcon(bitmap)
                                        .setContentIntent(clickIntent)
                                        .setDeleteIntent(deleteIntent)
                                        .setContentTitle(song.title)
                                        .setContentText(text)
                                        .setOngoing(isPlaying)
                                        .setShowWhen(false)
                                        .addAction(previousAction)
                                        .addAction(playPauseAction)
                                        .addAction(nextAction);

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    builder.setStyle(new NotificationCompat.MediaStyle().setMediaSession(service.getMediaSession().getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                                    if (PreferenceUtil.getInstance(service).coloredNotification())
                                        builder.setColor(color);
                                }

                                if (stopped)
                                    return; // notification has been stopped before loading was finished
                                updateNotifyModeAndPostNotification(builder.build());
                            }
                        });
            }
        });
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);

        return PendingIntent.getService(service, 0, intent, 0);
    }

    private void updateNotifyModeAndPostNotification(Notification notification) {
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

    @Override
    public synchronized void stop() {
        stopped = true;
        service.stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}
