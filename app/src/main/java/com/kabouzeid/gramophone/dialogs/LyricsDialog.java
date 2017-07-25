package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.model.lyrics.Lyrics;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LyricsDialog extends DialogFragment {
    public static LyricsDialog create(@NonNull Lyrics lyrics) {
        LyricsDialog dialog = new LyricsDialog();
        Bundle args = new Bundle();
        args.putString("title", lyrics.song.title);
        args.putString("lyrics", lyrics.getText());
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //noinspection ConstantConditions
        return new MaterialDialog.Builder(getActivity())
                .title(getArguments().getString("title"))
                .content(getArguments().getString("lyrics"))
                .build();
    }
}
