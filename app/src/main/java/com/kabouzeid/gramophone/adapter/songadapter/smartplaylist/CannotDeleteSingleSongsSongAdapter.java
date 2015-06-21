package com.kabouzeid.gramophone.adapter.songadapter.smartplaylist;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.smartplaylist.SmartPlaylist;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class CannotDeleteSingleSongsSongAdapter extends SmartPlaylistSongAdapter {

    public CannotDeleteSingleSongsSongAdapter(AppCompatActivity activity, SmartPlaylist smartPlaylist, @Nullable CabHolder cabHolder) {
        super(activity, smartPlaylist, cabHolder);
    }

    @Override
    protected int getMultiSelectMenuRes() {
        return R.menu.menu_cannot_delete_single_songs_playlist_songs_selection;
    }

    @Override
    protected int getSongMenuRes() {
        return R.menu.menu_item_cannot_delete_single_songs_playlist_song;
    }

    @Override
    protected void onDeleteFromPlaylist(Song song) {
        // you cannot delete single songs from this playlist
    }

    @Override
    protected void onDeleteFromPlaylist(ArrayList<Song> songs) {
        // you cannot delete single songs from this playlist
    }
}
