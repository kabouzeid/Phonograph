package com.kabouzeid.gramophone.helper;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.util.Pair;
import android.view.MenuItem;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.SongFilePathLoader;
import com.kabouzeid.gramophone.misc.AppKeys;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.ui.activities.tageditor.SongTagEditorActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.io.File;

/**
 * Created by karim on 11.04.15.
 */
public class MenuItemClickHelper {
    public static boolean handleSongMenuClick(Activity activity, Song song, MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_delete_from_disk:
                DeleteSongsDialogHelper.getDialog(activity, song).show();
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialogHelper.getDialog(activity, song).show();
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(song);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(song);
                return true;
            case R.id.action_tag_editor:
                Intent intent = new Intent(activity, SongTagEditorActivity.class);
                intent.putExtra(AppKeys.E_ID, song.id);
                activity.startActivity(intent);
                return true;
            case R.id.action_details:
                String songFilePath = SongFilePathLoader.getSongFilePath(activity, song.id);
                File songFile = new File(songFilePath);
                SongDetailDialogHelper.getDialog(activity, songFile).show();
                return true;
            case R.id.action_go_to_album:
                Pair[] albumPairs = null;
                if (activity instanceof AbsFabActivity)
                    albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                NavigationUtil.goToAlbum(activity, song.albumId, albumPairs);
                return true;
            case R.id.action_go_to_artist:
                Pair[] artistPairs = null;
                if (activity instanceof AbsFabActivity)
                    artistPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(artistPairs);
                NavigationUtil.goToArtist(activity, song.artistId, artistPairs);
                return true;
        }
        return false;
    }

    public static boolean handlePlaylistMenuClick(Activity activity, Playlist playlist, MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_rename_playlist:
                RenamePlaylistDialogHelper.getDialog(activity, playlist.id).show();
                return true;
            case R.id.action_delete_playlist:
                DeletePlaylistDialogHelper.getDialog(activity, playlist.id).show();
                return true;
        }
        return false;
    }
}
