package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.widget.ImageView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.lastfm.artist.LastFMArtistThumbnailUrlLoader;
import com.squareup.picasso.Picasso;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Artist implements SearchEntry {
    public final int id;
    public final String name;
    public final int albumCount;
    public final int songCount;

    public Artist(final int id, final String name, final int albumCount, final int songCount) {
        this.id = id;
        this.name = name;
        this.songCount = songCount;
        this.albumCount = albumCount;
    }

    public Artist() {
        id = -1;
        name = "";
        songCount = -1;
        albumCount = -1;
    }

    @Override
    public String getTitle() {
        return name;
    }

    @Override
    public String getSubTitle() {
        return songCount + " Songs | " + albumCount + " Albums";
    }

    @Override
    public void loadImage(final Context context, final ImageView imageView) {
        imageView.setImageResource(R.drawable.default_artist_image);
        LastFMArtistThumbnailUrlLoader.loadArtistThumbnailUrl(context, name, false, new LastFMArtistThumbnailUrlLoader.ArtistThumbnailUrlLoaderCallback() {
            @Override
            public void onArtistThumbnailUrlLoaded(String url) {
                Picasso.with(context)
                        .load(url)
                        .into(imageView);
            }
        });
    }
}
