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
        final EditText editText = new EditText(context);
        ViewGroup layout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.dialog_empty_frame, null);
        if (editText.getParent() != null) {
            ((ViewGroup) editText.getParent()).removeView(editText);
        }
        editText.setText(PlaylistsUtil.getNameForPlaylist(context, playlistId));
        layout.addView(editText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.rename_playlist))
                .customView(layout, false)
                .positiveText(context.getResources().getString(R.string.ok))
                .negativeText(context.getResources().getString(R.string.cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                              @Override
                              public void onPositive(MaterialDialog dialog) {
                                  super.onPositive(dialog);
                                  final String playlistName = editText.getText().toString();
                                  if (!playlistName.trim().equals("")) {
                                      PlaylistsUtil.renamePlaylist(context, playlistId, playlistName);
                                  }
                              }

                              @Override
                              public void onNegative(MaterialDialog dialog) {
                                  super.onNegative(dialog);
                                  dialog.dismiss();
                              }
                          }
                )
                .build();
    }
}
