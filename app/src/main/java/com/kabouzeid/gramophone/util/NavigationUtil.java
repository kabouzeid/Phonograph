package com.kabouzeid.gramophone.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.View;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.ui.activities.AlbumDetailActivity;
import com.kabouzeid.gramophone.ui.activities.ArtistDetailActivity;
import com.kabouzeid.gramophone.ui.activities.PlaylistDetailActivity;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class NavigationUtil {

    public static void goToArtist(@NonNull final Activity activity, final int artistId, @Nullable Pair... sharedElements) {
        if (!disableViewsAndCheckIsReadyForTransition(activity)) return;

        final Intent intent = new Intent(activity, ArtistDetailActivity.class);
        intent.putExtra(ArtistDetailActivity.EXTRA_ARTIST_ID, artistId);

        sharedElements = addMiniPlayerSharedElement(activity, sharedElements);

        //noinspection unchecked
        activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
    }

    public static void goToAlbum(@NonNull final Activity activity, final int albumId, @Nullable Pair... sharedElements) {
        if (!disableViewsAndCheckIsReadyForTransition(activity)) return;

        final Intent intent = new Intent(activity, AlbumDetailActivity.class);
        intent.putExtra(AlbumDetailActivity.EXTRA_ALBUM_ID, albumId);

        sharedElements = addMiniPlayerSharedElement(activity, sharedElements);

        //noinspection unchecked
        activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
    }

    public static void goToPlaylist(@NonNull final Activity activity, final Playlist playlist, @Nullable Pair... sharedElements) {
        if (!disableViewsAndCheckIsReadyForTransition(activity)) return;

        final Intent intent = new Intent(activity, PlaylistDetailActivity.class);
        intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST, playlist);

        sharedElements = addMiniPlayerSharedElement(activity, sharedElements);

        //noinspection unchecked
        activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
    }

    private static Pair[] addMiniPlayerSharedElement(Activity activity, @Nullable Pair... sharedElements) {
        if (activity instanceof AbsSlidingMusicPanelActivity) {
            View miniPlayer = ((AbsSlidingMusicPanelActivity) activity).getMiniPlayerFragment().getView();
            Pair miniPlayerSharedElement = Pair.create(miniPlayer, activity.getString(R.string.transition_mini_player));

            if (sharedElements != null) {
                Pair[] tmpSharedElements;
                tmpSharedElements = new Pair[sharedElements.length + 1];
                System.arraycopy(sharedElements, 0, tmpSharedElements, 0, sharedElements.length);
                sharedElements = tmpSharedElements;

                sharedElements[sharedElements.length - 1] = miniPlayerSharedElement;
            } else {
                sharedElements = new Pair[]{miniPlayerSharedElement};
            }
        }
        return sharedElements;
    }

    private static boolean disableViewsAndCheckIsReadyForTransition(@NonNull final Activity activity) {
        if (activity instanceof KabViewsDisableAble) {
            if (((KabViewsDisableAble) activity).areViewsEnabled()) {
                ((KabViewsDisableAble) activity).disableViews();
                return true;
            }
        }
        return false;
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
