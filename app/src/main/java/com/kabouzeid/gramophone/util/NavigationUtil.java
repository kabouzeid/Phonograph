package com.kabouzeid.gramophone.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.ActionBarActivity;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.PlayingQueueDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.ui.activities.AlbumDetailActivity;
import com.kabouzeid.gramophone.ui.activities.ArtistDetailActivity;
import com.kabouzeid.gramophone.ui.activities.MusicControllerActivity;
import com.kabouzeid.gramophone.ui.activities.PlaylistDetailActivity;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class NavigationUtil {

    public static void goToArtist(final Activity activity, final int artistId, final Pair[] sharedViews) {
        if (activity instanceof ArtistDetailActivity)
            return;
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();
            final Intent intent = new Intent(activity, ArtistDetailActivity.class);
            intent.putExtra(AppKeys.E_ARTIST, artistId);
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

    public static void goToAlbum(final Activity activity, final int albumId, final Pair[] sharedViews) {
        if (activity instanceof AlbumDetailActivity)
            return;
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();
            final Intent intent = new Intent(activity, AlbumDetailActivity.class);
            intent.putExtra(AppKeys.E_ALBUM, albumId);
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

    public static void goToPlaylist(final Activity activity, final int playlistId, final Pair[] sharedViews) {
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();
            final Intent intent = new Intent(activity, PlaylistDetailActivity.class);
            intent.putExtra(AppKeys.E_PLAYLIST, playlistId);
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

    public static void openCurrentPlayingIfPossible(final Activity activity, final Pair[] sharedViews) {
        if (activity instanceof MusicControllerActivity) {
            activity.onBackPressed();
            return;
        }
        if (MusicPlayerRemote.getPosition() != -1) {
            if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
                if (activity instanceof KabViewsDisableAble)
                    ((KabViewsDisableAble) activity).disableViews();
                Intent intent = new Intent(activity, MusicControllerActivity.class);
                if (sharedViews != null) {
                    @SuppressWarnings("unchecked") ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                            sharedViews
                    );
                    ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
                } else {
                    activity.startActivity(intent);
                }
            }
        } else {
            Toast.makeText(activity, activity.getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openPlayingQueueDialog(final ActionBarActivity activity) {
        PlayingQueueDialog dialog = PlayingQueueDialog.create();
        if (dialog != null) {
            dialog.show(activity.getSupportFragmentManager(), "PLAY_QUEUE");
        } else {
            Toast.makeText(activity, activity.getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
        }
    }

    public static void openEqualizer(final Activity activity) {
        final int sessionId = MusicPlayerRemote.getAudioSessionId();
        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            Toast.makeText(activity, activity.getResources().getString(R.string.no_audio_ID), Toast.LENGTH_LONG).show();
        } else {
            try {
                final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId);
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                activity.startActivityForResult(effects, 0);
            } catch (final ActivityNotFoundException notFound) {
                Toast.makeText(activity, activity.getResources().getString(R.string.no_equalizer), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
