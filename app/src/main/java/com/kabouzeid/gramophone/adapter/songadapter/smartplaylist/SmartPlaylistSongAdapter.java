package com.kabouzeid.gramophone.adapter.songadapter.smartplaylist;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.songadapter.AbsPlaylistSongAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.smartplaylist.AbsSmartPlaylist;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SmartPlaylistSongAdapter extends AbsPlaylistSongAdapter<SmartPlaylistSongAdapter.ViewHolder, Song> {

    public static final String TAG = SmartPlaylistSongAdapter.class.getSimpleName();
    protected final AppCompatActivity activity;
    private AbsSmartPlaylist playlist;
    protected ArrayList<Song> dataSet;

    @Override
    public void updateDataSet() {
        dataSet = playlist.getSongs(activity);
        notifyDataSetChanged();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    public SmartPlaylistSongAdapter(AppCompatActivity activity, AbsSmartPlaylist playlist, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_cannot_delete_single_songs_playlist_songs_selection);
        this.activity = activity;
        this.playlist = playlist;
        dataSet = playlist.getSongs(activity);
    }

    @Override
    public ArrayList<Song> getDataSet() {
        return dataSet;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_song, parent, false);
        return new ViewHolder(view, R.menu.menu_item_cannot_delete_single_songs_playlist_song);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        holder.itemView.setActivated(isChecked(song));
        holder.songTitle.setText(song.title);
        holder.songInfo.setText(song.artistName);
        ImageLoader.getInstance().displayImage(
                MusicUtil.getAlbumArtUri(song.albumId).toString(),
                holder.albumArt,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .build()
        );
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Song getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<Song> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_add_to_playlist:
                onAddToPlaylist(selection);
                break;
            case R.id.action_add_to_current_playing:
                onAddToCurrentPlaying(selection);
                break;
        }
    }

    protected void onAddToPlaylist(ArrayList<Song> songs) {
        AddToPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
    }

    protected void onAddToCurrentPlaying(ArrayList<Song> songs) {
        MusicPlayerRemote.enqueue(songs);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final TextView songTitle;
        final TextView songInfo;
        final ImageView overflowButton;
        final ImageView albumArt;

        public ViewHolder(View itemView, final int songMenu) {
            super(itemView);
            songTitle = (TextView) itemView.findViewById(R.id.song_title);
            songInfo = (TextView) itemView.findViewById(R.id.song_info);
            albumArt = (ImageView) itemView.findViewById(R.id.album_art);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            overflowButton = (ImageView) itemView.findViewById(R.id.menu);
            overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popupMenu = new PopupMenu(activity, v);
                    popupMenu.inflate(songMenu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.action_go_to_album:
                                    Pair[] albumPairs = new Pair[]{
                                            Pair.create(albumArt, activity.getString(R.string.transition_album_cover))
                                    };
                                    if (activity instanceof AbsFabActivity)
                                        albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                                    NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).albumId, albumPairs);
                                    return true;
                            }
                            return MenuItemClickHelper.handleSongMenuClick(activity, dataSet.get(getAdapterPosition()), item);
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                //noinspection unchecked
                MusicPlayerRemote.openQueue((ArrayList<Song>) (List) dataSet, getAdapterPosition(), true);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}