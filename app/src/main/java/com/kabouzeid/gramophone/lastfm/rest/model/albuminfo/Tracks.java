
package com.kabouzeid.gramophone.lastfm.rest.model.albuminfo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Tracks {

    @Expose
    private List<Track> track = new ArrayList<Track>();

    /**
     * @return The track
     */
    public List<Track> getTrack() {
        return track;
    }

    /**
     * @param track The track
     */
    public void setTrack(List<Track> track) {
        this.track = track;
    }

}
