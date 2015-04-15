package com.kabouzeid.gramophone.helper;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.PlayingQueueAdapter;
import com.kabouzeid.gramophone.model.Song;
import com.mobeta.android.dslv.DragSortListView;

import java.util.List;

/**
 * Created by karim on 24.01.15.
 */
public class PlayingQueueDialogHelper {
    public static MaterialDialog getDialog(final Activity activity) {
        final List<Song> playingQueue = MusicPlayerRemote.getPlayingQueue();
        if (playingQueue.isEmpty()) {
            return null;
        }

        MaterialDialog dialog = new MaterialDialog.Builder(activity)
                .title(activity.getResources().getString(R.string.label_current_playing_queue))
                .customView(R.layout.dialog_playlist, false)
                .positiveText(activity.getResources().getString(R.string.save_as_playlist))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                        AddToPlaylistDialogHelper.getDialog(activity, playingQueue).show();
                    }
                })
                .build();
        final DragSortListView dragSortListView = (DragSortListView) dialog.getCustomView().findViewById(R.id.dragSortListView);
        final PlayingQueueAdapter playingQueueAdapter = new PlayingQueueAdapter(activity, playingQueue);
        dragSortListView.setAdapter(playingQueueAdapter);
        dragSortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MusicPlayerRemote.playSongAt(position);
                playingQueueAdapter.notifyDataSetChanged();
            }
        });
        dragSortListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                MusicPlayerRemote.moveSong(from, to);
                playingQueueAdapter.notifyDataSetChanged();
            }
        });
        return dialog;
    }
}
