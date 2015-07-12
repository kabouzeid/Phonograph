package com.kabouzeid.gramophone.adapter.song.smartplaylist;

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
import android.widget.TextView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.song.AbsPlaylistSongAdapter;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.SongMenuHelper;
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

import butterknife.ButterKnife;
import butterknife.InjectView;

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
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    public SmartPlaylistSongAdapter(AppCompatActivity activity, @NonNull AbsSmartPlaylist playlist, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_cannot_delete_single_songs_playlist_songs_selection);
        this.activity = activity;
        this.playlist = playlist;
        dataSet = playlist.getSongs(activity);
        setHasStableIds(true);
    }

    @Override
    public ArrayList<Song> getDataSet() {
        return dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Song song = dataSet.get(position);

        holder.itemView.setActivated(isChecked(song));
        holder.title.setText(song.title);
        holder.text.setText(song.artistName);
        ImageLoader.getInstance().displayImage(
                MusicUtil.getSongImageLoaderString(song),
                holder.image,
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
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Song> selection) {
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

    protected void onAddToCurrentPlaying(@NonNull ArrayList<Song> songs) {
        MusicPlayerRemote.enqueue(songs);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        @InjectView(R.id.title)
        TextView title;
        @InjectView(R.id.text)
        TextView text;
        @InjectView(R.id.menu)
        ImageView menu;
        @InjectView(R.id.image)
        ImageView image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                image.setTransitionName(activity.getString(R.string.transition_album_art));
            }

            menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                @Override
                public Song getSong() {
                    return dataSet.get(getAdapterPosition());
                }

                @Override
                public int getMenuRes() {
                    return R.menu.menu_item_cannot_delete_single_songs_playlist_song;
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.action_go_to_album) {
                        Pair[] albumPairs = new Pair[]{
                                Pair.create(image, activity.getString(R.string.transition_album_art))
                        };
                        if (activity instanceof AbsFabActivity)
                            albumPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(albumPairs);
                        NavigationUtil.goToAlbum(activity, dataSet.get(getAdapterPosition()).albumId, albumPairs);
                        return true;
                    }
                    return super.onMenuItemClick(item);
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