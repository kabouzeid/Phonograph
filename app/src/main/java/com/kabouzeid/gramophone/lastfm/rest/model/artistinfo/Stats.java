
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

public class Stats {

    @Expose
    private String listeners;
    @Expose
    private String playcount;

    /**
     * @return The listeners
     */
    public String getListeners() {
        return listeners;
    }

    /**
     * @param listeners The listeners
     */
    public void setListeners(String listeners) {
        this.listeners = listeners;
    }

    /**
     * @return The playcount
     */
    public String getPlaycount() {
        return playcount;
    }

    /**
     * @param playcount The playcount
     */
    public void setPlaycount(String playcount) {
        this.playcount = playcount;
    }

}
