package com.kabouzeid.gramophone.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.SongMenuHelper;
import com.kabouzeid.gramophone.lastfm.rest.LastFMRestClient;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.ArtistInfo;
import com.kabouzeid.gramophone.lastfm.rest.model.artistinfo.Image;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.DataBaseChangedEvent;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.Optional;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final int HEADER = 0;
    private static final int ALBUM = 1;
    private static final int ARTIST = 2;
    private static final int SONG = 3;

    @NonNull
    private final AppCompatActivity activity;
    @NonNull
    private List results = Collections.emptyList();
    private String query;
    @NonNull
    private final LastFMRestClient lastFMRestClient;

    public SearchAdapter(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        lastFMRestClient = new LastFMRestClient(activity);
    }

    @SuppressWarnings("unchecked")
    public void search(@NonNull String query) {
        this.query = query;
        results = new ArrayList();
        if (!query.trim().equals("")) {
            List songs = SongLoader.getSongs(activity, query);
            if (!songs.isEmpty()) {
                results.add(activity.getResources().getString(R.string.songs));
                results.addAll(songs);
            }

            List artists = ArtistLoader.getArtists(activity, query);
            if (!artists.isEmpty()) {
                results.add(activity.getResources().getString(R.string.artists));
                results.addAll(artists);
            }

            List albums = AlbumLoader.getAlbums(activity, query);
            if (!albums.isEmpty()) {
                results.add(activity.getResources().getString(R.string.albums));
                results.addAll(albums);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (results.get(position) instanceof Album) return ALBUM;
        if (results.get(position) instanceof Artist) return ARTIST;
        if (results.get(position) instanceof Song) return SONG;
        return HEADER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == HEADER)
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.sub_header, parent, false), viewType);
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false), viewType);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ALBUM:
                final Album album = (Album) results.get(position);
                holder.title.setText(album.title);
                holder.text.setText(album.artistName);
                ImageLoader.getInstance().displayImage(
                        MusicUtil.getAlbumImageLoaderString(album),
                        holder.image,
                        new DisplayImageOptions.Builder()
                                .cacheInMemory(true)
                                .showImageOnFail(R.drawable.default_album_art)
                                .resetViewBeforeLoading(true)
                                .build()
                );
                break;
            case ARTIST:
                final Artist artist = (Artist) results.get(position);
                holder.title.setText(artist.name);
                holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
                if (MusicUtil.isArtistNameUnknown(artist.name)) {
                    holder.image.setImageResource(R.drawable.default_artist_image);
                    break;
                }
                lastFMRestClient.getApiService().getArtistInfo(artist.name, null, new Callback<ArtistInfo>() {
                    @Override
                    public void success(@NonNull ArtistInfo artistInfo, Response response) {
                        if (artistInfo.getArtist() != null) {
                            int thumbnailIndex = 0;
                            List<Image> images = artistInfo.getArtist().getImage();
                            if (images.size() > 2) {
                                thumbnailIndex = 2;
                            } else if (images.size() > 1) {
                                thumbnailIndex = 1;
                            }
                            ImageLoader.getInstance().displayImage(images.get(thumbnailIndex).getText(),
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
                break;
            case SONG:
                final Song song = (Song) results.get(position);
                holder.title.setText(song.title);
                holder.text.setText(song.albumName);
                break;
            default:
                holder.title.setText(results.get(position).toString());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @InjectView(R.id.title)
        TextView title;
        @Nullable
        @Optional
        @InjectView(R.id.image)
        ImageView image;
        @Nullable
        @Optional
        @InjectView(R.id.text)
        TextView text;
        @Nullable
        @Optional
        @InjectView(R.id.menu)
        View menu;

        @SuppressWarnings("ConstantConditions")
        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);
            ButterKnife.inject(this, itemView);

            switch (itemViewType) {
                case SONG:
                    menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                        @Override
                        public Song getSong() {
                            return (Song) results.get(getAdapterPosition());
                        }
                    });
                    break;
                case ALBUM:
                    menu.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        image.setTransitionName(activity.getString(R.string.transition_album_art));
                    }
                    break;
                case ARTIST:
                    menu.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        image.setTransitionName(activity.getString(R.string.transition_artist_image));
                    }
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            Object item = results.get(getAdapterPosition());
            switch (getItemViewType()) {
                case ALBUM:
                    NavigationUtil.goToAlbum(activity,
                            ((Album) item).id,
                            new Pair[]{
                                    Pair.create(image,
                                            activity.getResources().getString(R.string.transition_album_art)
                                    )
                            });
                    break;
                case ARTIST:
                    NavigationUtil.goToArtist(activity,
                            ((Artist) item).id,
                            new Pair[]{
                                    Pair.create(image,
                                            activity.getResources().getString(R.string.transition_album_art)
                                    )
                            });
                    break;
                case SONG:
                    ArrayList<Song> playList = new ArrayList<>();
                    playList.add((Song) item);
                    MusicPlayerRemote.openQueue(playList, 0, true);
                    break;
            }
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
            case DataBaseChangedEvent.ALBUMS_CHANGED:
            case DataBaseChangedEvent.DATABASE_CHANGED:
                search(query);
                break;
        }
    }
}
