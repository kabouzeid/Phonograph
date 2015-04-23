package com.kabouzeid.gramophone.model;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Album {

    public final int id;
    public final int artistId;
    public final String title;
    public final String artistName;
    public final int songCount;
    public final int year;
    public final String albumArtPath; //used as cache key

    public Album(final int id, final String title, final String artistName, final int artistId,
                 final int songNumber, final int albumYear, final String albumArtPath) {
        this.id = id;
        this.title = title;
        this.artistName = artistName;
        this.artistId = artistId;
        songCount = songNumber;
        year = albumYear;
        this.albumArtPath = albumArtPath != null ? albumArtPath : "";
    }

    public Album() {
        this.id = -1;
        this.title = "";
        this.artistName = "";
        this.artistId = -1;
        songCount = -1;
        year = -1;
        this.albumArtPath = "";
    }
}
