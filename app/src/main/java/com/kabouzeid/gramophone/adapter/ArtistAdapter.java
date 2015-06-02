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

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.interfaces.OnUpdatedListener;
import com.kabouzeid.gramophone.interfaces.SelfUpdating;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistThumbnailUrlLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsFabActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAdapter extends AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist> implements SelfUpdating {
    protected final AppCompatActivity activity;
    protected List<Artist> dataSet;
    private OnUpdatedListener listener;

    public ArtistAdapter(AppCompatActivity activity, @Nullable CabHolder cabHolder) {
        super(cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        loadDataSet();
    }

    private void loadDataSet() {
        dataSet = ArtistLoader.getAllArtists(activity);
        if (listener != null) listener.onUpdated(this);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.item_list_artist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Artist artist = dataSet.get(position);

        holder.artistName.setText(artist.name);
        holder.artistInfo.setText(MusicUtil.getArtistInfoString(activity, artist));
        holder.artistImage.setImageResource(R.drawable.default_artist_image);
        holder.view.setActivated(isChecked(artist));

        LastFMArtistThumbnailUrlLoader.loadArtistThumbnailUrl(activity, artist.name, false, new LastFMArtistThumbnailUrlLoader.ArtistThumbnailUrlLoaderCallback() {
            @Override
            public void onArtistThumbnailUrlLoaded(final String url) {
                ImageLoader.getInstance().displayImage(url,
                        holder.artistImage,
                        new DisplayImageOptions.Builder()
                                .cacheInMemory(true)
                                .cacheOnDisk(true)
                                .build()
                );
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Artist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected void onMultipleItemAction(MenuItem menuItem, ArrayList<Artist> selection) {
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

    private ArrayList<Song> getSongList(List<Artist> artists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Artist artist : artists) {
            songs.addAll(ArtistSongLoader.getArtistSongList(activity, artist.id));
        }
        return songs;
    }

    @Override
    public void setOnUpdatedListener(OnUpdatedListener listener) {
        this.listener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final TextView artistName;
        final TextView artistInfo;
        final ImageView artistImage;
        final View view;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            artistName = (TextView) itemView.findViewById(R.id.artist_name);
            artistInfo = (TextView) itemView.findViewById(R.id.artist_info);
            artistImage = (ImageView) itemView.findViewById(R.id.artist_image);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] artistPairs = new Pair[]{
                        Pair.create(artistImage,
                                activity.getResources().getString(R.string.transition_artist_image)
                        )};
                if (activity instanceof AbsFabActivity)
                    artistPairs = ((AbsFabActivity) activity).getSharedViewsWithFab(artistPairs);
                NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()).id, artistPairs);
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
            case DataBaseChangedEvent.ARTISTS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                loadDataSet();
                notifyDataSetChanged();
                break;
        }
    }
}
