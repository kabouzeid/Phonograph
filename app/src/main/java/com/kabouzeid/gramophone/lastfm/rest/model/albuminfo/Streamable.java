
package com.kabouzeid.gramophone.lastfm.rest.model.albuminfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Streamable {

    @SerializedName("#text")
    @Expose
    private String Text;
    @Expose
    private String fulltrack;

    /**
     * @return The Text
     */
    public String getText() {
        return Text;
    }

    /**
     * @param Text The #text
     */
    public void setText(String Text) {
        this.Text = Text;
    }

    /**
     * @return The fulltrack
     */
    public String getFulltrack() {
        return fulltrack;
    }

    /**
     * @param fulltrack The fulltrack
     */
    public void setFulltrack(String fulltrack) {
        this.fulltrack = fulltrack;
    }

}
