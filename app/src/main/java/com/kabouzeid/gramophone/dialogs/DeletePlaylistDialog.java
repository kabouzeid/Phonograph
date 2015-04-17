package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        long playlistId = getArguments().getLong("playlist_id");
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.delete_playlist_title)
                .content(Html.fromHtml(getString(R.string.delete_playlist_x,
                        PlaylistsUtil.getNameForPlaylist(getActivity(), playlistId))))
                .positiveText(R.string.delete_action)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        if (getActivity() == null)
                            return;
                        long playlistId = getArguments().getLong("playlist_id");
                        PlaylistsUtil.deletePlaylist(getActivity(), playlistId);
                    }
                }).build();
    }
}