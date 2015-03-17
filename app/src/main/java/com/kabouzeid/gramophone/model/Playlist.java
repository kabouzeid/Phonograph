package com.kabouzeid.gramophone.model;

public class Playlist {
    public int id;
    public String playlistName;

    public Playlist(final int id, final String playlistName) {
        this.id = id;
        this.playlistName = playlistName;
    }

    public Playlist() {
        this.id = -1;
        this.playlistName = "";
    }
}
