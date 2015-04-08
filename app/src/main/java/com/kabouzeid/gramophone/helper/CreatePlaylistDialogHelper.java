package com.kabouzeid.gramophone.helper;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 17.03.15.
 */
public class CreatePlaylistDialogHelper {
    public static MaterialDialog getDialog(final Context context, final Song song) {
        List<Song> tmpSong = new ArrayList<>();
        tmpSong.add(song);
        return getDialog(context, tmpSong);
    }

    public static MaterialDialog getDialog(final Context context, final List<Song> songs) {
        return new MaterialDialog.Builder(context)
                .title(R.string.action_new_playlist)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input("", "", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        if (!charSequence.toString().trim().equals("")) {
                            final int playlistId = PlaylistsUtil.createPlaylist(context, charSequence.toString());
                            if (playlistId != -1 && songs != null) {
                                PlaylistsUtil.addToPlaylist(context, songs, playlistId);
                            }
                        }
                    }
                })
                .build();
    }

    public static MaterialDialog getDialog(final Context context) {
        return getDialog(context, (List<Song>) null);
    }
}
