package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class CreatePlaylistDialog extends DialogFragment {

    public static CreatePlaylistDialog create() {
        return create((Song) null);
    }

    public static CreatePlaylistDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        if (song != null)
            list.add(song);
        return create(list);
    }

    public static CreatePlaylistDialog create(ArrayList<Song> songs) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle args = new Bundle();
        args.putSerializable("songs", songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.new_playlist_title)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(R.string.playlist_name, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        if (getActivity() == null)
                            return;
                        if (!charSequence.toString().trim().isEmpty()) {
                            final int playlistId = PlaylistsUtil.createPlaylist(getActivity(), charSequence.toString());
                            if (playlistId != -1 && getActivity() != null) {
                                //noinspection unchecked
                                ArrayList<Song> songs = (ArrayList<Song>) getArguments().getSerializable("songs");
                                PlaylistsUtil.addToPlaylist(getActivity(), songs, playlistId);
                            }
                        }
                    }
                }).build();
    }
}