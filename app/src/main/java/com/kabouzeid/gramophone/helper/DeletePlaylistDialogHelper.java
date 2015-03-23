package com.kabouzeid.gramophone.helper;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

/**
 * Created by karim on 19.03.15.
 */
public class DeletePlaylistDialogHelper {
    public static MaterialDialog getDialog(final Context context, final int playlistId) {
        return new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.delete_playlist) + PlaylistsUtil.getNameForPlaylist(context, playlistId))
                .positiveText(context.getResources().getString(R.string.ok))
                .negativeText(context.getResources().getString(R.string.cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        PlaylistsUtil.deletePlaylist(context, playlistId);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                }).build();
    }
}
