package com.kabouzeid.materialmusic.model;

import android.widget.ImageView;

/**
 * Created by karim on 22.11.14.
 */
public class Album implements SearchEntry {

    public int id;
    public int artistId;
    public String title;
    public String artistName;
    public int songCount;
    public int year;

    public Album(final int id, final String title, final String artistName, final int artistId,
                 final int songNumber, final int albumYear) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.artistId = artistId;
        songCount = songNumber;
        year = albumYear;
    }

    public Album() {
        this.id = -1;
        this.title = "";
        this.artistName = "";
        songCount = -1;
        year = -1;
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
    public void loadImage(ImageView imageView) {

    }
}
