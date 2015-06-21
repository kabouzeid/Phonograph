package com.kabouzeid.gramophone.model.smartplaylist;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.smartplaylist.CannotDeleteSingleSongsSongAdapter;
import com.kabouzeid.gramophone.adapter.songadapter.smartplaylist.SmartPlaylistSongAdapter;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.LastAddedLoader;
import com.kabouzeid.gramophone.model.Song;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastAddedPlaylist extends SmartPlaylist {

    public LastAddedPlaylist(Context context) {
        super(context.getString(R.string.last_added), R.drawable.ic_queue_white_24dp);
    }

    @Override
    public ArrayList<Song> getSongs(Context context) {
        return LastAddedLoader.getLastAddedSongs(context);
    }

    @Override
    public SmartPlaylistSongAdapter createAdapter(AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        return new CannotDeleteSingleSongsSongAdapter(activity, this, cabHolder);
    }

    @Override
    public void clear(Context context) {
        // TODO
    }
}
