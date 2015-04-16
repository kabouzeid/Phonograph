package com.kabouzeid.gramophone.helper;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class DeleteSongsDialogHelper {

    public static MaterialDialog getDialog(final Context context, final Song song) {
        List<Song> tmpList = new ArrayList<>();
        tmpList.add(song);
        return getDialog(context, tmpList);
    }

    public static MaterialDialog getDialog(final Context context, final List<Song> songs) {
        String title = context.getResources().getString(R.string.delete_songs_1);
        title = songs.size() > 1 ? title + songs.size() + context.getResources().getString(R.string.delete_songs_2) : title + "'" + songs.get(0).title + "' " + "?";
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(context.getResources().getString(R.string.delete_warning))
                .positiveText(context.getResources().getString(R.string.delete))
                .negativeText(context.getResources().getString(android.R.string.cancel))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        MusicUtil.deleteTracks(context, songs);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.dismiss();
                    }
                }).build();
    }
}
