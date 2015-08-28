package com.kabouzeid.gramophone.adapter;

import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.ClearSmartPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeletePlaylistDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.PlaylistMenuHelper;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.PlaylistSongLoader;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.model.smartplaylist.AbsSmartPlaylist;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.kabouzeid.gramophone.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> {

    public static final String TAG = PlaylistAdapter.class.getSimpleName();

    private static final int SMART_PLAYLIST = 0;
    private static final int DEFAULT_PLAYLIST = 1;

    protected final AppCompatActivity activity;
    protected ArrayList<Playlist> dataSet;
    protected int itemLayoutRes;
    protected int favoritePlaylistId;

    public PlaylistAdapter(AppCompatActivity activity, ArrayList<Playlist> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_playlists_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;

        favoritePlaylistId = MusicUtil.getFavoritesPlaylist(activity).id;

        setHasStableIds(true);
    }

    public ArrayList<Playlist> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Playlist> dataSet) {
        this.dataSet = dataSet;
        favoritePlaylistId = MusicUtil.getFavoritesPlaylist(activity).id;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);

        holder.itemView.setActivated(isChecked(playlist));

        if (holder.title != null) {
            holder.title.setText(playlist.name);
        }

        if (holder.image != null) {
            holder.image.setImageDrawable(Util.getTintedDrawable(
                    activity,
                    getIconRes(playlist),
                    ColorUtil.resolveColor(activity, android.R.attr.textColorSecondary)
            ));
        }
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

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);

            if (itemViewType == SMART_PLAYLIST) {
                if (shortSeparator != null) {
                    shortSeparator.setVisibility(View.GONE);
                }
                itemView.setBackgroundColor(ColorUtil.resolveColor(activity, R.attr.cardBackgroundColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
                }
            }

            if (image != null) {
                int iconPadding = activity.getResources().getDimensionPixelSize(R.dimen.list_item_image_icon_padding);
                image.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
            }

            if (menu != null) {
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
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Playlist playlist = dataSet.get(getAdapterPosition());
                NavigationUtil.goToPlaylist(activity, playlist);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
