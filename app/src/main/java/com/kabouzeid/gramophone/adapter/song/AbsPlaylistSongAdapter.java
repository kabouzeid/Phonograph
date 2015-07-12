package com.kabouzeid.gramophone.adapter.song;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;

import com.kabouzeid.gramophone.adapter.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class AbsPlaylistSongAdapter<VH extends RecyclerView.ViewHolder, S extends Song> extends AbsMultiSelectAdapter<VH, S> {
    public static final String TAG = AbsPlaylistSongAdapter.class.getSimpleName();

    public AbsPlaylistSongAdapter(Context context, @Nullable CabHolder cabHolder, int menuRes) {
        super(context, cabHolder, menuRes);
    }

    public abstract ArrayList<S> getDataSet();

    public abstract void updateDataSet();
}
