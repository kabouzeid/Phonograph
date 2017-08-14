package com.kabouzeid.gramophone.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import com.kabouzeid.gramophone.misc.DialogAsyncTask;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.saf.SAFGuideActivity;
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
                        songsToRemove = songs;
                        new DeleteSongsAsyncTask(DeleteSongsDialog.this).execute(new DeleteSongsAsyncTask.LoadingInfo(songs, null));
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        dismiss();
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
            case SAFGuideActivity.REQUEST_CODE_SAF_GUIDE:
                SAFUtil.openTreePicker(this);
                break;

            case SAFUtil.REQUEST_SAF_PICK_TREE:
            case SAFUtil.REQUEST_SAF_PICK_FILE:
                new DeleteSongsAsyncTask(this).execute(new DeleteSongsAsyncTask.LoadingInfo(requestCode, resultCode, intent));
                break;
        }
    }

    private static class DeleteSongsAsyncTask extends DialogAsyncTask<DeleteSongsAsyncTask.LoadingInfo, Integer, Void> {
        private DeleteSongsDialog dialog;
        private Activity activity;

        public DeleteSongsAsyncTask(DeleteSongsDialog dialog) {
            super(dialog.getActivity());
            this.dialog = dialog;
            this.activity = dialog.getActivity();
        }

        @Override
        protected Void doInBackground(LoadingInfo... params) {
            try {
                LoadingInfo info = params[0];

                if (dialog == null || activity == null)
                    return null;

                if (!info.isIntent) {
                    if (!SAFUtil.isSAFRequiredForSongs(info.songs)) {
                        dialog.deleteSongs(info.songs, null);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (SAFUtil.isSDCardAccessGranted(activity)) {
                                dialog.deleteSongs(info.songs, null);
                            } else {
                                dialog.startActivityForResult(new Intent(activity, SAFGuideActivity.class), SAFGuideActivity.REQUEST_CODE_SAF_GUIDE);
                            }
                        } else {
                            dialog.deleteSongsKitkat();
                        }
                    }
                } else {
                    switch (info.requestCode) {
                        case SAFUtil.REQUEST_SAF_PICK_TREE:
                            if (info.resultCode == Activity.RESULT_OK) {
                                SAFUtil.saveTreeUri(activity, info.intent);
                                dialog.deleteSongs(dialog.songsToRemove, null);
                            }
                            break;

                        case SAFUtil.REQUEST_SAF_PICK_FILE:
                            if (info.resultCode == Activity.RESULT_OK) {
                                dialog.deleteSongs(Collections.singletonList(dialog.currentSong), Collections.singletonList(info.intent.getData()));
                            }
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            if (dialog != null && activity != null && !activity.isFinishing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (dialog != null && activity != null && !activity.isFinishing()) {
                dialog.dismiss();
            }
        }

        @Override
        protected Dialog createDialog(@NonNull Context context) {
            return new MaterialDialog.Builder(context)
                    .title(R.string.deleting_songs)
                    .cancelable(false)
                    .progress(true, 0)
                    .build();
        }

        public static class LoadingInfo {
            public boolean isIntent;

            public List<Song> songs;
            public List<Uri> safUris;

            public int requestCode;
            public int resultCode;
            public Intent intent;

            public LoadingInfo(List<Song> songs, List<Uri> safUris) {
                this.isIntent = false;
                this.songs = songs;
                this.safUris = safUris;
            }

            public LoadingInfo(int requestCode, int resultCode, Intent intent) {
                this.isIntent = true;
                this.requestCode = requestCode;
                this.resultCode = resultCode;
                this.intent = intent;
            }
        }
    }
}