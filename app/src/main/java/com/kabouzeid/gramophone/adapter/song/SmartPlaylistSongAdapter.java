package com.kabouzeid.gramophone.adapter.song;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.ArrayList;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SmartPlaylistSongAdapter extends SongAdapter {

    public static final String TAG = SmartPlaylistSongAdapter.class.getSimpleName();

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    public SmartPlaylistSongAdapter(AppCompatActivity activity, @NonNull ArrayList<Song> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, cabHolder);
        overrideMultiSelectMenuRes(R.menu.menu_cannot_delete_single_songs_playlist_songs_selection);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    public class ViewHolder extends SongAdapter.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_cannot_delete_single_songs_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_go_to_album) {
                Pair[] albumPairs = new Pair[]{
                        Pair.create(image, activity.getString(R.string.transition_album_art))
                };
                if (activity instanceof AbsSlidingMusicPanelActivity)
                    albumPairs = ((AbsSlidingMusicPanelActivity) activity).getSharedViewsWithPlayPauseFab(albumPairs);
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).albumId, albumPairs);
                return true;
            }
            return super.onSongMenuItemClick(item);
        }
    }
}