package com.kabouzeid.materialmusic.model;

/**
 * Created by karim on 29.12.14.
 */
public class Artist {
    public int id;
    public String name;
    public int albumCount;
    public int songCount;

    public Artist(final int id, final String name, final int songCount,
                  final int albumCount) {
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
