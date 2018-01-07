package com.kabouzeid.gramophone.service.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.app.NotificationCompat.MediaStyle;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.ui.activities.SleepTimerActivity;
import com.kabouzeid.gramophone.glide.SongGlideRequest;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import static com.kabouzeid.gramophone.service.MusicService.ACTION_REWIND;
import static com.kabouzeid.gramophone.service.MusicService.ACTION_SKIP;
import static com.kabouzeid.gramophone.service.MusicService.ACTION_TOGGLE_PAUSE;

public class PlayingNotificationImpl24 extends PlayingNotification {

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
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PendingIntent clickIntent = PendingIntent.getActivity(service, 0, action, 0);
        final PendingIntent deleteIntent = PendingIntent.getService(service, 1, stopPlayingIntent(), 0);
        PendingIntent sleepTimerIntent = PendingIntent.getActivity(service, 0, new Intent(service, SleepTimerActivity.class), 0);

        final int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        service.runOnUiThread(() -> SongGlideRequest.Builder.from(Glide.with(service), song)
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

                        Notification notification;

                        if (isSleepTimerActive()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                notification = getChronoNotification(bitmap, color);
                            } else {
                                notification = getLegacyNotification(bitmap, color);
                            }
                        } else {
                            notification = getLegacyNotification(bitmap, color);
                        }

                        if (stopped)
                            return; // notification has been stopped before loading was finished
                        updateNotifyModeAndPostNotification(notification);
                    }

                    @RequiresApi(26)
                    Notification getChronoNotification(Bitmap bitmap, int color) {
                        long time = PreferenceUtil.getInstance(service).getNextSleepTimerElapsedRealTime();
                        Notification.Action playPauseAction = new Notification.Action(playButtonResId,
                                service.getString(R.string.action_play_pause),
                                retrievePlaybackAction(ACTION_TOGGLE_PAUSE));
                        Notification.Action previousAction = new Notification.Action(R.drawable.ic_skip_previous_white_24dp,
                                service.getString(R.string.action_previous),
                                retrievePlaybackAction(ACTION_REWIND));
                        Notification.Action nextAction = new Notification.Action(R.drawable.ic_skip_next_white_24dp,
                                service.getString(R.string.action_next),
                                retrievePlaybackAction(ACTION_SKIP));
                        Notification.Action sleepTimerAction = new Notification.Action(R.drawable.ic_timer_white_24dp,
                                service.getString(R.string.action_sleep_timer),
                                sleepTimerIntent);
                        Notification.Builder builder = new Notification.Builder(service, NOTIFICATION_CHANNEL_ID)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(bitmap)
                                .setContentIntent(clickIntent)
                                .setDeleteIntent(deleteIntent)
                                .setContentTitle(song.title)
                                .setContentText(text)
                                .setOngoing(isPlaying)
                                .setShowWhen(isPlaying)
                                .setUsesChronometer(true)
                                .setChronometerCountDown(true)
                                .setWhen(System.currentTimeMillis() + (time - SystemClock.elapsedRealtime()))
                                .addAction(previousAction)
                                .addAction(playPauseAction)
                                .addAction(nextAction)
                                .addAction(sleepTimerAction)
                                .setStyle(new Notification.MediaStyle().setMediaSession((android.media.session.MediaSession.Token)service.getMediaSession().getSessionToken().getToken()).setShowActionsInCompactView(0, 1, 2));

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && PreferenceUtil.getInstance(service).coloredNotification()) {
                            builder.setColor(color);
                        }
                        return builder.build();
                    }

                    Notification getLegacyNotification(Bitmap bitmap, int color) {
                        NotificationCompat.Action playPauseAction = new NotificationCompat.Action(playButtonResId,
                                service.getString(R.string.action_play_pause),
                                retrievePlaybackAction(ACTION_TOGGLE_PAUSE));
                        NotificationCompat.Action previousAction = new NotificationCompat.Action(R.drawable.ic_skip_previous_white_24dp,
                                service.getString(R.string.action_previous),
                                retrievePlaybackAction(ACTION_REWIND));
                        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_skip_next_white_24dp,
                                service.getString(R.string.action_next),
                                retrievePlaybackAction(ACTION_SKIP));
                        NotificationCompat.Action sleepTimerAction = new NotificationCompat.Action(R.drawable.ic_timer_white_24dp,
                                service.getString(R.string.action_sleep_timer),
                                sleepTimerIntent);
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
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
                                .addAction(nextAction)
                                .addAction(sleepTimerAction);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder.setStyle(new MediaStyle().setMediaSession(service.getMediaSession().getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && PreferenceUtil.getInstance(service).coloredNotification())
                                builder.setColor(color);
                        }
                        return builder.build();
                    }
                }));
    }

    private PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        return PendingIntent.getService(service, 0, intent, 0);
    }

    private boolean isSleepTimerActive() {
        return PendingIntent.getService(service, 0, stopPlayingIntent(), PendingIntent.FLAG_NO_CREATE) != null;
    }

    private Intent stopPlayingIntent() {
        return new Intent(service, MusicService.class).setAction(MusicService.ACTION_QUIT);
    }
}
