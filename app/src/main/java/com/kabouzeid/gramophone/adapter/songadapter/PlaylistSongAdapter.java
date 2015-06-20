package com.kabouzeid.gramophone.adapter.songadapter;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.kabouzeid.gramophone.dialogs.RemoveFromPlaylistDialog;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.PlaylistSong;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistSongAdapter extends AbsPlaylistSongAdapter<PlaylistSong> {

    public PlaylistSongAdapter(AppCompatActivity activity, ArrayList<PlaylistSong> objects, @Nullable CabHolder cabHolder) {
        super(activity, objects, cabHolder);
    }

    @Override
    protected void onDeleteFromPlaylist(ArrayList<PlaylistSong> songs) {
        super.onDeleteFromPlaylist(songs);
        RemoveFromPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
    }

    @Override
    protected void onDeleteFromPlaylist(PlaylistSong song) {
        super.onDeleteFromPlaylist(song);
        RemoveFromPlaylistDialog.create(song).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
    }
}
