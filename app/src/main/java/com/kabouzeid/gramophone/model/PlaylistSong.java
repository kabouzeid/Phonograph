package com.kabouzeid.gramophone.model;

public class PlaylistSong extends Song {
    public int playlistId;
    public int idInPlayList;

    public PlaylistSong(final int id, final int albumId, final int artistId, final String title, final String artistName,
                        final String albumName, final long duration, final int trackNumber, final int playlistId, final int idInPlayList) {
        super(id, albumId, artistId, title, artistName, albumName, duration, trackNumber);
        this.playlistId = playlistId;
        this.idInPlayList = idInPlayList;
    }

    public PlaylistSong() {
        super();
        playlistId = -1;
        id = -1;
    }
}
