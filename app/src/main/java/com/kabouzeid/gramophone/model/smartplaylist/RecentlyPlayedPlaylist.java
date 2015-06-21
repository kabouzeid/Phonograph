package com.kabouzeid.gramophone.model.smartplaylist;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.smartplaylist.CannotDeleteSingleSongsSongAdapter;
import com.kabouzeid.gramophone.adapter.songadapter.smartplaylist.SmartPlaylistSongAdapter;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.TopAndRecentlyPlayedTracksLoader;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.RecentlyPlayedStore;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RecentlyPlayedPlaylist extends SmartPlaylist {

    public RecentlyPlayedPlaylist(Context context) {
        super(context.getString(R.string.recently_played), R.drawable.ic_access_time_white_24dp);
    }

    @Override
    public ArrayList<Song> getSongs(Context context) {
        return TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(context);
    }

    @Override
    public SmartPlaylistSongAdapter createAdapter(AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        return new CannotDeleteSingleSongsSongAdapter(activity, this, cabHolder);
    }

    @Override
    public void clear(Context context) {
        RecentlyPlayedStore.getInstance(context).clear();
        App.bus.post(new DataBaseChangedEvent(DataBaseChangedEvent.PLAYLISTS_CHANGED));
    }
}
