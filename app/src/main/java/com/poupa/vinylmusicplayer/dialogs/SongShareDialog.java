package com.poupa.vinylmusicplayer.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.poupa.vinylmusicplayer.R;
import com.poupa.vinylmusicplayer.model.Song;
import com.poupa.vinylmusicplayer.util.MusicUtil;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongShareDialog extends DialogFragment {
    @NonNull
    public static SongShareDialog create(final Song song) {
        final SongShareDialog dialog = new SongShareDialog();
        final Bundle args = new Bundle();
        args.putParcelable("song", song);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Song song = getArguments().getParcelable("song");
        final String currentlyListening = getString(R.string.currently_listening_to_x_by_x, song.title, song.artistName);
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.what_do_you_want_to_share)
                .items(getString(R.string.the_audio_file), "\u201C" + currentlyListening + "\u201D")
                .itemsCallback((materialDialog, view, i, charSequence) -> {
                    switch (i) {
                        case 0:
                            startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song, getContext()), null));
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
                })
                .build();
    }
}
