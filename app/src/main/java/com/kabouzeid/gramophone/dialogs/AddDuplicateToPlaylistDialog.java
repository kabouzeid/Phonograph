package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AddDuplicateToPlaylistDialog extends DialogFragment {

    private int selection = 0;

    private SelectionListener selListener;

    public AddDuplicateToPlaylistDialog() {
        selListener = null;
    }

    public void setSelListener(SelectionListener SelListener) {
        this.selListener = SelListener;
    }
    public interface SelectionListener {
        public void selectedOption(int selection);
    }

    @NonNull
    public static AddDuplicateToPlaylistDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static AddDuplicateToPlaylistDialog create(ArrayList<Song> songs) {
        AddDuplicateToPlaylistDialog dialog = new AddDuplicateToPlaylistDialog();
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
        final CharSequence content;
        title = R.string.add_duplicate_title;
        content = Html.fromHtml(getString(R.string.add_duplicate_msg, songs.get(0).title));
        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .content(content)
                .positiveText(R.string.add_action)
                .negativeText(R.string.dont_add_duplicate)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (getActivity() == null) return;
                        selection = 0;
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (getActivity() == null) return;
                        selection = 1;
                    }
                })
                .build();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        selListener.selectedOption(selection);
    }

    public int getSelection() {
        return selection;
    }
}