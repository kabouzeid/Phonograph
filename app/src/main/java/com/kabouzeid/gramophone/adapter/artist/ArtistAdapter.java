package com.kabouzeid.gramophone.adapter.artist;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.dialogs.AddToPlaylistDialog;
import com.kabouzeid.gramophone.dialogs.DeleteSongsDialog;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.ArtistInfo;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.Image;
import com.kabouzeid.gramophone.loader.ArtistSongLoader;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class ArtistAdapter extends AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist> {
    protected final AppCompatActivity activity;
    protected ArrayList<Artist> dataSet;
    protected int itemLayoutRes;
    protected final LastFMRestClient lastFMRestClient;

    public ArtistAdapter(@NonNull AppCompatActivity activity, ArrayList<Artist> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        lastFMRestClient = new LastFMRestClient(activity);
        setHasStableIds(true);
    }

    public ArrayList<Artist> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(ArrayList<Artist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view);
    }

    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Artist artist = dataSet.get(position);

        if (holder.title != null) {
            holder.title.setText(artist.name);
        }
        if (holder.text != null) {
            holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
        }
        holder.itemView.setActivated(isChecked(artist));

        if (holder.image == null) {
            return;
        }

        if (MusicUtil.isArtistNameUnknown(artist.name)) {
            holder.image.setImageResource(R.drawable.default_artist_image);
            return;
        }

        lastFMRestClient.getApiService().getArtistInfo(artist.name, null, new Callback<ArtistInfo>() {
            @Override
            public void success(@NonNull ArtistInfo artistInfo, Response response) {
                if (artistInfo.getArtist() != null) {
                    List<Image> images = artistInfo.getArtist().getImage();
                    if (images == null || images.isEmpty()) {
                        return;
                    }
                    ImageLoader.getInstance().displayImage(images.get(images.size() - 1).getText(),
                            holder.image,
                            new DisplayImageOptions.Builder()
                                    .cacheInMemory(true)
                                    .cacheOnDisk(true)
                                    .resetViewBeforeLoading(true)
                                    .showImageOnFail(R.drawable.default_artist_image)
                                    .showImageForEmptyUri(R.drawable.default_artist_image)
                                    .build()
                    );
                } else {
                    holder.image.setImageResource(R.drawable.default_artist_image);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                holder.image.setImageResource(R.drawable.default_artist_image);
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
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull ArrayList<Artist> selection) {
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

    @NonNull
    private ArrayList<Song> getSongList(@NonNull List<Artist> artists) {
        final ArrayList<Song> songs = new ArrayList<>();
        for (Artist artist : artists) {
            songs.addAll(ArtistSongLoader.getArtistSongList(activity, artist.id));
        }
        return songs;
    }

    public class ViewHolder extends MediaEntryViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_artist_image));
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] artistPairs = new Pair[]{
                        Pair.create(image,
                                activity.getResources().getString(R.string.transition_artist_image)
                        )};
                if (activity instanceof AbsSlidingMusicPanelActivity)
                    artistPairs = ((AbsSlidingMusicPanelActivity) activity).getSharedViewsWithPlayPauseFab(artistPairs);
                NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()).id, artistPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
