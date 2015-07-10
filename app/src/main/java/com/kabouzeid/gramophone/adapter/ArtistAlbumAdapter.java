package com.kabouzeid.gramophone.adapter;

import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.loader.AlbumSongLoader;
import com.kabouzeid.gramophone.model.Album;
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
public class ArtistAlbumAdapter extends AbsMultiSelectAdapter<ArtistAlbumAdapter.ViewHolder, Album> {
    public static final String TAG = AlbumAdapter.class.getSimpleName();

    private static final int TYPE_FIRST = 1;
    private static final int TYPE_MIDDLE = 2;
    private static final int TYPE_LAST = 3;

    private final AppCompatActivity activity;
    private ArrayList<Album> dataSet;
    private final int listMargin;

    public ArtistAlbumAdapter(AppCompatActivity activity, ArrayList<Album> objects, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        dataSet = objects;
        listMargin = activity.getResources().getDimensionPixelSize(R.dimen.default_item_margin);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    public void updateDataSet(ArrayList<Album> objects) {
        dataSet = objects;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_grid_artist_album, parent, false);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (viewType == TYPE_FIRST) {
            params.leftMargin = listMargin;
        } else if (viewType == TYPE_LAST) {
            params.rightMargin = listMargin;
        }
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Album album = dataSet.get(position);

        ImageLoader.getInstance().displayImage(
                MusicUtil.getAlbumImageLoaderString(album),
                holder.albumArt,
                new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .showImageOnFail(R.drawable.default_album_art)
                        .resetViewBeforeLoading(true)
                        .build()
        );

        holder.title.setText(album.title);
        holder.year.setText(String.valueOf(album.year));
        holder.view.setActivated(isChecked(album));
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_FIRST;
        } else if (position == getItemCount() - 1) {
            return TYPE_LAST;
        } else return TYPE_MIDDLE;
    }

    @Override
    protected Album getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<Album> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_from_disk:
                DeleteSongsDialog.create(getSongList(selection)).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                break;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(getSongList(selection)).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                break;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(getSongList(selection));
                break;
        }
    }

    private ArrayList<Song> getSongList(List<Album> albums) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Album album : albums) {
            songs.addAll(AlbumSongLoader.getAlbumSongList(activity, album.id));
        }
        return songs;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final ImageView albumArt;
        final TextView title;
        final TextView year;
        final View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            albumArt = (ImageView) itemView.findViewById(R.id.album_art);
            title = (TextView) itemView.findViewById(R.id.album_title);
            year = (TextView) itemView.findViewById(R.id.album_year);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] albumPairs = new Pair[]{
                        Pair.create(albumArt,
                                activity.getResources().getString(R.string.transition_album_cover)
                        )};
                if (activity instanceof AbsFabActivity)
                    albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).id, albumPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
