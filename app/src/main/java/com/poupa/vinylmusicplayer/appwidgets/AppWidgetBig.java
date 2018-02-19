package com.poupa.vinylmusicplayer.appwidgets;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.appwidgets.base.BaseAppWidget;
import com.poupa.vinylmusicplayer.glide.SongGlideRequest;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.service.MusicService;
import com.poupa.vinylmusicplayer.ui.activities.MainActivity;
import com.poupa.vinylmusicplayer.util.Util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class AppWidgetBig extends BaseAppWidget {
    public static final String NAME = "app_widget_big";

    private static AppWidgetBig mInstance;
    private Target<Bitmap> target; // for cancellation

    public static synchronized AppWidgetBig getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetBig();
        }
        return mInstance;
    }

    /**
     * Initialize given widgets to default state, where we launch Music on
     * default click and hide actions if service not running.
     */
    protected void defaultAppWidget(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.app_widget_big);

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art);
        appWidgetView.setImageViewBitmap(R.id.button_next, createBitmap(Util.getTintedVectorDrawable(context, R.drawable.ic_skip_next_white_24dp, MaterialValueHelper.getPrimaryTextColor(context, false)), 1f));
        appWidgetView.setImageViewBitmap(R.id.button_prev, createBitmap(Util.getTintedVectorDrawable(context, R.drawable.ic_skip_previous_white_24dp, MaterialValueHelper.getPrimaryTextColor(context, false)), 1f));
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, createBitmap(Util.getTintedVectorDrawable(context, R.drawable.ic_play_arrow_white_24dp, MaterialValueHelper.getPrimaryTextColor(context, false)), 1f));

        linkButtons(context, appWidgetView);
        pushUpdate(context, appWidgetIds, appWidgetView);
    }

    /**
     * Update all active widget instances by pushing changes
     */
    public void performUpdate(final MusicService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_big);

        final boolean isPlaying = service.isPlaying();
        final Song song = service.getCurrentSong();

        // Set the titles and artwork
        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
            appWidgetView.setTextViewText(R.id.title, song.title);
            appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song));
        }

        // Set correct drawable for pause state
        int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, createBitmap(Util.getTintedVectorDrawable(service, playPauseRes, MaterialValueHelper.getPrimaryTextColor(service, false)), 1f));

        // Set prev/next button drawables
        appWidgetView.setImageViewBitmap(R.id.button_next, createBitmap(Util.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp, MaterialValueHelper.getPrimaryTextColor(service, false)), 1f));
        appWidgetView.setImageViewBitmap(R.id.button_prev, createBitmap(Util.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp, MaterialValueHelper.getPrimaryTextColor(service, false)), 1f));

        // Link actions buttons to intents
        linkButtons(service, appWidgetView);

        // Load the album cover async and push the update on completion
        Point p = Util.getScreenSize(service);
        final int widgetImageSize = Math.min(p.x, p.y);
        final Context appContext = service.getApplicationContext();
        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (target != null) {
                    Glide.with(appContext).clear(target);
                }
                target = SongGlideRequest.Builder.from(Glide.with(appContext), song)
                        .checkIgnoreMediaStore(appContext)
                        .asBitmap()
                        .build()
                        .into(new SimpleTarget<Bitmap>(widgetImageSize, widgetImageSize) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap bitmap, Transition<? super Bitmap> transition) {
                                update(bitmap);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                update(null);
                            }

                            private void update(@Nullable Bitmap bitmap) {
                                if (bitmap == null) {
                                    appWidgetView.setImageViewResource(R.id.image, R.drawable.default_album_art);
                                } else {
                                    appWidgetView.setImageViewBitmap(R.id.image, bitmap);
                                }
                                pushUpdate(appContext, appWidgetIds, appWidgetView);
                            }
                        });
            }
        });
    }

    /**
     * Link up various button actions using {@link PendingIntent}.
     */
    private void linkButtons(final Context context, final RemoteViews views) {
        Intent action;
        PendingIntent pendingIntent;

        final ComponentName serviceName = new ComponentName(context, MusicService.class);

        // Home
        action = new Intent(context, MainActivity.class);
        action.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pendingIntent = PendingIntent.getActivity(context, 0, action, 0);
        views.setOnClickPendingIntent(R.id.clickable_area, pendingIntent);

        // Previous track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_REWIND, serviceName);
        views.setOnClickPendingIntent(R.id.button_prev, pendingIntent);

        // Play and pause
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_TOGGLE_PAUSE, serviceName);
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, pendingIntent);

        // Next track
        pendingIntent = buildPendingIntent(context, MusicService.ACTION_SKIP, serviceName);
        views.setOnClickPendingIntent(R.id.button_next, pendingIntent);
    }
}
