package com.kabouzeid.gramophone.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MusicControllerActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Implementation of App Widget functionality.
 */
public class MusicPlayerWidget extends AppWidgetProvider {
    private static MusicPlayerWidget instance;

    public static synchronized MusicPlayerWidget getInstance() {
        if (instance == null) {
            instance = new MusicPlayerWidget();
        }
        return instance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.music_player_widget);

        appWidgetManager.updateAppWidget(appWidgetIds, views);
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private void linkButtons(final Context context, final RemoteViews views) {
        views.setOnClickPendingIntent(R.id.album_art, retrievePlaybackActions(context, 0));
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, retrievePlaybackActions(context, 1));
        views.setOnClickPendingIntent(R.id.button_next, retrievePlaybackActions(context, 2));
        views.setOnClickPendingIntent(R.id.button_prev, retrievePlaybackActions(context, 3));
    }

    private PendingIntent retrievePlaybackActions(final Context context, final int which) {
        Intent action;
        PendingIntent pendingIntent;
        final ComponentName serviceName = new ComponentName(context, MusicService.class);
        switch (which) {
            case 0:
                action = new Intent(context, MusicControllerActivity.class);
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

    public void performUpdate(final  MusicService service, final Song song){
        final RemoteViews views = new RemoteViews(service.getPackageName(), R.layout.music_player_widget);
        linkButtons(service, views);
        loadAlbumArt(views, MusicUtil.getAlbumArtUri(song.albumId).toString());
        views.setTextViewText(R.id.song_title, song.title);
    }

    private static void loadAlbumArt(RemoteViews widgetView, String albumArtUri) {
        Bitmap albumArtBitmap = ImageLoader.getInstance().loadImageSync(albumArtUri);
        if (albumArtBitmap == null) {
            widgetView.setImageViewResource(R.id.album_art, R.drawable.default_album_art);
        } else {
            widgetView.setImageViewBitmap(R.id.album_art, albumArtBitmap);
        }
    }
}


