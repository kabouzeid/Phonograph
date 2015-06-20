package com.kabouzeid.gramophone.adapter.songadapter.smartplaylist;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.LastAddedPlaylist;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class LastAddedSongAdapter extends SmartPlaylistSongAdapter {

    public LastAddedSongAdapter(AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(activity, new LastAddedPlaylist(activity), cabHolder);
    }

    @Override
    protected int getMultiSelectMenuRes() {
        return R.menu.menu_last_added_playlist_songs_selection;
    }

    @Override
    protected int getSongMenuRes() {
        return R.menu.menu_item_last_added_playlist_song;
    }
}
