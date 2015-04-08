package com.kabouzeid.gramophone.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

/**
 * Created by karim on 19.03.15.
 */
public class RenamePlaylistDialogHelper {
    public static MaterialDialog getDialog(final Context context, final int playlistId) {
        return new MaterialDialog.Builder(context)
                .title(R.string.rename_playlist)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .input("", PlaylistsUtil.getNameForPlaylist(context, playlistId), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                        if (!charSequence.toString().trim().equals("")) {
                            PlaylistsUtil.renamePlaylist(context, playlistId, charSequence.toString());
                        }
                    }
                })
                .build();
    }
}
