package com.kabouzeid.gramophone.model;

import java.io.Serializable;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Serializable {

    public final int id;
    public final int albumId;
    public final int artistId;
    public final String title;
    public final String artistName;
    public final String albumName;
    public final long duration;
    public final int trackNumber;
    public final long dateModified; //used as cache key

    public Song(final int id, final int albumId, final int artistId, final String title, final String artistName,
                final String albumName, final long duration, final int trackNumber, final long dateModified) {
        this.id = id;
        this.albumId = albumId;
        this.artistId = artistId;
        this.title = title;
        this.artistName = artistName;
        this.albumName = albumName;
        this.duration = duration;
        this.trackNumber = trackNumber;
        this.dateModified = dateModified;
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
        this.dateModified = -1;
    }
}
