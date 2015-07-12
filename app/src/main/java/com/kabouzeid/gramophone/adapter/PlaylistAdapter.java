package com.kabouzeid.gramophone.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
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

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.ClearSmartPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeletePlaylistDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.PlaylistMenuHelper;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.PlaylistLoader;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.smartplaylist.AbsSmartPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.LastAddedPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.MyTopTracksPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.RecentlyPlayedPlaylist;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> {

    public static final String TAG = PlaylistAdapter.class.getSimpleName();

    private int SMART_PLAYLIST = 0;
    private int DEFAULT_PLAYLIST = 1;

    protected final AppCompatActivity activity;
    protected List<Playlist> dataSet;
    int favoritePlaylistId;

    public PlaylistAdapter(AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_playlists_selection);
        this.activity = activity;
        loadDataSet();
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    public void loadDataSet() {
        dataSet = new ArrayList<>();
        dataSet.add(new LastAddedPlaylist(activity));
        dataSet.add(new RecentlyPlayedPlaylist(activity));
        dataSet.add(new MyTopTracksPlaylist(activity));
        dataSet.addAll(PlaylistLoader.getAllPlaylists(activity));
        favoritePlaylistId = MusicUtil.getFavoritesPlaylist(activity).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_single_row, parent, false);
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);
        holder.playlistName.setText(playlist.name);
        holder.itemView.setActivated(isChecked(playlist));

        holder.icon.setImageDrawable(Util.getTintedDrawable(
                activity,
                getIconRes(playlist),
                ColorUtil.resolveColor(activity, R.attr.themed_drawable_color)
        ));
    }

    private int getIconRes(Playlist playlist) {
        if (playlist instanceof AbsSmartPlaylist) {
            return ((AbsSmartPlaylist) playlist).iconRes;
        }
        return playlist.id == favoritePlaylistId ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_queue_music_white_24dp;
    }

    @Override
    public int getItemViewType(int position) {
        return dataSet.get(position) instanceof AbsSmartPlaylist ? SMART_PLAYLIST : DEFAULT_PLAYLIST;
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
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Playlist> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_playlist:
                for (int i = 0; i < selection.size(); i++) {
                    Playlist playlist = selection.get(i);
                    if (playlist instanceof AbsSmartPlaylist) {
                        AbsSmartPlaylist absSmartPlaylist = (AbsSmartPlaylist) playlist;
                        ClearSmartPlaylistDialog.create(absSmartPlaylist).show(activity.getSupportFragmentManager(), "CLEAR_PLAYLIST_" + absSmartPlaylist.name);
                        selection.remove(playlist);
                        i--;
                    }
                }
                if (selection.size() > 0) {
                    DeletePlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                }
                break;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(getSongList(selection)).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(getSongList(selection));
                break;
        }
    }

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Playlist> playlists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Playlist playlist : playlists) {
            if (playlist instanceof AbsSmartPlaylist) {
                songs.addAll(((AbsSmartPlaylist) playlist).getSongs(activity));
            } else {
                songs.addAll(PlaylistSongLoader.getPlaylistSongList(activity, playlist.id));
            }
        }
        return songs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @InjectView(R.id.title)
        TextView playlistName;
        @InjectView(R.id.menu)
        View menu;
        @InjectView(R.id.image)
        ImageView icon;
        @InjectView(R.id.short_separator)
        View shortSeparator;

        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            if (itemViewType == SMART_PLAYLIST) {
                shortSeparator.setVisibility(View.GONE);
                itemView.setBackgroundColor(ColorUtil.resolveColor(activity, R.attr.card_color));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
                }
            }

            int padding = activity.getResources().getDimensionPixelSize(R.dimen.list_item_image_icon_padding);
            icon.setPadding(padding, padding, padding, padding);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    popupMenu.inflate(getItemViewType() == SMART_PLAYLIST ? R.menu.menu_item_smart_playlist : R.menu.menu_item_playlist);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(@NonNull MenuItem item) {
                            if (item.getItemId() == R.id.action_clear_playlist) {
                                Playlist playlist = dataSet.get(getAdapterPosition());
                                if (playlist instanceof AbsSmartPlaylist) {
                                    ClearSmartPlaylistDialog.create((AbsSmartPlaylist) playlist).show(activity.getSupportFragmentManager(), "CLEAR_SMART_PLAYLIST_" + playlist.name);
                                    return true;
                                }
                            }
                            return PlaylistMenuHelper.handleMenuClick(
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
                Playlist playlist = dataSet.get(getAdapterPosition());
                NavigationUtil.goToPlaylist(activity, playlist, sharedViews);
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
    public void onDataBaseEvent(@NonNull DataBaseChangedEvent event) {
        switch (event.getAction()) {
            case DataBaseChangedEvent.PLAYLISTS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }
}
