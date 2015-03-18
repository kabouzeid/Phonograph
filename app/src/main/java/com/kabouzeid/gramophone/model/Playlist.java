package com.kabouzeid.gramophone.model;

public class Playlist {
    public int id;
    public String name;

    public Playlist(final int id, final String name) {
        this.id = id;
        this.name = name;
    }

    public Playlist() {
        this.id = -1;
        this.name = "";
    }
}
