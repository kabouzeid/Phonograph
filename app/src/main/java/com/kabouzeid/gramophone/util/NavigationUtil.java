package com.kabouzeid.gramophone.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.PlayingQueueDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.ui.activities.AlbumDetailActivity;
import com.kabouzeid.gramophone.ui.activities.ArtistDetailActivity;
import com.kabouzeid.gramophone.ui.activities.PlaylistDetailActivity;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class NavigationUtil {

    public static void goToArtist(final Activity activity, final int artistId, @Nullable final Pair[] sharedViews) {
        if (activity instanceof ArtistDetailActivity)
            return;
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();
            final Intent intent = new Intent(activity, ArtistDetailActivity.class);
            intent.putExtra(ArtistDetailActivity.EXTRA_ARTIST_ID, artistId);
            if (sharedViews != null) {
                @SuppressWarnings("unchecked") ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        sharedViews
                );
                ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
            } else {
                activity.startActivity(intent);
            }
        }
    }

    public static void goToAlbum(final Activity activity, final int albumId, @Nullable final Pair[] sharedViews) {
        if (activity instanceof AlbumDetailActivity)
            return;
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();
            final Intent intent = new Intent(activity, AlbumDetailActivity.class);
            intent.putExtra(AlbumDetailActivity.EXTRA_ALBUM_ID, albumId);
            if (sharedViews != null) {
                @SuppressWarnings("unchecked") ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        sharedViews
                );
                ActivityCompat.startActivityForResult(activity, intent, 1001, optionsCompat.toBundle());
            } else {
                activity.startActivity(intent);
            }
        }
    }

    public static void goToPlaylist(final Activity activity, final Playlist playlist, @Nullable final Pair[] sharedViews) {
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();

            final Intent intent;
            intent = new Intent(activity, PlaylistDetailActivity.class);
            intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST, playlist);

            if (sharedViews != null) {
                @SuppressWarnings("unchecked") ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        sharedViews
                );
                ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
            } else {
                activity.startActivity(intent);
            }
        }
    }

    public static void openPlayingQueueDialog(@NonNull final AppCompatActivity activity) {
        PlayingQueueDialog.create().show(activity.getSupportFragmentManager(), "PLAY_QUEUE");
    }

    public static void openEqualizer(@NonNull final Activity activity) {
        final int sessionId = MusicPlayerRemote.getAudioSessionId();
        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            Toast.makeText(activity, activity.getResources().getString(R.string.no_audio_ID), Toast.LENGTH_LONG).show();
        } else {
            try {
                final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId);
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                activity.startActivityForResult(effects, 0);
            } catch (@NonNull final ActivityNotFoundException notFound) {
                Toast.makeText(activity, activity.getResources().getString(R.string.no_equalizer), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
