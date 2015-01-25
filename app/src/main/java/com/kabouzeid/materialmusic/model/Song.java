package com.kabouzeid.materialmusic.model;

import java.io.Serializable;

/**
 * Created by karim on 23.11.14.
 */
public class Song implements Serializable {
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
}
