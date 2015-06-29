
package com.kabouzeid.gramophone.lastfm.rest.model.albuminfo;

import com.google.gson.annotations.Expose;

public class Attr {

    @Expose
    private String rank;

    /**
     * @return The rank
     */
    public String getRank() {
        return rank;
    }

    /**
     * @param rank The rank
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

}
