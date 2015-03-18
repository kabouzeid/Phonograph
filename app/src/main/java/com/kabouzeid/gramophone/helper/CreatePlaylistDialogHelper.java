package com.kabouzeid.gramophone.helper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by karim on 17.03.15.
 */
public class CreatePlaylistDialogHelper {
    public static MaterialDialog getDialog(final Context context, final Song song) {
        List<Song> tmpSong = new ArrayList<>();
        tmpSong.add(song);
        return getDialog(context, tmpSong);
    }

    public static MaterialDialog getDialog(final Context context, final List<Song> songs) {
        final EditText editText = new EditText(context);
        ViewGroup layout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.dialog_create_playlist, null);
        if (editText.getParent() != null) {((ViewGroup) editText.getParent()).removeView(editText);}
        layout.addView(editText, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.action_new_playlist))
                .customView(layout, false)
                .positiveText(context.getResources().getString(R.string.ok))
                .negativeText(context.getResources().getString(R.string.cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                              @Override
                              public void onPositive(MaterialDialog dialog) {
                                  super.onPositive(dialog);
                                  final String playlistName = editText.getText().toString();
                                  if (!playlistName.trim().equals("")) {
                                      dialog.dismiss();
                                      final int playlistId = PlaylistsUtil.createPlaylist(context, playlistName);
                                      if (playlistId != -1) {
                                          if (songs != null) {
                                              PlaylistsUtil.addToPlaylist(context, songs, playlistId);
                                          }
                                      }
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

    public static MaterialDialog getDialog(final Context context) {
        return getDialog(context, (List<Song>) null);
    }
}
