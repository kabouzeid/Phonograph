package com.kabouzeid.gramophone.helper;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.glide.SongGlideRequest;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.PhonographColorUtil;
import com.kabouzeid.gramophone.util.PreferenceUtil;
import com.kabouzeid.gramophone.util.Util;

public class PlayingNotificationHelper {

    public static final String TAG = PlayingNotificationHelper.class.getSimpleName();

    private final MusicService service;

    private final NotificationManager notificationManager;
    private Notification notification;
    private int notificationId = 1;

    private RemoteViews notificationLayout;
    private RemoteViews notificationLayoutBig;

    private Song currentSong;
    private boolean isPlaying;

    private boolean isDark;
    private boolean isColored;

    private Target<BitmapPaletteWrapper> target;

    public PlayingNotificationHelper(@NonNull final MusicService service) {
        this.service = service;
        notificationManager = (NotificationManager) service
                .getSystemService(Context.NOTIFICATION_SERVICE);

        int bigNotificationImageSize = service.getResources().getDimensionPixelSize(R.dimen.notification_big_image_size);
        target = new SimpleTarget<BitmapPaletteWrapper>(bigNotificationImageSize, bigNotificationImageSize) {
            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                setAlbumCover(null, Color.TRANSPARENT);
            }

            @Override
            public void onResourceReady(BitmapPaletteWrapper resource, GlideAnimation<? super BitmapPaletteWrapper> glideAnimation) {
                setAlbumCover(resource.getBitmap(), PhonographColorUtil.getColor(resource.getPalette(), Color.TRANSPARENT));
            }
        };
    }

    public void updateNotification() {
        updateNotification(PreferenceUtil.getInstance(service).coloredNotification());
    }

    public void updateNotification(final boolean isColored) {
        Song song = service.getCurrentSong();
        if (song.id == -1) {
            service.stopForeground(true);
            return;
        }
        currentSong = song;
        this.isColored = isColored;
        this.isPlaying = service.isPlaying();

        notificationLayout = new RemoteViews(service.getPackageName(), R.layout.notification);
        notificationLayoutBig = new RemoteViews(service.getPackageName(), R.layout.notification_big);

        notification = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(getOpenMusicControllerPendingIntent())
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContent(notificationLayout)
                .build();

        notification.bigContentView = notificationLayoutBig;

        setUpCollapsedLayout();
        setUpExpandedLayout();
        loadAlbumArt();
        setUpPlaybackActions();
        setUpExpandedPlaybackActions();

        service.startForeground(notificationId, notification);
    }

    private PendingIntent getOpenMusicControllerPendingIntent() {
        Intent intent = new Intent(service, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setUpExpandedPlaybackActions() {
        notificationLayoutBig.setOnClickPendingIntent(R.id.action_play_pause,
                retrievePlaybackActions(1));

        notificationLayoutBig.setOnClickPendingIntent(R.id.action_next,
                retrievePlaybackActions(2));

        notificationLayoutBig.setOnClickPendingIntent(R.id.action_prev,
                retrievePlaybackActions(3));

        notificationLayoutBig.setOnClickPendingIntent(R.id.action_quit,
                retrievePlaybackActions(4));
    }

    private void setUpPlaybackActions() {
        notificationLayout.setOnClickPendingIntent(R.id.action_play_pause,
                retrievePlaybackActions(1));

        notificationLayout.setOnClickPendingIntent(R.id.action_next,
                retrievePlaybackActions(2));

        notificationLayout.setOnClickPendingIntent(R.id.action_prev,
                retrievePlaybackActions(3));
    }

    private PendingIntent retrievePlaybackActions(final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(service, MusicService.class);
        switch (which) {
            case 1:
                action = new Intent(MusicService.ACTION_TOGGLE_PAUSE);
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
        notificationLayout.setTextViewText(R.id.title, currentSong.title);
        notificationLayout.setTextViewText(R.id.text, currentSong.artistName);
        notificationLayout.setTextViewText(R.id.text2, currentSong.albumName);
    }

    private void setUpExpandedLayout() {
        notificationLayoutBig.setTextViewText(R.id.title, currentSong.title);
        notificationLayoutBig.setTextViewText(R.id.text, currentSong.artistName);
        notificationLayoutBig.setTextViewText(R.id.text2, currentSong.albumName);
    }

    private void loadAlbumArt() {
        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SongGlideRequest.Builder.from(Glide.with(service), currentSong)
                        .checkIgnoreMediaStore(service)
                        .generatePalette(service).build()
                        .into(target);
            }
        });
    }

    private void setAlbumCover(@Nullable Bitmap cover, int bgColor) {
        if (cover != null) {
            notificationLayout.setImageViewBitmap(R.id.icon, cover);
            notificationLayoutBig.setImageViewBitmap(R.id.icon, cover);
        } else {
            notificationLayout.setImageViewResource(R.id.icon, R.drawable.default_album_art);
            notificationLayoutBig.setImageViewResource(R.id.icon, R.drawable.default_album_art);
        }

        if (!isColored) {
            bgColor = Color.TRANSPARENT;
        }
        setBackgroundColor(bgColor);
        setDarkNotificationContent(bgColor == Color.TRANSPARENT ? Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP : ColorUtil.isColorLight(bgColor));

        if (notification != null) {
            notificationManager.notify(notificationId, notification);
        }
    }

    private void setBackgroundColor(int color) {
        notificationLayout.setInt(R.id.root, "setBackgroundColor", color);
        notificationLayoutBig.setInt(R.id.root, "setBackgroundColor", color);
    }

    public void killNotification() {
        service.stopForeground(true);
        notification = null;
    }

    public void updatePlayState(final boolean setPlaying) {
        isPlaying = setPlaying;

        if (notification == null) {
            updateNotification();
        }
        setPlayPauseDrawable();
        if (notification != null) {
            notificationManager.notify(notificationId, notification);
        }
    }

    private void setDarkNotificationContent(boolean dark) {
        isDark = dark;
        setPlayPauseDrawable();

        if (notificationLayout != null && notificationLayoutBig != null) {
            int primary = MaterialValueHelper.getPrimaryTextColor(service, dark);
            int secondary = MaterialValueHelper.getSecondaryTextColor(service, dark);

            Bitmap prev = createBitmap(Util.getTintedDrawable(service, R.drawable.ic_skip_previous_white_24dp, primary), 1.5f);
            Bitmap next = createBitmap(Util.getTintedDrawable(service, R.drawable.ic_skip_next_white_24dp, primary), 1.5f);
            Bitmap close = createBitmap(Util.getTintedDrawable(service, R.drawable.ic_close_white_24dp, secondary), 1f);

            notificationLayout.setTextColor(R.id.title, primary);
            notificationLayout.setTextColor(R.id.text, secondary);
            notificationLayout.setImageViewBitmap(R.id.action_prev, prev);
            notificationLayout.setImageViewBitmap(R.id.action_next, next);

            notificationLayoutBig.setTextColor(R.id.title, primary);
            notificationLayoutBig.setTextColor(R.id.text, secondary);
            notificationLayoutBig.setTextColor(R.id.text2, secondary);
            notificationLayoutBig.setImageViewBitmap(R.id.action_prev, prev);
            notificationLayoutBig.setImageViewBitmap(R.id.action_next, next);
            notificationLayoutBig.setImageViewBitmap(R.id.action_quit, close);
        }
    }

    private void setPlayPauseDrawable() {
        Bitmap playPause = createBitmap(Util.getTintedDrawable(service, isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp, MaterialValueHelper.getPrimaryTextColor(service, isDark)), 1.5f);
        if (notificationLayout != null) {
            notificationLayout.setImageViewBitmap(R.id.action_play_pause, playPause);
        }
        if (notificationLayoutBig != null) {
            notificationLayoutBig.setImageViewBitmap(R.id.action_play_pause, playPause);
        }
    }

    private static Bitmap createBitmap(Drawable drawable, float sizeMultiplier) {
        Bitmap bitmap = Bitmap.createBitmap((int) (drawable.getIntrinsicWidth() * sizeMultiplier), (int) (drawable.getIntrinsicHeight() * sizeMultiplier), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        drawable.setBounds(0, 0, c.getWidth(), c.getHeight());
        drawable.draw(c);
        return bitmap;
    }
}
