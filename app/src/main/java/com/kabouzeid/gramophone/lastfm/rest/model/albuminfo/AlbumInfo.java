
package com.kabouzeid.gramophone.lastfm.rest.model.albuminfo;

import com.google.gson.annotations.Expose;

public class AlbumInfo {

    @Expose
    private Album album;

    /**
     * @return The album
     */
    public Album getAlbum() {
        return album;
    }

    /**
     * @param album The album
     */
    public void setAlbum(Album album) {
        this.album = album;
    }

}
