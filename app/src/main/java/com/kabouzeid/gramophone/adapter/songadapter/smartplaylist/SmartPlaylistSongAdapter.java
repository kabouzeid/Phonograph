package com.kabouzeid.gramophone.adapter.songadapter.smartplaylist;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kabouzeid.gramophone.adapter.songadapter.AbsPlaylistSongAdapter;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.smartplaylist.SmartPlaylist;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public abstract class SmartPlaylistSongAdapter extends AbsPlaylistSongAdapter<Song> {
    private SmartPlaylist playlist;

    public SmartPlaylistSongAdapter(AppCompatActivity activity, SmartPlaylist playlist, @Nullable CabHolder cabHolder) {
        super(activity, playlist.getSongs(activity), cabHolder);
        this.playlist = playlist;
    }

    public void updateDataSet() {
        updateDataSet(playlist.getSongs(activity));
    }

}
