package com.kabouzeid.gramophone.adapter.songadapter;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Song;
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
public abstract class AbsPlaylistSongAdapter<S extends Song> extends AbsMultiSelectAdapter<PlaylistSongViewHolder, S> implements PlaylistSongViewHolder.onViewHolderClickListener, PlaylistSongViewHolder.onViewHolderLongClickListener, PlaylistSongViewHolder.onViewHolderMenuItemClickListener {

    public static final String TAG = AlbumSongAdapter.class.getSimpleName();
    protected final AppCompatActivity activity;
    protected ArrayList<S> dataSet;

    public AbsPlaylistSongAdapter(AppCompatActivity activity, ArrayList<S> objects, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_playlists_songs_selection);
        setMultiSelectMenuRes(getMultiSelectMenuRes());
        this.activity = activity;
        dataSet = objects;
    }

    public void updateDataSet(ArrayList<S> objects) {
        dataSet = objects;
        notifyDataSetChanged();
    }

    protected int getMultiSelectMenuRes() {
        return R.menu.menu_playlists_songs_selection;
    }

    protected int getSongMenuRes() {
        return R.menu.menu_item_playlist_song;
    }

    @Override
    public PlaylistSongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_song, parent, false);
        return new PlaylistSongViewHolder(this, view, getSongMenuRes());
    }

    @Override
    public void onBindViewHolder(final PlaylistSongViewHolder holder, int position) {
        final S song = dataSet.get(position);

        holder.view.setActivated(isChecked(song));
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
    protected S getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<S> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_from_playlist:
                onDeleteFromPlaylist(selection);
                break;
            case R.id.action_add_to_playlist:
                onAddToPlaylist(selection);
                break;
            case R.id.action_add_to_current_playing:
                onAddToCurrentPlaying(selection);
                break;
        }
    }

    @Override
    public void onClick(View v, int adapterPosition) {
        if (isInQuickSelectMode()) {
            toggleChecked(adapterPosition);
        } else {
            //noinspection unchecked
            MusicPlayerRemote.openQueue((ArrayList<Song>) (List) dataSet, adapterPosition, true);
        }
    }

    @Override
    public boolean onLongClick(View v, int adapterPosition) {
        toggleChecked(adapterPosition);
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item, PlaylistSongViewHolder viewHolder, int adapterPosition) {
        switch (item.getItemId()) {
            case R.id.action_delete_from_playlist:
                onDeleteFromPlaylist(dataSet.get(adapterPosition));
                return true;
            case R.id.action_go_to_album:
                Pair[] albumPairs = new Pair[]{
                        Pair.create(viewHolder.albumArt, activity.getString(R.string.transition_album_cover))
                };
                if (activity instanceof AbsFabActivity)
                    albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                NavigationUtil.goToAlbum(activity, dataSet.get(adapterPosition).albumId, albumPairs);
                return true;
        }
        return MenuItemClickHelper.handleSongMenuClick(activity, dataSet.get(adapterPosition), item);
    }

    protected void onDeleteFromPlaylist(S song) {

    }

    protected void onDeleteFromPlaylist(ArrayList<S> songs) {

    }

    protected void onAddToPlaylist(ArrayList<S> songs) {
        //noinspection unchecked
        AddToPlaylistDialog.create((ArrayList<Song>) (List) songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
    }

    protected void onAddToCurrentPlaying(ArrayList<S> songs) {
        //noinspection unchecked
        MusicPlayerRemote.enqueue((ArrayList<Song>) (List) songs);
    }
}
