package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
public class AddToPlaylistDialog extends DialogFragment {

    @NonNull
    public static AddToPlaylistDialog create(Song song) {
        List<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static AddToPlaylistDialog create(List<Song> songs) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog();
        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", new ArrayList<>(songs));
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
        final ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");

        int[] songIds = new int[songs.size()];
        boolean[][] isSongInPlaylist = new boolean[playlists.size()][songs.size()];
        if(songs != null)
        {

            for(int i = 0; i < songs.size(); i++){
                songIds[i] = songs.get(i).id;
            }

            for (int i = 0; i < playlists.size(); i++) {
                int playlistId = playlists.get(i).id;

                isSongInPlaylist [i] = PlaylistsUtil.doPlaylistContains(getActivity(), playlistId, songIds);
                boolean isAnySongInPlaylist = PlaylistsUtil.playlistContainsAnySong(getActivity(), isSongInPlaylist[i]);
                boolean areAllSongsInPlaylist = PlaylistsUtil.playlistContainsAllSongs(getActivity(), isSongInPlaylist[i]);

                //TODO: display checkboxes instead of checkmark
                if (isAnySongInPlaylist) {
                    if(areAllSongsInPlaylist){
                        playlistNames[i + 1] = playlists.get(i).name + " \u2713"; //Add checkmark
                    }
                    else{
                        playlistNames[i + 1] = playlists.get(i).name + " (\u2713)"; //Add checkmark in brackets
                    }
                }
            }
        }

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.add_playlist_title)
                .items(playlistNames)
                .itemsCallback((materialDialog, view, i, charSequence) -> {
                    //noinspection unchecked
                    if (songs == null) return;
                    if (i == 0) {
                        materialDialog.dismiss();
                        CreatePlaylistDialog.create(songs).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                    } else {
                        materialDialog.dismiss();
                        PlaylistsUtil.addToPlaylistWithoutDuplicates(getActivity(), songs, playlists.get(i - 1).id, isSongInPlaylist[i-1], true);
                    }
                })
                .build();
    }
}
