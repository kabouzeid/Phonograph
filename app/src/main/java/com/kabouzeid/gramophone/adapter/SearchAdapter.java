package com.kabouzeid.gramophone.adapter;

import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.glide.artistimage.ArtistImageRequest;
import com.kabouzeid.gramophone.helper.MusicPlayerRemote;
import com.kabouzeid.gramophone.helper.menu.SongMenuHelper;
import com.kabouzeid.gramophone.loader.AlbumLoader;
import com.kabouzeid.gramophone.loader.ArtistLoader;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.ArtistSignatureUtil;
import com.kabouzeid.gramophone.util.ColorUtil;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.kabouzeid.gramophone.util.NavigationUtil;

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

    private final AppCompatActivity activity;
    private List results = Collections.emptyList();

    public SearchAdapter(@NonNull AppCompatActivity activity) {
        this.activity = activity;
    }

    @SuppressWarnings("unchecked")
    public void search(@NonNull String query) {
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
                holder.title.setText(album.getTitle());
                holder.text.setText(album.getArtistName());
                Glide.with(activity)
                        .loadFromMediaStore(MusicUtil.getAlbumArtUri(album.getId()))
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .error(R.drawable.default_album_art)
                        .animate(android.R.anim.fade_in)
                        .signature(new MediaStoreSignature("", album.getDateModified(), 0))
                        .into(holder.image);
                break;
            case ARTIST:
                final Artist artist = (Artist) results.get(position);
                holder.title.setText(artist.name);
                holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
                Glide.with(activity)
                        .load(new ArtistImageRequest(artist.name, false))
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .error(R.drawable.default_album_art)
                        .animate(android.R.anim.fade_in)
                        .signature(ArtistSignatureUtil.getInstance(activity).getArtistSignature(artist.name))
                        .into(holder.image);
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

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);
            itemView.setOnLongClickListener(null);

            if (itemViewType != HEADER) {
                itemView.setBackgroundColor(ColorUtil.resolveColor(activity, R.attr.cardBackgroundColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
                }
                if (shortSeparator != null) {
                    shortSeparator.setVisibility(View.GONE);
                }
            }

            if (menu != null) {
                if (itemViewType == SONG) {
                    menu.setVisibility(View.VISIBLE);
                    menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                        @Override
                        public Song getSong() {
                            return (Song) results.get(getAdapterPosition());
                        }
                    });
                } else {
                    menu.setVisibility(View.GONE);
                }
            }

            switch (itemViewType) {
                case ALBUM:
                    setImageTransitionName(activity.getString(R.string.transition_album_art));
                    break;
                case ARTIST:
                    setImageTransitionName(activity.getString(R.string.transition_artist_image));
                    break;
                default:
                    View container = itemView.findViewById(R.id.image_container);
                    if (container != null) {
                        container.setVisibility(View.GONE);
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
                            ((Album) item).getId(),
                            Pair.create(image,
                                    activity.getResources().getString(R.string.transition_album_art)
                            ));
                    break;
                case ARTIST:
                    NavigationUtil.goToArtist(activity,
                            ((Artist) item).id,
                            Pair.create(image,
                                    activity.getResources().getString(R.string.transition_artist_image)
                            ));
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
