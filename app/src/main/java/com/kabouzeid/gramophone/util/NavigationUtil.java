package com.kabouzeid.gramophone.util;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.PlayingQueueDialogHelper;
import com.kabouzeid.gramophone.interfaces.KabViewsDisableAble;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.ui.activities.AlbumDetailActivity;
import com.kabouzeid.gramophone.ui.activities.ArtistDetailActivity;
import com.kabouzeid.gramophone.ui.activities.MusicControllerActivity;
import com.kabouzeid.gramophone.ui.activities.PlaylistDetailActivity;

/**
 * Created by karim on 12.03.15.
 */
public class NavigationUtil {
    public static void goToArtist(final Activity activity, final int artistId, final Pair[] sharedViews) {
        if (activity instanceof ArtistDetailActivity) {
            return;
        }
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();
            final Intent intent = new Intent(activity, ArtistDetailActivity.class);
            intent.putExtra(AppKeys.E_ARTIST, artistId);
            if (sharedViews != null) {
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        sharedViews
                );
                ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
            } else {
                activity.startActivity(intent);
            }
        }
    }

    public static void goToAlbum(final Activity activity, final int albumId, final Pair[] sharedViews) {
        if (activity instanceof AlbumDetailActivity) {
            return;
        }
        if ((activity instanceof KabViewsDisableAble && ((KabViewsDisableAble) activity).areViewsEnabled()) || !(activity instanceof KabViewsDisableAble)) {
            if (activity instanceof KabViewsDisableAble)
                ((KabViewsDisableAble) activity).disableViews();
            final Intent intent = new Intent(activity, AlbumDetailActivity.class);
            intent.putExtra(AppKeys.E_ALBUM, albumId);
            if (sharedViews != null) {
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                        sharedViews
                );
                ActivityCompat.startActivity(activity, intent, optionsCompat.toBundle());
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
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
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
                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
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

    public static void openPlayingQueueDialog(final Activity activity) {
        final MaterialDialog materialDialog = PlayingQueueDialogHelper.getDialog(activity);
        if (materialDialog != null) {
            materialDialog.show();
        } else {
            Toast.makeText(activity, activity.getResources().getString(R.string.nothing_playing), Toast.LENGTH_SHORT).show();
        }
    }
}
