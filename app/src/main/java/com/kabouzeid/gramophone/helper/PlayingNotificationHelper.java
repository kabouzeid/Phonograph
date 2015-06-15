package com.kabouzeid.gramophone.helper;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.support.v7.graphics.Palette;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.service.MusicService;
import com.kabouzeid.gramophone.ui.activities.MusicControllerActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PreferenceUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PlayingNotificationHelper {

    public static final String TAG = PlayingNotificationHelper.class.getSimpleName();

    public static Notification buildNotification(final Context context, MediaSessionCompat.Token sessionToken, final Song song, final boolean isPlaying) {

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(0, 1, 2);

        style.setShowCancelButton(true);
        style.setCancelButtonIntent(retrievePlaybackAction(context, 3));

        Bitmap albumArt = ImageLoader.getInstance().loadImageSync(MusicUtil.getAlbumArtUri(song.albumId).toString());
        if (albumArt == null) {
            albumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art);
        }
        int notificationColor = PreferenceUtils.getInstance(context).coloredNotification() ?
                Palette.from(albumArt).generate().getVibrantColor(Color.TRANSPARENT) :
                Color.TRANSPARENT;

        return new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(albumArt)
                .setContentIntent(getOpenMusicControllerPendingIntent(context))
                .setContentTitle(song.title)
                .setContentText(song.artistName)
                .setSubText(song.albumName)
                .setWhen(0)
                .setShowWhen(false)
                .setStyle(style)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                .addAction(R.drawable.ic_skip_previous_white_36dp,
                        "",
                        retrievePlaybackAction(context, 2))

                .addAction(isPlaying ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_arrow_white_36dp,
                        "",
                        retrievePlaybackAction(context, 0))

                .addAction(R.drawable.ic_skip_next_white_36dp,
                        "",
                        retrievePlaybackAction(context, 1))

                .setOnlyAlertOnce(true)
                .setColor(notificationColor)
                .build();
    }

    private static PendingIntent getOpenMusicControllerPendingIntent(final Context context) {
        Intent result = new Intent(context, MusicControllerActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MusicControllerActivity.class);
        taskStackBuilder.addNextIntent(result);
        return taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent retrievePlaybackAction(final Context context, final int which) {
        String actionString = null;
        switch (which) {
            case 0:
                actionString = MusicService.ACTION_TOGGLE_PLAYBACK;
                break;
            case 1:
                actionString = MusicService.ACTION_SKIP;
                break;
            case 2:
                actionString = MusicService.ACTION_REWIND;
                break;
            case 3:
                actionString = MusicService.ACTION_QUIT;
                break;
        }
        if (actionString != null) {
            final ComponentName serviceName = new ComponentName(context, MusicService.class);
            Intent actionIntent = new Intent(actionString);
            actionIntent.setComponent(serviceName);
            return PendingIntent.getService(context, 0, actionIntent, 0);
        }
        return null;
    }
}
