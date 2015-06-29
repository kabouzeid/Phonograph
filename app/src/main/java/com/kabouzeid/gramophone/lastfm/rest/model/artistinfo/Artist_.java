
package com.kabouzeid.gramophone.lastfm.rest.model.artistinfo;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Artist_ {

    @Expose
    private String name;
    @Expose
    private String url;
    @Expose
    private List<Image_> image = new ArrayList<Image_>();

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The image
     */
    public List<Image_> getImage() {
        return image;
    }

    /**
     * @param image The image
     */
    public void setImage(List<Image_> image) {
        this.image = image;
    }

}
