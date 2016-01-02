package com.kabouzeid.gramophone.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.MusicUtil;

public class WidgetMedium extends AppWidgetProvider {
    private static RemoteViews widgetLayout;

    public static void updateWidgets(@NonNull final Context context, @NonNull final Song song, boolean isPlaying) {
        if (song.id == -1) return;
        widgetLayout = new RemoteViews(context.getPackageName(), R.layout.widget_medium);
        linkButtons(context, widgetLayout);
        widgetLayout.setTextViewText(R.id.title, song.title);
        widgetLayout.setTextViewText(R.id.song_secondary_information, song.artistName + " | " + song.albumName);

        updateWidgetsPlayState(context, isPlaying);
        loadAlbumCover(context, song);
    }

    public static void updateWidgetsPlayState(@NonNull final Context context, boolean isPlaying) {
        if (widgetLayout == null)
            widgetLayout = new RemoteViews(context.getPackageName(), R.layout.widget_medium);
        int playPauseRes = isPlaying ? R.drawable.ic_pause_dark_36dp : R.drawable.ic_play_arrow_dark_36dp;
        widgetLayout.setImageViewResource(R.id.button_toggle_play_pause, playPauseRes);
        updateWidgets(context);
    }

    private static void updateWidgets(@NonNull final Context context) {
        AppWidgetManager man = AppWidgetManager.getInstance(context);
        int[] ids = man.getAppWidgetIds(
                new ComponentName(context, WidgetMedium.class));
        for (int widgetId : ids) {
            man.updateAppWidget(widgetId, widgetLayout);
        }
    }

    private static Handler uiThreadHandler;
    private static Target<Bitmap> target;

    private static void loadAlbumCover(@NonNull final Context context, @Nullable final Song song) {
        if (song == null) return;

        if (uiThreadHandler == null) {
            uiThreadHandler = new Handler(Looper.getMainLooper());
        }
        if (target == null) {
            int widgetImageSize = context.getResources().getDimensionPixelSize(R.dimen.widget_medium_image_size);
            target = new SimpleTarget<Bitmap>(widgetImageSize, widgetImageSize) {
                @Override
                public void onLoadFailed(Exception e, Drawable errorDrawable) {
                    super.onLoadFailed(e, errorDrawable);
                    setAlbumCover(context, null);
                }

                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    setAlbumCover(context, resource);
                }
            };
        }

        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Glide.with(context)
                        .loadFromMediaStore(MusicUtil.getAlbumArtUri(song.albumId))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(target);
            }
        });
    }

    private static void setAlbumCover(@NonNull final Context context, @Nullable final Bitmap albumArt) {
        if (albumArt != null) {
            widgetLayout.setImageViewBitmap(R.id.image, albumArt);
        } else {
            widgetLayout.setImageViewResource(R.id.image, R.drawable.default_album_art);
        }
        updateWidgets(context);
    }

    private static void linkButtons(@NonNull final Context context, @NonNull final RemoteViews views) {
        views.setOnClickPendingIntent(R.id.image, retrievePlaybackActions(context, 0));
        views.setOnClickPendingIntent(R.id.media_titles, retrievePlaybackActions(context, 0));
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, retrievePlaybackActions(context, 1));
        views.setOnClickPendingIntent(R.id.button_next, retrievePlaybackActions(context, 2));
        views.setOnClickPendingIntent(R.id.button_prev, retrievePlaybackActions(context, 3));
    }

    private static PendingIntent retrievePlaybackActions(@NonNull final Context context, final int which) {
        final ComponentName serviceName = new ComponentName(context, MusicService.class);
        Intent intent;
        switch (which) {
            case 0:
                intent = new Intent(context, MainActivity.class);
                return PendingIntent.getActivity(context, 0, intent, 0);
            case 1:
                intent = new Intent(MusicService.ACTION_TOGGLE_PAUSE);
                intent.setComponent(serviceName);
                return PendingIntent.getService(context, 1, intent, 0);
            case 2:
                intent = new Intent(MusicService.ACTION_SKIP);
                intent.setComponent(serviceName);
                return PendingIntent.getService(context, 2, intent, 0);
            case 3:
                intent = new Intent(MusicService.ACTION_REWIND);
                intent.setComponent(serviceName);
                return PendingIntent.getService(context, 3, intent, 0);
        }
        return null;
    }

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        updateWidgets(context, MusicPlayerRemote.getCurrentSong(), MusicPlayerRemote.isPlaying());
        for (int widgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(widgetId, widgetLayout);
        }
    }
}


