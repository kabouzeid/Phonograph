package com.kabouzeid.gramophone.model;

public class PlaylistSong extends Song {

    private static final long serialVersionUID = 1098600801627571043L;

    public final int playlistId;
    public final int idInPlayList;

    public PlaylistSong(final int id, final int albumId, final int artistId, final String title, final String artistName,
                        final String albumName, final long duration, final int trackNumber, final int playlistId, final int idInPlayList) {
        super(id, albumId, artistId, title, artistName, albumName, duration, trackNumber);
        this.playlistId = playlistId;
        this.idInPlayList = idInPlayList;
    }

    public PlaylistSong() {
        super();
        playlistId = -1;
        idInPlayList = -1;
    }
}
