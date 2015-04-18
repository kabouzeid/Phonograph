package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.InputType;

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
                .title(R.string.rename_playlist_title)
                .positiveText(R.string.rename_action)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME |
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(getString(R.string.playlist_name), PlaylistsUtil.getNameForPlaylist(getActivity(), playlistId), false,
                        new MaterialDialog.InputCallback() {
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