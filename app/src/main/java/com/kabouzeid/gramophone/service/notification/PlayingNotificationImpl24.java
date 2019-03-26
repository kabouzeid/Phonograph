package com.kabouzeid.gramophone.service.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.glide.SongGlideRequest;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.SleepTimerUtil;

import static com.kabouzeid.gramophone.service.MusicService.ACTION_REWIND;
import static com.kabouzeid.gramophone.service.MusicService.ACTION_SKIP;
import static com.kabouzeid.gramophone.service.MusicService.ACTION_TOGGLE_PAUSE;

@RequiresApi(24)
public class PlayingNotificationImpl24 extends PlayingNotification {

    @Override
    public synchronized void update() {
        stopped = false;

        final Song song = service.getCurrentSong();

        final boolean isPlaying = service.isPlaying();
        final String text = MusicUtil.getSongInfoString(song);

        final int playButtonResId = isPlaying
                ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;

        final PendingIntent clickIntent = clickAction();
        final PendingIntent deleteIntent = deleteAction();
        PendingIntent sleepTimerIntent = sleepTimerAction();

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
                        long time = PreferenceUtil.getInstance(service).getNextSleepTimerElapsedRealTime();
                        boolean sleepTimer = SleepTimerUtil.isTimerRunning(service);
                        Notification.Action playPauseAction = new Notification.Action(playButtonResId,
                                service.getString(R.string.action_play_pause),
                                playbackAction(ACTION_TOGGLE_PAUSE));
                        Notification.Action previousAction = new Notification.Action(R.drawable.ic_skip_previous_white_24dp,
                                service.getString(R.string.action_previous),
                                playbackAction(ACTION_REWIND));
                        Notification.Action nextAction = new Notification.Action(R.drawable.ic_skip_next_white_24dp,
                                service.getString(R.string.action_next),
                                playbackAction(ACTION_SKIP));
                        Notification.Action sleepTimerAction = new Notification.Action(R.drawable.ic_timer_white_24dp,
                                service.getString(R.string.action_sleep_timer),
                                sleepTimerIntent);
                        Notification.Builder builder = builder()
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(bitmap)
                                .setContentIntent(clickIntent)
                                .setDeleteIntent(deleteIntent)
                                .setContentTitle(song.title)
                                .setContentText(text)
                                .setOngoing(isPlaying)
                                .setShowWhen(sleepTimer)
                                .setUsesChronometer(true)
                                .setChronometerCountDown(true)
                                .setWhen(sleepTimer ? System.currentTimeMillis() + (time - SystemClock.elapsedRealtime()) : 0)
                                .addAction(previousAction)
                                .addAction(playPauseAction)
                                .addAction(nextAction)
                                .addAction(sleepTimerAction)
                                .setStyle(new Notification.MediaStyle().setMediaSession((android.media.session.MediaSession.Token)service.getMediaSession().getSessionToken().getToken()).setShowActionsInCompactView(0, 1, 2));

                        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O && PreferenceUtil.getInstance(service).coloredNotification()) {
                            builder.setColor(color);
                        }

                        if (stopped)
                            return; // notification has been stopped before loading was finished
                        updateNotifyModeAndPostNotification(builder.build());
                    }
                }));
    }

    @NonNull
    private Notification.Builder builder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Notification.Builder(service, NOTIFICATION_CHANNEL_ID);
        }
        return new Notification.Builder(service);
    }
}
