package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class AddToPlaylistDialog extends DialogFragment {

    @NonNull
    public static AddToPlaylistDialog create(Song song) {
        ArrayList<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static AddToPlaylistDialog create(ArrayList<Song> songs) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", songs);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<Playlist> playlists = PlaylistLoader.getAllPlaylists(getActivity());
        CharSequence[] playlistNames = new CharSequence[playlists.size() + 1];
        playlistNames[0] = getActivity().getResources().getString(R.string.action_new_playlist);
        for (int i = 1; i < playlistNames.length; i++) {
            playlistNames[i] = playlists.get(i - 1).name;
        }
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.add_playlist_title)
                .items(playlistNames)
                .itemsCallback((materialDialog, view, i, charSequence) -> {
                    //noinspection unchecked
                    final ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");
                    if (songs == null) return;
                    if (i == 0) {
                        materialDialog.dismiss();
                        CreatePlaylistDialog.create(songs).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                    } else {
                        materialDialog.dismiss();
                        ArrayList<Song> updatedSongs = getNotExistingSongs(songs,  playlists.get(i - 1).id);
                        if (updatedSongs.size() > 0 ){
                            PlaylistsUtil.addToPlaylist(getActivity(), updatedSongs,  playlists.get(i - 1).id, true);
                        }else {
                            Toast.makeText(getActivity(), " Inserted "+updatedSongs.size()+" songs ins to the Playlist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .build();
    }

    /**
     * getNotExistingSongs helps get rid of songs that already exxist in a playlist,
     * returns a list of songs
     * @param songs list of songs to be added to playlist
     * @param playlistId playlist Id
     */
    private ArrayList<Song> getNotExistingSongs(ArrayList<Song> songs, int playlistId ){
        ArrayList<Song> newSongsList = new ArrayList<>();
        if (songs.size()>0){
            for (Song song : songs){
                if (!PlaylistsUtil.doPlaylistContains(getActivity(), playlistId, song.id)){
                    newSongsList.add(song);
                }
            }
        }
        return newSongsList;
    }
}
