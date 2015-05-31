package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongShareDialog extends DialogFragment {
    public static SongShareDialog create(final int songId) {
        final SongShareDialog dialog = new SongShareDialog();
        final Bundle args = new Bundle();
        args.putInt("song_id", songId);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final int songId = getArguments().getInt("song_id");
        final Song song = SongLoader.getSong(getActivity(), songId);
        final String currentlyListening = getString(R.string.currently_listening_to_x_by_x, song.title, song.artistName);
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.what_do_you_want_to_share)
                .items(new CharSequence[]{getString(R.string.the_audio_file), currentlyListening})
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                        switch (i) {
                            case 0:
                                startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(getActivity(), songId), null));
                                break;
                            case 1:
                                getActivity().startActivity(
                                        Intent.createChooser(
                                                new Intent()
                                                        .setAction(Intent.ACTION_SEND)
                                                        .putExtra(Intent.EXTRA_TEXT, currentlyListening)
                                                        .setType("text/plain"),
                                                null
                                        )
                                );
                                break;
                        }
                    }
                })
                .build();
    }
}
