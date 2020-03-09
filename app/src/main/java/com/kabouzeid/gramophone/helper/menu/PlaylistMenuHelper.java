package com.kabouzeid.gramophone.helper.menu;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.SongAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeletePlaylistDialog;
import com.kabouzeid.gramophone.dialogs.RenamePlaylistDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.SortOrder;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.misc.WeakContextAsyncTask;
import com.kabouzeid.gramophone.model.AbsCustomPlaylist;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PlaylistsUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistMenuHelper {

    private static String sortOrder = null;

    public static boolean handleMenuClick(@NonNull AppCompatActivity activity, @NonNull final Playlist playlist, @NonNull MenuItem item, SongAdapter adapter) {
        switch (item.getItemId()) {
            case R.id.action_play:
                MusicPlayerRemote.openQueue(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)), 0, true);
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)));
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)));
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(new ArrayList<>(getPlaylistSongs(activity, playlist, null))).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_rename_playlist:
                RenamePlaylistDialog.create(playlist.id).show(activity.getSupportFragmentManager(), "RENAME_PLAYLIST");
                return true;
            case R.id.action_delete_playlist:
                DeletePlaylistDialog.create(playlist).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                return true;
            case R.id.action_save_playlist:
                new SavePlaylistAsyncTask(activity).execute(playlist);
                return true;
            case R.id.action_playlist_sort_order_asc:
                if (adapter == null) return true;
                sortOrder = SortOrder.SongSortOrder.SONG_A_Z;
                adapter.swapDataSet(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)));

                return true;
            case R.id.action_playlist_sort_order_desc:
                if (adapter == null) return true;
                sortOrder = SortOrder.SongSortOrder.SONG_Z_A;
                adapter.swapDataSet(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)));
                return true;
            case R.id.action_playlist_sort_order_artist:
                if (adapter == null) return true;
                sortOrder = SortOrder.SongSortOrder.SONG_ARTIST;
                adapter.swapDataSet(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)));
                return true;
            case R.id.action_playlist_sort_order_year:
                if (adapter == null) return true;
                sortOrder =  SortOrder.SongSortOrder.SONG_YEAR;
                adapter.swapDataSet(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)));
                return true;
            case R.id.action_playlist_sort_order_album:
                if (adapter == null) return true;
                sortOrder = SortOrder.SongSortOrder.SONG_ALBUM;
                adapter.swapDataSet(new ArrayList<>(getPlaylistSongs(activity, playlist, sortOrder)));
                return true;
        }
        return false;
    }

    @NonNull
    private static List<? extends Song> getPlaylistSongs(@NonNull Activity activity, Playlist playlist, String sortOrder) {
        return playlist instanceof AbsCustomPlaylist ?
                ((AbsCustomPlaylist) playlist).getSongs(activity) :
                PlaylistSongLoader.getPlaylistSongList(activity, playlist.id, sortOrder);
    }


    private static class SavePlaylistAsyncTask extends WeakContextAsyncTask<Playlist, String, String> {
        public SavePlaylistAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackground(Playlist... params) {
            try {
                return String.format(App.getInstance().getApplicationContext().getString(R.string.saved_playlist_to), PlaylistsUtil.savePlaylist(App.getInstance().getApplicationContext(), params[0]));
            } catch (IOException e) {
                e.printStackTrace();
                return String.format(App.getInstance().getApplicationContext().getString(R.string.failed_to_save_playlist), e);
            }
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);
            Context context = getContext();
            if (context != null) {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show();
            }
        }
    }
}
