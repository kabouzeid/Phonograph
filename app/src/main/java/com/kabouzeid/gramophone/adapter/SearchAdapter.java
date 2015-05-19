package com.kabouzeid.gramophone.adapter;

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
import com.kabouzeid.gramophone.helper.MenuItemClickHelper;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistThumbnailUrlLoader;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final int HEADER = 0;
    private static final int ALBUM = 1;
    private static final int ARTIST = 2;
    private static final int SONG = 3;

    private AppCompatActivity activity;
    private List results = Collections.emptyList();

    public SearchAdapter(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void search(String query) {
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
        if (results.isEmpty()) {
            results.add(activity.getResources().getString(R.string.no_results).toUpperCase());
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ALBUM)
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_search_album, parent, false), viewType);
        if (viewType == ARTIST)
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_search_artist, parent, false), viewType);
        if (viewType == SONG)
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_search_song, parent, false), viewType);
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_search_header, parent, false), viewType);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ALBUM:
                final Album album = (Album) results.get(position);
                holder.title.setText(album.title);
                holder.subTitle.setText(album.artistName);
                ImageLoader.getInstance().displayImage(
                        MusicUtil.getAlbumArtUri(album.id).toString(),
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
                holder.subTitle.setText(MusicUtil.getArtistInfoString(activity, artist));
                holder.image.setImageResource(R.drawable.default_artist_image);
                LastFMArtistThumbnailUrlLoader.loadArtistThumbnailUrl(activity, artist.name, false, new LastFMArtistThumbnailUrlLoader.ArtistThumbnailUrlLoaderCallback() {
                    @Override
                    public void onArtistThumbnailUrlLoaded(final String url) {
                        ImageLoader.getInstance().displayImage(url,
                                holder.image,
                                new DisplayImageOptions.Builder()
                                        .cacheInMemory(true)
                                        .cacheOnDisk(true)
                                        .showImageOnFail(R.drawable.default_artist_image)
                                        .resetViewBeforeLoading(true)
                                        .build()
                        );
                    }
                });
                break;
            case SONG:
                final Song song = (Song) results.get(position);
                holder.title.setText(song.title);
                holder.subTitle.setText(song.albumName);
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
        private final ImageView image;
        public final TextView title;
        public final TextView subTitle;
        private final int viewType;

        public ViewHolder(View itemView, int viewType) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.viewType = viewType;
            switch (viewType) {
                case ALBUM:
                    image = (ImageView) itemView.findViewById(R.id.album_art);
                    title = (TextView) itemView.findViewById(R.id.album_title);
                    subTitle = (TextView) itemView.findViewById(R.id.album_artist);
                    break;
                case ARTIST:
                    image = (ImageView) itemView.findViewById(R.id.artist_image);
                    title = (TextView) itemView.findViewById(R.id.artist_name);
                    subTitle = (TextView) itemView.findViewById(R.id.artist_info);
                    break;
                case SONG:
                    image = null;
                    title = (TextView) itemView.findViewById(R.id.song_title);
                    subTitle = (TextView) itemView.findViewById(R.id.song_info);
                    itemView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PopupMenu popupMenu = new PopupMenu(activity, view);
                            popupMenu.inflate(R.menu.menu_item_song);
                            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem menuItem) {
                                    return MenuItemClickHelper.handleSongMenuClick(activity, (Song) results.get(getAdapterPosition()), menuItem);
                                }
                            });
                            popupMenu.show();
                        }
                    });
                    break;
                default:
                    image = null;
                    title = (TextView) itemView.findViewById(R.id.title);
                    subTitle = null;
                    itemView.setOnClickListener(null);
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            Object item = results.get(getAdapterPosition());
            switch (viewType) {
                case ALBUM:
                    NavigationUtil.goToAlbum(activity,
                            ((Album) item).id,
                            new Pair[]{
                                    Pair.create(image,
                                            activity.getResources().getString(R.string.transition_album_cover)
                                    )
                            });
                    break;
                case ARTIST:
                    NavigationUtil.goToArtist(activity,
                            ((Artist) item).id,
                            new Pair[]{
                                    Pair.create(image,
                                            activity.getResources().getString(R.string.transition_artist_image)
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
}
