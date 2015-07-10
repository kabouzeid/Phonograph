package com.kabouzeid.gramophone.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RemoteViews;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MainActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

public class WidgetMedium extends AppWidgetProvider {
    private static RemoteViews widgetLayout;
    private static String currentAlbumArtUri;

    @Override
    public void onUpdate(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, @NonNull int[] appWidgetIds) {
        updateWidgets(context, MusicPlayerRemote.getCurrentSong(), MusicPlayerRemote.isPlaying());
        for (int widgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(widgetId, widgetLayout);
        }
    }

    public static void updateWidgets(@NonNull final Context context, @NonNull final Song song, boolean isPlaying) {
        if (song.id == -1) return;
        widgetLayout = new RemoteViews(context.getPackageName(), R.layout.widget_medium);
        linkButtons(context, widgetLayout);
        widgetLayout.setTextViewText(R.id.song_title, song.title);
        widgetLayout.setTextViewText(R.id.song_secondary_information, song.artistName + " | " + song.albumName);
        updateWidgetsPlayState(context, isPlaying);
        loadAlbumArt(context, song);
    }

    public static void updateWidgetsPlayState(@NonNull final Context context, boolean isPlaying) {
        if (widgetLayout == null)
            widgetLayout = new RemoteViews(context.getPackageName(), R.layout.widget_medium);
        int playPauseRes = isPlaying ? R.drawable.ic_pause_black_36dp : R.drawable.ic_play_arrow_black_36dp;
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

    private static void loadAlbumArt(@NonNull final Context context, @Nullable final Song song) {
        if (song != null) {
            int widgetImageSize = context.getResources().getDimensionPixelSize(R.dimen.widget_medium_image_size);
            currentAlbumArtUri = MusicUtil.getSongImageLoaderString(song);
            ImageLoader.getInstance().displayImage(
                    currentAlbumArtUri,
                    new NonViewAware(new ImageSize(widgetImageSize, widgetImageSize), ViewScaleType.CROP),
                    new DisplayImageOptions.Builder()
                            .postProcessor(new BitmapProcessor() {
                                @Override
                                public Bitmap process(Bitmap bitmap) {
                                    // The RemoteViews might wants to recycle the bitmaps thrown at it, so we need
                                    // to make sure not to hand out our cache copy
                                    Bitmap.Config config = bitmap.getConfig();
                                    if (config == null) {
                                        config = Bitmap.Config.ARGB_8888;
                                    }
                                    bitmap = bitmap.copy(config, false);
                                    return bitmap.copy(bitmap.getConfig(), true);
                                }
                            }).build(),
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, @Nullable Bitmap loadedImage) {
                            if (currentAlbumArtUri.equals(imageUri)) {
                                setAlbumArt(context, loadedImage);
                            }
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            if (currentAlbumArtUri.equals(imageUri)) {
                                setAlbumArt(context, null);
                            }
                        }
                    });
        }
    }

    private static void setAlbumArt(@NonNull final Context context, @Nullable final Bitmap albumArt) {
        if (albumArt != null) {
            widgetLayout.setImageViewBitmap(R.id.album_art, albumArt);
        } else {
            widgetLayout.setImageViewResource(R.id.album_art, R.drawable.default_album_art);
        }
        updateWidgets(context);
    }

    private static void linkButtons(@NonNull final Context context, @NonNull final RemoteViews views) {
        views.setOnClickPendingIntent(R.id.album_art, retrievePlaybackActions(context, 0));
        views.setOnClickPendingIntent(R.id.media_titles, retrievePlaybackActions(context, 0));
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, retrievePlaybackActions(context, 1));
        views.setOnClickPendingIntent(R.id.button_next, retrievePlaybackActions(context, 2));
        views.setOnClickPendingIntent(R.id.button_prev, retrievePlaybackActions(context, 3));
    }

    private static PendingIntent retrievePlaybackActions(@NonNull final Context context, final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(context, MusicService.class);
        switch (which) {
            case 0:
                action = new Intent(context, MainActivity.class);
                pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
                return pendingIntent;
            case 1:
                action = new Intent(MusicService.ACTION_TOGGLE_PLAYBACK);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(context, 1, action, 0);
                return pendingIntent;
            case 2:
                action = new Intent(MusicService.ACTION_SKIP);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(context, 2, action, 0);
                return pendingIntent;
            case 3:
                action = new Intent(MusicService.ACTION_REWIND);
                action.setComponent(serviceName);
                pendingIntent = PendingIntent.getService(context, 3, action, 0);
                return pendingIntent;
        }
        return null;
    }
}


