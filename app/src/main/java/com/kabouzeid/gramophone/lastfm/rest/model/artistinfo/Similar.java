
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Similar {

    @Expose
    private List<Artist_> artist = new ArrayList<Artist_>();

    /**
     * @return The artist
     */
    public List<Artist_> getArtist() {
        return artist;
    }

    /**
     * @param artist The artist
     */
    public void setArtist(List<Artist_> artist) {
        this.artist = artist;
    }

}
