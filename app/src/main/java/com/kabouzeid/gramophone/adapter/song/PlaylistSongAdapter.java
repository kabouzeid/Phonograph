package com.kabouzeid.gramophone.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.RemoveFromPlaylistDialog;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
@SuppressWarnings("unchecked")
public class PlaylistSongAdapter extends SongAdapter {

    public static final String TAG = PlaylistSongAdapter.class.getSimpleName();

    public PlaylistSongAdapter(@NonNull AppCompatActivity activity, @NonNull ArrayList<PlaylistSong> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, (ArrayList<Song>) (List) dataSet, itemLayoutRes, cabHolder);
        overrideMultiSelectMenuRes(R.menu.menu_playlists_songs_selection);
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Song> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_remove_from_playlist:
                RemoveFromPlaylistDialog.create((ArrayList<PlaylistSong>) (List) selection).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
        }
    }

    public class ViewHolder extends SongAdapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove_from_playlist:
                    RemoveFromPlaylistDialog.create((PlaylistSong) getSong()).show(activity.getSupportFragmentManager(), "REMOVE_FROM_PLAYLIST");
                    return true;
                case R.id.action_go_to_album:
                    Pair[] albumPairs = new Pair[]{
                            Pair.create(image, activity.getString(R.string.transition_album_art))
                    };
                    if (activity instanceof AbsFabActivity)
                        albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                    NavigationUtil.goToAlbum(activity, ((PlaylistSong) getSong()).albumId, albumPairs);
                    return true;
            }
            return super.onSongMenuItemClick(item);
        }
    }
}
