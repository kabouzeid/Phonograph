package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputType;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class CreatePlaylistDialog extends LeakDetectDialogFragment {

    @NonNull
    public static CreatePlaylistDialog create() {
        return create((Song) null);
    }

    @NonNull
    public static CreatePlaylistDialog create(@Nullable Song song) {
        ArrayList<Song> list = new ArrayList<>();
        if (song != null)
            list.add(song);
        return create(list);
    }

    @NonNull
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
                .positiveText(R.string.create_action)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(R.string.playlist_name_empty, 0, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, @NonNull CharSequence charSequence) {
                        if (getActivity() == null)
                            return;
                        if (!charSequence.toString().trim().isEmpty()) {
                            final int playlistId = PlaylistsUtil.createPlaylist(getActivity(), charSequence.toString());
                            if (playlistId != -1 && getActivity() != null) {
                                //noinspection unchecked
                                ArrayList<Song> songs = (ArrayList<Song>) getArguments().getSerializable("songs");
                                if (songs != null) {
                                    PlaylistsUtil.addToPlaylist(getActivity(), songs, playlistId, true);
                                }
                            }
                        }
                    }
                }).build();
    }
}