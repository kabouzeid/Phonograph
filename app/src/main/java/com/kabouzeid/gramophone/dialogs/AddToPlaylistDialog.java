package com.kabouzeid.gramophone.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.DialogAction;
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
        CharSequence[] playlistNames = new CharSequence[playlists.size()];
        for (int i = 0; i < playlistNames.length; i++) {
            playlistNames[i] = playlists.get(i).name;
        }
        final ArrayList<Song> songs = getArguments().getParcelableArrayList("songs");

        int[] songIds = new int[songs.size()];

        boolean isAnySongInPlaylist[] = new boolean[playlists.size()];
        boolean areAllSongsInPlaylist[] = new boolean[playlists.size()];
        List<Integer> checkedPlaylists = new ArrayList<Integer>();

        if(songs != null)
        {
            for(int i = 0; i < songs.size(); i++){
                songIds[i] = songs.get(i).id;
            }

            for (int i = 0; i < playlists.size(); i++) {
                int playlistId = playlists.get(i).id;

                isAnySongInPlaylist[i] = PlaylistsUtil.doPlaylistContainsAnySong(getActivity(), playlistId, songIds);
                areAllSongsInPlaylist[i] = PlaylistsUtil.doPlaylistContainsAllSongs(getActivity(), playlistId, songIds);

                //TODO: display checkboxes instead of checkmark
                if (isAnySongInPlaylist[i]) {
                    if(areAllSongsInPlaylist[i]){
                        playlistNames[i] = playlists.get(i).name + " \u2713"; //Add checkmark
                    }
                    else{
                        playlistNames[i] = playlists.get(i).name + " (\u2713)"; //Add checkmark in brackets
                    }
                }

                if (areAllSongsInPlaylist[i]){
                    checkedPlaylists.add(i);
                }
            }
        }

        Integer[] temp = checkedPlaylists.toArray(new Integer[0]); //TODO



        return new MaterialDialog.Builder(getActivity())
                .title(R.string.add_playlist_title)
                .items(playlistNames)
                .itemsCallbackMultiChoice(temp, new MaterialDialog.ListCallbackMultiChoice() { //TODO
                    @Override
                    public boolean onSelection(MaterialDialog materialDialog, Integer[] which, CharSequence[] charSequence) {
                        boolean[] checked = new boolean[playlistNames.length];
                        for (int i: which){
                            checked[i] = true;
                        }
                        for (int i = 0; i < playlists.size(); i++){
                            if (checked[i] ^ areAllSongsInPlaylist[i]){
                                if(checked[i]){
                                    PlaylistsUtil.addToPlaylistWithoutDuplicates(getActivity(), songs, songIds, playlists.get(i).id, true);
                                }
                                else{
                                    for(Song song : songs){
                                        PlaylistsUtil.removeFromPlaylist(getActivity(), song, playlists.get(i).id);
                                    }
                                }
                            }
                        }
                        return true;
                    }
                })
                .positiveText(R.string.action_ok)
                .neutralText(R.string.action_new_playlist)
                .onNeutral( new MaterialDialog.SingleButtonCallback(){
                    @Override
                    public void onClick(MaterialDialog dialog, DialogAction action){
                        CreatePlaylistDialog.create(songs).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                    }
                })
                .build();

    }
}
