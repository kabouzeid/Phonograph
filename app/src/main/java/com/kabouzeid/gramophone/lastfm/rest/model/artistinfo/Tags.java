
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Tags {

    @Expose
    private List<Tag> tag = new ArrayList<Tag>();

    /**
     * @return The tag
     */
    public List<Tag> getTag() {
        return tag;
    }

    /**
     * @param tag The tag
     */
    public void setTag(List<Tag> tag) {
        this.tag = tag;
    }

}
