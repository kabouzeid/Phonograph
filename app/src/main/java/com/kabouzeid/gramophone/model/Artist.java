package com.kabouzeid.gramophone.model;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Artist {
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
}
