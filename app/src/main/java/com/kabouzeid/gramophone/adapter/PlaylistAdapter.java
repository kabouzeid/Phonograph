package com.kabouzeid.gramophone.adapter;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeletePlaylistDialog;
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.OnUpdatedListener;
import com.kabouzeid.gramophone.interfaces.SelfUpdating;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> implements SelfUpdating {

    public static final String TAG = PlaylistAdapter.class.getSimpleName();
    protected final AppCompatActivity activity;
    protected List<Playlist> dataSet;
    private OnUpdatedListener listener;

    public PlaylistAdapter(AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(cabHolder, R.menu.menu_playlists_selection);
        this.activity = activity;
        loadDataSet();
    }

    public void loadDataSet() {
        dataSet = PlaylistLoader.getAllPlaylists(activity);
        if (listener != null) listener.onUpdated(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);
        holder.playlistName.setText(playlist.name);
        holder.view.setActivated(isChecked(playlist));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Playlist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<Playlist> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_playlist:
                DeletePlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                break;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(getSongList(selection)).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(getSongList(selection));
                break;
        }
    }

    private ArrayList<Song> getSongList(List<Playlist> playlists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            songs.addAll(PlaylistSongLoader.getPlaylistSongList(activity, playlist.id));
        }
        return songs;
    }

    @Override
    public void setOnUpdatedListener(OnUpdatedListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public final TextView playlistName;
        private final View menu;
        private final View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            playlistName = (TextView) itemView.findViewById(R.id.playlist_name);
            menu = itemView.findViewById(R.id.menu);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.inflate(R.menu.menu_item_playlist);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            return MenuItemClickHelper.handlePlaylistMenuClick(
                                    activity, dataSet.get(getAdapterPosition()), item);
                        }
                    });
                    popupMenu.show();
                }
            });
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] sharedViews = null;
                if (activity instanceof AbsFabActivity)
                    sharedViews = ((AbsFabActivity) activity).getSharedViewsWithFab(null);
                NavigationUtil.goToPlaylist(activity, dataSet.get(getAdapterPosition()).id, sharedViews);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        App.bus.unregister(this);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        App.bus.register(this);
    }

    @Subscribe
    public void onDataBaseEvent(DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.PLAYLISTS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }
}
