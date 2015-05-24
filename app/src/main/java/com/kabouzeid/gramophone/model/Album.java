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
        this.artistId = -1;
        songCount = -1;
        year = -1;
    }
}
