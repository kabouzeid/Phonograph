package com.kabouzeid.gramophone.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.SAFUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class DeleteSongsDialog extends DialogFragment {

    private ArrayList<Song> songsToRemove;
    private Song currentSong;

    @NonNull
    public static DeleteSongsDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static DeleteSongsDialog create(ArrayList<Song> songs) {
        DeleteSongsDialog dialog = new DeleteSongsDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //noinspection unchecked
        final ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");
        int title;
        CharSequence content;
        if (songs.size() > 1) {
            title = R.string.delete_songs_title;
            content = Html.fromHtml(getString(R.string.delete_x_songs, songs.size()));
        } else {
            title = R.string.delete_song_title;
            content = Html.fromHtml(getString(R.string.delete_song_x, songs.get(0).title));
        }
        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .content(content)
                .positiveText(R.string.delete_action)
                .negativeText(android.R.string.cancel)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Activity activity = getActivity();

                        if (activity == null)
                            return;

                        songsToRemove = songs;

                        if (!SAFUtil.isSAFRequiredForSongs(songs)) {
                            deleteSongs(songs, null);
                            dismiss();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                if (SAFUtil.isSDCardAccessGranted(activity)) {
                                    deleteSongs(songs, null);
                                    dismiss();
                                } else {
                                    Toast.makeText(activity, R.string.saf_pick_sdcard, Toast.LENGTH_LONG).show();
                                    SAFUtil.openTreePicker(DeleteSongsDialog.this);
                                }
                            } else {
                                deleteSongsKitkat();
                            }
                        }
                    }
                })
                .build();
    }

    private void deleteSongs(List<Song> songs, List<Uri> safUris) {
        MusicUtil.deleteTracks(getActivity(), songs, safUris);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void deleteSongsKitkat() {
        if (songsToRemove.size() < 1) {
            dismiss();
            return;
        }

        currentSong = songsToRemove.remove(0);

        if (!SAFUtil.isSAFRequired(currentSong)) {
            deleteSongs(Collections.singletonList(currentSong), null);
            deleteSongsKitkat();
        } else {
            Toast.makeText(getActivity(), String.format(getString(R.string.saf_pick_file), currentSong.data), Toast.LENGTH_LONG).show();
            SAFUtil.openFilePicker(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case SAFUtil.REQUEST_SAF_PICK_TREE:
                if (resultCode == Activity.RESULT_OK) {
                    SAFUtil.saveTreeUri(getActivity(), intent);
                    deleteSongs(songsToRemove, null);
                    dismiss();
                }
                break;

            case SAFUtil.REQUEST_SAF_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    deleteSongs(Collections.singletonList(currentSong), Collections.singletonList(intent.getData()));
                }
                break;
        }
    }
}