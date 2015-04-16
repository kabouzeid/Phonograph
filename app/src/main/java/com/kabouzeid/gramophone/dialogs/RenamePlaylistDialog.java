package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class RenamePlaylistDialog extends DialogFragment {

    public static RenamePlaylistDialog create(long playlistId) {
        RenamePlaylistDialog dialog = new RenamePlaylistDialog();
        Bundle args = new Bundle();
        args.putLong("playlist_id", playlistId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long playlistId = getArguments().getLong("playlist_id");
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.rename_playlist)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .input(null, PlaylistsUtil.getNameForPlaylist(getActivity(), playlistId), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        if (!charSequence.toString().trim().equals("")) {
                            long playlistId = getArguments().getLong("playlist_id");
                            PlaylistsUtil.renamePlaylist(getActivity(), playlistId, charSequence.toString());
                        }
                    }
                })
                .build();
    }
}