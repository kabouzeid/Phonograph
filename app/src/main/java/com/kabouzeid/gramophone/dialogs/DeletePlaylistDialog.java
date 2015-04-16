package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class DeletePlaylistDialog extends DialogFragment {

    public static DeletePlaylistDialog create(long playlistId) {
        DeletePlaylistDialog dialog = new DeletePlaylistDialog();
        Bundle args = new Bundle();
        args.putLong("playlist_id", playlistId);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long playlistId = getArguments().getLong("playlist_id");
        return new MaterialDialog.Builder(getActivity())
                .title(getActivity().getResources().getString(R.string.delete_playlist) +
                        PlaylistsUtil.getNameForPlaylist(getActivity(), playlistId))
                .positiveText(getActivity().getResources().getString(android.R.string.ok))
                .negativeText(getActivity().getResources().getString(android.R.string.cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        if (getActivity() == null)
                            return;
                        long playlistId = getArguments().getLong("playlist_id");
                        PlaylistsUtil.deletePlaylist(getActivity(), playlistId);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                }).build();
    }
}