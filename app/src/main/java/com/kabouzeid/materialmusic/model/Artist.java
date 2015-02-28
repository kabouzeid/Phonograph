package com.kabouzeid.materialmusic.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.kabouzeid.materialmusic.R;
import com.kabouzeid.materialmusic.lastfm.artist.LastFMArtistThumbnailLoader;

/**
 * Created by karim on 29.12.14.
 */
public class Artist implements SearchEntry {
    public int id;
    public String name;
    public int albumCount;
    public int songCount;

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
    public void loadImage(Context context, final ImageView imageView) {
        imageView.setTag(name);
        LastFMArtistThumbnailLoader.loadArtistThumbnail(context, name, new LastFMArtistThumbnailLoader.ArtistThumbnailLoaderCallback() {
            @Override
            public void onArtistThumbnailLoaded(Bitmap thumbnail) {
                if (imageView.getTag().equals(name)) {
                    if (thumbnail != null) {
                        imageView.setImageBitmap(thumbnail);
                    } else {
                        imageView.setImageResource(R.drawable.default_artist_image);
                    }
                }
            }
        });
    }
}
