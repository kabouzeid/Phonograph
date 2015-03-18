package com.kabouzeid.gramophone.helper;

import android.content.Context;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 17.03.15.
 */
public class AddToPlaylistDialogHelper {
    public static MaterialDialog getDialog(final Context context, final Song song) {
        List<Song> tmpSong = new ArrayList<>();
        tmpSong.add(song);
        return getDialog(context, tmpSong);
    }

    public static MaterialDialog getDialog(final Context context, final List<Song> songs) {
        final List<Playlist> playlists = PlaylistLoader.getAllPlaylists(context);
        CharSequence[] playlistNames = new CharSequence[playlists.size() + 1];
        playlistNames[0] = context.getResources().getString(R.string.action_new_playlist);
        for (int i = 1; i < playlistNames.length; i++) {
            playlistNames[i] = playlists.get(i-1).name;
        }
        return new MaterialDialog.Builder(context)
                .items(playlistNames)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        if (i == 0) {
                            materialDialog.dismiss();
                            CreatePlaylistDialogHelper.getDialog(context, songs).show();
                        } else {
                            materialDialog.dismiss();
                            PlaylistsUtil.addToPlaylist(context, songs, playlists.get(i - 1).id);
                        }
                    }
                })
                .build();
    }
}
