package com.kabouzeid.materialmusic.helper;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.materialmusic.App;
import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.adapter.PlayListAdapter;
import com.kabouzeid.materialmusic.model.Song;
import com.mobeta.android.dslv.DragSortListView;

/**
 * Created by karim on 24.01.15.
 */
public class PlayingQueueDialogHelper {
    public static MaterialDialog getDialog(Context context) {
        final App app = (App) context.getApplicationContext();
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(context.getResources().getString(R.string.label_current_playing_queue))
                .customView(R.layout.dialog_playlist, false)
                .positiveText(context.getResources().getString(R.string.close))
                .negativeText(context.getResources().getString(R.string.save_as_playlist))
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .build();
        final DragSortListView dragSortListView = (DragSortListView) dialog.getCustomView().findViewById(R.id.dragSortListView);
        final PlayListAdapter playListAdapter = new PlayListAdapter(context, app.getMusicPlayerRemote().getPlayingQueue());
        dragSortListView.setAdapter(playListAdapter);
        dragSortListView.setDropListener(new DragSortListView.DropListener() {
            @Override
            public void drop(int from, int to) {
                Song songToMove = app.getMusicPlayerRemote().getPlayingQueue().get(from);
                app.getMusicPlayerRemote().getPlayingQueue().remove(from);
                app.getMusicPlayerRemote().getPlayingQueue().add(to, songToMove);
                playListAdapter.notifyDataSetChanged();
            }
        });
        return dialog;
    }
}
