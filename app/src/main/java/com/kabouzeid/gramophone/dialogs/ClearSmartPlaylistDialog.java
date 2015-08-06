package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.smartplaylist.AbsSmartPlaylist;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ClearSmartPlaylistDialog extends LeakDetectDialogFragment {

    @NonNull
    public static ClearSmartPlaylistDialog create(AbsSmartPlaylist playlist) {
        ClearSmartPlaylistDialog dialog = new ClearSmartPlaylistDialog();
        Bundle args = new Bundle();
        args.putSerializable("playlist", playlist);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //noinspection unchecked
        final AbsSmartPlaylist playlist = (AbsSmartPlaylist) getArguments().getSerializable("playlist");
        int title = R.string.clear_playlist_title;
        //noinspection ConstantConditions
        CharSequence content = Html.fromHtml(getString(R.string.clear_playlist_x, playlist.name));

        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .content(content)
                .positiveText(R.string.clear_action)
                .negativeText(android.R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        if (getActivity() == null) {
                            return;
                        }
                        playlist.clear(getActivity());
                    }
                }).build();
    }
}