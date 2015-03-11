package com.kabouzeid.gramophone.model;

import android.content.Context;
import android.widget.ImageView;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.util.MusicUtil;
import com.squareup.picasso.Picasso;

import java.io.Serializable;

/**
 * Created by karim on 23.11.14.
 */
public class Song implements Serializable, SearchEntry {
    public int id;
    public int albumId;
    public int artistId;
    public String title;
    public String artistName;
    public String albumName;
    public long duration;
    public int trackNumber;

    public Song(final int id, final int albumId, final int artistId, final String title, final String artistName,
                final String albumName, final long duration, final int trackNumber) {
        this.id = id;
        this.albumId = albumId;
        this.artistId = artistId;
        this.title = title;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.trackNumber = trackNumber;
    }

    public Song() {
        this.id = -1;
        this.albumId = -1;
        this.artistId = -1;
        this.title = "";
        this.artistName = "";
        this.albumName = "";
        this.duration = -1;
        this.trackNumber = -1;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSubTitle() {
        return artistName;
    }

    @Override
    public void loadImage(Context context, ImageView imageView) {
        imageView.setImageResource(R.drawable.default_album_art);
        Picasso.with(context)
                .load(MusicUtil.getAlbumArtUri(albumId))
                .into(imageView);
    }
}
